package com.qa.framework.api.auth;

import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public final class AuthUtils {

    private AuthUtils() {
    }

    public static RequestSpecification basic(String username, String password) {
        return given().auth().preemptive().basic(username, password);
    }

    public static RequestSpecification bearer(String token) {
        return given().auth().oauth2(token);
    }

    public static RequestSpecification oauth2(String token) {
        return bearer(token);
    }

    public static RequestSpecification digest(String username, String password) {
        return given().auth().digest(username, password);
    }

    public static RequestSpecification form(String username, String password) {
        return given().auth().form(username, password);
    }
}
