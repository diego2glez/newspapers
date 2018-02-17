package downloader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import com.machinepublishers.jbrowserdriver.JBrowserDriver;

public class CMJournalDownloader {

	private static final String geckoPath = "/usr/bin/geckodriver";
	//private static final String geckoPath = "C:\\Users\\Diego Gonzalez\\git\\newspapers\\newspapers\\lib\\browserDrivers\\geckodriver.exe";
	private static final String baseUrl = "http://cofina.pressreader.com/";

	private static String downloadPath = null;
	private static String urlsFilePath = null;

	private static String orgCookiesPath = null;
	private static String destCookiesPath = null;

	private static String accessToken = null;
	private static String issueToken = null;

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

		// 1. Configure Webriver

		WebDriver driver = setUpFirefox();

		File dir = new File(downloadPath);
		if (!dir.exists()) {

			dir.mkdir();

		}

		if (!dir.exists()) {

			System.err.println("No existe el directorio " + downloadPath);

		}

		System.out.println("1. Start Login");

		// 1. Login
		driver.get(baseUrl + "/correio-da-manh%C3%A3");
		driver.findElement(By.cssSelector("span.userphoto")).click();
		driver.findElement(By.id("email")).clear();
		driver.findElement(By.id("email")).sendKeys("jramongil@hotmail.com");
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys("art59ba3");
		driver.findElement(By.id("loginBtn")).click();

		// Wait for login textbox
		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(@href,'2070')]")));

		try {
			Thread.sleep(15000);
		} catch (Exception e) {
		}

		// driver.get(baseUrl + "/correio-da-manh%C3%A3");
		driver.findElement(By.xpath("//a[contains(@href,'2070')]")).click();

		// Get accessToken from html
		String html = driver.getPageSource();

		Pattern pattern = Pattern.compile("\\\"accessToken\":\"(.*)\"");
		Matcher matcher = pattern.matcher(html);
		while (matcher.find()) {

			System.out.println("group 0: " + matcher.group(0));

			accessToken = matcher.group(1);

		}

		System.out.println("AccessToken = " + accessToken);

		// Get getImage.aspx url to extract issue
		WebElement issueElement = driver.findElement(By.xpath("//img[contains(@src,'2070')]"));

		String issueUrl = issueElement.getAttribute("src");

		System.out.println(issueUrl);

		pattern = Pattern.compile("file=([0-9]*)&");

		matcher = pattern.matcher(issueUrl);
		while (matcher.find()) {

			System.out.println("group 0: " + matcher.group(0));

			issueToken = matcher.group(1);

		}

		System.out.println("IssueToken = " + issueToken);

		boolean success = true;

		urls = new ArrayList<String>();

		int page = 1;

		while (success) {

			System.out.println("Query page " + page);

			String url = "http://services.pressreader.com/se2skyservices/print/GetImageByRegion/?accessToken="
					+ accessToken + "&useContentProxy=true&issue=" + issueToken + "&page=" + page
					+ "&paper=A4&scale=false&scaleToLandscape=false";

			try {

				driver.get(url);

				String jsonText = driver.findElement(By.tagName("pre")).getText();

				System.out.println("jsonText : " + jsonText);

				Object obj = new JSONParser().parse(jsonText);

				// typecasting obj to JSONObject
				JSONObject jo = (JSONObject) obj;

				System.out.println(jo.get("Data"));

				Map address = ((Map) jo.get("Data"));

				success = jo.get("Status").equals("success");

				System.out.println("Status = " + success);

				if (success) {
					urls.add((String) address.get("Src"));
				}

				page++;

			} catch (ParseException e) {
				e.printStackTrace();
			}

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

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
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