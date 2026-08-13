package com.qa.framework.api.config;

import com.qa.framework.core.config.FrameworkConfig;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.mapper.ObjectMapperType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Initializes REST Assured once per JVM. Each request uses Rest Assured's default client
 * so TestNG method-level parallelism does not share a non-thread-safe connection manager.
 */
public final class RestAssuredManager {

    private static final Logger LOG = LoggerFactory.getLogger(RestAssuredManager.class);
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    private RestAssuredManager() {
    }

    public static void initialize() {
        if (!INITIALIZED.compareAndSet(false, true)) {
            return;
        }
        RestAssured.config = RestAssuredConfig.config()
                .objectMapperConfig(ObjectMapperConfig.objectMapperConfig().defaultObjectMapperType(ObjectMapperType.JACKSON_2));

        String baseUri = FrameworkConfig.apiBaseUri();
        if (baseUri != null && !baseUri.isBlank()) {
            RestAssured.baseURI = baseUri;
        }
        int port = FrameworkConfig.apiPort();
        if (port > 0) {
            RestAssured.port = port;
        }
        String basePath = FrameworkConfig.apiBasePath();
        if (basePath != null && !basePath.isBlank()) {
            RestAssured.basePath = basePath;
        }
        LOG.info("REST Assured initialized baseUri={} poolSize={} slaMinutes={}",
                RestAssured.baseURI, FrameworkConfig.apiThreadCount(), FrameworkConfig.apiMaxDurationMinutes());
    }
}
