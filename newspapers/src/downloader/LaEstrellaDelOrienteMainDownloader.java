package downloader;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.Settings;

public class LaEstrellaDelOrienteMainDownloader {

	private static final String geckoPath = "/usr/bin/geckodriver";
	//private static final String geckoPath = "C:\\Users\\Diego Gonzalez\\Desktop\\Workspace\\Git\\newspapers\\newspapers\\lib\\geckodriver.exe";
	private static String laEstrellaUrl = "http://www.leo.bo/";

	private static String downloadPath = null;
	private static String urlsFilePath = null;

	private static DateFormat dateYear = new SimpleDateFormat("yyyy");
	private static DateFormat dateMonth = new SimpleDateFormat("MM");
	private static DateFormat dateDay = new SimpleDateFormat("dd");
	private static DateFormat dateDayOne = new SimpleDateFormat("d");
	private static DateFormat dateMonthOne = new SimpleDateFormat("M");

	private static String currentYear;
	private static String currentMonth;
	private static String currentDay;
	private static String currentMonthOne;
	private static String currentDayOne;

	private static Map<String, String> nameUrlPaper = null;
	private static ArrayList<String> urls = null;

	private static Map<String, String> mainPageUrls = null;

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

			// Current Date
			Date date = new Date();
			// date.setDate(7);
			currentYear = dateYear.format(date);
			currentMonth = dateMonth.format(date);
			currentDay = dateDay.format(date);
			currentDayOne = dateDayOne.format(date);
			currentMonthOne = dateMonthOne.format(date);

			System.out.println("Current Date: " + dateYear + " - " + dateMonth + " - " + dateDay);

			//laEstrellaUrl = "http://www.leo.bo/" + currentYear + "/" + currentMonth + "/" + currentDay + "/edicion-"+ currentDayOne + "-" + currentMonthOne + "-" + currentYear + "/";

		} else {

			System.err.println("Need params: java -jar xxxx DOWNLOADPATH");

			return;

		}

		mainPageUrls = new HashMap<String, String>();

		// 1. Configure Webriver
		WebDriver driver = setUpFirefox();

		System.out.println("1. Start Login");

		// 2. Go to Cambio page
		driver.get(laEstrellaUrl);

		WebDriverWait wait = new WebDriverWait(driver, 60);

		//Enter the last newspaper
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(@href,'edicion')]")));
		
		String href = driver.findElement(By.xpath("//a[contains(@href,'edicion')]")).getAttribute("href");
				
		System.out.println(href);
		
		driver.get(href);
		
		wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.tagName("iframe"))));

		WebElement iframe = null;

		// Switch Iframe
		List<WebElement> iframes = driver.findElements(By.tagName("iframe"));

		System.out.println(iframes.toString());

		iframe = iframes.get(0);

		driver.switchTo().frame(iframe);

		int count = 1;

		while (count < 80) {

			// Recuperamos todos los elementos que haya visibles
			List<WebElement> elements = driver.findElements(By.className("page-image"));

			for (WebElement e : elements) {

				// Get line
				String src = e.getAttribute("src");

				String pageId = StringUtils.substringBetween(src, "items/", "/covers");

				String pageNumber = e.getAttribute("data-page");

				if (mainPageUrls.containsKey(pageNumber) || pageNumber == null) {
					continue;
				}

				System.out.println(pageId + " --- " + pageNumber);

				mainPageUrls.put(pageNumber, pageId);

				System.out.println(
						"https://cdn.flipsnack.com/collections/items/" + pageId + "/covers/page_1/original?v=1");

				urls.add(pageNumber + "|https://cdn.flipsnack.com/collections/items/" + pageId
						+ "/covers/page_1/original?v=1");

			}

			try {
				TimeUnit.SECONDS.sleep(5);

				((JavascriptExecutor) driver).executeScript("Backbone.Mediator.pub(WidgetEvent.NEXT_PAGE);");

				// iframe.findElement(By.className("flip-next-page")).click();
				// iframe.findElement(By.cssSelector("#docView > div.page-navigation-view >
				// div.flip-next-page.nav-button > svg.nav-button")).click();
			} catch (Exception e) {
				break;
			}

			count++;

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
