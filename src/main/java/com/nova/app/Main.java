/**
 * Main class for scraper
 *
 */
package com.nova.app;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Properties;

public class Main {
	private static Properties props;

	private static final String PROP_PATH = "scraper.properties";
	
/**
 * main() scrapes team stats over date range
 * @param args startDate OR startDate endDate
 * @throws Exception
 */
	public static void main(String[] args) throws Exception {
		props = new Properties();

		if (args.length == 1) {
			props.setProperty("startDate", args[0]);
			props.setProperty("endDate", args[0]);
		} else if (args.length == 2) {
			props.setProperty("startDate", args[0]);
			props.setProperty("endDate", args[1]);
		} else {
			loadProperties();
		}

		LinkScraper links = new LinkScraper(props.getProperty("startDate"), props.getProperty("endDate"));

		links.findURLs();

		links.scrapeNBA();
	}

	private static void loadProperties() {
		Path configPath = Paths.get(PROP_PATH);
		if (!Files.exists(configPath)) {
			writeDefaultProperties();
		} else {
			readPropFile();
		}

	}

	/**
	 * writes default prop file with date range from yesterday to yesterday
	 */
	private static void writeDefaultProperties() {

		try (FileOutputStream out = new FileOutputStream(PROP_PATH)) {
			String yesterday = LocalDate.now().minusDays(1).toString().replaceAll("-", "");
			props.setProperty("startDate", yesterday);
			props.setProperty("endDate", yesterday);
			props.store(out, null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void readPropFile() {
		try {
			FileInputStream in = new FileInputStream(PROP_PATH);
			props.load(in);
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
