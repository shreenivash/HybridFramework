package com.qa.framework.mobile.actions;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.List;

public final class MobileActions {

    private static final PointerInput FINGER = new PointerInput(PointerInput.Kind.TOUCH, "finger");

    private MobileActions() {
    }

    public static void tap(AppiumDriver driver, By locator) {
        driver.findElement(locator).click();
    }

    public static void tap(AppiumDriver driver, int x, int y) {
        Sequence tap = new Sequence(FINGER, 1);
        tap.addAction(FINGER.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y));
        tap.addAction(FINGER.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(new Pause(FINGER, Duration.ofMillis(100)));
        tap.addAction(FINGER.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(tap));
    }

    public static void longPress(AppiumDriver driver, By locator, Duration hold) {
        WebElement element = driver.findElement(locator);
        Point center = center(element);
        Sequence press = new Sequence(FINGER, 1);
        press.addAction(FINGER.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), center.x, center.y));
        press.addAction(FINGER.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        press.addAction(new Pause(FINGER, hold));
        press.addAction(FINGER.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(press));
    }

    public static void swipe(AppiumDriver driver, Point start, Point end, Duration duration) {
        Sequence swipe = new Sequence(FINGER, 1);
        swipe.addAction(FINGER.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), start.x, start.y));
        swipe.addAction(FINGER.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(FINGER.createPointerMove(duration, PointerInput.Origin.viewport(), end.x, end.y));
        swipe.addAction(FINGER.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(swipe));
    }

    public static void swipeUp(AppiumDriver driver) {
        Dimension size = driver.manage().window().getSize();
        swipe(driver, new Point(size.width / 2, (int) (size.height * 0.8)),
                new Point(size.width / 2, (int) (size.height * 0.2)), Duration.ofMillis(400));
    }

    public static void swipeDown(AppiumDriver driver) {
        Dimension size = driver.manage().window().getSize();
        swipe(driver, new Point(size.width / 2, (int) (size.height * 0.2)),
                new Point(size.width / 2, (int) (size.height * 0.8)), Duration.ofMillis(400));
    }

    public static void pinch(AppiumDriver driver, WebElement element) {
        Point center = center(element);
        PointerInput finger1 = new PointerInput(PointerInput.Kind.TOUCH, "finger1");
        PointerInput finger2 = new PointerInput(PointerInput.Kind.TOUCH, "finger2");
        Sequence pinch1 = pinchSequence(finger1, center, 120, 20);
        Sequence pinch2 = pinchSequence(finger2, center, -120, -20);
        driver.perform(List.of(pinch1, pinch2));
    }

    public static void zoom(AppiumDriver driver, WebElement element) {
        Point center = center(element);
        PointerInput finger1 = new PointerInput(PointerInput.Kind.TOUCH, "finger1");
        PointerInput finger2 = new PointerInput(PointerInput.Kind.TOUCH, "finger2");
        Sequence zoom1 = pinchSequence(finger1, center, 20, 120);
        Sequence zoom2 = pinchSequence(finger2, center, -20, -120);
        driver.perform(List.of(zoom1, zoom2));
    }

    public static void sendKeys(AppiumDriver driver, By locator, CharSequence... keys) {
        WebElement element = driver.findElement(locator);
        element.click();
        element.sendKeys(keys);
    }

    public static void hideKeyboard(AppiumDriver driver) {
        try {
            driver.executeScript("mobile: hideKeyboard");
        } catch (RuntimeException ignored) {
            // not all drivers support hideKeyboard
        }
    }

    public static WebElement scrollToText(AppiumDriver driver, String text) {
        return driver.findElement(AppiumBy.androidUIAutomator(
                "new UiScrollable(new UiSelector().scrollable(true)).scrollTextIntoView(\"" + text + "\")"));
    }

    private static Sequence pinchSequence(PointerInput finger, Point center, int startOffset, int endOffset) {
        Sequence sequence = new Sequence(finger, 0);
        sequence.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(),
                center.x + startOffset, center.y + startOffset));
        sequence.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        sequence.addAction(finger.createPointerMove(Duration.ofMillis(400), PointerInput.Origin.viewport(),
                center.x + endOffset, center.y + endOffset));
        sequence.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        return sequence;
    }

    private static Point center(WebElement element) {
        Point location = element.getLocation();
        Dimension size = element.getSize();
        return new Point(location.x + size.width / 2, location.y + size.height / 2);
    }
}
