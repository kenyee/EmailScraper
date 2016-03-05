package com.test.scrapers;

/**
 * Prints out status to stdout
 */
public class StdoutStatusDumper implements StatusListener {
    @Override
    public void loadedUrl(String url) {
        System.out.println("Scraped " + url);
    }

    @Override
    public void skippedUrl(String url) {
        //System.out.println("Skipped " + url);
    }

    @Override
    public void invalidUrl(String url) {
        //System.out.println("Skipped invalid URL " + url);
    }

    @Override
    public void foundEmail(String email) {
        System.out.println("Found " + email);
    }
}
