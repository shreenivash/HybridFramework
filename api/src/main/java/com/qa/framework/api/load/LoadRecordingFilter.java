package com.qa.framework.api.load;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

/**
 * Records each HTTP call for later JMX export when recording is enabled.
 */
public class LoadRecordingFilter implements Filter {

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext ctx) {
        if (LoadRecordingContext.isEnabled()) {
            String method = requestSpec.getMethod() == null ? "GET" : requestSpec.getMethod();
            String path = extractPath(requestSpec.getURI());
            String body = requestSpec.getBody() == null ? "" : String.valueOf(requestSpec.getBody());
            LoadRecordingContext.record(method, path, body);
        }
        return ctx.next(requestSpec, responseSpec);
    }

    private static String extractPath(String uri) {
        if (uri == null || uri.isBlank()) {
            return "/";
        }
        int scheme = uri.indexOf("://");
        if (scheme < 0) {
            return uri.startsWith("/") ? uri : "/" + uri;
        }
        int pathStart = uri.indexOf('/', scheme + 3);
        if (pathStart < 0) {
            return "/";
        }
        int query = uri.indexOf('?', pathStart);
        return query > 0 ? uri.substring(pathStart, query) : uri.substring(pathStart);
    }
}
