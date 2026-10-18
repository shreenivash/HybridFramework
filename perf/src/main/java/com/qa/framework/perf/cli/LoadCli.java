package com.qa.framework.perf.cli;

import com.qa.framework.perf.export.ApiSuiteJmxExporter;
import com.qa.framework.perf.journey.JourneyLoader;
import com.qa.framework.perf.jmx.JmxGenerator;
import com.qa.framework.perf.model.ApiJourney;
import com.qa.framework.perf.model.LoadProfile;
import com.qa.framework.perf.plan.LoadPlanExplainer;
import com.qa.framework.perf.run.JmeterRunner;
import org.apache.commons.io.FileUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * CLI for YAML journeys, API suite export, and JMeter runs with fixed TPS.
 */
public final class LoadCli {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }
        String command = args[0].toLowerCase(Locale.ROOT);

        if ("export-suite".equals(command)) {
            CliOptions options = CliOptions.parse(args, 1, false);
            LoadProfile profile = options.toProfile();
            Path out = Path.of(options.outputPath);
            ApiSuiteJmxExporter.ExportReport report;
            if (!options.recordedYaml.isBlank()) {
                report = ApiSuiteJmxExporter.fromRecordedYaml(Path.of(options.recordedYaml), profile, out);
            } else {
                if (options.testPackage.isBlank()) {
                    throw new IllegalArgumentException("export-suite requires --testPackage or --recorded");
                }
                List<String> testClasspath = parseClasspathList(options.testClasspath);
                report = ApiSuiteJmxExporter.fromAnnotations(
                        options.testPackage, options.groups, options.baseUrl, testClasspath, profile, out);
            }
            System.out.println("Wrote " + report.jmxPath().toAbsolutePath() + " (" + report.stepCount() + " steps)");
            for (String warning : report.warnings()) {
                System.out.println("WARN: " + warning);
            }
            System.out.println();
            System.out.print(report.explainText());
            return;
        }

        CliOptions options = CliOptions.parse(args, 1, true);
        ApiJourney journey = JourneyLoader.load(Path.of(options.journeyPath));
        LoadProfile profile = options.toProfile();

        switch (command) {
            case "explain" -> System.out.print(LoadPlanExplainer.explain(journey, profile));
            case "generate" -> {
                String jmx = JmxGenerator.generate(journey, profile);
                Path out = Path.of(options.outputPath);
                Files.createDirectories(out.getParent() != null ? out.getParent() : Path.of("."));
                FileUtils.writeStringToFile(out.toFile(), jmx, StandardCharsets.UTF_8);
                System.out.println("Wrote " + out.toAbsolutePath());
                System.out.println();
                System.out.print(LoadPlanExplainer.explain(journey, profile));
            }
            case "run" -> {
                Path jmxPath = Path.of(options.outputPath);
                if (!Files.exists(jmxPath) || options.regenerate) {
                    String jmx = JmxGenerator.generate(journey, profile);
                    Files.createDirectories(jmxPath.getParent() != null ? jmxPath.getParent() : Path.of("."));
                    FileUtils.writeStringToFile(jmxPath.toFile(), jmx, StandardCharsets.UTF_8);
                    System.out.println("Generated " + jmxPath.toAbsolutePath());
                }
                System.out.print(LoadPlanExplainer.explain(journey, profile));
                Path jtl = options.jtlPath == null ? null : Path.of(options.jtlPath);
                int exit = JmeterRunner.run(jmxPath, profile, jtl);
                if (exit != 0) {
                    System.exit(exit);
                }
            }
            default -> {
                printUsage();
                System.exit(1);
            }
        }
    }

    private static List<String> parseClasspathList(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private static void printUsage() {
        System.out.println("""
                Hybrid Framework load CLI (JMeter fixed TPS)

                Usage:
                  explain       --journey <path.yaml> [load options]
                  generate      --journey <path.yaml> --out <file.jmx> [load options]
                  run           --journey <path.yaml> --out <file.jmx> [--jtl file] [--regenerate] [load options]
                  export-suite  --testPackage <pkg> --baseUrl <url> --out <file.jmx> [--testClasspath dir:...] [--groups api] [load options]
                  export-suite  --recorded <recorded.yaml> --out <file.jmx> [load options]

                export-suite scans @ApiLoadStep on BaseApiTest classes, or converts a recorded YAML.

                Record during functional tests:
                  mvn -pl examples test -Dapi.load.record=true -Dapi.load.record.file=target/recorded.yaml

                Load options:
                  --tps --users --duration --rampUp --latencyMs
                """);
    }
}
