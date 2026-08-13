package com.qa.framework.web.actions;

import com.qa.framework.core.config.FrameworkConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.function.Function;

public final class WaitUtils {

    private WaitUtils() {
    }

    public static WebDriverWait explicit(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(FrameworkConfig.explicitWaitSeconds()));
    }

    public static WebElement visible(WebDriver driver, By locator) {
        return explicit(driver).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement clickable(WebDriver driver, By locator) {
        return explicit(driver).until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static WebElement present(WebDriver driver, By locator) {
        return explicit(driver).until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public static boolean invisible(WebDriver driver, By locator) {
        return explicit(driver).until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public static void titleContains(WebDriver driver, String title) {
        explicit(driver).until(ExpectedConditions.titleContains(title));
    }

    public static void urlContains(WebDriver driver, String fraction) {
        explicit(driver).until(ExpectedConditions.urlContains(fraction));
    }

    public static void documentReady(WebDriver driver) {
        explicit(driver).until((ExpectedCondition<Boolean>) d ->
                "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
    }

    public static <T> T fluent(WebDriver driver, Function<WebDriver, T> condition) {
        return new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(FrameworkConfig.explicitWaitSeconds()))
                .pollingEvery(Duration.ofMillis(250))
                .ignoring(NoSuchElementException.class)
                .until(condition);
    }
}
