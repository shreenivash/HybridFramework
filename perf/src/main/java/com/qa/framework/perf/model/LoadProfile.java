package com.qa.framework.perf.model;

/**
 * Load knobs passed to JMeter as {@code -J} properties and embedded in the JMX via JEXL.
 */
public record LoadProfile(double tps, int users, int durationSeconds, int rampUpSeconds, int assumedLatencyMs) {

    public static LoadProfile defaults() {
        return new LoadProfile(10, 50, 300, 30, 500);
    }

    public double throughputPerMinute() {
        return tps * 60.0;
    }

    public int minUsersForTps() {
        double latencySeconds = Math.max(0.001, assumedLatencyMs / 1000.0);
        return (int) Math.ceil(tps * latencySeconds);
    }
}
