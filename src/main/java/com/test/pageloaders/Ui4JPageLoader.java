package com.test.pageloaders;

import com.ui4j.api.browser.BrowserEngine;
import com.ui4j.api.browser.BrowserFactory;
import com.ui4j.api.browser.Page;

/**
 * Loads a page using the Ui4j webkit engine
 */
public class Ui4JPageLoader implements PageLoader {
    private BrowserEngine browser;

    public Ui4JPageLoader() {
        System.setProperty("ui4j.headless", "true");

        browser = BrowserFactory.getWebKit();
    }

    public void cleanup() {
        browser.shutdown();
    }

    public String loadPage(String url) {
        try (Page page = browser.navigate(url)) {
            String html = (String) page.executeScript("document.documentElement.innerHTML");
            return html;
        }
    }
}
