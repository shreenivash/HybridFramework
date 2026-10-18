package com.qa.framework.perf.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiJourney {

    private String name = "API Journey";
    private String baseUrl;
    private String description;
    private Map<String, String> variables = new LinkedHashMap<>();
    private List<JourneyStep> steps = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    public List<JourneyStep> getSteps() {
        return steps;
    }

    public void setSteps(List<JourneyStep> steps) {
        this.steps = steps;
    }
}
