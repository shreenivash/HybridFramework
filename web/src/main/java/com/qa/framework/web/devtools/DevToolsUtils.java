package com.qa.framework.web.devtools;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v131.emulation.Emulation;
import org.openqa.selenium.devtools.v131.log.Log;
import org.openqa.selenium.devtools.v131.network.Network;
import org.openqa.selenium.devtools.v131.performance.Performance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Chrome DevTools Protocol helpers. On remote Grid sessions CDP may be unavailable; methods log a warning and no-op.
 */
public final class DevToolsUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DevToolsUtils.class);

    private DevToolsUtils() {
    }

    public static Optional<DevTools> connect(WebDriver driver) {
        try {
            if (driver instanceof HasDevTools hasDevTools) {
                DevTools devTools = hasDevTools.getDevTools();
                devTools.createSession();
                return Optional.of(devTools);
            }
        } catch (RuntimeException e) {
            LOG.warn("CDP is not available on this session: {}", e.getMessage());
        }
        if (!(driver instanceof ChromeDriver)) {
            LOG.warn("DevToolsUtils requires a Chrome session that implements HasDevTools");
        }
        return Optional.empty();
    }

    public static void enableNetwork(WebDriver driver) {
        connect(driver).ifPresent(devTools -> {
            devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
            LOG.info("CDP Network domain enabled");
        });
    }

    public static List<String> consoleLogs(WebDriver driver) {
        List<String> messages = new ArrayList<>();
        connect(driver).ifPresent(devTools -> {
            devTools.send(Log.enable());
            devTools.addListener(Log.entryAdded(), entry -> messages.add(entry.getText()));
        });
        return messages;
    }

    public static void geolocation(WebDriver driver, double latitude, double longitude, double accuracy) {
        connect(driver).ifPresent(devTools ->
                devTools.send(Emulation.setGeolocationOverride(
                        java.util.Optional.<Number>of(latitude),
                        java.util.Optional.<Number>of(longitude),
                        java.util.Optional.<Number>of(accuracy))));
    }

    public static void enablePerformance(WebDriver driver) {
        connect(driver).ifPresent(devTools -> {
            devTools.send(Performance.enable(Optional.empty()));
            LOG.info("CDP Performance domain enabled");
        });
    }
}
