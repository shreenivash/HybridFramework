package com.qa.framework.web.util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class WindowUtils {

    private WindowUtils() {
    }

    public static void switchToWindow(WebDriver driver, String titleFragment) {
        String current = driver.getWindowHandle();
        for (String handle : driver.getWindowHandles()) {
            driver.switchTo().window(handle);
            if (driver.getTitle() != null && driver.getTitle().contains(titleFragment)) {
                return;
            }
        }
        driver.switchTo().window(current);
        throw new IllegalStateException("No window with title containing: " + titleFragment);
    }

    public static void switchToLatest(WebDriver driver) {
        List<String> handles = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(handles.getLast());
    }

    public static void closeOthers(WebDriver driver) {
        String current = driver.getWindowHandle();
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(current)) {
                driver.switchTo().window(handle).close();
            }
        }
        driver.switchTo().window(current);
    }

    public static void frame(WebDriver driver, By locator) {
        WebElement frame = driver.findElement(locator);
        driver.switchTo().frame(frame);
    }

    public static void frame(WebDriver driver, String nameOrId) {
        driver.switchTo().frame(nameOrId);
    }

    public static void defaultContent(WebDriver driver) {
        driver.switchTo().defaultContent();
    }

    public static void parentFrame(WebDriver driver) {
        driver.switchTo().parentFrame();
    }

    public static void alertAccept(WebDriver driver) {
        driver.switchTo().alert().accept();
    }

    public static void alertDismiss(WebDriver driver) {
        driver.switchTo().alert().dismiss();
    }
}
