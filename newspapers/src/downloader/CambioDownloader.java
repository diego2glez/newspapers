package downloader;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import util.JBrowserDriverUtil;

public class CambioDownloader {

	// private static final String geckoPath = "/usr/bin/geckodriver";
	private static final String geckoPath = "C:\\Users\\dgonzalezgon\\Desktop\\Workspace Beto\\geckodriver.exe";
	private static final String baseUrl = "https://issuu.com";
	private static final String cambioUrl = "https://issuu.com/cambio2020/";

	private static String downloadPath = null;
	private static String urlsFilePath = null;

	private static DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy");
	private static String currentDate;

	private static Map<String, String> urls = null;

	public static void main(String[] args) {

		// 0. Get args downloadpath
		if (args.length > 0) {

			downloadPath = args[0];

			if (downloadPath == null || downloadPath.isEmpty()) {

				System.err.println("Need params: java -jar xxxx DOWNLOADPATH");

				return;

			}
			
			if(downloadPath.substring(downloadPath.length() - 1) != "\\") {
				
				downloadPath = downloadPath + "\\";
				
			}

			urlsFilePath = downloadPath + "URLs.txt";

			// Current Date
			Date date = new Date();
			currentDate = dateFormat.format(date);
			System.out.println("Current Date: " + currentDate);

		} else {

			System.err.println("Need params: java -jar xxxx DOWNLOADPATH");

			return;

		}

		// 1. Configure Webriver
		WebDriver driver = setUpFirefox();

		System.out.println("1. Start Login");

		driver.get(baseUrl);

		// Wait for LogIn button
		WebDriverWait wait = new WebDriverWait(driver, 30);
		
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@data-track=\"signin\"]")));

		driver.findElement(By.xpath("//a[@data-track=\"signin\"]")).click();

		// Wait for username TextBox
		wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-username")));

		driver.findElement(By.id("login-username")).sendKeys("jourrapide8");
		driver.findElement(By.id("login-password")).sendKeys("12345678");

		driver.findElement(By.id("login-button")).click();

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@data-track=\"upload\"]")));
		
		// 2. Go to Cambio page
		driver.get(cambioUrl);

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("cover")));

		// Get newspaper list
		List<WebElement> itemList = driver.findElements(By.className("cover"));

		System.out.println("Childs: " + itemList.size());

		urls = new HashMap<String, String>();

		int counter = itemList.size();

		for (int i = 0; i < counter; i++) {

			WebElement newspaper = itemList.get(i);

			if (newspaper.getAttribute("href").contains(currentDate)) {

				String href = newspaper.getAttribute("href");

				System.out.println("Match! - " + href);

				String newspaperName = href.substring(href.lastIndexOf("/"), href.length());
				String newspaperUrl = newspaper.getAttribute("href");
				
				urls.put(newspaperName, newspaperUrl);

			}

		}
		
		//

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
		//driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

		return driver;

	}

	// Clean and Close
	private static void clearAndExit(WebDriver driver) {

		driver.manage().deleteAllCookies();
		driver.quit();

	}

}