/**
 * BoxScoreNBA scrapes team stats for a single game
 */

package com.nova.app;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
//import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class BoxScoreNBA {

	private static final class Const {

		private static final int AWAY_FGMA = 2;
		private static final int AWAY_3PMA = 3;
		private static final int AWAY_FTMA = 4;
		private static final int AWAY_OREB = 5;
		private static final int AWAY_DREB = 6;
		private static final int AWAY_TREB = 7;
		private static final int AWAY_AST = 8;
		private static final int AWAY_PF = 9;
		private static final int AWAY_STL = 10;
		private static final int AWAY_BLK = 12;
		private static final int AWAY_PTS = 14;

		private static final int HOME_FGMA = 19;
		private static final int HOME_3PMA = 20;
		private static final int HOME_FTMA = 21;
		private static final int HOME_OREB = 22;
		private static final int HOME_DREB = 23;
		private static final int HOME_TREB = 24;
		private static final int HOME_AST = 25;
		private static final int HOME_PF = 26;
		private static final int HOME_STL = 27;
		private static final int HOME_BLK = 29;
		private static final int HOME_PTS = 31;

		private static final String overTime1 = "//*[@id='nbaLineScoreAJAX']/div[2]/div[3]/table/tbody/tr[1]/th[5]";
		private static final String overTime2 = "//*[@id='nbaLineScoreAJAX']/div[2]/div[3]/table/tbody/tr[1]/th[6]";
		private static final String overTime3 = "//*[@id='nbaLineScoreAJAX']/div[2]/div[3]/table/tbody/tr[1]/th[7]";
		private static final String overTime4 = "//*[@id='nbaLineScoreAJAX']/div[2]/div[3]/table/tbody/tr[1]/th[8]";
		private static final String overTime5 = "//*[@id='nbaLineScoreAJAX']/div[2]/div[3]/table/tbody/tr[1]/th[8]";

	}

	private WebDriver driver;
	private String currentTime;
	private int timeOut;
	private String url;

	private String gameId;
	private String gameDate;
	private int numQuarters;

	private String gameInfoCSV, awayCSV, homeCSV;

	private String awayName, homeName;
	private int awayTotal, homeTotal;

	private int awayFgm, awayFga, away3pm, away3pa, awayFtm, awayFta, awayOreb, awayDreb, awayReb, awayAst, awayStl,
			awayBlk, awayTeamTO, awayPf;
	private int homeFgm, homeFga, home3pm, home3pa, homeFtm, homeFta, homeOreb, homeDreb, homeReb, homeAst, homeStl,
			homeBlk, homeTeamTO, homePf;

	/**
	 * 
	 * @param url
	 *            link to nba.com game
	 * @param currentTime
	 *            unique id for writing to files
	 * @param timeOut
	 *            selenium pageload timeout time
	 */
	public BoxScoreNBA(String url, String currentTime, int timeOut) {
		this.url = url;
		this.currentTime = currentTime;
		this.timeOut = timeOut;

	}

	/**
	 * Scrapes box score stats into files
	 * 
	 * @return 0 for success, -1 for failure
	 */
	public int scrapeBoxScore() {

		return scrapeHelper();
	}

	/**
	 * Prints CSVs of game information and teamstats
	 */
	public void printStats() {
		System.out.println(gameInfoCSV);
		System.out.println(awayCSV);
		System.out.println(homeCSV);
	}

	/**
	 * @return CSV of game information
	 */
	public String getGameInfoRow() {
		return gameInfoCSV;
	}

	/**
	 * @return CSV of away stats
	 */
	public String getAwayRow() {
		return awayCSV;
	}

	/**
	 * @return CSV of home stats
	 */
	public String getHomeRow() {
		return homeCSV;
	}

	/**
	 * @return -1 for failure, 0 for success
	 */
	private int scrapeHelper() {

		try {
			driver = new FirefoxDriver();
			driver.manage().timeouts().pageLoadTimeout(timeOut, TimeUnit.SECONDS);
			loadPage();
			parseTeamNames();
			scrapeTeamStats();
			scrapeOverTime();
			scrapeTO();
			fillCSV();
			writeFile();
		} catch (Exception e) {
			return -1;
		} finally {
			driver.quit();
		}
		return 0;
	}

	private void loadPage() {
		try {
			driver.get(url);
		} catch (Exception e) {
			// timeout reached, page still loading (slow ad?)
		}

	}

	private void parseTeamNames() throws Exception {

		String[] parsed = url.split("games/|/gameinfo");
		gameId = parsed[1].replaceAll("/", "");
		gameDate = gameId.substring(0, 8);
		awayName = gameId.substring(8, 11);
		homeName = gameId.substring(11, 14);
	}

	private void scrapeTeamStats() throws Exception {
		List<WebElement> stats = driver.findElements(By.className("nbaGIScrTot"));

		String[] awayFg = stats.get(Const.AWAY_FGMA).getText().split("-");
		awayFgm = Integer.parseInt(awayFg[0]);
		awayFga = Integer.parseInt(awayFg[1]);

		String[] away3p = stats.get(Const.AWAY_3PMA).getText().split("-");
		away3pm = Integer.parseInt(away3p[0]);
		away3pa = Integer.parseInt(away3p[1]);

		String[] awayFt = stats.get(Const.AWAY_FTMA).getText().split("-");
		awayFtm = Integer.parseInt(awayFt[0]);
		awayFta = Integer.parseInt(awayFt[1]);

		awayOreb = Integer.parseInt(stats.get(Const.AWAY_OREB).getText());
		awayDreb = Integer.parseInt(stats.get(Const.AWAY_DREB).getText());
		awayReb = Integer.parseInt(stats.get(Const.AWAY_TREB).getText());

		awayAst = Integer.parseInt(stats.get(Const.AWAY_AST).getText());
		awayStl = Integer.parseInt(stats.get(Const.AWAY_STL).getText());
		awayBlk = Integer.parseInt(stats.get(Const.AWAY_BLK).getText());

		awayPf = Integer.parseInt(stats.get(Const.AWAY_PF).getText());

		awayTotal = Integer.parseInt(stats.get(Const.AWAY_PTS).getText());

		String[] homeFg = stats.get(Const.HOME_FGMA).getText().split("-");
		homeFgm = Integer.parseInt(homeFg[0]);
		homeFga = Integer.parseInt(homeFg[1]);

		String[] home3p = stats.get(Const.HOME_3PMA).getText().split("-");
		home3pm = Integer.parseInt(home3p[0]);
		home3pa = Integer.parseInt(home3p[1]);

		String[] homeFt = stats.get(Const.HOME_FTMA).getText().split("-");
		homeFtm = Integer.parseInt(homeFt[0]);
		homeFta = Integer.parseInt(homeFt[1]);

		homeOreb = Integer.parseInt(stats.get(Const.HOME_OREB).getText());
		homeDreb = Integer.parseInt(stats.get(Const.HOME_DREB).getText());
		homeReb = Integer.parseInt(stats.get(Const.HOME_TREB).getText());

		homeAst = Integer.parseInt(stats.get(Const.HOME_AST).getText());
		homeStl = Integer.parseInt(stats.get(Const.HOME_STL).getText());
		homeBlk = Integer.parseInt(stats.get(Const.HOME_BLK).getText());

		homePf = Integer.parseInt(stats.get(Const.HOME_PF).getText());
		homeTotal = Integer.parseInt(stats.get(Const.HOME_PTS).getText());

	}

	private void scrapeOverTime() throws Exception {
		String ot1 = driver.findElement(By.xpath(Const.overTime1)).getText();
		if (!ot1.matches("OT1")) {
			numQuarters = 4;
			return;
		}
		String ot2 = driver.findElement(By.xpath(Const.overTime2)).getText();
		if (!ot2.matches("OT2")) {
			numQuarters = 5;
			return;
		}

		String ot3 = driver.findElement(By.xpath(Const.overTime3)).getText();
		if (!ot3.matches("OT3")) {
			numQuarters = 6;
			return;
		}

		String ot4 = driver.findElement(By.xpath(Const.overTime4)).getText();
		if (!ot4.matches("OT4")) {
			numQuarters = 7;
			return;
		}

		String ot5 = driver.findElement(By.xpath(Const.overTime5)).getText();
		if (!ot5.matches("OT5")) {
			numQuarters = 8;
			return;
		}

	}

	private void scrapeTO() throws Exception {
		List<WebElement> turnOvers = driver.findElements(By.className("nbaGIBtmTbl"));
		String away[] = turnOvers.get(0).getText().trim().split("TO:");
		String home[] = turnOvers.get(1).getText().trim().split("TO:");
		awayTeamTO = Integer.parseInt(away[1].trim());
		homeTeamTO = Integer.parseInt(home[1].trim());
	}

	private void fillCSV() {
		fillGameInfoCSV();
		fillAwayCSV();
		fillHomeCSV();
	}

	private void fillGameInfoCSV() {

		gameInfoCSV = gameId + "," + gameDate + "," + numQuarters + "," + awayName + "," + homeName + "," + awayTotal
				+ "," + homeTotal;
	}

	private void fillAwayCSV() {

		awayCSV = gameId + "," + "0," + awayFgm + "," + awayFga + "," + away3pm + "," + away3pa + "," + awayFtm + ","
				+ awayFta + "," + awayOreb + "," + awayDreb + "," + awayReb + "," + awayAst + "," + awayPf + ","
				+ awayStl + "," + awayTeamTO + "," + awayBlk;

	}

	private void fillHomeCSV() {
		homeCSV = gameId + "," + "1," + homeFgm + "," + homeFga + "," + home3pm + "," + home3pa + "," + homeFtm + ","
				+ homeFta + "," + homeOreb + "," + homeDreb + "," + homeReb + "," + homeAst + "," + homePf + ","
				+ homeStl + "," + homeTeamTO + "," + homeBlk;
	}

	private void writeFile() throws IOException {

		try (BufferedWriter writer = new BufferedWriter(new FileWriter("gameInfo_" + currentTime, true))) {
			writer.write(gameInfoCSV);
			writer.newLine();
		}
		try (BufferedWriter writer = new BufferedWriter(new FileWriter("teamStats_" + currentTime, true))) {
			writer.write(awayCSV);
			writer.newLine();
			writer.write(homeCSV);
			writer.newLine();
		}

	}

}