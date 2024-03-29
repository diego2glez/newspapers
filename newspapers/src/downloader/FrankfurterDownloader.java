package downloader;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.Settings;

public class FrankfurterDownloader {

	private static final String geckoPath = "/usr/bin/geckodriver";
	//private static final String geckoPath = "C:\\Users\\Diego Gonzalez\\git\\newspapers\\newspapers\\lib\\browserDrivers\\geckodriver.exe";
	private static final String FrankfurterUrl = "https://epaper.faz.net/";

	private static String downloadPath = null;
	private static String urlsFilePath = null;

	private static DateFormat dateFormat1 = new SimpleDateFormat("dd.MM.yyyy");

	private static String currentDate1;

	private static String urlTypeOfPaper;
	private static String xpathTypeOfPaper;

	private static ArrayList<String> urls = null;

	public static void main(String[] args) {

		// 0. Get args downloadpath
		if (args.length > 0) {

			downloadPath = args[0];

			if (downloadPath == null || downloadPath.isEmpty()) {

				System.err.println("Need params: java -jar xxxx DOWNLOADPATH");

				return;

			}

			urlsFilePath = downloadPath + "URLs.txt";

			urls = new ArrayList<String>();

			// PAPER NAME VARS
			urlTypeOfPaper = "FAZ";
			xpathTypeOfPaper = "F.A.Z.";

			// Current Date
			Date date = new Date();
			currentDate1 = dateFormat1.format(date);

			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

			// DIFF PAPER ON SUNDAYS
			if (dayOfWeek == Calendar.SUNDAY) {
				urlTypeOfPaper = "FAS";
				xpathTypeOfPaper = "F.A.S.";
			}

			System.out.println("Current Date: " + currentDate1);

		} else {

			System.err.println("Need params: java -jar xxxx DOWNLOADPATH");

			return;

		}

		// 1. Configure Webriver
		WebDriver driver = setUpFirefox();

		System.out.println("1. Start Login");

		// 2. Go to Frankfurter page
		driver.get(FrankfurterUrl + urlTypeOfPaper + "/" + currentDate1);

		WebDriverWait wait = new WebDriverWait(driver, 30);

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//img[@alt='" + xpathTypeOfPaper + "']")));

		// 3. Login
		driver.findElement(By.xpath("//img[@alt='" + xpathTypeOfPaper + "']")).click();

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("loginName")));

		driver.findElement(By.name("loginName")).clear();
		driver.findElement(By.name("loginName")).sendKeys("jramongil");

		driver.findElement(By.name("password")).clear();
		driver.findElement(By.name("password")).sendKeys("art59ba3");

		driver.findElement(By.xpath("//input[@value='Anmelden']")).click();

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("newspapers-row")));

		// 4. Go to paper (depend on the day)
		driver.get(FrankfurterUrl + urlTypeOfPaper + "/" + currentDate1);

		wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.xpath("//img[contains(@alt,\"" + xpathTypeOfPaper + "\")]")));

		WebElement imgLink = driver.findElement(By.xpath("//img[contains(@alt,\"" + xpathTypeOfPaper + "\")]"));

		// 5. Format final url
		String rawLink = imgLink.getAttribute("src");

		rawLink = rawLink.replace("published/", "");

		rawLink = rawLink.replace("0-cover-big.jpg", "");

		System.out.println(rawLink);

		urls.add(urlTypeOfPaper + "|" + rawLink);

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

		DesiredCapabilities dc = DesiredCapabilities.firefox();
		dc.setAcceptInsecureCerts(true);
		dc.setJavascriptEnabled(true);
		dc.setCapability(FirefoxDriver.MARIONETTE, true);

		WebDriver driver = new FirefoxDriver(dc);

		driver.manage().deleteAllCookies();

		// Set check loop in WebDriverWaits
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

		return driver;

	}

	private static WebDriver setUpJBrowser() {

		// 0. Creacion de directorio y configuracion del webdriver
		System.out.println("0. Creating directory and configuration");

		Settings settings = Settings.builder().javascript(true).build();

		WebDriver driver = new JBrowserDriver(settings);

		driver.manage().deleteAllCookies();

		// Set check loop in WebDriverWaits
		// driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

		return driver;

	}

	// Clean and Close
	private static void clearAndExit(WebDriver driver) {

		driver.manage().deleteAllCookies();
		driver.quit();

	}

}
