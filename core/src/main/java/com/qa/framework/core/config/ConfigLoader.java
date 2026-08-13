package com.qa.framework.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads framework configuration with precedence:
 * JVM {@code -D} properties &gt; environment variables &gt; {@code config-{env}.*} &gt; {@code config.properties} / {@code config.yaml}.
 * Environment variable {@code GRID_URL} maps to key {@code grid.url}.
 */
public final class ConfigLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigLoader.class);
    private static final ConfigLoader INSTANCE = new ConfigLoader();

    private final ConcurrentHashMap<String, String> values = new ConcurrentHashMap<>();

    private ConfigLoader() {
        reload();
    }

    public static ConfigLoader getInstance() {
        return INSTANCE;
    }

    public void reload() {
        values.clear();
        loadClasspathProperties("config.properties");
        loadClasspathYaml("config.yaml");

        String env = firstNonBlank(
                System.getProperty("env"),
                System.getenv("ENV"),
                values.get("env"),
                "qa");
        values.put("env", env);
        loadClasspathProperties("config-" + env + ".properties");
        loadClasspathYaml("config-" + env + ".yaml");

        overlayEnvironmentVariables();
        overlaySystemProperties();
        LOG.info("Configuration loaded for env={}", env);
    }

    public String get(String key) {
        return values.get(key);
    }

    public String get(String key, String defaultValue) {
        String value = values.get(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    public int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            LOG.warn("Invalid integer for {}: {}", key, value);
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }

    public Map<String, String> snapshot() {
        return Map.copyOf(values);
    }

    private void loadClasspathProperties(String resource) {
        try (InputStream in = threadClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                return;
            }
            Properties props = new Properties();
            props.load(in);
            props.forEach((k, v) -> {
                if (v != null && !v.toString().isBlank()) {
                    values.put(k.toString(), v.toString().trim());
                }
            });
            LOG.debug("Loaded {}", resource);
        } catch (IOException e) {
            LOG.warn("Unable to load {}: {}", resource, e.getMessage());
        }
    }

    private void loadClasspathYaml(String resource) {
        try (InputStream in = threadClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                return;
            }
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper(
                            new com.fasterxml.jackson.dataformat.yaml.YAMLFactory());
            @SuppressWarnings("unchecked")
            Map<String, Object> map = mapper.readValue(in, Map.class);
            flatten("", map);
            LOG.debug("Loaded {}", resource);
        } catch (IOException e) {
            LOG.warn("Unable to load {}: {}", resource, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void flatten(String prefix, Map<String, Object> map) {
        if (map == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> nested) {
                flatten(key, (Map<String, Object>) nested);
            } else if (value != null) {
                values.put(key, String.valueOf(value).trim());
            }
        }
    }

    private void overlayEnvironmentVariables() {
        System.getenv().forEach((name, value) -> {
            if (value == null || value.isBlank()) {
                return;
            }
            String key = name.toLowerCase(Locale.ROOT).replace('_', '.');
            values.put(key, value.trim());
        });
    }

    private void overlaySystemProperties() {
        System.getProperties().forEach((k, v) -> {
            if (v != null && !v.toString().isBlank()) {
                values.put(k.toString(), v.toString().trim());
            }
        });
    }

    private static ClassLoader threadClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return cl != null ? cl : ConfigLoader.class.getClassLoader();
    }

    private static String firstNonBlank(String... candidates) {
        if (candidates == null) {
            return null;
        }
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate.trim();
            }
        }
        return null;
    }
}
