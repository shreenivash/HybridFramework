package com.qa.framework.core.test;

import com.qa.framework.core.config.ConfigLoader;
import com.qa.framework.core.config.FrameworkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeSuite;

/**
 * Suite-level setup shared by web, mobile, and API bases.
 */
public abstract class BaseTest {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @BeforeSuite(alwaysRun = true)
    public void configureSuite() {
        ConfigLoader.getInstance().reload();
        System.setProperty("allure.results.directory", FrameworkConfig.allureResultsDir());
        log.info("Hybrid Framework env={} grid={} apiThreads={}",
                FrameworkConfig.env(),
                FrameworkConfig.hasGrid() ? FrameworkConfig.gridUrl() : "local",
                FrameworkConfig.apiThreadCount());
    }
}
