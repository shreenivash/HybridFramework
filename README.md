# Hybrid Framework

[![CI](https://github.com/shreenivash/HybridFramework/actions/workflows/ci.yml/badge.svg)](https://github.com/shreenivash/HybridFramework/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.9%2B-red.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](LICENSE)

Unified, Maven-packaged test automation library for **Web (Selenium 4)**, **Mobile (Appium 9)**, and **API (REST Assured)** with TestNG, Selenium Grid + live VNC, maximum API parallelism (20-minute SLA), and Allure reports generated when the suite finishes.

**Get the code:** [https://github.com/shreenivash/HybridFramework](https://github.com/shreenivash/HybridFramework)

```bash
git clone https://github.com/shreenivash/HybridFramework.git
```

## Modules

| Artifact | Use it for |
|---|---|
| `core` | Config, logging, retry, Allure listener, data providers |
| `web` | Selenium 4, Grid, page objects, element actions |
| `mobile` | Appium Android / iOS |
| `api` | REST Assured client, pooling, schema validation |
| `testng-engine` | Runtime TestNG XML (`cross_browser`, `api_parallel`, …) |
| `perf` | YAML API journeys → JMeter JMX, fixed TPS (`explain` / `generate` / `run` CLI) |
| `examples` | Sample tests (not published) |

Consumers add only the jars they need:

```xml
<dependency>
    <groupId>com.qa.framework</groupId>
    <artifactId>web</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Quick start

```bash
git clone git@github.com:shreenivash/HybridFramework.git
cd HybridFramework
mvn -B -DskipTests install
mvn -pl examples test          # API examples (no Grid required)
mvn -pl examples allure:report # HTML report → examples/target/site/allure-report
```

Watch **web** tests on Grid with a VNC viewer:

```bash
docker compose up -d
mvn -pl examples test -DsuiteXmlFile=src/test/resources/testng-web.xml \
  -Dgrid.url=http://localhost:4444 -Dheadless=false
```

Then connect to `vnc://localhost:5900` (see [docs/web-grid-vnc.md](docs/web-grid-vnc.md)).

## Documentation

- [Getting started](docs/getting-started.md)
- [Configuration](docs/configuration.md)
- [Modules](docs/modules.md)
- [Web + Grid + VNC](docs/web-grid-vnc.md)
- [API parallelism (20-minute SLA)](docs/api-parallel.md)
- [Load testing: journeys → JMX (fixed TPS)](docs/perf-load.md)
- [Blog: One API suite, two rails](blog/posts/one-api-suite-two-rails.html)
- [Mobile](docs/mobile.md)
- [Reporting](docs/reporting.md)
- [Jenkins](docs/jenkins.md)

## Parallel modes

| Mode | Behaviour |
|---|---|
| `cross_browser` | Every selected test on every browser |
| `random_browser` | Each test once, distributed across browsers |
| `api_parallel` | Method-level parallel, max threads, 20-minute cap |
| `cross_device` / `random_device` | Same idea for Appium devices |
| `methods` / `classes` | Standard TestNG parallel |

## License

Apache License 2.0 — see [LICENSE](LICENSE).
