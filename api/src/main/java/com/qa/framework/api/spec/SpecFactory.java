package com.qa.framework.api.spec;

import com.qa.framework.api.config.RestAssuredManager;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.Filter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.util.List;
import java.util.Map;

/**
 * Immutable request/response specs. Always {@link RequestSpecification#spec(RequestSpecification) clone} rather than mutate.
 */
public final class SpecFactory {

    private SpecFactory() {
    }

    public static RequestSpecification baseRequest() {
        RestAssuredManager.initialize();
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .build();
    }

    public static RequestSpecification withHeaders(Map<String, String> headers) {
        RequestSpecBuilder builder = new RequestSpecBuilder().addRequestSpecification(baseRequest());
        if (headers != null) {
            builder.addHeaders(headers);
        }
        return builder.build();
    }

    public static RequestSpecification withFilters(Filter... filters) {
        RequestSpecBuilder builder = new RequestSpecBuilder().addRequestSpecification(baseRequest());
        if (filters != null) {
            builder.addFilters(List.of(filters));
        }
        return builder.build();
    }

    public static ResponseSpecification okJson() {
        return new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectContentType(ContentType.JSON)
                .build();
    }

    public static ResponseSpecification status(int statusCode) {
        return new ResponseSpecBuilder()
                .expectStatusCode(statusCode)
                .build();
    }
}
