package com.test.pageloaders;

import java.io.IOException;

/**
 * Web page downloader
 */
public interface PageLoader {
    String loadPage(String urlpath) throws IOException;
    void cleanup();
}
