package com.test.scrapers;

/**
 * Interface for page loader status updates
 */
public interface StatusListener {
    void loadedUrl(String url);
    void skippedUrl(String url);
    void invalidUrl(String url);
    void foundEmail(String email);
}
