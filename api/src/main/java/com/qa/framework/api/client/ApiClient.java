package com.qa.framework.api.client;

import com.qa.framework.api.retry.ApiRetryFilter;
import com.qa.framework.api.spec.SpecFactory;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.File;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Thread-safe HTTP helpers. Each call clones {@link SpecFactory#baseRequest()} and never mutates a shared spec.
 */
public class ApiClient {

    private final RequestSpecification baseSpec;

    public ApiClient() {
        this(SpecFactory.withFilters(new ApiRetryFilter()));
    }

    public ApiClient(RequestSpecification baseSpec) {
        this.baseSpec = baseSpec;
    }

    public Response get(String path) {
        return get(path, Map.of());
    }

    public Response get(String path, Map<String, ?> queryParams) {
        return spec().queryParams(queryParams == null ? Map.of() : queryParams).get(path);
    }

    public Response post(String path, Object body) {
        return spec().body(body).post(path);
    }

    public Response put(String path, Object body) {
        return spec().body(body).put(path);
    }

    public Response patch(String path, Object body) {
        return spec().body(body).patch(path);
    }

    public Response delete(String path) {
        return spec().delete(path);
    }

    public Response head(String path) {
        return spec().head(path);
    }

    public Response options(String path) {
        return spec().options(path);
    }

    public Response multipart(String path, String controlName, File file) {
        return spec().contentType(ContentType.MULTIPART).multiPart(controlName, file).post(path);
    }

    public <T> T as(Response response, Class<T> type) {
        return response.then().extract().as(type);
    }

    protected RequestSpecification spec() {
        return given().spec(baseSpec);
    }
}
