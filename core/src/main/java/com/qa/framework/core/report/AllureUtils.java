package com.qa.framework.core.report;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public final class AllureUtils {

    private AllureUtils() {
    }

    public static void step(String name, Runnable action) {
        Allure.step(name, () -> {
            action.run();
            return null;
        });
    }

    public static <T> T step(String name, Supplier<T> action) {
        return Allure.step(name, action::get);
    }

    public static void attachText(String name, String content) {
        if (content == null || content.isBlank()) {
            return;
        }
        Allure.addAttachment(name, "text/plain", content, ".txt");
    }

    public static void attachJson(String name, String json) {
        if (json == null || json.isBlank()) {
            return;
        }
        Allure.addAttachment(name, "application/json", json, ".json");
    }

    public static void attachHtml(String name, String html) {
        if (html == null || html.isBlank()) {
            return;
        }
        Allure.addAttachment(name, "text/html", html, ".html");
    }

    public static void attachPng(String name, byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return;
        }
        Allure.addAttachment(name, "image/png", new ByteArrayInputStream(bytes), ".png");
    }

    public static void markBroken(String message) {
        Allure.step(message, Status.BROKEN);
    }

    public static byte[] utf8(String value) {
        return value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
    }
}
