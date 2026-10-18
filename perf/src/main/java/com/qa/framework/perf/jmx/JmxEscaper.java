package com.qa.framework.perf.jmx;

final class JmxEscaper {

    private JmxEscaper() {
    }

    static String xml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
