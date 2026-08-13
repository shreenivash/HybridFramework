package com.qa.framework.core.report;

import com.qa.framework.core.config.ConfigLoader;
import com.qa.framework.core.config.FrameworkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Writes Allure {@code environment.properties}, {@code executor.json}, and {@code categories.json} after the suite.
 */
public final class AllureEnvironmentWriter {

    private static final Logger LOG = LoggerFactory.getLogger(AllureEnvironmentWriter.class);

    private AllureEnvironmentWriter() {
    }

    public static void write() {
        Path results = Path.of(FrameworkConfig.allureResultsDir());
        try {
            Files.createDirectories(results);
            writeEnvironment(results);
            writeExecutor(results);
            copyCategories(results);
        } catch (IOException e) {
            LOG.warn("Unable to write Allure metadata: {}", e.getMessage());
        }
    }

    private static void writeEnvironment(Path results) throws IOException {
        Map<String, String> env = new LinkedHashMap<>();
        env.put("env", FrameworkConfig.env());
        env.put("grid.url", blankToDash(FrameworkConfig.gridUrl()));
        env.put("grid.headless", String.valueOf(FrameworkConfig.gridHeadless()));
        env.put("api.threadCount", String.valueOf(FrameworkConfig.apiThreadCount()));
        env.put("api.maxDurationMinutes", String.valueOf(FrameworkConfig.apiMaxDurationMinutes()));
        env.put("java.version", System.getProperty("java.version", ""));
        env.put("os.name", System.getProperty("os.name", ""));
        String gitCommit = firstNonBlank(System.getenv("GIT_COMMIT"), System.getenv("GITHUB_SHA"));
        if (gitCommit != null) {
            env.put("git.commit", gitCommit);
        }
        ConfigLoader.getInstance().snapshot().forEach((k, v) -> {
            if (k.startsWith("browsers") || k.startsWith("devices") || k.equals("parallelMode")) {
                env.put(k, v);
            }
        });

        Properties props = new Properties();
        env.forEach(props::setProperty);
        try (var out = Files.newOutputStream(results.resolve("environment.properties"))) {
            props.store(out, "Hybrid Framework Allure environment");
        }
    }

    private static void writeExecutor(Path results) throws IOException {
        String buildUrl = firstNonBlank(System.getenv("BUILD_URL"), System.getenv("GITHUB_SERVER_URL"));
        String buildName = firstNonBlank(System.getenv("BUILD_TAG"), System.getenv("GITHUB_RUN_ID"), "local");
        String reportName = firstNonBlank(System.getenv("JOB_NAME"), System.getenv("GITHUB_WORKFLOW"), "Hybrid Framework");
        String json = """
                {
                  "name": "%s",
                  "type": "%s",
                  "buildName": "%s",
                  "buildUrl": "%s",
                  "reportName": "Allure Report"
                }
                """.formatted(
                escape(reportName),
                System.getenv("BUILD_URL") != null ? "jenkins" : "github",
                escape(buildName),
                escape(buildUrl == null ? "" : buildUrl)
        );
        Files.writeString(results.resolve("executor.json"), json, StandardCharsets.UTF_8);
    }

    private static void copyCategories(Path results) throws IOException {
        try (InputStream in = AllureEnvironmentWriter.class.getClassLoader()
                .getResourceAsStream("allure/categories.json")) {
            if (in == null) {
                return;
            }
            Files.copy(in, results.resolve("categories.json"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String blankToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @SuppressWarnings("unused")
    private static String joinSnapshot() {
        return ConfigLoader.getInstance().snapshot().entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(","));
    }
}
