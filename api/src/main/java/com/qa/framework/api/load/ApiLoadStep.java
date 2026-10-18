package com.qa.framework.api.load;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares one HTTP step for load export. Place on {@code @Test} methods alongside functional API tests.
 * Format for {@link #extractJson}: {@code variableName=$.json.path} (comma-separated in annotation array).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ApiLoadStep {

    String method();

    String path();

    /** Raw JSON body for POST/PUT/PATCH; empty for GET/DELETE. */
    String body() default "";

    /**
     * JSONPath extractions, e.g. {@code "postId=$.id"}.
     */
    String[] extractJson() default {};
}
