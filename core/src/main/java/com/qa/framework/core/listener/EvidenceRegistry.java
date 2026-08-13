package com.qa.framework.core.listener;

/**
 * Thread-local registry so listeners can capture screenshots and API payloads without depending on Selenium or REST Assured.
 */
public final class EvidenceRegistry {

    private static final ThreadLocal<EvidenceCapture> CAPTURE = new ThreadLocal<>();

    private EvidenceRegistry() {
    }

    public static void register(EvidenceCapture capture) {
        CAPTURE.set(capture);
    }

    public static void clear() {
        CAPTURE.remove();
    }

    public static EvidenceCapture current() {
        EvidenceCapture capture = CAPTURE.get();
        return capture != null ? capture : new EvidenceCapture() {
        };
    }
}
