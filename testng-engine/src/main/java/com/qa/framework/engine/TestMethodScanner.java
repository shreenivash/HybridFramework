package com.qa.framework.engine;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class TestMethodScanner {

    public record ScannedMethod(String className, String methodName, Set<String> groups) {
    }

    private TestMethodScanner() {
    }

    public static List<ScannedMethod> scan(String testPackage, Set<String> requiredGroups) {
        if (testPackage == null || testPackage.isBlank()) {
            throw new IllegalArgumentException("-DtestPackage is required (e.g. com.qa.examples)");
        }
        List<ScannedMethod> methods = new ArrayList<>();
        try (ScanResult result = new ClassGraph()
                .enableClassInfo()
                .enableMethodInfo()
                .enableAnnotationInfo()
                .acceptPackages(testPackage)
                .scan()) {
            for (ClassInfo classInfo : result.getClassesWithMethodAnnotation(Test.class.getName())) {
                if (classInfo.isAbstract() || classInfo.isInterface()) {
                    continue;
                }
                Class<?> clazz = classInfo.loadClass();
                for (Method method : clazz.getDeclaredMethods()) {
                    if (!Modifier.isPublic(method.getModifiers())) {
                        continue;
                    }
                    Test test = method.getAnnotation(Test.class);
                    if (test == null || test.enabled() == false) {
                        continue;
                    }
                    Set<String> groups = new LinkedHashSet<>(Arrays.asList(test.groups()));
                    if (!matches(groups, requiredGroups)) {
                        continue;
                    }
                    methods.add(new ScannedMethod(clazz.getName(), method.getName(), groups));
                }
            }
        }
        return methods;
    }

    private static boolean matches(Set<String> methodGroups, Set<String> required) {
        if (required == null || required.isEmpty() || required.contains("*")) {
            return true;
        }
        if (methodGroups.isEmpty()) {
            return false;
        }
        for (String group : required) {
            if (methodGroups.contains(group)) {
                return true;
            }
        }
        return false;
    }
}
