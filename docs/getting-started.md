# Getting started

## Requirements

- JDK 21
- Maven 3.9+
- Optional: Docker Desktop for Selenium Grid + VNC

## Clone and install

```bash
git clone git@github.com:shreenivash/HybridFramework.git
cd HybridFramework
mvn -B -DskipTests install
```

This installs `core`, `web`, `mobile`, `api`, and `testng-engine` into the local Maven repository.

## Run API examples

No Grid required. Tests hit `https://jsonplaceholder.typicode.com`.

```bash
mvn -pl examples test
mvn -pl examples allure:report
open examples/target/site/allure-report/index.html
```

The default suite uses `parallel="methods"` so API tests finish quickly.

## Use the library in your project

```xml
<dependency>
    <groupId>com.qa.framework</groupId>
    <artifactId>api</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

```java
public class HealthTest extends BaseApiTest {
    @Test(groups = "smoke")
    public void health() {
        api.get("/health").then().statusCode(200);
    }
}
```

See [modules.md](modules.md) for which artifact to depend on.
