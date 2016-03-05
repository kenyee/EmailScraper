package com.test.scrapers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * Interface for
 */
public interface Scraper {
    Set<String> getEmails(String url) throws Exception;

    void cleanup();

    void scrapeEmailsFromPage(URI rootUri, String path) throws Exception;
}
