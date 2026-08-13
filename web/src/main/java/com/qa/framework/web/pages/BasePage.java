package com.qa.framework.web.pages;

import com.qa.framework.web.actions.ElementActions;
import com.qa.framework.web.actions.WaitUtils;
import com.qa.framework.web.driver.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public abstract class BasePage {

    protected final WebDriver driver;

    protected BasePage() {
        this(WebDriverManager.getDriver());
    }

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    protected void click(By locator) {
        ElementActions.click(driver, locator);
    }

    protected void type(By locator, CharSequence... text) {
        ElementActions.type(driver, locator, text);
    }

    protected String text(By locator) {
        return ElementActions.text(driver, locator);
    }

    protected void waitVisible(By locator) {
        WaitUtils.visible(driver, locator);
    }

    public String title() {
        return driver.getTitle();
    }

    public String url() {
        return driver.getCurrentUrl();
    }
}
