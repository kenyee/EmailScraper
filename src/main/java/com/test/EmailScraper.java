package com.test;

import com.test.scrapers.JsoupScraper;
import com.test.scrapers.Scraper;
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
	private boolean noAjax;

	public static void main(String[] args) {
		new EmailScraper().runMain(args);
	}

	private void runMain(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
			if( arguments.isEmpty() )
				throw new CmdLineException("Must specify site");
		} catch( CmdLineException e ) {
			System.err.println("  Example: java -jar EmailParser-1.0.jar <website>"+parser.printExample(ALL));
			System.exit(0);
		}

		// Java doesn't have a web browser's SSL cert keystore so just disable SSL validation
		try {
			SslUtils.disableSSLCertCheck();
		} catch (NoSuchAlgorithmException e) {
			logger.warn("Unable to find SSL algorithms");
		} catch (KeyManagementException e) {
			logger.warn("Unable to trust all SSL connections");
		}

		Scraper scraper = new JsoupScraper(!noAjax);
		Set<String> emails = null;
		try {
			emails = scraper.getEmails(arguments.get(0));
		} catch (Exception e) {
			logger.error("Error scraping pages: ", e);
			System.exit(-2);
		}

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
