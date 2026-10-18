# API journeys → JMeter (fixed TPS)

The **`perf`** module turns API **journeys** into **JMeter `.jmx`** plans with a **Constant Throughput Timer** and **JEXL** so load stays at a **fixed TPS** even when you add more users.

You can build journeys three ways:

| Approach | Best for | Effort |
|----------|----------|--------|
| **YAML journey** | Curated E2E flows, demos | Medium — one YAML file |
| **`@ApiLoadStep` on existing tests** | Reuse API suite structure | Low — annotate methods you want in load |
| **Record while tests run** | Exact traffic from functional runs | Lowest code change — run once with `-Dapi.load.record=true` |

See [blog: One API Suite, Two Rails](../blog/posts/one-api-suite-two-rails.html) for pros, cons, and ownership.

## Journey YAML

```yaml
name: My E2E Journey
baseUrl: https://api.example.com
variables:
  tenantId: "acme"
steps:
  - name: Login
    method: POST
    path: /auth/login
    body: '{"user":"demo","pass":"secret"}'
    extractJson:
      token: $.accessToken
  - name: Get profile
    method: GET
    path: /users/me
    headers:
      Authorization: Bearer ${token}
```

Example in the repo: [examples/src/test/resources/journeys/jsonplaceholder-journey.yaml](../examples/src/test/resources/journeys/jsonplaceholder-journey.yaml).

## CLI

| Command | Purpose |
|---------|---------|
| `explain` | Print TPS, users, CTT samples/min, `-J` props, capacity warning |
| `generate` | Write `.jmx` |
| `run` | Generate (optional) and invoke `jmeter` on `PATH` or `$JMETER_HOME/bin/jmeter` |

```bash
mvn -q -pl perf -am install

mvn -q -pl perf exec:java -Dexec.args="explain \
  --journey examples/src/test/resources/journeys/jsonplaceholder-journey.yaml \
  --tps 50 --users 200 --duration 600"

mvn -q -pl perf exec:java -Dexec.args="generate \
  --journey examples/src/test/resources/journeys/jsonplaceholder-journey.yaml \
  --out target/jsonplaceholder.jmx \
  --tps 50 --users 200"

jmeter -n -t target/jsonplaceholder.jmx -Jtps=50 -Jusers=200 -Jduration=600 -JrampUp=60 -l results.jtl
```

Or:

```bash
mvn -q -pl perf exec:java -Dexec.args="run \
  --journey examples/src/test/resources/journeys/jsonplaceholder-journey.yaml \
  --out target/jsonplaceholder.jmx \
  --jtl target/results.jtl \
  --tps 50 --users 500 --regenerate"
```

## Fixed TPS behavior

- **Constant Throughput Timer** target = `TPS × 60` samples per minute.
- JMX expression: `${__jexl3(${__P(tps,N)} * 60)}` so `-Jtps=` on the CLI is honored.
- **calcMode = 2** (all active threads in the JVM): adding users increases wait/throttle; it does **not** raise throughput above the timer target.
- **`explain`** warns when `users` is below `ceil(TPS × assumed_latency_seconds)` — you need enough threads to *achieve* the target, but extra threads won't exceed it.

## Dependency

```xml
<dependency>
    <groupId>com.qa.framework</groupId>
    <artifactId>perf</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Install [Apache JMeter](https://jmeter.apache.org/) separately for `run` / manual execution.

## Export all API tests to one JMX (`export-suite`)

Annotate functional tests (same class, same `@Test` methods):

```java
@ApiLoadSuite(name = "Checkout API", baseUrl = "https://api.example.com")
public class CheckoutApiTest extends BaseApiTest {

    @Test(groups = "api")
    @ApiLoadStep(method = "POST", path = "/login", body = "{\"user\":\"a\",\"pass\":\"b\"}",
            extractJson = "token=$.accessToken")
    public void login() { /* REST Assured assertions */ }
}
```

```bash
mvn -q -pl examples,perf -am install -DskipTests

# examples test-jar is on the perf CLI test classpath (see perf/pom.xml)
mvn -q -pl perf exec:java -Dexec.args="export-suite \
  --testPackage com.qa.examples.api \
  --baseUrl https://jsonplaceholder.typicode.com \
  --out target/api-suite.jmx --tps 50 --users 200"
```

For your own modules, compile tests and pass `--testClasspath path/to/target/test-classes` (comma-separated for several modules).

Methods without `@ApiLoadStep` are skipped (warning printed). Order follows class `@ApiLoadSuite.order` then declaration order.

### Record mode (no annotations)

```bash
mvn -pl examples test \
  -Dapi.load.record=true \
  -Dapi.load.record.file=target/recorded-api-journey.yaml \
  -Dapi.baseUri=https://jsonplaceholder.typicode.com

mvn -q -pl perf exec:java -Dexec.args="export-suite \
  --recorded target/recorded-api-journey.yaml \
  --out target/from-recording.jmx --tps 30 --users 100"
```

Recording captures **call order per thread** during the functional run; merge/refine YAML for true E2E journeys if tests are isolated per method.
