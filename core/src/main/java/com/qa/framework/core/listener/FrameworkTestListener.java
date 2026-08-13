package com.qa.framework.core.listener;

import com.qa.framework.core.report.AllureEnvironmentWriter;
import com.qa.framework.core.report.AllureUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Logs test lifecycle events and attaches failure evidence to Allure. Writes environment metadata when the suite finishes.
 */
public class FrameworkTestListener implements ITestListener, ISuiteListener {

    private static final Logger LOG = LoggerFactory.getLogger(FrameworkTestListener.class);

    @Override
    public void onStart(ISuite suite) {
        LOG.info("Suite started: {}", suite.getName());
    }

    @Override
    public void onFinish(ISuite suite) {
        LOG.info("Suite finished: {}", suite.getName());
        AllureEnvironmentWriter.write();
    }

    @Override
    public void onTestStart(ITestResult result) {
        LOG.info("START {}", qualifiedName(result));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        LOG.info("PASS  {}", qualifiedName(result));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        LOG.error("FAIL  {}", qualifiedName(result), result.getThrowable());
        attachEvidence();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        LOG.warn("SKIP  {}", qualifiedName(result));
    }

    private void attachEvidence() {
        EvidenceCapture capture = EvidenceRegistry.current();
        capture.screenshot().ifPresent(bytes -> AllureUtils.attachPng("Failure screenshot", bytes));
        capture.pageSource().ifPresent(html -> AllureUtils.attachHtml("Page source", html));
        capture.lastRequest().ifPresent(body -> AllureUtils.attachText("Last API request", body));
        capture.lastResponse().ifPresent(body -> AllureUtils.attachText("Last API response", body));
    }

    private static String qualifiedName(ITestResult result) {
        return result.getTestClass().getName() + "." + result.getMethod().getMethodName();
    }
}
