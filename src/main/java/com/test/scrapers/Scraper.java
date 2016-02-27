package com.test.scrapers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * Interface for
 */
public interface Scraper {
    Set<String> getEmails(String url) throws Exception;
}
