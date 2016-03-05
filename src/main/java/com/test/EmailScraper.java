package com.test;

import com.test.pageloaders.PageLoader;
import com.test.pageloaders.SeleniumPageLoader;
import com.test.pageloaders.SimplePageLoader;
import com.test.pageloaders.Ui4JPageLoader;
import com.test.scrapers.JsoupScraper;
import com.test.scrapers.Scraper;
import com.test.scrapers.StdoutStatusDumper;
import com.test.utils.SslUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kohsuke.args4j.ExampleMode.ALL;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.BooleanOptionHandler;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Email scraper main class
 */
public class EmailScraper {
	
	private static final Logger logger = LoggerFactory.getLogger(EmailScraper.class);

	@Argument
	private List<String> arguments = new ArrayList<String>();

	@Option(name="-noajax",usage="Disables support for AJAX web sites")
	private boolean noAjax = false;

    @Option(name="-ui4j",usage="Use Ui4j to download AJAX web sites instead of jBrowserDriver (must build with Monocle)")
    private boolean useUi4j = false;

    @Option(name="-loglevel",usage="Default is INFO; can set to TRACE, DEBUG, or NONE")
    private String logLevel = "WARN";

	public static void main(String[] args) {
		new EmailScraper().runMain(args);
	}

	private void runMain(String[] args) {
        parseArguments(args);
        disableJavaSslCheck();

        PageLoader pageLoader;
        if (noAjax) {
            pageLoader = new SimplePageLoader();
        } else if (useUi4j) {
            pageLoader = new Ui4JPageLoader();
        } else {
            pageLoader = new SeleniumPageLoader();
        }

		Scraper scraper = new JsoupScraper(pageLoader, new StdoutStatusDumper());
        Set<String> emails = doScraping(pageLoader, scraper);
		showEmails(emails);
	}

    private Set<String> doScraping(PageLoader pageLoader, Scraper scraper) {
        Set<String> emails = null;
        try {
            String url = arguments.get(0);
            emails = scraper.getEmails(url);
        } catch (Exception e) {
            logger.error("Error scraping pages: ", e);
            System.exit(-2);
        } finally {
            pageLoader.cleanup();
            scraper.cleanup();
        }
        return emails;
    }

    private void disableJavaSslCheck() {
        // Java doesn't have a web browser's SSL cert keystore so just disable SSL validation
        try {
            SslUtils.disableSSLCertCheck();
        } catch (NoSuchAlgorithmException e) {
            logger.warn("Unable to find SSL algorithms");
        } catch (KeyManagementException e) {
            logger.warn("Unable to trust all SSL connections");
        }
    }

    private void parseArguments(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
            if( arguments.isEmpty() )
                throw new CmdLineException("Must specify site");
        } catch( CmdLineException e ) {
            System.err.println("  Example: java -jar EmailParser-1.0.jar <website>"+parser.printExample(ALL));
            System.exit(0);
        }

        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, logLevel);
    }

    private void showEmails(Set<String> emails) {
		if (emails.size() == 0) {
			System.out.println("\nNo emails found");
		} else {
			System.out.println("\nFound these email addresses:");
			for (String email: emails) {
				System.out.println(email);
			}
		}
	}
}
