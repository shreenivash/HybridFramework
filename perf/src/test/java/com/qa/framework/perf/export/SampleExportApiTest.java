package com.qa.framework.perf.export;

import com.qa.framework.api.load.ApiLoadStep;
import com.qa.framework.api.load.ApiLoadSuite;
import com.qa.framework.api.test.BaseApiTest;
import org.testng.annotations.Test;

@ApiLoadSuite(baseUrl = "https://example.com")
public class SampleExportApiTest extends BaseApiTest {

    @Test(groups = "api", enabled = false)
    @ApiLoadStep(method = "GET", path = "/health")
    void health() {
        // fixture for ClassGraph scan only
    }
}
