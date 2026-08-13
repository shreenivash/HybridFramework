# Configuration

`ConfigLoader` merges sources in this order (later wins):

1. `config.properties` on the classpath
2. `config.yaml` on the classpath
3. `config-{env}.properties` / `config-{env}.yaml` (`-Denv=qa`)
4. Environment variables (`GRID_URL` → `grid.url`)
5. JVM system properties (`-Dgrid.url=...`)

## Keys

| Key | Default | Meaning |
|---|---|---|
| `env` | `qa` | Environment name |
| `grid.url` | empty | Selenium Grid URL; empty = local browsers |
| `grid.headless` | `false` | Headless hides the session from VNC |
| `grid.screenResolution` | `1920x1080` | `se:screenResolution` for Grid nodes |
| `browser` | `chrome` | Default browser when no TestNG parameter |
| `retry.count` | `1` | Failed-test retries |
| `explicit.wait` | `20` | Seconds for explicit waits |
| `allure.results` | `target/allure-results` | Allure raw results directory |
| `api.baseUri` | JSONPlaceholder in examples | REST Assured base URI |
| `api.basePath` | empty | REST Assured base path |
| `api.port` | unset | REST Assured port |
| `api.threadCount` | `max(50, CPUs×8)` | HTTP pool + default API threads |
| `api.maxDurationMinutes` | `20` | Documented SLA (Jenkins/Surefire enforce it) |
| `api.connectionTimeoutMs` | `10000` | HTTP connect timeout |
| `api.socketTimeoutMs` | `30000` | HTTP socket timeout |
| `appium.url` | `http://127.0.0.1:4723` | Appium 2 server (no `/wd/hub`) |
| `device.name` | Android Emulator / iPhone 14 | Default device |
| `platform.name` | Android or inferred | `Android` or `iOS` |
| `app` / `-Dapp.path` | empty | Path to `.apk` / `.ipa`; mobile tests skip if missing |
| `autoGrantPermissions` | `true` | Android |

## Examples

```bash
mvn -pl examples test -Denv=qa -Dapi.baseUri=https://api.example.com
mvn -pl examples test -DsuiteXmlFile=src/test/resources/testng-web.xml -Dgrid.url=http://localhost:4444
```
