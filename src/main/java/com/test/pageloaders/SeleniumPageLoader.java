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

    private JBrowserDriver browserDriver;

    public SeleniumPageLoader() {
        // disable SSL checking (set to "compatible" for Firefox SSL certs)
        Properties props = System.getProperties();
        props.setProperty("jbd.pemfile", "trustanything");

        browserDriver = new JBrowserDriver(Settings.builder().
                timezone(Timezone.AMERICA_NEWYORK).build());
    }

    public void cleanup() {
        // Close the browser. Allows the Selenium thread to terminate.
        browserDriver.quit();
    }

    public String loadPage(String url) {
        // this can throw a lot of different exceptions

        // This will block for the page load and any
        // associated AJAX requests
        browserDriver.get(url);

        // ignore pages w/ errors
        if (browserDriver.getStatusCode() != HttpStatus.SC_OK) {
            return "";
        }

        // Returns the page source in its current state, including
        // any DOM updates that occurred after page load
        try {
            String result = browserDriver.getPageSource();

            return result;
        } catch (Exception e) {
            // we can get a java.net.SocketException if the URL cannot be connected to
            logger.info("Error getting page source: ", e);
            return "";
        }
    }
}
