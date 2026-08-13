package com.qa.examples.web;

import com.qa.examples.web.pages.LoginPage;
import com.qa.framework.web.actions.ElementActions;
import com.qa.framework.web.test.BaseWebTest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

@Epic("Web")
@Feature("the-internet")
public class TheInternetWebTest extends BaseWebTest {

    @Test(groups = {"smoke", "web"})
    @Description("Valid login on the-internet")
    public void loginSuccess() {
        LoginPage page = new LoginPage().open().login("tomsmith", "SuperSecretPassword!");
        Assert.assertTrue(page.flashMessage().contains("You logged into a secure area"));
    }

    @Test(groups = {"web"})
    @Description("Add/remove elements")
    public void addElement() {
        driver().get("https://the-internet.herokuapp.com/add_remove_elements/");
        ElementActions.click(driver(), By.cssSelector("button[onclick='addElement()']"));
        Assert.assertTrue(ElementActions.displayed(driver(), By.className("added-manually")));
    }
}
