package com.qa.framework.engine;

import java.util.Arrays;
import java.util.Locale;

public enum ParallelMode {
    CROSS_BROWSER,
    RANDOM_BROWSER,
    METHODS,
    CLASSES,
    CROSS_DEVICE,
    RANDOM_DEVICE,
    API_PARALLEL;

    public static ParallelMode from(String value) {
        if (value == null || value.isBlank()) {
            return CROSS_BROWSER;
        }
        return ParallelMode.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    public static String csv() {
        return Arrays.stream(values())
                .map(mode -> mode.name().toLowerCase(Locale.ROOT))
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }
}
