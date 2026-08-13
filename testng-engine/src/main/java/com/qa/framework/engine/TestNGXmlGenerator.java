package com.qa.framework.engine;

import com.qa.framework.core.config.FrameworkConfig;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generates {@code target/testng-runtime.xml} from Jenkins/system properties.
 */
public final class TestNGXmlGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(TestNGXmlGenerator.class);

    private TestNGXmlGenerator() {
    }

    public static void main(String[] args) throws IOException {
        XmlSuite suite = generate();
        Path output = Path.of(System.getProperty("testng.runtime.xml", "target/testng-runtime.xml"));
        FileUtils.forceMkdirParent(output.toFile());
        FileUtils.writeStringToFile(output.toFile(), suite.toXml(), StandardCharsets.UTF_8);
        LOG.info("Wrote {} ({} tests, thread-count={})", output.toAbsolutePath(), suite.getTests().size(), suite.getThreadCount());
        System.out.println("Wrote " + output.toAbsolutePath());
    }

    public static XmlSuite generate() {
        ParallelMode mode = ParallelMode.from(System.getProperty("parallelMode", "cross_browser"));
        Set<String> groups = split(System.getProperty("groups", "smoke"));
        List<String> browsers = splitList(System.getProperty("browsers", "chrome,firefox,edge"));
        List<String> devices = splitList(System.getProperty("devices", "Pixel_7,iPhone_14"));
        String testPackage = System.getProperty("testPackage", "com.qa.examples");
        int requestedThreads = Integer.getInteger("threadCount", FrameworkConfig.apiThreadCount());

        List<TestMethodScanner.ScannedMethod> methods = TestMethodScanner.scan(testPackage, groups);
        if (methods.isEmpty()) {
            LOG.warn("No @Test methods found in package {} for groups {}", testPackage, groups);
        }

        XmlSuite suite = new XmlSuite();
        suite.setName("RuntimeSuite-" + mode.name().toLowerCase());
        suite.setListeners(List.of(
                "com.qa.framework.core.listener.FrameworkTestListener",
                "com.qa.framework.core.retry.RetryAnnotationTransformer"
        ));

        switch (mode) {
            case CROSS_BROWSER -> crossBrowser(suite, methods, browsers, groups, requestedThreads);
            case RANDOM_BROWSER -> randomBuckets(suite, methods, browsers, "browser", requestedThreads);
            case CROSS_DEVICE -> crossDevice(suite, methods, devices, groups, requestedThreads);
            case RANDOM_DEVICE -> randomBuckets(suite, methods, devices, "device", requestedThreads);
            case METHODS -> single(suite, methods, groups, XmlSuite.ParallelMode.METHODS, requestedThreads);
            case CLASSES -> single(suite, methods, groups, XmlSuite.ParallelMode.CLASSES, requestedThreads);
            case API_PARALLEL -> apiParallel(suite, methods, groups, requestedThreads);
        }
        return suite;
    }

    private static void crossBrowser(XmlSuite suite, List<TestMethodScanner.ScannedMethod> methods,
                                     List<String> browsers, Set<String> groups, int threads) {
        suite.setParallel(XmlSuite.ParallelMode.TESTS);
        suite.setThreadCount(Math.max(1, Math.min(threads, browsers.size())));
        for (String browser : browsers) {
            XmlTest test = new XmlTest(suite);
            test.setName("Test on " + browser);
            test.addParameter("browser", browser);
            test.setIncludedGroups(new ArrayList<>(groups));
            test.setXmlClasses(toClasses(methods));
        }
    }

    private static void crossDevice(XmlSuite suite, List<TestMethodScanner.ScannedMethod> methods,
                                    List<String> devices, Set<String> groups, int threads) {
        suite.setParallel(XmlSuite.ParallelMode.TESTS);
        suite.setThreadCount(Math.max(1, Math.min(threads, devices.size())));
        for (String device : devices) {
            XmlTest test = new XmlTest(suite);
            test.setName("Test on " + device);
            test.addParameter("device", device);
            test.setIncludedGroups(new ArrayList<>(groups));
            test.setXmlClasses(toClasses(methods));
        }
    }

    private static void randomBuckets(XmlSuite suite, List<TestMethodScanner.ScannedMethod> methods,
                                      List<String> buckets, String parameterName, int threads) {
        suite.setParallel(XmlSuite.ParallelMode.TESTS);
        suite.setThreadCount(Math.max(1, Math.min(threads, buckets.size())));
        List<TestMethodScanner.ScannedMethod> shuffled = new ArrayList<>(methods);
        Collections.shuffle(shuffled);
        List<List<TestMethodScanner.ScannedMethod>> assigned = new ArrayList<>();
        for (int i = 0; i < buckets.size(); i++) {
            assigned.add(new ArrayList<>());
        }
        for (int i = 0; i < shuffled.size(); i++) {
            assigned.get(i % buckets.size()).add(shuffled.get(i));
        }
        for (int i = 0; i < buckets.size(); i++) {
            XmlTest test = new XmlTest(suite);
            test.setName(parameterName + "-" + buckets.get(i) + "-bucket");
            test.addParameter(parameterName, buckets.get(i));
            test.setXmlClasses(toClassesWithMethods(assigned.get(i)));
        }
    }

    private static void single(XmlSuite suite, List<TestMethodScanner.ScannedMethod> methods, Set<String> groups,
                               XmlSuite.ParallelMode parallel, int threads) {
        suite.setParallel(parallel);
        suite.setThreadCount(Math.max(1, threads));
        XmlTest test = new XmlTest(suite);
        test.setName("Parallel " + parallel.name().toLowerCase());
        test.setIncludedGroups(new ArrayList<>(groups));
        test.setParallel(parallel);
        test.setXmlClasses(toClasses(methods));
    }

    private static void apiParallel(XmlSuite suite, List<TestMethodScanner.ScannedMethod> methods,
                                    Set<String> groups, int requestedThreads) {
        int methodCount = Math.max(1, methods.size());
        int threads = Math.max(1, Math.min(requestedThreads, methodCount));
        suite.setParallel(XmlSuite.ParallelMode.METHODS);
        suite.setThreadCount(threads);
        suite.setDataProviderThreadCount(threads);
        XmlTest test = new XmlTest(suite);
        test.setName("API parallel");
        test.setParallel(XmlSuite.ParallelMode.METHODS);
        test.setThreadCount(threads);
        test.setIncludedGroups(new ArrayList<>(groups));
        test.setXmlClasses(toClasses(methods));
        LOG.info("api_parallel methods={} threads={} slaMinutes={}", methodCount, threads, FrameworkConfig.apiMaxDurationMinutes());
    }

    private static List<XmlClass> toClasses(List<TestMethodScanner.ScannedMethod> methods) {
        LinkedHashMap<String, XmlClass> classes = new LinkedHashMap<>();
        for (TestMethodScanner.ScannedMethod method : methods) {
            classes.computeIfAbsent(method.className(), XmlClass::new);
        }
        return new ArrayList<>(classes.values());
    }

    private static List<XmlClass> toClassesWithMethods(List<TestMethodScanner.ScannedMethod> methods) {
        Map<String, XmlClass> classes = new LinkedHashMap<>();
        for (TestMethodScanner.ScannedMethod method : methods) {
            XmlClass xmlClass = classes.computeIfAbsent(method.className(), XmlClass::new);
            xmlClass.getIncludedMethods().add(new XmlInclude(method.methodName()));
        }
        return new ArrayList<>(classes.values());
    }

    private static Set<String> split(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    private static List<String> splitList(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
