package com.qa.framework.perf.export;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;

public class ApiSuiteScannerTest {

    @Test
    public void scansAnnotatedExampleTests() {
        var result = ApiSuiteScanner.buildFromClasses(
                List.of(SampleExportApiTest.class), Set.of("api"), "");
        Assert.assertEquals(result.journey().getSteps().size(), 1);
        Assert.assertEquals(result.journey().getBaseUrl(), "https://example.com");
    }
}
