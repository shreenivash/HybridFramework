# API parallelism and the 20-minute SLA

API suites must finish in **at most 20 minutes**. The framework does that by running TestNG methods in parallel on as many threads as the suite needs, with a pooled HTTP client sized to match.

## Mode `api_parallel`

```bash
mvn -pl examples exec:java -Dexec.classpathScope=test \
  -Dgroups=api \
  -DparallelMode=api_parallel \
  -DthreadCount=50 \
  -DtestPackage=com.qa.examples \
  -Dtestng.runtime.xml=target/testng-runtime.xml

mvn -pl examples test -DsuiteXmlFile=target/testng-runtime.xml
```

The generator sets `parallel="methods"` and:

```
threadCount = min(configuredThreads, scannedMethodCount)
```

Default configured threads: `max(50, availableProcessors × 8)` (`api.threadCount=0` in config means “use this default”).

Rule of thumb:

```
threads >= (methodCount × averageSecondsPerCall) / 1200
```

`@DataProvider(parallel = true)` is used in `DataProviderUtils`. REST Assured uses a **new HTTP client per request** (Rest Assured 5 default), which is safe under TestNG method-level parallelism. Do not enable `reuseHttpClientInstance()` for API suites.

## Hard cap

| Layer | Limit |
|---|---|
| Maven Surefire | `forkedProcessTimeoutInSeconds=1200` |
| Jenkins API stage | `timeout(time: 20, unit: 'MINUTES')` |
| GitHub Actions | `timeout-minutes: 20` |

Do not mutate `RestAssured.requestSpecification` in tests; `ApiClient` clones a spec per call.
