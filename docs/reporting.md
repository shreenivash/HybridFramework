# Reporting (Allure)

Allure is generated **automatically when the suite finishes**. You do not need to run `allure serve` by hand.

## Flow

1. Tests write raw results to `target/allure-results` (Allure TestNG + `FrameworkTestListener` + `AllureRestAssured`).
2. `mvn allure:report` (bound to `verify` in `examples`) writes `target/site/allure-report/index.html`.
3. Jenkins `post { always { } }` publishes the same results even when tests fail.

```bash
mvn -pl examples test
mvn -pl examples allure:report
```

## What is attached

- Web / mobile failure: PNG screenshot and page source
- API: request and response for every call (`AllureRestAssured`)
- `environment.properties` — env, grid URL, API thread count, 20-minute SLA, git SHA when present
- `categories.json` — product defects vs infrastructure vs broken tests
- `executor.json` — Jenkins or GitHub Actions metadata

GitHub Actions uploads `examples/target/site/allure-report` as the `allure-report` artifact.
