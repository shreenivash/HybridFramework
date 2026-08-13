package com.qa.examples.mobile;

import com.qa.framework.mobile.actions.MobileActions;
import com.qa.framework.mobile.test.BaseMobileTest;
import io.appium.java_client.AppiumBy;
import io.qameta.allure.Description;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Runs only when {@code -Dapp.path=/path/to/app.apk} (or {@code app} in config) is provided.
 */
public class SampleMobileTest extends BaseMobileTest {

    @Test(groups = {"mobile"})
    @Description("Launches the configured app and verifies the session")
    public void appLaunches() {
        Assert.assertNotNull(driver().getSessionId());
        MobileActions.hideKeyboard(driver());
        driver().findElements(AppiumBy.xpath("//*"));
    }
}
