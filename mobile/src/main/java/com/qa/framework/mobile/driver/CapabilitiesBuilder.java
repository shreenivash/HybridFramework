package com.qa.framework.mobile.driver;

import com.qa.framework.core.config.ConfigLoader;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.openqa.selenium.MutableCapabilities;

import java.time.Duration;
import java.util.Map;

/**
 * Builds Android UiAutomator2 and iOS XCUITest options from config, TestNG parameters, and extras.
 */
public final class CapabilitiesBuilder {

    private CapabilitiesBuilder() {
    }

    public static MutableCapabilities forDevice(String device) {
        ConfigLoader config = ConfigLoader.getInstance();
        String platform = config.get("platform.name", inferPlatform(device));
        if ("ios".equalsIgnoreCase(platform)) {
            return ios(device);
        }
        return android(device);
    }

    public static UiAutomator2Options android(String device) {
        ConfigLoader config = ConfigLoader.getInstance();
        UiAutomator2Options options = new UiAutomator2Options();
        options.setPlatformName("Android");
        options.setAutomationName("UiAutomator2");
        options.setDeviceName(first(device, config.get("device.name", "Android Emulator")));
        setIfPresent(config.get("udid"), options::setUdid);
        setIfPresent(config.get("platform.version"), options::setPlatformVersion);
        setIfPresent(config.get("app"), options::setApp);
        setIfPresent(config.get("appPackage"), options::setAppPackage);
        setIfPresent(config.get("appActivity"), options::setAppActivity);
        options.setNoReset(config.getBoolean("noReset", false));
        options.setAutoGrantPermissions(config.getBoolean("autoGrantPermissions", true));
        options.setNewCommandTimeout(Duration.ofSeconds(config.getInt("newCommandTimeout", 120)));
        return options;
    }

    public static XCUITestOptions ios(String device) {
        ConfigLoader config = ConfigLoader.getInstance();
        XCUITestOptions options = new XCUITestOptions();
        options.setPlatformName("iOS");
        options.setAutomationName("XCUITest");
        options.setDeviceName(first(device, config.get("device.name", "iPhone 14")));
        setIfPresent(config.get("udid"), options::setUdid);
        setIfPresent(config.get("platform.version"), options::setPlatformVersion);
        setIfPresent(config.get("app"), options::setApp);
        setIfPresent(config.get("bundleId"), options::setBundleId);
        options.setNoReset(config.getBoolean("noReset", false));
        options.setNewCommandTimeout(Duration.ofSeconds(config.getInt("newCommandTimeout", 120)));
        return options;
    }

    public static MutableCapabilities merge(MutableCapabilities base, Map<String, Object> extra) {
        if (extra != null) {
            extra.forEach(base::setCapability);
        }
        return base;
    }

    private static String inferPlatform(String device) {
        if (device != null && device.toLowerCase().contains("iphone")) {
            return "ios";
        }
        return ConfigLoader.getInstance().get("platform.name", "Android");
    }

    private static void setIfPresent(String value, java.util.function.Consumer<String> setter) {
        if (value != null && !value.isBlank()) {
            setter.accept(value);
        }
    }

    private static String first(String preferred, String fallback) {
        return preferred == null || preferred.isBlank() ? fallback : preferred.trim();
    }
}
