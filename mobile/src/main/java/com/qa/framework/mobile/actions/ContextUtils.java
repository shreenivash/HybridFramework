package com.qa.framework.mobile.actions;

import io.appium.java_client.AppiumDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class ContextUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ContextUtils.class);
    public static final String NATIVE_APP = "NATIVE_APP";

    private ContextUtils() {
    }

    @SuppressWarnings("unchecked")
    public static Set<String> contexts(AppiumDriver driver) {
        Object result = driver.executeScript("mobile: getContexts");
        Set<String> contexts = new LinkedHashSet<>();
        if (result instanceof Collection<?> collection) {
            for (Object item : collection) {
                if (item instanceof Map<?, ?> map && map.containsKey("id")) {
                    contexts.add(String.valueOf(map.get("id")));
                } else if (item != null) {
                    contexts.add(item.toString());
                }
            }
        }
        return contexts;
    }

    public static void nativeApp(AppiumDriver driver) {
        driver.executeScript("mobile: switchContext", Map.of("name", NATIVE_APP));
    }

    public static void webView(AppiumDriver driver) {
        for (String context : contexts(driver)) {
            if (context != null && context.toUpperCase().contains("WEBVIEW")) {
                LOG.info("Switching to context {}", context);
                driver.executeScript("mobile: switchContext", Map.of("name", context));
                return;
            }
        }
        throw new IllegalStateException("No WEBVIEW context available: " + contexts(driver));
    }

    public static String current(AppiumDriver driver) {
        Object result = driver.executeScript("mobile: getCurrentContext");
        return result == null ? NATIVE_APP : String.valueOf(result);
    }
}
