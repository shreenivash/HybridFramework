package com.qa.examples.web.pages;

import com.qa.framework.web.actions.ElementActions;
import com.qa.framework.web.pages.BasePage;
import org.openqa.selenium.By;

public class LoginPage extends BasePage {

    private static final By USERNAME = By.id("username");
    private static final By PASSWORD = By.id("password");
    private static final By SUBMIT = By.cssSelector("button[type='submit']");
    private static final By FLASH = By.id("flash");

    public LoginPage open() {
        driver.get("https://the-internet.herokuapp.com/login");
        return this;
    }

    public LoginPage login(String user, String pass) {
        ElementActions.type(driver, USERNAME, user);
        ElementActions.type(driver, PASSWORD, pass);
        ElementActions.click(driver, SUBMIT);
        return this;
    }

    public String flashMessage() {
        return ElementActions.text(driver, FLASH);
    }
}
