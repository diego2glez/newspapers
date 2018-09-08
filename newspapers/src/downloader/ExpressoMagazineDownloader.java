package downloader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;

public class ExpressoMagazineDownloader {

	private static final String geckoPath = "/usr/bin/geckodriver";
	private static final String baseUrl = "http://expresso.lojaimpresa.pt/";

	private static String downloadPath = null;
	private static String urlsFilePath = null;

	private static String orgCookiesPath = null;
	private static String destCookiesPath = null;

	public static void main(String[] args) {

		int count = 0;

		while (count < 5) {
			count++;

			try {

				run(args);
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

		WebDriver driver = setUpJBrowser();

		File dir = new File(downloadPath);
		if (!dir.exists()) {

			dir.mkdir();

		}

		if (!dir.exists()) {

			System.err.println("No existe el directorio " + downloadPath);

		}

		System.out.println("1. Start Login");

		// 1. Login
		driver.get(baseUrl);

		// Wait for login textbox
		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ctl00_lv1_txtLogin")));

		driver.findElement(By.id("ctl00_lv1_txtLogin")).clear();
		driver.findElement(By.id("ctl00_lv1_txtLogin")).sendKeys("ramon@tenao.com");
		driver.findElement(By.id("ctl00_lv1_txtPass")).clear();
		driver.findElement(By.id("ctl00_lv1_txtPass")).sendKeys("art59ba3");
		driver.findElement(By.id("ctl00_lv1_ibLogin")).click();

		wait = new WebDriverWait(driver, 15);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ctl00_lv1_ibLogout")));

		// 3. Open actual edition
		driver.findElement(By.id("ctl00_cph_dvtDay")).click();
		wait = new WebDriverWait(driver, 15);
		wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("img.thumbsmall")));
		driver.findElement(By.cssSelector("img.thumbsmall")).click();

		// 4. Preparing viewer
		wait = new WebDriverWait(driver, 15);
		wait.until(ExpectedConditions.elementToBeClickable(By.id("ctl00_ibSwitch")));
		driver.findElement(By.id("ctl00_ibSwitch")).click();

		// 5. Looking for index
		wait = new WebDriverWait(driver, 15);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bvdMenuImg")));

		WebElement bvdMenuImg = null;
		bvdMenuImg = driver.findElement(By.id("bvdMenuImg"));

		// 6. Get list of pages from index
		List<WebElement> childs = bvdMenuImg.findElements(By.xpath(".//img"));
		childs.remove(0);

		System.out.println("Childs: " + childs.size());

		ArrayList<String> images = new ArrayList<String>();

		// 7. Iterate over pages saving their download URL
		Iterator pagesIterator = childs.iterator();
		int pageCount = 0;
		while (pagesIterator.hasNext()) {

			WebElement page = (WebElement) pagesIterator.next();
			pageCount++;

			page.click();

			try {
				wait = new WebDriverWait(driver, 60);
				wait.until(ExpectedConditions.attributeContains(By.id("ctl00_cph_viewer1_imgPage"), "src",
						"f" + pageCount));
			} catch (StaleElementReferenceException e) {
				// CONTINUE
			}

			String url = (String) ((JavascriptExecutor) driver)
					.executeScript("return $(vprex + 'imgPage').attr('src');");

			String[] split = url.split("/");

			String urlDownload = baseUrl + split[1] + "/" + split[2] + "/" + split[3] + "/s5/" + split[4];

			System.out.println(urlDownload);

			images.add(urlDownload);

		}

		System.out.println("PageCount = " + pageCount);

		if (images.size() != childs.size()) {
			throw new Exception();

		}

		// 8. Iterate over pages and write to temp file
		pagesIterator = images.iterator();

		BufferedWriter outputWriter;
		try {
			outputWriter = new BufferedWriter(new FileWriter(urlsFilePath));

			while (pagesIterator.hasNext()) {

				outputWriter.write((String) pagesIterator.next());
				outputWriter.newLine();
			}

			outputWriter.flush();
			outputWriter.close();

		} catch (IOException e1) {

			e1.printStackTrace();
		}

		clearAndExit(driver);

		driver = setUpFirefox();

		System.out.println("1. Start Login");

		// 1. Login
		driver.get(baseUrl);

		// Wait for login textbox
		wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ctl00_lv1_txtLogin")));

		driver.findElement(By.id("ctl00_lv1_txtLogin")).clear();
		driver.findElement(By.id("ctl00_lv1_txtLogin")).sendKeys("ramon@tenao.com");
		driver.findElement(By.id("ctl00_lv1_txtPass")).clear();
		driver.findElement(By.id("ctl00_lv1_txtPass")).sendKeys("art59ba3");
		driver.findElement(By.id("ctl00_lv1_ibLogin")).click();

		wait = new WebDriverWait(driver, 15);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ctl00_lv1_ibLogout")));

		// 2. Get Firefox profile path
		Process proc;
		try {

			ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c",
					"find /tmp/ -maxdepth 1 -name \"rust_mozprofile.*\" -printf \"%T+\\t%p\\n\" | sort | tail -1 | awk '{print $2}'");

			proc = pb.start();

			proc.waitFor();
			StringBuffer output = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

			orgCookiesPath = reader.readLine() + "/cookies.sqlite";

			destCookiesPath = downloadPath + "cookies.sqlite";

			dumpFirefoxSqliteCookiesFile();

		} catch (IOException | InterruptedException e1) {

			e1.printStackTrace();
		}

		try {
			Thread.sleep(10000);

		} catch (Exception e) {
			// TODO: handle exception
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