# Changelog

All notable changes to this project are documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0-SNAPSHOT] - 2026-08-13

### Added

- Multi-module Maven library: `core`, `web`, `mobile`, `api`, `testng-engine`, `examples`
- Selenium 4 Grid support with headed sessions and docker-compose VNC / noVNC ports
- Appium 9 Android and iOS factories and mobile actions
- REST Assured client with pooled HTTP connections and `api_parallel` (20-minute SLA)
- Runtime TestNG XML generator (`cross_browser`, `random_browser`, `api_parallel`, device modes)
- Allure reporting generated after the suite (Maven `verify` / Jenkins `post { always }`)
- Jenkinsfile, GitHub Actions CI, and user documentation
