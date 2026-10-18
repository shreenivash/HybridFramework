package com.qa.framework.api.load;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;

import java.nio.file.Path;

/**
 * Writes a recorded journey YAML when {@code -Dapi.load.record=true} and {@code -Dapi.load.record.file=...}.
 */
public class LoadRecordingListener implements ISuiteListener {

    private static final Logger LOG = LoggerFactory.getLogger(LoadRecordingListener.class);

    @Override
    public void onStart(ISuite suite) {
        if (!LoadRecordingContext.isEnabled()) {
            return;
        }
        LoadRecordingContext.clear();
        LoadRecordingContext.configure(
                System.getProperty("api.load.record.name", suite.getName()),
                System.getProperty("api.baseUri", ""));
        LOG.info("API load recording enabled for suite {}", suite.getName());
    }

    @Override
    public void onFinish(ISuite suite) {
        if (!LoadRecordingContext.isEnabled()) {
            return;
        }
        String file = System.getProperty("api.load.record.file", "target/recorded-api-journey.yaml");
        try {
            LoadRecordingContext.writeYaml(Path.of(file));
            LOG.info("Recorded {} HTTP calls to {}", LoadRecordingContext.recordedCount(), file);
        } catch (Exception e) {
            LOG.error("Failed to write recorded journey: {}", e.getMessage());
        }
    }
}
