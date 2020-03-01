package downloader;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.Settings;

public class LaPatriaDownloader {

	private static final String geckoPath = "/usr/bin/geckodriver";
	//private static final String geckoPath = "C:\\Users\\Diego Gonzalez\\git\\newspapers\\newspapers\\lib\\browserDrivers\\geckodriver.exe";
	private static final String laPatriaUrl = "https://lapatriaenlinea.com/";

	private static String downloadPath = null;
	private static String urlsFilePath = null;

	private static DateFormat dateFormat1 = new SimpleDateFormat("dd-MM-yyyy");

	private static String currentDate1;

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
			currentDate1 = dateFormat1.format(date);
			System.out.println("Current Date: " + currentDate1);

		} else {

			System.err.println("Need params: java -jar xxxx DOWNLOADPATH");

			return;

		}

		// 1. Configure Webriver
		WebDriver driver = setUpFirefox();

		System.out.println("1. Start Login");

		// 2. Go to LaPatria page
		driver.get(laPatriaUrl);

		WebDriverWait wait = new WebDriverWait(driver, 30);

		driver.findElement(By.xpath("//div[@onclick=\"getElementById('bloque_pdf').style.display='block';\"]")).click();

		driver.findElement(By.id("email")).clear();
		driver.findElement(By.id("email")).sendKeys("jramongil@hotmail.com");
		driver.findElement(By.id("contrasena")).clear();
		driver.findElement(By.id("contrasena")).sendKeys("lapatria17");
		driver.findElement(By.id("button")).click();

		// 3. Get download links

		List<WebElement> links = driver.findElements(By.className("boton_pdf"));

		Iterator it = links.iterator();

		while (it.hasNext()) {

			WebElement linkDiv = (WebElement) it.next();

			String link = linkDiv.getAttribute("onclick");

			link = StringUtils.substringBetween(link, "file=", ".pdf");

			System.out.println("http://lapatriaenlinea.com/download.php?file=" + link + ".pdf");

			urls.add(link + "|http://lapatriaenlinea.com/download.php?file=" + link + ".pdf");

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
