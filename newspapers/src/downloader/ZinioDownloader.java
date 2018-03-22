package downloader;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;

public class ZinioDownloader {

	// private static final String geckoPath = "/usr/bin/geckodriver";
	private static final String geckoPath = "C:\\Users\\Diego Gonzalez\\git\\newspapers\\newspapers\\lib\\browserDrivers\\geckodriver.exe";
	private static final String baseUrl = "https://www.zinio.com/es/";

	private static String downloadPath = null;
	private static String urlsFilePath = null;

	private static String orgCookiesPath = null;
	private static String destCookiesPath = null;

	private static String magazineToken = null;
	private static String issueToken = null;

	private static String issueSearchId = null;

	private static ArrayList<String> urls = null;

	public static void main(String[] args) {

		// 0. Get args
		if (args.length > 0) {

			issueSearchId = args[0];

			if (issueSearchId == null || issueSearchId.isEmpty()) {

				System.err.println("Need params: java -jar xxxx SEARCHID DOWNLOADPATH");

				return;

			}

			downloadPath = args[1];

			if (downloadPath == null || downloadPath.isEmpty()) {

				System.err.println("Need params: java -jar xxxx SEARCHID DOWNLOADPATH");

				return;

			}

			urlsFilePath = downloadPath + "URLs.txt";

			urls = new ArrayList<String>();

		} else {

			System.err.println("Need params: java -jar xxxx DOWNLOADPATH");

			return;

		}

		// 1. Configure Webriver

		WebDriver driver = setUpFirefox();

		System.out.println("1. Start Login");

		// 1. Login
		driver.get(baseUrl);
		driver.findElement(By.id("user-logIn")).click();

		driver.findElement(By.id("signIn-email")).clear();
		driver.findElement(By.id("signIn-email")).sendKeys("jramongil@hotmail.com");

		driver.findElement(By.id("signIn-password")).clear();
		driver.findElement(By.id("signIn-password")).sendKeys("art59ba3");

		driver.findElement(By.id("signIn-submit")).click();

		// Wait for login dropdown
		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-accountSettings")));

		// Go to issuePage
		driver.get("https://es.zinio.com/www/browse/product.jsp?rf=sch&productId=" + issueSearchId);

		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@class=\"Product-actions\"]//div[2]//a")));

		driver.findElement(By.xpath("//div[@class=\"Product-actions\"]//div[2]//a")).click();

		// Get html code and extract ids
		String sourceCode = driver.getPageSource();

		// ziniopro\.com\/var\/(site_[0-9]*)\/storage\/issues\/([0-9]*)\/

		Pattern pattern = Pattern.compile("ziniopro\\.com\\/var\\/(site_[0-9]*)\\/storage\\/issues\\/([0-9]*)\\/");
		Matcher matcher = pattern.matcher(sourceCode);
		while (matcher.find()) {

			System.out.println("group 0: " + matcher.group(0));

			System.out.println("group 1: " + matcher.group(1));

			System.out.println("group 1: " + matcher.group(2));

			magazineToken = matcher.group(1);

			issueToken = matcher.group(2);

		}

		urls.add("https://cdn2.audiencemedia.com/var/" + magazineToken + "/storage/issues/" + issueToken + "/svg/");

		// Write urls to file
		BufferedWriter outputWriter;
		Iterator urlsIterator = urls.iterator();

		try {
			outputWriter = new BufferedWriter(new FileWriter(urlsFilePath));

			while (urlsIterator.hasNext()) {

				String u = (String) urlsIterator.next();

				System.out.println(u);

				outputWriter.write(u);
				outputWriter.newLine();
			}

			outputWriter.flush();
			outputWriter.close();

		} catch (IOException e1) {

			e1.printStackTrace();
		}

		clearAndExit(driver);

		System.out.println("DONE");

	}

	private static WebDriver setUpFirefox() {

		// 0. Creacion de directorio y configuracion del webdriver
		System.out.println("0. Creating directory and configuration");

		System.setProperty("webdriver.gecko.driver", geckoPath);

		FirefoxProfile profile = new FirefoxProfile();
		DesiredCapabilities dc = DesiredCapabilities.firefox();
		dc.setAcceptInsecureCerts(true);
		dc.setJavascriptEnabled(true);
		dc.setCapability(FirefoxDriver.MARIONETTE, true);

		profile.setPreference("devtools.jsonview.enabled", false);

		dc.setCapability(FirefoxDriver.PROFILE, profile);

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
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

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