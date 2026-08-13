package com.qa.framework.web.actions;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import java.nio.file.Path;
import java.util.List;

public final class ElementActions {

    private ElementActions() {
    }

    public static void click(WebDriver driver, By locator) {
        WaitUtils.clickable(driver, locator).click();
    }

    public static void click(WebDriver driver, WebElement element) {
        JavaScriptUtils.scrollIntoView(driver, element);
        element.click();
    }

    public static void type(WebDriver driver, By locator, CharSequence... text) {
        WebElement element = WaitUtils.visible(driver, locator);
        element.clear();
        element.sendKeys(text);
    }

    public static void type(WebDriver driver, WebElement element, CharSequence... text) {
        element.clear();
        element.sendKeys(text);
    }

    public static void clear(WebDriver driver, By locator) {
        WaitUtils.visible(driver, locator).clear();
    }

    public static String text(WebDriver driver, By locator) {
        return WaitUtils.visible(driver, locator).getText();
    }

    public static String attribute(WebDriver driver, By locator, String name) {
        return WaitUtils.present(driver, locator).getAttribute(name);
    }

    public static void selectByVisibleText(WebDriver driver, By locator, String text) {
        new Select(WaitUtils.visible(driver, locator)).selectByVisibleText(text);
    }

    public static void selectByValue(WebDriver driver, By locator, String value) {
        new Select(WaitUtils.visible(driver, locator)).selectByValue(value);
    }

    public static void selectByIndex(WebDriver driver, By locator, int index) {
        new Select(WaitUtils.visible(driver, locator)).selectByIndex(index);
    }

    public static void check(WebDriver driver, By locator) {
        WebElement element = WaitUtils.clickable(driver, locator);
        if (!element.isSelected()) {
            element.click();
        }
    }

    public static void uncheck(WebDriver driver, By locator) {
        WebElement element = WaitUtils.clickable(driver, locator);
        if (element.isSelected()) {
            element.click();
        }
    }

    public static void selectRadio(WebDriver driver, By locator) {
        check(driver, locator);
    }

    public static void upload(WebDriver driver, By locator, Path file) {
        WaitUtils.present(driver, locator).sendKeys(file.toAbsolutePath().toString());
    }

    public static void hover(WebDriver driver, By locator) {
        new Actions(driver).moveToElement(WaitUtils.visible(driver, locator)).perform();
    }

    public static void dragAndDrop(WebDriver driver, By source, By target) {
        new Actions(driver)
                .dragAndDrop(WaitUtils.visible(driver, source), WaitUtils.visible(driver, target))
                .perform();
    }

    public static void scrollTo(WebDriver driver, By locator) {
        JavaScriptUtils.scrollIntoView(driver, WaitUtils.present(driver, locator));
    }

    public static void press(WebDriver driver, By locator, Keys key) {
        WaitUtils.visible(driver, locator).sendKeys(key);
    }

    public static boolean displayed(WebDriver driver, By locator) {
        List<WebElement> elements = driver.findElements(locator);
        return !elements.isEmpty() && elements.getFirst().isDisplayed();
    }
}
