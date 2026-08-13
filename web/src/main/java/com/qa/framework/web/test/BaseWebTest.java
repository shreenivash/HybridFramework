package com.qa.framework.web.test;

import com.qa.framework.core.config.FrameworkConfig;
import com.qa.framework.core.listener.EvidenceCapture;
import com.qa.framework.core.listener.EvidenceRegistry;
import com.qa.framework.core.test.BaseTest;
import com.qa.framework.web.driver.BrowserFactory;
import com.qa.framework.web.driver.WebDriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;

public abstract class BaseWebTest extends BaseTest {

    @Parameters("browser")
    @BeforeMethod(alwaysRun = true)
    public void setUpWeb(@org.testng.annotations.Optional String browser) {
        String resolved = browser == null || browser.isBlank()
                ? FrameworkConfig.browser()
                : browser;
        WebDriver driver = BrowserFactory.createDriver(resolved);
        WebDriverManager.setDriver(driver);
        try {
            driver.manage().window().maximize();
        } catch (RuntimeException e) {
            log.debug("Unable to maximize window: {}", e.getMessage());
        }
        EvidenceRegistry.register(new WebEvidence());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownWeb(ITestResult result) {
        EvidenceRegistry.clear();
        WebDriverManager.quitDriver();
    }

    protected WebDriver driver() {
        return WebDriverManager.getDriver();
    }

    private static final class WebEvidence implements EvidenceCapture {
        @Override
        public java.util.Optional<byte[]> screenshot() {
            if (!WebDriverManager.hasDriver()) {
                return java.util.Optional.empty();
            }
            WebDriver driver = WebDriverManager.getDriver();
            if (driver instanceof TakesScreenshot takesScreenshot) {
                return java.util.Optional.of(takesScreenshot.getScreenshotAs(OutputType.BYTES));
            }
            return java.util.Optional.empty();
        }

        @Override
        public java.util.Optional<String> pageSource() {
            if (!WebDriverManager.hasDriver()) {
                return java.util.Optional.empty();
            }
            return java.util.Optional.ofNullable(WebDriverManager.getDriver().getPageSource());
        }
    }
}
