package downloader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.JsonObject;
import com.machinepublishers.jbrowserdriver.JBrowserDriver;

public class ValorDownloader {

	private static final String geckoPath = "/usr/bin/geckodriver";
	// private static final String geckoPath = "C:\\Users\\dgonzalezgon\\OneDrive -
	// DXC
	// Production\\Documents\\Workspace\\diego2glez\\Repos\\newspapers\\newspapers\\lib\\geckodriver.exee";

	private static String downloadPath = null;
	private static String urlsFilePath = null;

	private static String orgCookiesPath = null;
	private static String destCookiesPath = null;

	private static ArrayList<String> urls = null;

	public static void main(String[] args) {

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

		urls = new ArrayList<String>();

		// 1. Configure Webriver

		WebDriver driver = setUpFirefox();

		System.out.println("1. Start Login");

		// 1. Login
		driver.get("https://www.valor.com.br/virador");

		System.out.println(driver.getPageSource());
		
		// Wait for login textbox
		WebDriverWait wait = new WebDriverWait(driver, 60);

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("identification")));

		driver.findElement(By.id("identification")).click();
		driver.findElement(By.id("identification")).clear();
		driver.findElement(By.id("identification")).sendKeys("jramongil@hotmail.com");

		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys("art59ba3");

		driver.findElement(By.className("rdp-btn")).click();

		// Access issue
		wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className("more")));

		driver.findElements(By.className("more")).get(0).click();

		// 2. Get issue id
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("rdp-reader")));
		
		WebElement divReader = driver.findElement(By.id("rdp-reader"));

		String issueId = divReader.getAttribute("data-edition-id");

		System.out.println("issueId: " + issueId);

		String firstPage = driver.findElement(By.xpath("//img[contains(@alt,'gina 1') and @data-ga]"))
				.getAttribute("data-page-id").replace("'", "");

		int pageNum = Integer.parseInt(firstPage);

		System.out.println("First pageId= " + firstPage);

		// Get all pages
		URL url = null;

		HttpURLConnection con = null;
		
		int status = 200;

		while (status == 200) {

			try {
				url = new URL("https://sunflower2.digitalpages.com.br/html/getPageZoom?editionId=" + issueId
						+ "&pageId=" + pageNum + "&lvl=0&landscapeOnly=false");

				con = (HttpURLConnection) url.openConnection();

				con.setRequestMethod("GET");

				status = con.getResponseCode();

				if (status != 200) {
					break;
				}

				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

				String inputLine;

				JSONParser parser = new JSONParser();

				JSONObject obj;

				if ((inputLine = in.readLine()) != null) {

					System.out.println("Page " + pageNum + ": Status (" + ") - " + inputLine);

					obj = (JSONObject) parser.parse(inputLine);

					System.out.println("URL: " + obj.get("url"));
					
					if(obj.get("url").equals("notfound")) {
						break;
					}

					urls.add(obj.get("url").toString());

				}

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			pageNum++;

		}

		if(con != null) con.disconnect();
		
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