package com.qa.framework.web.util;

import com.qa.framework.web.driver.BrowserFactory;

import java.nio.file.Path;

public final class DownloadUtils {

    private DownloadUtils() {
    }

    public static Path directory() {
        return BrowserFactory.downloadDir();
    }

    public static Path resolve(String fileName) {
        return directory().resolve(fileName);
    }
}
