package com.qa.framework.perf.export;

import com.qa.framework.api.load.ApiLoadStep;
import com.qa.framework.api.load.ApiLoadSuite;
import com.qa.framework.api.test.BaseApiTest;
import com.qa.framework.perf.model.ApiJourney;
import com.qa.framework.perf.model.JourneyStep;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import org.testng.annotations.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Scans TestNG API test classes for {@link ApiLoadStep} and builds a single {@link ApiJourney}.
 */
public final class ApiSuiteScanner {

    public record SuiteScanResult(ApiJourney journey, List<String> warnings) {
    }

    private ApiSuiteScanner() {
    }

    public static SuiteScanResult scan(String testPackage, Set<String> groups, String defaultBaseUrl) {
        return scan(testPackage, groups, defaultBaseUrl, List.of());
    }

    public static SuiteScanResult scan(
            String testPackage, Set<String> groups, String defaultBaseUrl, List<String> extraClasspath) {
        if (testPackage == null || testPackage.isBlank()) {
            throw new IllegalArgumentException("--testPackage is required");
        }
        List<Class<?>> discovered = new ArrayList<>();
        ClassGraph classGraph = new ClassGraph()
                .enableClassInfo()
                .enableMethodInfo()
                .enableAnnotationInfo();
        if (extraClasspath != null && !extraClasspath.isEmpty()) {
            URL[] urls = extraClasspath.stream()
                    .map(p -> {
                        try {
                            return Path.of(p).toAbsolutePath().toUri().toURL();
                        } catch (java.net.MalformedURLException e) {
                            throw new IllegalArgumentException("Invalid --testClasspath entry: " + p, e);
                        }
                    })
                    .toArray(URL[]::new);
            ClassLoader parent = Thread.currentThread().getContextClassLoader();
            ClassLoader addon = new URLClassLoader(urls, parent);
            classGraph.addClassLoader(addon);
        }
        try (ScanResult scan = classGraph.acceptPackages(testPackage).scan()) {
            for (ClassInfo classInfo : scan.getClassesWithMethodAnnotation(Test.class.getName())) {
                Class<?> clazz = classInfo.loadClass();
                if (BaseApiTest.class.isAssignableFrom(clazz)) {
                    discovered.add(clazz);
                }
            }
        }
        return buildFromClasses(discovered, groups, defaultBaseUrl);
    }

    /** Visible for unit tests and explicit class lists. */
    static SuiteScanResult buildFromClasses(
            List<Class<?>> testClasses, Set<String> groups, String defaultBaseUrl) {
        List<String> warnings = new ArrayList<>();
        List<ClassEntry> classes = new ArrayList<>();

        for (Class<?> clazz : testClasses) {
            if (!BaseApiTest.class.isAssignableFrom(clazz)) {
                continue;
            }
            ApiLoadSuite suiteMeta = clazz.getAnnotation(ApiLoadSuite.class);
            List<MethodStep> steps = new ArrayList<>();
            for (Method method : clazz.getDeclaredMethods()) {
                ApiLoadStep load = method.getAnnotation(ApiLoadStep.class);
                Test test = method.getAnnotation(Test.class);
                if (load == null) {
                    if (test != null && test.enabled()) {
                        warnings.add("Skipped (no @ApiLoadStep): " + clazz.getSimpleName() + "#" + method.getName());
                    }
                    continue;
                }
                if (test != null && !matchesGroups(test.groups(), groups)) {
                    continue;
                }
                steps.add(new MethodStep(method.getName(), load));
            }
            if (!steps.isEmpty()) {
                int order = suiteMeta == null ? 0 : suiteMeta.order();
                String baseUrl = suiteMeta != null && !suiteMeta.baseUrl().isBlank()
                        ? suiteMeta.baseUrl()
                        : defaultBaseUrl;
                String name = suiteMeta != null && !suiteMeta.name().isBlank()
                        ? suiteMeta.name()
                        : clazz.getSimpleName();
                classes.add(new ClassEntry(order, name, baseUrl, steps));
            }
        }

        classes.sort(Comparator.comparingInt(ClassEntry::order).thenComparing(ClassEntry::name));

        ApiJourney journey = new ApiJourney();
        journey.setName("Exported API suite");
        journey.setBaseUrl(resolveBaseUrl(classes, defaultBaseUrl, warnings));

        List<JourneyStep> allSteps = new ArrayList<>();
        for (ClassEntry entry : classes) {
            for (MethodStep ms : entry.steps) {
                allSteps.add(toStep(ms));
            }
        }
        journey.setSteps(allSteps);

        if (allSteps.isEmpty()) {
            warnings.add("No @ApiLoadStep methods found. Annotate tests or use record mode (-Dapi.load.record=true).");
        }
        return new SuiteScanResult(journey, warnings);
    }

    private static boolean matchesGroups(String[] methodGroups, Set<String> required) {
        if (required == null || required.isEmpty() || required.contains("*")) {
            return true;
        }
        Set<String> method = new LinkedHashSet<>(Arrays.asList(methodGroups));
        for (String group : required) {
            if (method.contains(group)) {
                return true;
            }
        }
        return false;
    }

    private static String resolveBaseUrl(List<ClassEntry> classes, String defaultBaseUrl, List<String> warnings) {
        Set<String> bases = new LinkedHashSet<>();
        for (ClassEntry entry : classes) {
            if (entry.baseUrl != null && !entry.baseUrl.isBlank()) {
                bases.add(entry.baseUrl);
            }
        }
        if (bases.size() == 1) {
            return bases.iterator().next();
        }
        if (bases.isEmpty() && defaultBaseUrl != null && !defaultBaseUrl.isBlank()) {
            return defaultBaseUrl;
        }
        if (bases.size() > 1) {
            warnings.add("Multiple baseUrl values found; using first: " + bases.iterator().next());
            return bases.iterator().next();
        }
        throw new IllegalArgumentException("baseUrl is required (--baseUrl or @ApiLoadSuite.baseUrl)");
    }

    private static JourneyStep toStep(MethodStep ms) {
        JourneyStep step = new JourneyStep();
        step.setName(ms.methodName);
        step.setMethod(ms.load.method());
        step.setPath(ms.load.path());
        step.setBody(ms.load.body());
        Map<String, String> extract = new LinkedHashMap<>();
        for (String pair : ms.load.extractJson()) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                extract.put(pair.substring(0, eq).trim(), pair.substring(eq + 1).trim());
            }
        }
        step.setExtractJson(extract);
        return step;
    }

    private record ClassEntry(int order, String name, String baseUrl, List<MethodStep> steps) {
    }

    private record MethodStep(String methodName, ApiLoadStep load) {
    }
}
