package downloader;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;

public class VisaoMagazineDownloader {

	// private static final String geckoPath = "/usr/bin/geckodriver";
	private static final String geckoPath = "C:\\Users\\Diego Gonzalez\\Desktop\\Workspace\\Git\\newspapers\\newspapers\\lib\\geckodriver.exe";
	private static final String baseUrl = "http://visaodigitalsubs.visao.pt/";

	private static String downloadPath = null;
	private static String urlsFilePath = null;

	private static ArrayList<String> urls = null;

	public static void main(String[] args) {

		int count = 0;

		while (count < 5) {
			count++;

			try {

				run(args);
				System.out.println("COMPLETADO BREAK");
				break;

			} catch (Exception e) {
				continue;
			}

		}

		System.out.println("THIS IS THE END");
		System.exit(0);

	}

	public static void run(String[] args) throws Exception {

		// 0. Get args
		if (args.length > 0) {

			downloadPath = args[0];

			if (downloadPath == null || downloadPath.isEmpty()) {

				System.err.println("Need params: java -jar xxxx DOWNLOADPATH");

				return;

			}

			urlsFilePath = downloadPath + "URLs.txt";

		} else {

			System.err.println("Need params: java -jar xxxx DOWNLOADPATH");

			return;

		}

		// 1. Configure Webriver
		WebDriver driver = setUpFirefox();

		System.out.println("1. Start Login");

		// 1. Login
		driver.get(baseUrl);

		// Wait for login textbox
		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("txtAlreadyEmail")));

		driver.findElement(By.id("txtAlreadyEmail")).clear();
		driver.findElement(By.id("txtAlreadyEmail")).sendKeys("ramon@tenao.com");
		driver.findElement(By.id("txtAlreadyPassword")).clear();
		driver.findElement(By.id("txtAlreadyPassword")).sendKeys("62r38Ih#");
		driver.findElement(By.id("lnkbtnLogin")).click();
		
		wait.until(ExpectedConditions.elementToBeClickable(By.className("howTo-continue")));
		
		// 3. Get pag lenght
		long lenght = (long) ((JavascriptExecutor) driver).executeScript("return flatPlanData.pageGroups.length");

		System.out.println("Num pages " + lenght);

		urls = new ArrayList<String>();

		// 4. Iterate through pages
		long pageCount = 0;
		while (pageCount < lenght) {

			// Get page pdf
			String url = (String) ((JavascriptExecutor) driver)
					.executeScript("return flatPlanData.pageGroups[" + pageCount + "].pages[0].pdf");

			System.out.println("Page[" + pageCount + "]: " + url);

			urls.add(pageCount + "|" + url);

			pageCount++;

		}

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
		driver.manage().timeouts().implicitlyWait(120, TimeUnit.SECONDS);

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

	// Clean and Close
	private static void clearAndExit(WebDriver driver) {

		driver.manage().deleteAllCookies();
		driver.quit();

	}

}