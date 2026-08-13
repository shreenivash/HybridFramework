# Jenkins

Use [`jenkins/Jenkinsfile`](../jenkins/Jenkinsfile) as a parameterized pipeline.

## Parameters

| Parameter | Default | Purpose |
|---|---|---|
| `GROUPS` | `smoke` | TestNG groups |
| `BROWSERS` | `chrome,firefox,edge` | Cross / random browser |
| `PARALLEL_MODE` | `api_parallel` | See README table |
| `THREAD_COUNT` | `50` | Max TestNG threads |
| `GRID_URL` | `http://selenium-grid:4444` | Remote WebDriver |
| `DEVICES` | `Pixel_7,iPhone_14` | Mobile |
| `TEST_PACKAGE` | `com.qa.examples` | ClassGraph scan root |

## Stages

1. Checkout and `mvn -DskipTests install`
2. Generate `examples/target/testng-runtime.xml`
3. `mvn -pl examples test` — **API mode is wrapped in a 20-minute timeout**
4. `post { always }` — `allure:report` + Allure Jenkins plugin + archived HTML

## Watching web jobs

Keep Grid headed (`-Dheadless=false`, the Jenkinsfile default). Point a VNC viewer at the node host ports documented in [web-grid-vnc.md](web-grid-vnc.md).

Web:

```text
PARALLEL_MODE=cross_browser  GRID_URL=http://selenium-grid:4444
```

API:

```text
PARALLEL_MODE=api_parallel  THREAD_COUNT=50
```
