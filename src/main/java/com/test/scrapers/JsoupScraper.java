package com.test.scrapers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.machinepublishers.jbrowserdriver.Timezone;
import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.Settings;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Web scraper that uses JSoup (http://jsoup.org)
 */
public class JsoupScraper implements Scraper {

    private static final Logger logger = LoggerFactory.getLogger(JsoupScraper.class);

    private final Set<String> visitedUrls = new HashSet<>();
    private final Set<String> emails = new HashSet<>();
    private final Pattern emailRegEx = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");

    private String baseDomain;

    private final boolean useWebClient;
    private JBrowserDriver browserDriver;

    public JsoupScraper(boolean useWebClient) {
        this.useWebClient = useWebClient;
    }

    public Set<String> getEmails(String url) throws Exception {
        if (!url.contains("://")) {
            url = "http://" + url;
        }
        URI rootUri = null;
        try {
            rootUri = new URI(url);
        } catch (URISyntaxException e) {
            System.err.println("Invalid URL: " + url);
            System.exit(-1);
        }

        // calculate root domain...we only want the last two sections of the hostname
        String[] levels = rootUri.getHost().split("\\.");
        if (levels.length > 1)
        {
            baseDomain = levels[levels.length - 2] + "." + levels[levels.length - 1];
        }

        try {
            if (useWebClient) {
                browserDriver = new JBrowserDriver(Settings.builder().
                        timezone(Timezone.AMERICA_NEWYORK).build());
            }

            scrapeEmailsFromPage(rootUri, url);
        } catch (Exception ex) {
            if (emails.size() == 0) {
                throw ex;
            } else {
                // just log a debug warning because we got some useful results back
                logger.warn("Could not finish scraping site: " + ex);
            }
        } finally {
            if (useWebClient) {
                // Close the browser. Allows the Selenium thread to terminate.
                browserDriver.quit();
            }
        }

        return emails;
    }

    private void scrapeEmailsFromPage(URI rootUri, String path) throws Exception {
        if (!shouldFollowPath(rootUri, path)) {
            return;
        }

        String url = getUrlToFollow(rootUri, path);
        if (url == null) {
            return;
        }

        System.out.println("Scraping " + url);
        logger.debug("Scraping " + url);

        Document doc = useWebClient ?
                Jsoup.parse(loadPageSelenium(url))
                : Jsoup.connect(url).get();

        // extract emails
        Matcher matcher = emailRegEx.matcher(doc.text());
        while (matcher.find()) {
            String email = matcher.group();
            emails.add(email);

            logger.info("Found email: " + email);
            System.out.print("+");  // progress indicator
        }

        // now walk all the links on the page recursively
        Elements elements = doc.select("a[href]");
        for (Element e : elements) {
            String childUrl = e.attr("href");
            if (childUrl != null) {
                try {
                    scrapeEmailsFromPage(rootUri, childUrl);
                } catch (IOException | URISyntaxException ex) {
                    // just log exceptions
                    logger.warn("Error scraping '" + childUrl + "': " + ex);
                }
            }
        }
    }

    private String getUrlToFollow(URI rootUri, String path) throws URISyntaxException {
        String url;
        if (path.contains("://")) {
            url = path;
        } else {
            url = rootUri.toString();
            if (!path.startsWith("/")) {
                url += "/";
            }
            url += path;
        }

        // strips off protocol header
        try {
            URI uri = new URI(url.trim());
            String localPath = uri.getSchemeSpecificPart() + uri.getRawPath();
            if ((localPath.length() > 0) && visitedUrls.contains(localPath)) {
                // prevent circular loops
                return null;
            } else {
                visitedUrls.add(localPath);
            }
        } catch (URISyntaxException e) {
            System.err.println("Ignoring bad URL: " + path);
            return null;
        }

        return url;
    }

    private boolean shouldFollowPath(URI rootUri, String path) throws URISyntaxException {
        if (path.startsWith("javascript:") || path.startsWith("#")) {
            return false;
        }
        if (path.startsWith("mailto:")) {
            String email = path.substring("mailto".length()+1);
            emails.add(email);
            return false;
        }

        // don't follow links off site
        try {
            // get base domain of root URI
            // NOTE: this only works w/ real root domains.  If you have a site on a shared
            // hosting service like mysite.squarespace.com, this check will let us walk any
            // domains on the site :-(
            URI pathUri = new URI(path.trim());
            if ((pathUri.getHost() != null)
                    && !pathUri.getHost().endsWith(baseDomain)) {
                return false;
            }
        } catch (URISyntaxException e) {
            System.err.println("Ignoring bad URL: " + path);
            return false;
        }
        return true;
    }

    private String loadPageSelenium(String url) {
        // this can throw a lot of different exceptions

        // disable SSL checking (set to "compatible" for Firefox SSL certs)
        Properties props = System.getProperties();
        props.setProperty("jbd.pemfile", "trustanything");

        // This will block for the page load and any
        // associated AJAX requests
        browserDriver.get(url);

        // ignore pages w/ errors
        if (browserDriver.getStatusCode() != 200) {
            return "";
        }

        // Returns the page source in its current state, including
        // any DOM updates that occurred after page load
        String result = browserDriver.getPageSource();

        return result;
    }
}
