# Modules

Each Maven module produces a jar (`examples` is not published).

| Artifact | Depends on | When to use |
|---|---|---|
| `com.qa.framework:core` | TestNG, Jackson, Log4j2, POI | Always (pulled in transitively) |
| `com.qa.framework:web` | core, Selenium 4 | Browser tests, Grid, VNC |
| `com.qa.framework:mobile` | core, Appium Java Client 9 | Android / iOS |
| `com.qa.framework:api` | core, REST Assured | HTTP APIs |
| `com.qa.framework:testng-engine` | core, ClassGraph | Runtime suite generation |
| `com.qa.framework:perf` | core, Jackson YAML | JMeter JMX from API journey YAML, fixed-TPS CLI |

## Consumer POM

API-only project:

```xml
<dependency>
    <groupId>com.qa.framework</groupId>
    <artifactId>api</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Web + engine:

```xml
<dependency>
    <groupId>com.qa.framework</groupId>
    <artifactId>web</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>com.qa.framework</groupId>
    <artifactId>testng-engine</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Extend `BaseWebTest`, `BaseMobileTest`, or `BaseApiTest`. Registering `FrameworkTestListener` is optional if the SPI on the classpath is picked up; examples still declare it in `testng.xml` for clarity.
