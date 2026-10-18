package com.qa.framework.perf.journey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.qa.framework.perf.model.ApiJourney;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class JourneyLoader {

    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());

    private JourneyLoader() {
    }

    public static ApiJourney load(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            ApiJourney journey = YAML.readValue(in, ApiJourney.class);
            validate(journey);
            return journey;
        }
    }

    public static ApiJourney loadClasspath(String resource) throws IOException {
        try (InputStream in = JourneyLoader.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalArgumentException("Classpath journey not found: " + resource);
            }
            ApiJourney journey = YAML.readValue(in, ApiJourney.class);
            validate(journey);
            return journey;
        }
    }

    private static void validate(ApiJourney journey) {
        if (journey.getBaseUrl() == null || journey.getBaseUrl().isBlank()) {
            throw new IllegalArgumentException("journey.baseUrl is required");
        }
        if (journey.getSteps() == null || journey.getSteps().isEmpty()) {
            throw new IllegalArgumentException("journey.steps must contain at least one step");
        }
        for (var step : journey.getSteps()) {
            if (step.getPath() == null || step.getPath().isBlank()) {
                throw new IllegalArgumentException("Each step requires path");
            }
        }
    }
}
