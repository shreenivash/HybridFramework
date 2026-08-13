package com.qa.framework.mobile.actions;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.ScreenOrientation;

import java.io.File;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

public final class AppUtils {

    private AppUtils() {
    }

    public static void install(AppiumDriver driver, String appPath) {
        driver.executeScript("mobile: installApp", Map.of("appPath", appPath));
    }

    public static void uninstall(AppiumDriver driver, String bundleOrPackage) {
        driver.executeScript("mobile: removeApp", Map.of("appId", bundleOrPackage));
    }

    public static void reset(AppiumDriver driver, String appId) {
        terminate(driver, appId);
        driver.executeScript("mobile: clearApp", Map.of("appId", appId));
    }

    public static void background(AppiumDriver driver, Duration duration) {
        driver.executeScript("mobile: backgroundApp", Map.of("seconds", duration.toSeconds()));
    }

    public static void activate(AppiumDriver driver, String appId) {
        driver.executeScript("mobile: activateApp", Map.of("appId", appId));
    }

    public static void terminate(AppiumDriver driver, String appId) {
        driver.executeScript("mobile: terminateApp", Map.of("appId", appId));
    }

    public static void deepLink(AppiumDriver driver, String url, String appId) {
        driver.executeScript("mobile: deepLink", Map.of("url", url, "package", appId));
    }

    public static void pushFile(AppiumDriver driver, String remotePath, File localFile) {
        try {
            String payload = Base64.getEncoder().encodeToString(Files.readAllBytes(localFile.toPath()));
            driver.executeScript("mobile: pushFile", Map.of("remotePath", remotePath, "payload", payload));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to push file " + localFile, e);
        }
    }

    public static byte[] pullFile(AppiumDriver driver, String remotePath) {
        Object result = driver.executeScript("mobile: pullFile", Map.of("remotePath", remotePath));
        if (result instanceof String encoded) {
            return Base64.getDecoder().decode(encoded);
        }
        if (result instanceof byte[] bytes) {
            return bytes;
        }
        return new byte[0];
    }

    public static void lock(AppiumDriver driver) {
        driver.executeScript("mobile: lock", Map.of("seconds", 0));
    }

    public static void unlock(AppiumDriver driver) {
        driver.executeScript("mobile: unlock");
    }

    public static void rotate(AppiumDriver driver, ScreenOrientation orientation) {
        driver.executeScript("mobile: setOrientation", Map.of("orientation", orientation.name()));
    }
}
