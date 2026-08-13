# Contributing

Thanks for improving Hybrid Framework.

## Prerequisites

- JDK 21
- Maven 3.9+
- Docker (only for Grid / VNC web runs)

## Workflow

1. Fork [shreenivash/HybridFramework](https://github.com/shreenivash/HybridFramework) and create a branch.
2. `mvn -B -DskipTests install`
3. `mvn -B -pl examples test` — API examples must pass.
4. Open a pull request using the template.

## Conventions

- Java 21, no static `WebDriver` / `AppiumDriver` fields — use the ThreadLocal managers.
- Do not mutate shared REST Assured specs; clone via `given().spec(...)`.
- Keep API-only consumers free of Selenium/Appium (put driver code in `web` / `mobile`).
- Update `CHANGELOG.md` and `docs/` when behaviour changes.

## Running web examples

```bash
docker compose up -d
mvn -pl examples test -DsuiteXmlFile=src/test/resources/testng-web.xml -Dgrid.url=http://localhost:4444
```

Watch sessions in VNC as described in [docs/web-grid-vnc.md](docs/web-grid-vnc.md).

## Code of conduct

See [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).
