package com.qa.framework.mobile.driver;

import com.qa.framework.core.config.FrameworkConfig;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.MutableCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

public final class AppiumDriverFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AppiumDriverFactory.class);

    private AppiumDriverFactory() {
    }

    public static AppiumDriver createDriver(String device) {
        return createDriver(device, Map.of());
    }

    public static AppiumDriver createDriver(String device, Map<String, Object> extra) {
        MutableCapabilities capabilities = CapabilitiesBuilder.merge(CapabilitiesBuilder.forDevice(device), extra);
        URL server = resolveServerUrl();
        LOG.info("Creating Appium driver device={} server={}", device, server);
        return new AppiumDriver(server, capabilities);
    }

    private static URL resolveServerUrl() {
        String url = FrameworkConfig.hasGrid() ? FrameworkConfig.gridUrl() : FrameworkConfig.appiumUrl();
        try {
            return URI.create(url).toURL();
        } catch (MalformedURLException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Appium/Grid URL: " + url, e);
        }
    }
}
