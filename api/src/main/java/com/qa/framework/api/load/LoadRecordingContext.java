package com.qa.framework.api.load;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Collects HTTP calls during a functional API run when {@code -Dapi.load.record=true}.
 */
public final class LoadRecordingContext {

    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());
    private static final CopyOnWriteArrayList<RecordedCall> CALLS = new CopyOnWriteArrayList<>();
    private static volatile String baseUrl = "";
    private static volatile String journeyName = "Recorded API Suite";

    private LoadRecordingContext() {
    }

    public static boolean isEnabled() {
        return Boolean.parseBoolean(System.getProperty("api.load.record", "false"));
    }

    public static void configure(String name, String base) {
        if (name != null && !name.isBlank()) {
            journeyName = name;
        }
        if (base != null && !base.isBlank()) {
            baseUrl = base;
        }
    }

    public static void record(String method, String path, String body) {
        if (!isEnabled()) {
            return;
        }
        CALLS.add(new RecordedCall(method, path, body == null ? "" : body));
    }

    public static void clear() {
        CALLS.clear();
        baseUrl = "";
    }

    public static RecordedJourneyYaml toYamlModel() {
        RecordedJourneyYaml journey = new RecordedJourneyYaml();
        journey.setName(journeyName);
        journey.setBaseUrl(baseUrl.isBlank() ? "http://localhost" : baseUrl);
        List<RecordedJourneyYaml.RecordedStepYaml> steps = new ArrayList<>();
        for (RecordedCall call : CALLS) {
            RecordedJourneyYaml.RecordedStepYaml step = new RecordedJourneyYaml.RecordedStepYaml();
            step.setName(call.method + " " + call.path);
            step.setMethod(call.method);
            step.setPath(call.path);
            step.setBody(call.body);
            steps.add(step);
        }
        journey.setSteps(steps);
        return journey;
    }

    public static void writeYaml(Path path) throws IOException {
        RecordedJourneyYaml journey = toYamlModel();
        if (journey.getSteps().isEmpty()) {
            throw new IllegalStateException("No HTTP calls recorded. Run API tests with -Dapi.load.record=true");
        }
        Files.createDirectories(path.getParent() != null ? path.getParent() : Path.of("."));
        YAML.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), journey);
    }

    public static int recordedCount() {
        return CALLS.size();
    }

    public record RecordedCall(String method, String path, String body) {
    }
}
