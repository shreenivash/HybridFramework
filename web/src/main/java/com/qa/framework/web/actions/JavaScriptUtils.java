package com.qa.framework.web.actions;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public final class JavaScriptUtils {

    private JavaScriptUtils() {
    }

    public static Object execute(WebDriver driver, String script, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }

    public static void scrollIntoView(WebDriver driver, WebElement element) {
        execute(driver, "arguments[0].scrollIntoView({block:'center'});", element);
    }

    public static void scrollToBottom(WebDriver driver) {
        execute(driver, "window.scrollTo(0, document.body.scrollHeight);");
    }

    public static void highlight(WebDriver driver, WebElement element) {
        execute(driver, "arguments[0].style.border='3px solid #e74c3c'", element);
    }

    public static void click(WebDriver driver, WebElement element) {
        execute(driver, "arguments[0].click();", element);
    }

    public static void setValue(WebDriver driver, WebElement element, String value) {
        execute(driver, "arguments[0].value=arguments[1];", element, value);
    }

    /**
     * Returns the shadow root of {@code host} as a {@link SearchContext} for nested queries.
     */
    public static SearchContext shadowRoot(WebDriver driver, WebElement host) {
        Object root = execute(driver, "return arguments[0].shadowRoot", host);
        if (root instanceof SearchContext searchContext) {
            return searchContext;
        }
        throw new IllegalStateException("Unable to access shadow root");
    }

    public static WebElement inShadow(WebDriver driver, By host, By inner) {
        WebElement hostElement = driver.findElement(host);
        SearchContext root = shadowRoot(driver, hostElement);
        return root.findElement(inner);
    }

    @SuppressWarnings("unchecked")
    public static List<WebElement> querySelectorAll(WebDriver driver, String css) {
        return (List<WebElement>) execute(driver, "return document.querySelectorAll(arguments[0])", css);
    }
}
