package com.test;

import com.test.scrapers.JsoupScraper;
import com.test.scrapers.Scraper;
import com.test.utils.SslUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

/**
 * Email scraper main class
 */
public class EmailScraper {
	
	private static final Logger logger = LoggerFactory.getLogger(EmailScraper.class);

	public static void main(String[] args) {
		if ((args.length == 0) || (args.length > 1)) {
			System.out.println("Usage: EmailScraper <hostname>");
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

		Scraper scraper = new JsoupScraper(false);
		Set<String> emails = null;
		try {
			emails = scraper.getEmails(args[0]);
		} catch (Exception e) {
			logger.error("Error scraping pages" + e);
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
