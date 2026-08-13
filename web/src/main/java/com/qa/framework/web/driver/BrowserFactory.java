package com.qa.framework.web.driver;

import com.qa.framework.core.config.FrameworkConfig;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates local or Grid {@link WebDriver} instances. Grid sessions are headed by default so they are visible over VNC.
 */
public final class BrowserFactory {

    private static final Logger LOG = LoggerFactory.getLogger(BrowserFactory.class);

    private BrowserFactory() {
    }

    public static WebDriver createDriver(String browser) {
        return createDriver(browser, Map.of());
    }

    public static WebDriver createDriver(String browser, Map<String, Object> extraCapabilities) {
        String name = browser == null || browser.isBlank() ? FrameworkConfig.browser() : browser.trim();
        MutableCapabilities capabilities = buildCapabilities(name, extraCapabilities);
        if (FrameworkConfig.hasGrid()) {
            String gridUrl = FrameworkConfig.gridUrl();
            LOG.info("Creating RemoteWebDriver browser={} grid={} headless={}", name, gridUrl, FrameworkConfig.gridHeadless());
            try {
                return new RemoteWebDriver(URI.create(gridUrl).toURL(), capabilities);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Invalid grid.url: " + gridUrl, e);
            }
        }
        LOG.info("Creating local WebDriver browser={} headless={}", name, FrameworkConfig.gridHeadless());
        return localDriver(name, capabilities);
    }

    public static MutableCapabilities buildCapabilities(String browser, Map<String, Object> extra) {
        boolean headless = FrameworkConfig.gridHeadless();
        String resolution = FrameworkConfig.screenResolution();
        Map<String, Object> extras = extra == null ? Map.of() : extra;

        return switch (browser.toLowerCase()) {
            case "firefox", "ff" -> firefox(headless, resolution, extras);
            case "edge", "msedge" -> edge(headless, resolution, extras);
            case "safari" -> safari(resolution, extras);
            default -> chrome(headless, resolution, extras);
        };
    }

    private static ChromeOptions chrome(boolean headless, String resolution, Map<String, Object> extra) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-dev-shm-usage", "--no-sandbox", "--window-size=" + resolution.replace('x', ','));
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.setCapability("se:screenResolution", resolution);
        extra.forEach(options::setCapability);
        Path downloads = downloadDir();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", downloads.toAbsolutePath().toString());
        prefs.put("download.prompt_for_download", false);
        options.setExperimentalOption("prefs", prefs);
        return options;
    }

    private static FirefoxOptions firefox(boolean headless, String resolution, Map<String, Object> extra) {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless");
        }
        options.addArguments("--width=" + resolution.split("x")[0], "--height=" + resolution.split("x")[1]);
        options.setCapability("se:screenResolution", resolution);
        extra.forEach(options::setCapability);
        return options;
    }

    private static EdgeOptions edge(boolean headless, String resolution, Map<String, Object> extra) {
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--disable-dev-shm-usage", "--window-size=" + resolution.replace('x', ','));
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.setCapability("se:screenResolution", resolution);
        extra.forEach(options::setCapability);
        return options;
    }

    private static SafariOptions safari(String resolution, Map<String, Object> extra) {
        SafariOptions options = new SafariOptions();
        options.setCapability("se:screenResolution", resolution);
        extra.forEach(options::setCapability);
        return options;
    }

    private static WebDriver localDriver(String browser, MutableCapabilities capabilities) {
        return switch (browser.toLowerCase()) {
            case "firefox", "ff" -> new FirefoxDriver((FirefoxOptions) capabilities);
            case "edge", "msedge" -> new EdgeDriver((EdgeOptions) capabilities);
            case "safari" -> new SafariDriver((SafariOptions) capabilities);
            default -> new ChromeDriver((ChromeOptions) capabilities);
        };
    }

    public static Path downloadDir() {
        Path dir = Path.of("downloads");
        dir.toFile().mkdirs();
        return dir;
    }
}
