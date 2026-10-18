package com.qa.framework.perf.cli;

import com.qa.framework.perf.model.LoadProfile;

final class CliOptions {

    String journeyPath = "";
    String recordedYaml = "";
    String testPackage = "";
    String testClasspath = "";
    String groups = "api";
    String baseUrl = "";
    String outputPath = "target/load-test.jmx";
    String jtlPath;
    boolean regenerate;
    double tps = 10;
    int users = 50;
    int duration = 300;
    int rampUp = 30;
    int latencyMs = 500;

    static CliOptions parse(String[] args, int startIndex, boolean journeyRequired) {
        CliOptions o = new CliOptions();
        for (int i = startIndex; i < args.length; i++) {
            String flag = args[i];
            switch (flag) {
                case "--journey" -> o.journeyPath = requireValue(args, ++i, flag);
                case "--recorded" -> o.recordedYaml = requireValue(args, ++i, flag);
                case "--testPackage" -> o.testPackage = requireValue(args, ++i, flag);
                case "--testClasspath" -> o.testClasspath = requireValue(args, ++i, flag);
                case "--groups" -> o.groups = requireValue(args, ++i, flag);
                case "--baseUrl" -> o.baseUrl = requireValue(args, ++i, flag);
                case "--out" -> o.outputPath = requireValue(args, ++i, flag);
                case "--jtl" -> o.jtlPath = requireValue(args, ++i, flag);
                case "--tps" -> o.tps = Double.parseDouble(requireValue(args, ++i, flag));
                case "--users" -> o.users = Integer.parseInt(requireValue(args, ++i, flag));
                case "--duration" -> o.duration = Integer.parseInt(requireValue(args, ++i, flag));
                case "--rampUp" -> o.rampUp = Integer.parseInt(requireValue(args, ++i, flag));
                case "--latencyMs" -> o.latencyMs = Integer.parseInt(requireValue(args, ++i, flag));
                case "--regenerate" -> o.regenerate = true;
                default -> throw new IllegalArgumentException("Unknown option: " + flag);
            }
        }
        if (journeyRequired && o.journeyPath.isBlank()) {
            throw new IllegalArgumentException("--journey is required");
        }
        if (o.tps <= 0) {
            throw new IllegalArgumentException("--tps must be > 0");
        }
        if (o.users <= 0) {
            throw new IllegalArgumentException("--users must be > 0");
        }
        return o;
    }

    LoadProfile toProfile() {
        return new LoadProfile(tps, users, duration, rampUp, latencyMs);
    }

    private static String requireValue(String[] args, int index, String flag) {
        if (index >= args.length) {
            throw new IllegalArgumentException("Missing value for " + flag);
        }
        return args[index];
    }
}
