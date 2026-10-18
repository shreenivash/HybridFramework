package com.qa.framework.perf.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JourneyStep {

    private String name;
    private String method = "GET";
    private String path;
    private Map<String, String> headers = new LinkedHashMap<>();
    private String body;
    private Map<String, String> extractJson = new LinkedHashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getExtractJson() {
        return extractJson;
    }

    public void setExtractJson(Map<String, String> extractJson) {
        this.extractJson = extractJson;
    }
}
