package com.qa.framework.api.validation;

import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.hamcrest.Matcher;

import static io.restassured.matcher.RestAssuredMatchers.matchesXsdInClasspath;

public final class SchemaValidation {

    private SchemaValidation() {
    }

    public static void jsonSchema(Response response, String classpathSchema) {
        response.then().assertThat().body(JsonSchemaValidator.matchesJsonSchemaInClasspath(classpathSchema));
    }

    public static Matcher<?> jsonSchema(String classpathSchema) {
        return JsonSchemaValidator.matchesJsonSchemaInClasspath(classpathSchema);
    }

    public static void xmlSchema(Response response, String classpathXsd) {
        response.then().assertThat().body(matchesXsdInClasspath(classpathXsd));
    }
}
