package com.qa.framework.api.retry;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Retries idempotent requests on 5xx or transport failures.
 */
public class ApiRetryFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(ApiRetryFilter.class);
    private static final Set<String> IDEMPOTENT = Set.of("GET", "HEAD", "OPTIONS", "PUT", "DELETE");

    private final int maxAttempts;
    private final long backoffMs;

    public ApiRetryFilter() {
        this(3, 200);
    }

    public ApiRetryFilter(int maxAttempts, long backoffMs) {
        this.maxAttempts = Math.max(1, maxAttempts);
        this.backoffMs = backoffMs;
    }

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext ctx) {
        String method = requestSpec.getMethod() == null ? "GET" : requestSpec.getMethod().toUpperCase();
        if (!IDEMPOTENT.contains(method)) {
            return ctx.next(requestSpec, responseSpec);
        }
        RuntimeException lastError = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                Response response = ctx.next(requestSpec, responseSpec);
                if (response.statusCode() < 500 || attempt == maxAttempts) {
                    return response;
                }
                LOG.warn("API {} {} returned {} — retry {}/{}", method, requestSpec.getURI(),
                        response.statusCode(), attempt, maxAttempts);
            } catch (RuntimeException e) {
                lastError = e;
                if (attempt == maxAttempts) {
                    throw e;
                }
                LOG.warn("API {} {} failed ({}) — retry {}/{}", method, requestSpec.getURI(),
                        e.getMessage(), attempt, maxAttempts);
            }
            sleep();
        }
        if (lastError != null) {
            throw lastError;
        }
        return ctx.next(requestSpec, responseSpec);
    }

    private void sleep() {
        try {
            Thread.sleep(backoffMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
