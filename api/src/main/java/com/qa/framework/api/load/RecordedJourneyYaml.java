package com.qa.framework.api.load;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RecordedJourneyYaml {

    private String name = "Recorded API Suite";
    private String baseUrl;
    private List<RecordedStepYaml> steps = new ArrayList<>();

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

    public List<RecordedStepYaml> getSteps() {
        return steps;
    }

    public void setSteps(List<RecordedStepYaml> steps) {
        this.steps = steps;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecordedStepYaml {
        private String name;
        private String method;
        private String path;
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
}
