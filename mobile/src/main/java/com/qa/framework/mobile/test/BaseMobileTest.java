package com.qa.framework.mobile.test;

import com.qa.framework.core.config.ConfigLoader;
import com.qa.framework.core.listener.EvidenceCapture;
import com.qa.framework.core.listener.EvidenceRegistry;
import com.qa.framework.core.test.BaseTest;
import com.qa.framework.mobile.driver.AppiumDriverFactory;
import com.qa.framework.mobile.driver.AppiumDriverManager;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.OutputType;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;

public abstract class BaseMobileTest extends BaseTest {

    @Parameters("device")
    @BeforeMethod(alwaysRun = true)
    public void setUpMobile(@org.testng.annotations.Optional String device) {
        String app = ConfigLoader.getInstance().get("app", System.getProperty("app.path", ""));
        if (app == null || app.isBlank()) {
            throw new SkipException("Mobile tests require -Dapp.path or config key 'app'");
        }
        String resolved = device == null || device.isBlank()
                ? ConfigLoader.getInstance().get("device.name", "Android Emulator")
                : device;
        AppiumDriver driver = AppiumDriverFactory.createDriver(resolved);
        AppiumDriverManager.setDriver(driver);
        EvidenceRegistry.register(new MobileEvidence());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownMobile(ITestResult result) {
        EvidenceRegistry.clear();
        AppiumDriverManager.quitDriver();
    }

    protected AppiumDriver driver() {
        return AppiumDriverManager.getDriver();
    }

    private static final class MobileEvidence implements EvidenceCapture {
        @Override
        public java.util.Optional<byte[]> screenshot() {
            if (!AppiumDriverManager.hasDriver()) {
                return java.util.Optional.empty();
            }
            return java.util.Optional.of(AppiumDriverManager.getDriver().getScreenshotAs(OutputType.BYTES));
        }

        @Override
        public java.util.Optional<String> pageSource() {
            if (!AppiumDriverManager.hasDriver()) {
                return java.util.Optional.empty();
            }
            return java.util.Optional.ofNullable(AppiumDriverManager.getDriver().getPageSource());
        }
    }
}
