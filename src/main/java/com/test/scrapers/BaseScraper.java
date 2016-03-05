package com.test.scrapers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared scraping code
 */
public abstract class BaseScraper {

    private static final Logger logger = LoggerFactory.getLogger(BaseScraper.class);

    private static String excludeExtensions[] = {
        ".mpg", ".mp4", ".avi", ".pdf", ".gif", ".png", ".jpg", ".css", ".js"
    };

    protected final Set<String> visitedUrls = new ConcurrentSkipListSet<>();
    protected final Set<String> emails = new ConcurrentSkipListSet <>();
    protected final Pattern emailRegEx = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");

    protected String baseDomain;

    protected abstract void scrapeEmailsFromPage(URI rootUri, String path) throws Exception;

    protected StatusListener statusListener;

    private final ThreadPoolExecutor threadExecutor;
    private final int maxThreads;

    public BaseScraper(StatusListener statusListener, int maxThreads) {
        this.statusListener = statusListener;
        this.maxThreads = maxThreads;
        threadExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxThreads);
    }

    protected void cleanup() {
        threadExecutor.shutdown();
    }

    protected void init(String url) {
        if (!url.contains("://")) {
            url = "http://" + url;
        }
        URI rootUri = null;
        try {
            rootUri = new URI(url);
        } catch (URISyntaxException e) {
            statusListener.invalidUrl(url);
            System.exit(-1);
        }

        // calculate root domain...we only want the last two sections of the hostname
        String[] levels = rootUri.getHost().split("\\.");
        if (levels.length > 1)
        {
            baseDomain = levels[levels.length - 2] + "." + levels[levels.length - 1];
        } else {
            baseDomain = rootUri.toString();
        }
    }

    public Set<String> getEmails(String url) throws Exception {
        init(url);

        if (!url.contains("://")) {
            url = "http://" + url;
        }
        URI rootUri = null;
        try {
            rootUri = new URI(url);

            addScrapeJob(rootUri, url);
        } catch (URISyntaxException e) {
            logger.info("Invalid URL " + url, e);
            statusListener.invalidUrl(url);
        } catch (Exception ex) {
            if (emails.size() == 0) {
                throw ex;
            } else {
                // just log a debug warning because we got some useful results back
                logger.warn("Could not finish scraping site: " + ex);
            }
        } finally {
            cleanup();
        }

        return emails;
    }

    protected String getUrlToFollow(URI rootUri, String path) throws URISyntaxException {
        if (!shouldFollowPath(rootUri, path)) {
            statusListener.skippedUrl(path);
            return null;
        }

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
            String localPath = uri.getSchemeSpecificPart();
            if ((localPath.length() > 0) && visitedUrls.contains(localPath)) {
                // prevent circular loops
                statusListener.skippedUrl(url);
                return null;
            } else {
                visitedUrls.add(localPath);
            }
        } catch (URISyntaxException e) {
            statusListener.invalidUrl(path);
            return null;
        }

        return url;
    }

    protected boolean shouldFollowPath(URI rootUri, String path) throws URISyntaxException {
        if (path.startsWith("javascript:") || path.startsWith("#")) {
            return false;
        }

        // exclude multimedia files from being parsed
        for (String ext : excludeExtensions) {
            if (path.toLowerCase().endsWith(ext)) {
                return false;
            }
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
            statusListener.invalidUrl(path);
            return false;
        }
        return true;
    }

    protected void extractEmailsFromText(String text) {
        // extract emails
        Matcher matcher = emailRegEx.matcher(text);
        while (matcher.find()) {
            String email = matcher.group();
            emails.add(email);

            statusListener.foundEmail(email);
        }
    }

    public void addScrapeJob(URI rootUri, String path) throws Exception {
        if (maxThreads == 1) {
            scrapeEmailsFromPage(rootUri, path);
        } else {
            threadExecutor.execute(new ScrapeUrlTask(rootUri, path));
        }
    }

    public void waitDone() throws InterruptedException {
        threadExecutor.awaitTermination(60, TimeUnit.SECONDS);
    }

    class ScrapeUrlTask implements Runnable
    {
        private URI rootUri;
        private String path;

        public ScrapeUrlTask(URI rootUri, String path)
        {
            this.rootUri = rootUri;
            this.path = path;
        }

        @Override
        public void run()
        {
            try
            {
                scrapeEmailsFromPage(rootUri, path);
            }
            catch (Exception ex) {
                logger.warn("Error scraping '" + path + "': " + ex);
            }
        }
    }
}
