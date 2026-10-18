package com.qa.framework.perf.plan;

import com.qa.framework.perf.model.ApiJourney;
import com.qa.framework.perf.model.LoadProfile;

public final class LoadPlanExplainer {

    private LoadPlanExplainer() {
    }

    public static String explain(ApiJourney journey, LoadProfile profile) {
        int minUsers = profile.minUsersForTps();
        boolean userWarning = profile.users() < minUsers;

        StringBuilder sb = new StringBuilder();
        sb.append("=== Hybrid Framework load plan ===\n");
        sb.append("Journey: ").append(journey.getName()).append("\n");
        sb.append("Base URL: ").append(journey.getBaseUrl()).append("\n");
        sb.append("Steps: ").append(journey.getSteps().size()).append(" (end-to-end per iteration)\n\n");

        sb.append("--- Target load (fixed TPS) ---\n");
        sb.append("TPS: ").append(profile.tps()).append("\n");
        sb.append("Users (threads): ").append(profile.users()).append("\n");
        sb.append("Duration: ").append(profile.durationSeconds()).append(" s\n");
        sb.append("Ramp-up: ").append(profile.rampUpSeconds()).append(" s\n");
        sb.append("Assumed latency (planning): ").append(profile.assumedLatencyMs()).append(" ms\n\n");

        sb.append("--- JMeter Constant Throughput Timer ---\n");
        sb.append("Mode: all active threads in JVM (calcMode=2)\n");
        sb.append("Throughput: ").append(profile.throughputPerMinute()).append(" samples/min ");
        sb.append("(TPS × 60, honored via JEXL)\n");
        sb.append("JEXL in JMX: ${__jexl3(${__P(tps,").append((int) profile.tps()).append(")} * 60)}\n");
        sb.append("Extra users add wait time; they do NOT increase TPS above the timer target.\n\n");

        sb.append("--- JMeter command-line properties ---\n");
        sb.append("-Jtps=").append(profile.tps()).append("\n");
        sb.append("-Jusers=").append(profile.users()).append("\n");
        sb.append("-Jduration=").append(profile.durationSeconds()).append("\n");
        sb.append("-JrampUp=").append(profile.rampUpSeconds()).append("\n\n");

        sb.append("--- Capacity hint ---\n");
        sb.append("Min recommended users ≈ ceil(TPS × latencySeconds) = ").append(minUsers).append("\n");
        if (userWarning) {
            sb.append("WARNING: users=").append(profile.users())
                    .append(" may be too low to sustain ").append(profile.tps())
                    .append(" TPS at ").append(profile.assumedLatencyMs()).append(" ms latency.\n");
        } else {
            sb.append("Users look sufficient for the assumed latency.\n");
        }
        return sb.toString();
    }
}
