# Mobile (Appium 9)

The `mobile` module talks to **Appium 2** at `http://host:4723` (no `/wd/hub`). If `grid.url` is set, the same factory targets Selenium Grid nodes that expose Appium.

## Capabilities

`CapabilitiesBuilder` reads:

- `platform.name`, `device.name`, `udid`, `platform.version`
- `app` (or `-Dapp.path`)
- Android: `appPackage`, `appActivity`, `autoGrantPermissions`, `noReset`
- iOS: `bundleId`

Device names containing `iphone` select XCUITest; otherwise UiAutomator2.

## Running the sample

The example test is skipped unless an app is configured:

```bash
mvn -pl examples test -Dgroups=mobile -Dapp.path=/absolute/path/to/app.apk
```

## Helpers

- `MobileActions` — tap, swipe, long press, pinch/zoom, hide keyboard
- `ContextUtils` — `NATIVE_APP` / `WEBVIEW`
- `AppUtils` — install, background, deep link, lock/unlock, rotate
