package com.qa.framework.core.retry;

import com.qa.framework.core.config.FrameworkConfig;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retries a failed test up to {@code retry.count} times (default 1).
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private int attempt;

    @Override
    public synchronized boolean retry(ITestResult result) {
        int max = FrameworkConfig.retryCount();
        if (attempt < max) {
            attempt++;
            return true;
        }
        return false;
    }
}
