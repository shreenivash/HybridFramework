package com.qa.framework.api.load;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a TestNG API test class as exportable to a JMeter journey via the perf {@code export-suite} CLI.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ApiLoadSuite {

    /** Journey display name; defaults to simple class name. */
    String name() default "";

    /** Base URI for HTTP samplers when not set globally on export. */
    String baseUrl() default "";

    /** Lower values export earlier when merging multiple classes. */
    int order() default 0;
}
