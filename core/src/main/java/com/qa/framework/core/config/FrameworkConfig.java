package com.qa.framework.core.config;

/**
 * Typed accessors for common framework keys.
 */
public final class FrameworkConfig {

    private FrameworkConfig() {
    }

    public static String env() {
        return ConfigLoader.getInstance().get("env", "qa");
    }

    public static String gridUrl() {
        return ConfigLoader.getInstance().get("grid.url", "");
    }

    public static boolean gridHeadless() {
        if (System.getProperty("headless") != null) {
            return Boolean.parseBoolean(System.getProperty("headless"));
        }
        return ConfigLoader.getInstance().getBoolean("grid.headless", false);
    }

    public static boolean hasGrid() {
        String url = gridUrl();
        return url != null && !url.isBlank();
    }

    public static int retryCount() {
        return ConfigLoader.getInstance().getInt("retry.count", 1);
    }

    public static int explicitWaitSeconds() {
        return ConfigLoader.getInstance().getInt("explicit.wait", 20);
    }

    public static String allureResultsDir() {
        return ConfigLoader.getInstance().get("allure.results", "target/allure-results");
    }

    public static int apiThreadCount() {
        int configured = ConfigLoader.getInstance().getInt("api.threadCount", 0);
        if (configured > 0) {
            return configured;
        }
        return Math.max(50, Runtime.getRuntime().availableProcessors() * 8);
    }

    public static int apiMaxDurationMinutes() {
        return ConfigLoader.getInstance().getInt("api.maxDurationMinutes", 20);
    }

    public static String browser() {
        return ConfigLoader.getInstance().get("browser", "chrome");
    }

    public static String screenResolution() {
        return ConfigLoader.getInstance().get("grid.screenResolution", "1920x1080");
    }

    public static String appiumUrl() {
        return ConfigLoader.getInstance().get("appium.url", "http://127.0.0.1:4723");
    }

    public static String apiBaseUri() {
        return ConfigLoader.getInstance().get("api.baseUri", "");
    }

    public static int apiPort() {
        return ConfigLoader.getInstance().getInt("api.port", -1);
    }

    public static String apiBasePath() {
        return ConfigLoader.getInstance().get("api.basePath", "");
    }

    public static int apiConnectionTimeoutMs() {
        return ConfigLoader.getInstance().getInt("api.connectionTimeoutMs", 10_000);
    }

    public static int apiSocketTimeoutMs() {
        return ConfigLoader.getInstance().getInt("api.socketTimeoutMs", 30_000);
    }
}
