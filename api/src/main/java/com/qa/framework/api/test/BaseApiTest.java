package com.qa.framework.api.test;

import com.qa.framework.api.client.ApiClient;
import com.qa.framework.api.config.RestAssuredManager;
import com.qa.framework.api.spec.SpecFactory;
import com.qa.framework.core.test.BaseTest;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeMethod;

public abstract class BaseApiTest extends BaseTest {

    protected ApiClient api;
    protected RequestSpecification requestSpec;

    @BeforeMethod(alwaysRun = true)
    public void setUpApi() {
        RestAssuredManager.initialize();
        requestSpec = SpecFactory.baseRequest();
        api = new ApiClient();
    }
}
