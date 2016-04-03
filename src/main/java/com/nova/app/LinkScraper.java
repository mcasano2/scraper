/**
 * LinkScraper finds urls of nba games over given date range
 * and calls BoxScoreNBA to scrape each game for stats
 */

package com.nova.app;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class LinkScraper {

	private WebDriver driver;
	private String currentTime, startDate, endDate;
	private Set<String> urls = new HashSet<String>();
	private Set<String> failedGames = new HashSet<String>();

	/**
	 * set date range to yesterday
	 */
	public LinkScraper() {
		this.startDate = LocalDate.now().minusDays(1).toString().replaceAll("-", "");
		this.endDate = LocalDate.now().minusDays(1).toString().replaceAll("-", "");
	}

	/**
	 * sets date range for a single day
	 * 
	 * @param startDate
	 */
	public LinkScraper(String startDate) {
		this.startDate = startDate;
		this.endDate = startDate;
	}

	/**
	 * sets date range where startDate <= endDate
	 * 
	 * @param startDate
	 * @param endDate
	 */
	public LinkScraper(String startDate, String endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}

	/**
	 * finds links to nba boxscores for given daterange
	 */
	public void findURLs() {
		datesLoop();
	}

	private void datesLoop() {
		try {
			driver = new FirefoxDriver();
			driver.manage().timeouts().pageLoadTimeout(1, TimeUnit.SECONDS);
			currentTime = LocalDateTime.now().toString();
			System.out.println("Start Time: " + currentTime);
			LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.BASIC_ISO_DATE);
			LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.BASIC_ISO_DATE);
			while (!start.isAfter(end)) {
				if (findURLsDate(start.toString()) == -1) {
					driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
					if (findURLsDate(start.toString()) == -1) {
						System.out.println("Failed, skipping date " + start.toString());
					}
				}
				start = start.plus(1, ChronoUnit.DAYS);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			driver.quit();
		}
	}

	/**
	 * 
	 * @param date day to find links
	 * @return -1 for failure, 0 for success
	 * 
	 */
	private int findURLsDate(String date) {
		List<WebElement> links = null;
		try {
			driver.get("http://www.nba.com/gameline/" + date.replaceAll("[-]", ""));
			links = driver.findElements(By.tagName("a"));
		} catch (Exception e) {
			return -1;
		}

		links.forEach(i -> {
			String url = i.getAttribute("href");
			if (url != null) {
				if (url.indexOf("http://www.nba.com/games/") != -1) {
					urls.add(url + "#nbaGIboxscore");
				}
			}

		});
		return 0;
	}

	/**
	 * scrapes nba games for given date range
	 */
	public void scrapeNBA() {

		scrapeNBAHelper();
	}

	private void scrapeNBAHelper() {

		System.out.println("# of games:" + urls.size());
		urls.forEach(s -> {
			BoxScoreNBA nbaGame = new BoxScoreNBA(s, currentTime, 1);
			if (nbaGame.scrapeBoxScore() == -1) {
				failedGames.add(s);
			}
		});
		if (!failedGames.isEmpty()) {
			failedGames.forEach(s -> {
				BoxScoreNBA nbaGame = new BoxScoreNBA(s, currentTime, 15);
				if (nbaGame.scrapeBoxScore() == -1) {
					System.out.println("Failed twice:" + s);
				}
			});
		}
		System.out.println("End Time: " + LocalDateTime.now().toString());
	}

	/**
	 * 
	 * @return date to begin scraping
	 */
	public String getStartDate() {
		return startDate;
	}

	/**
	 * 
	 * @return date to end scraping
	 */
	public String getEndDate() {
		return endDate;
	}

}