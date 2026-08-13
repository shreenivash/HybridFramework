package com.qa.framework.core.listener;

import java.util.Optional;

/**
 * Optional evidence source registered by web/mobile/api modules on the current thread.
 */
public interface EvidenceCapture {

    default Optional<byte[]> screenshot() {
        return Optional.empty();
    }

    default Optional<String> pageSource() {
        return Optional.empty();
    }

    default Optional<String> lastRequest() {
        return Optional.empty();
    }

    default Optional<String> lastResponse() {
        return Optional.empty();
    }
}
