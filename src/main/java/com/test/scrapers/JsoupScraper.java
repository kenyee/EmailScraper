package com.test.scrapers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.BrowserVersionFeatures;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.WebClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
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
    private final boolean useWebClient;
    private WebClient webClient;
    private final BrowserVersion browserVersion = BrowserVersion.CHROME;

    public JsoupScraper(boolean useWebClient) {
        this.useWebClient = useWebClient;

        if (useWebClient){
            String applicationName = browserVersion.getApplicationName();
            String applicationVersion = browserVersion.getApplicationVersion();
            String userAgent = browserVersion.getUserAgent();
            float browserVersionNumeric = browserVersion.getBrowserVersionNumeric();

            BrowserVersion browser = new BrowserVersion(applicationName, applicationVersion, userAgent, browserVersionNumeric) {
                public boolean hasFeature(BrowserVersionFeatures property) {

                    // change features here
                    return browserVersion.hasFeature(property);
                }
            };

            webClient = new WebClient(browser);
        }
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

        try {
            scrapeEmailsFromPage(rootUri, url);
        } catch (Exception ex) {
            if (emails.size() == 0) {
                throw ex;
            } else {
                // just log a debug warning because we got some useful results back
                logger.warn("Could not finish scraping site: " + ex);
            }
        }

        return emails;
    }

    private void scrapeEmailsFromPage(URI rootUri, String path) throws Exception {
        if (path.startsWith("javascript:") || path.startsWith("#")) {
            return;
        }
        if (path.startsWith("mailto:")) {
            String email = path.substring("mailto".length()+1);
            emails.add(email);
            return;
        }

        String url;
        if (path.contains("://")) {
            URI pathUri = new URI(path);
            if (!pathUri.getHost().contains(rootUri.getHost())) {
                // don't follow links off site
                return;
            }
            url = path;
        } else {
            url = rootUri.toString();
            if (!path.startsWith("/")) {
                url += "/";
            }
            url += path;
        }

        // strips off protocol header
        URI uri = new URI(url);
        String localPath = uri.getSchemeSpecificPart() + uri.getRawPath();
        if ((localPath.length() > 0) && visitedUrls.contains(localPath)) {
            // prevent circular loops
            return;
        } else {
            visitedUrls.add(localPath);
        }
        logger.debug("Scraping " + url);
        System.out.print(".");  // progress indicator

        Document doc = useWebClient ?
                Jsoup.parse(loadPage(url))
                : Jsoup.connect(url).get();

        logger.debug("Document is " + doc.outerHtml());

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

    private String loadPage(String url) throws Exception {
        // the WebClient throws a lot of different exceptions
        HtmlPage page = webClient.getPage(url);
        logger.debug("***** XML is : " + page.asXml());
        return page.asXml();
    }
}
