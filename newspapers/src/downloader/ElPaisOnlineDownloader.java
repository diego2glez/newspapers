package downloader;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

public class ElPaisOnlineDownloader {

	private static final String geckoPath = "/usr/bin/geckodriver";
	// private static final String geckoPath =
	// "C:\\Users\\dgonzalezgon\\Desktop\\Workspace Beto\\geckodriver.exe";
	private static final String baseUrl = "http://elpaisonline.com/index.php/edicion-virtual";

	private static String downloadPath = null;
	private static String urlsFilePath = null;

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

		} else {

			System.err.println("Need params: java -jar xxxx DOWNLOADPATH");

			return;

		}

		// 1. Configure Webriver
		WebDriver driver = setUpFirefox();// setUpJBrowser();

		System.out.println("1. Start Login");

		driver.get(baseUrl);

		// Wait for container
		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("itemListPrimary")));

		WebElement itemContainer = driver.findElement(By.id("itemListPrimary"));
		// Get newspaper list counter
		List<WebElement> itemList = itemContainer.findElements(By.className("itemContainer"));

		System.out.println("Childs: " + itemList.size());

		urls = new ArrayList<>();

		int counter = itemList.size();

		for (int i = 0; i < counter; i++) {

			// Reload newspaper list container (losted reference)
			itemContainer = driver.findElement(By.id("itemListPrimary"));
			itemList = itemContainer.findElements(By.className("itemContainer"));

			WebElement newspaper = itemList.get(i);

			String newspaperName = "Impresion" + i;

			WebElement link = null;

			try {
				// Get newspaper name
				newspaperName = newspaper.findElement(By.className("catItemTitle")).findElement(By.tagName("a"))
						.getText();

				newspaperName = newspaperName.substring(0, newspaperName.indexOf(" "));

				System.out.println(newspaperName);

				// Open paper
				newspaper.findElement(By.className("img-responsive")).click();

				// Find container div
				wait = new WebDriverWait(driver, 30);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("itemFullText")));

				// Switch to iframe
				WebElement container = driver.findElement(By.className("itemFullText"));
				WebElement iframe = container.findElement(By.xpath("//iframe"));
				driver.switchTo().frame(iframe);

				// Find link
				link = driver.findElement(By.xpath("//a[contains(@href,'pagina')]"));

			} catch (Exception e) {
				System.err.println("Couldn't get newspaper name");
			}

			if (link != null) {
				String href = link.getAttribute("href");

				urls.add(newspaperName + "|" + href.substring(0, href.lastIndexOf("/")));
			}

			// Go back
			driver.get(baseUrl);

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
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

		return driver;

	}

	private static WebDriver setUpJBrowser() {

		// 0. Creacion de directorio y configuracion del webdriver
		System.out.println("0. Creating directory and configuration");

		Settings settings = new Settings.Builder().javascript(false).build();

		WebDriver driver = new JBrowserDriver(settings);

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