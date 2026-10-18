package com.qa.framework.perf.jmx;

import java.net.URI;

record BaseUrlParts(String protocol, String domain, String port) {

    static BaseUrlParts parse(String baseUrl) {
        URI uri = URI.create(baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl);
        String protocol = uri.getScheme() == null ? "https" : uri.getScheme();
        String host = uri.getHost();
        if (host == null) {
            throw new IllegalArgumentException("Invalid baseUrl: " + baseUrl);
        }
        int port = uri.getPort();
        String portStr = port < 0 ? "" : String.valueOf(port);
        return new BaseUrlParts(protocol, host, portStr);
    }
}
