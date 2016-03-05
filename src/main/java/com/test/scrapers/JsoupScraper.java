package com.test.scrapers;

import com.test.pageloaders.PageLoader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Web scraper that uses JSoup (http://jsoup.org) and jBrowserdriver
 */
public class JsoupScraper extends BaseScraper implements Scraper {

    private static final Logger logger = LoggerFactory.getLogger(JsoupScraper.class);

    private PageLoader pageLoader;

    public JsoupScraper(PageLoader pageLoader, StatusListener statusListener) {
        super(statusListener);

        this.pageLoader = pageLoader;
    }

    @Override
    protected void init(String url) {
        super.init(url);
    }

    public void cleanup() {
    }

    public void scrapeEmailsFromPage(URI rootUri, String path) throws Exception {
        String url = getUrlToFollow(rootUri, path);
        if (url == null) {
            return;
        }

        Document doc = Jsoup.parse(pageLoader.loadPage(url));

        extractEmailsFromText(doc.text());

        statusListener.loadedUrl(url);

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
}
