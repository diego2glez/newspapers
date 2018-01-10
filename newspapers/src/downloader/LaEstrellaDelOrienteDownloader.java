package downloader;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.Settings;

public class LaEstrellaDelOrienteDownloader {

	private static final String geckoPath = "/usr/bin/geckodriver";
	//private static final String geckoPath = "C:\\Users\\Diego Gonzalez\\git\\newspapers\\newspapers\\lib\\browserDrivers\\geckodriver.exe";
	private static final String laEstrellaUrl = "http://www.laestrelladeloriente.com";

	private static String downloadPath = null;
	private static String urlsFilePath = null;

	private static DateFormat dateFormat1 = new SimpleDateFormat("d-M-yyyy");

	private static String currentDate1;

	private static Map<String, String> nameUrlPaper = null;
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

			// Current Date
			Date date = new Date();
			date.setDate(7);
			currentDate1 = dateFormat1.format(date);
			System.out.println("Current Date: " + currentDate1);

		} else {

			System.err.println("Need params: java -jar xxxx DOWNLOADPATH");

			return;

		}

		// 1. Configure Webriver
		WebDriver driver = setUpFirefox();

		System.out.println("1. Start Login");

		// 2. Go to Cambio page
		driver.get(laEstrellaUrl);

		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Edición Impresa")));

		driver.findElement(By.linkText("Edición Impresa")).click();
		
		//3. Get Edicion Id
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("itemListLeading")));
		
		WebElement edicionLinkElement = driver.findElement(By.xpath("//a[contains(@title, \"Edición "+currentDate1+"\")]"));
				
		driver.get(edicionLinkElement.getAttribute("href"));
		
		//Switch Iframe
		WebElement iframe = driver.findElement(By.xpath("//iframe[contains(@src,\"flipsnack\")]"));
		
		driver.switchTo().frame(iframe);
		
		WebElement edicionIdElement = driver.findElement(By.xpath("//script[contains(@src, \"links.js\")]"));
		
		String edicionIdRaw = edicionIdElement.getAttribute("src");
		
		String edicionId = StringUtils.substringBetween(edicionIdRaw,"items/","/json");
		
		System.out.println(edicionId);
		
		urls.add("Edicion|"+edicionId);
		
		//4. Get Sports Id
		driver.get("http://www.laestrelladeloriente.com/index.php?option=com_k2&view=itemlist&layout=category&task=category&id=19&Itemid=794");

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("itemListLeading")));
		
		WebElement deportesLinkElement = driver.findElement(By.xpath("//a[contains(@title, \"Deportes "+currentDate1+"\")]"));
				
		driver.get(deportesLinkElement.getAttribute("href"));
		
		//Switch Iframe
		iframe = driver.findElement(By.xpath("//iframe[contains(@src,\"flipsnack\")]"));
		
		driver.switchTo().frame(iframe);
		
		WebElement deportesIdElement = driver.findElement(By.xpath("//script[contains(@src, \"links.js\")]"));
		
		String deportesIdRaw = deportesIdElement.getAttribute("src");
		
		String deportesId = StringUtils.substringBetween(deportesIdRaw,"items/","/json");
		
		System.out.println(deportesId);
		
		urls.add("Deportes|"+deportesId);
		
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
