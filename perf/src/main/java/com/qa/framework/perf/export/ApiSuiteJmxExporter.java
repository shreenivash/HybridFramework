package com.qa.framework.perf.export;

import com.qa.framework.perf.journey.JourneyLoader;
import com.qa.framework.perf.jmx.JmxGenerator;
import com.qa.framework.perf.model.ApiJourney;
import com.qa.framework.perf.model.LoadProfile;
import com.qa.framework.perf.plan.LoadPlanExplainer;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class ApiSuiteJmxExporter {

    private ApiSuiteJmxExporter() {
    }

    public static ExportReport fromAnnotations(String testPackage, String groupsCsv, String baseUrl,
                                               LoadProfile profile, Path outJmx) throws Exception {
        return fromAnnotations(testPackage, groupsCsv, baseUrl, List.of(), profile, outJmx);
    }

    public static ExportReport fromAnnotations(String testPackage, String groupsCsv, String baseUrl,
                                               List<String> extraClasspath, LoadProfile profile, Path outJmx)
            throws Exception {
        Set<String> groups = parseGroups(groupsCsv);
        ApiSuiteScanner.SuiteScanResult scan =
                ApiSuiteScanner.scan(testPackage, groups, baseUrl, extraClasspath);
        return write(scan.journey(), scan.warnings(), profile, outJmx);
    }

    public static ExportReport fromRecordedYaml(Path yaml, LoadProfile profile, Path outJmx) throws Exception {
        ApiJourney journey = JourneyLoader.load(yaml);
        return write(journey, List.of(), profile, outJmx);
    }

    private static ExportReport write(ApiJourney journey, java.util.List<String> warnings,
                                      LoadProfile profile, Path outJmx) throws Exception {
        String jmx = JmxGenerator.generate(journey, profile);
        Files.createDirectories(outJmx.getParent() != null ? outJmx.getParent() : Path.of("."));
        Files.writeString(outJmx, jmx, StandardCharsets.UTF_8);
        String explain = LoadPlanExplainer.explain(journey, profile);
        return new ExportReport(outJmx, journey.getSteps().size(), warnings, explain);
    }

    private static Set<String> parseGroups(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of("api");
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public record ExportReport(Path jmxPath, int stepCount, java.util.List<String> warnings, String explainText) {
    }
}
