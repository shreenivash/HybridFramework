package com.qa.framework.perf.run;

import com.qa.framework.perf.model.LoadProfile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class JmeterRunner {

    private JmeterRunner() {
    }

    public static int run(Path jmxFile, LoadProfile profile, Path resultJtl) throws IOException, InterruptedException {
        String jmeter = System.getenv().getOrDefault("JMETER_HOME", "");
        String command = jmeter.isBlank() ? "jmeter" : Path.of(jmeter, "bin", "jmeter").toString();

        List<String> cmd = new ArrayList<>();
        cmd.add(command);
        cmd.add("-n");
        cmd.add("-t");
        cmd.add(jmxFile.toAbsolutePath().toString());
        cmd.add("-Jtps=" + profile.tps());
        cmd.add("-Jusers=" + profile.users());
        cmd.add("-Jduration=" + profile.durationSeconds());
        cmd.add("-JrampUp=" + profile.rampUpSeconds());
        if (resultJtl != null) {
            cmd.add("-l");
            cmd.add(resultJtl.toAbsolutePath().toString());
        }

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.inheritIO();
        Process process = pb.start();
        return process.waitFor();
    }
}
