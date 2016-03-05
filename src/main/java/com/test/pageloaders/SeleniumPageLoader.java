package com.test.pageloaders;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.Settings;
import com.machinepublishers.jbrowserdriver.Timezone;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Uses Selenium to download an AJAX/SPA web page
 */
public class SeleniumPageLoader implements PageLoader {

    private static final Logger logger = LoggerFactory.getLogger(SeleniumPageLoader.class);

    private ThreadLocal<JBrowserDriver> browserDriver = new ThreadLocal<JBrowserDriver>() {
        @Override
        protected JBrowserDriver initialValue() {
            try {
                JBrowserDriver instance = new JBrowserDriver(Settings.builder()
                        .timezone(Timezone.AMERICA_NEWYORK)
                        .build());
                return instance;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    };

    public SeleniumPageLoader() {
        // disable SSL checking (set to "compatible" for Firefox SSL certs)
        Properties props = System.getProperties();
        props.setProperty("jbd.pemfile", "trustanything");
    }

    public void cleanup() {
        // no need to call browserDriver.quit() because garbage collection will clean it up
    }

    public String loadPage(String url) {
        // this can throw a lot of different exceptions
        JBrowserDriver driver = browserDriver.get();

        // This will block for the page load and any
        // associated AJAX requests
        driver.get(url);

        // ignore pages w/ errors
        if (driver.getStatusCode() != HttpStatus.SC_OK) {
            return "";
        }

        // Returns the page source in its current state, including
        // any DOM updates that occurred after page load
        try {
            String result = driver.getPageSource();

            return result;
        } catch (Exception e) {
            // we can get a java.net.SocketException if the URL cannot be connected to
            logger.info("Error getting page source: ", e);
            return "";
        }
    }
}
