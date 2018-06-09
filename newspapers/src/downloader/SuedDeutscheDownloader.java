package downloader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;

public class SuedDeutscheDownloader {

	// private static final String geckoPath = "/usr/bin/geckodriver";
	private static final String geckoPath = "C:\\Users\\Diego Gonzalez\\git\\newspapers\\newspapers\\lib\\browserDrivers\\geckodriver.exe";
	private static final String loginUrl = "https://login.kataweb.it/registrazione/repubblica.it/login.jsp?ssoOnly=false&backurl=https%3A%2F%2Fwww.repubblica.it%2Fsocial%2Fsites%2Frepubblica%2Fnazionale%2Floader.php%3FmClose%3D2%26backUrl%3Dhttps%253A%2F%2Fquotidiano.repubblica.it%2Fedicola%2Fmanager%253Fservice%253Dlogin.social&origin=null&optbackurl=https%3A%2F%2Fwww.repubblica.it%2Fsocial%2Fsites%2Frepubblica%2Fnazionale%2Floader.php%3FmClose%3D2%26backUrl%3Dhttps%253A%2F%2Fquotidiano.repubblica.it%2Fedicola%2Fmanager%253Fservice%253Dlogin.social";

	private static String downloadPath = null;
	private static String urlsFilePath = null;

	private static String issueId = null;

	private static String orgCookiesPath = null;
	private static String destCookiesPath = null;

	private static DateFormat dateFormat = new SimpleDateFormat("dd");
	private static DateFormat dateFormat2 = new SimpleDateFormat("YYYY");
	private static String currentDay;
	private static String currentYear;

	public static void main(String[] args) {

		// 0. Get args
		if (args.length > 0) {

			downloadPath = args[0];

			if (downloadPath == null || downloadPath.isEmpty()) {

				System.err.println("Need params: java -jar xxxx DOWNLOADPATH");

				return;

			}

			urlsFilePath = downloadPath + "URLs.txt";

			// Current date
			Date date = new Date();
			currentDay = dateFormat.format(date);
			currentYear = dateFormat2.format(date);
			System.out.println("Current day: " + currentDay);
			System.out.println("Current year: " + currentYear);
		} else {

			System.err.println("Need params: java -jar xxxx DOWNLOADPATH");

			return;

		}

		// 1. Configure Webriver

		WebDriver driver = setUpFirefox();

		System.out.println("1. Start Login");

		// 1. Login
		driver.get("https://epaper.sueddeutsche.de");

		// Wait for login textbox
		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Anmelden")));

		driver.findElement(By.linkText("Anmelden")).click();

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("id_login")));

		driver.findElement(By.id("id_login")).click();
		driver.findElement(By.id("id_login")).clear();
		driver.findElement(By.id("id_login")).sendKeys("jramongil@hotmail.com");

		driver.findElement(By.id("id_password")).clear();
		driver.findElement(By.id("id_password")).sendKeys("art59ba3");

		driver.findElement(By.id("id_remember_me")).click();

		driver.findElement(By.id("authentication-button")).click();

		wait.until(ExpectedConditions
				.visibilityOfAllElementsLocatedBy(By.xpath("//img[contains(@class,'issue__cover')][1]")));

		// 2. Get issue id

		String issueSrc = driver.findElement(By.xpath("//img[contains(@class,'issue__cover')][1]")).getAttribute("src");

		String dayHeader = driver.findElement(By.className("day__header")).getText();

		System.out.println("DayHeader = " + dayHeader);

		System.out.println("IssueSRC = " + issueSrc);

		if (issueSrc != null && dayHeader != null && dayHeader.contains(currentDay)
				&& dayHeader.contains(currentYear)) {

			Pattern pattern = Pattern.compile("pages\\/(.*)\\/");
			Matcher matcher = pattern.matcher(issueSrc);
			while (matcher.find()) {

				System.out.println("group 0: " + matcher.group(0));

				issueId = matcher.group(1);

			}

			System.out.println("IssueId = " + issueId);

			String url = "https://epaper.sueddeutsche.de/download/" + issueId;

			// Write url to file
			BufferedWriter outputWriter;

			try {
				outputWriter = new BufferedWriter(new FileWriter(urlsFilePath));

				outputWriter.write(url);
				outputWriter.newLine();

				outputWriter.flush();
				outputWriter.close();

			} catch (IOException e1) {

				e1.printStackTrace();
			}

		}

		// 2. Get Firefox profile path
		Process proc;
		try {

			ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c",
					"find /tmp/ -maxdepth 1 -name \"rust_mozprofile.*\" -printf \"%T+\\t%p\\n\" | sort | tail -1 | awk '{print $2}'");

			proc = pb.start();

			proc.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

			orgCookiesPath = reader.readLine() + "/cookies.sqlite";

			destCookiesPath = downloadPath + "cookies.sqlite";

			dumpFirefoxSqliteCookiesFile();

		} catch (IOException | InterruptedException e1) {

			e1.printStackTrace();
		}

		clearAndExit(driver);

		System.out.println("DONE");

	}

	private static WebDriver setUpFirefox() {

		// 0. Creacion de directorio y configuracion del webdriver
		System.out.println("0. Creating directory and configuration");

		System.setProperty("webdriver.gecko.driver", geckoPath);

		FirefoxProfile firefoxProfile = new FirefoxProfile();

		firefoxProfile.setPreference("pdfjs.disabled", true);
		firefoxProfile.setPreference("browser.download.folderList", 2);
		firefoxProfile.setPreference("browser.download.manager.showWhenStarting", false);
		firefoxProfile.setPreference("browser.download.dir", downloadPath);
		firefoxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/pdf");

		DesiredCapabilities dc = DesiredCapabilities.firefox();
		dc.setAcceptInsecureCerts(true);
		dc.setJavascriptEnabled(true);
		dc.setCapability(FirefoxDriver.MARIONETTE, true);
		dc.setCapability(FirefoxDriver.PROFILE, firefoxProfile);

		WebDriver driver = new FirefoxDriver(dc);

		driver.manage().deleteAllCookies();

		// Set check loop in WebDriverWaits
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

		return driver;

	}

	private static WebDriver setUpJBrowser() {

		// 0. Creacion de directorio y configuracion del webdriver
		System.out.println("0. Creating directory and configuration");

		WebDriver driver = new JBrowserDriver();

		driver.manage().deleteAllCookies();

		// Set check loop in WebDriverWaits
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

		return driver;

	}

	// Dump Firefox Cookies Database to DownloadPath
	private static void dumpFirefoxSqliteCookiesFile() throws IOException {

		ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", "rm " + destCookiesPath);

		pb.start();

		ProcessBuilder pb2 = new ProcessBuilder("/bin/bash", "-c",
				"echo \".dump\" | sqlite3 " + orgCookiesPath + " | sqlite3 " + destCookiesPath);

		pb2.start();

	}

	// Clean and Close
	private static void clearAndExit(WebDriver driver) {

		driver.manage().deleteAllCookies();
		driver.quit();

	}

}