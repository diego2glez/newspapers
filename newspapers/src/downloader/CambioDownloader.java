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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.Settings;

public class CambioDownloader {

	private static final String geckoPath = "/usr/bin/geckodriver";
	//private static final String geckoPath = "C:\\Users\\dgonzalezgon\\Desktop\\Workspace Beto\\geckodriver.exe";
	private static final String baseUrl = "https://issuu.com";
	private static final String cambioUrl = "https://issuu.com/cambio2020/";

	private static String downloadPath = null;
	private static String urlsFilePath = null;

	private static DateFormat dateFormat1 = new SimpleDateFormat("dd-MM-yy");
	private static DateFormat dateFormat2 = new SimpleDateFormat("dd-MM-yyyy");
	private static String currentDate1;
	private static String currentDate2;

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

			// Current Date
			Date date = new Date();
			currentDate1 = dateFormat1.format(date);
			currentDate2 = dateFormat2.format(date);
			System.out.println("Current Date: " + currentDate1 + ", " + currentDate2);

		} else {

			System.err.println("Need params: java -jar xxxx DOWNLOADPATH");

			return;

		}

		// 1. Configure Webriver
		WebDriver driver = setUpFirefox();

		System.out.println("1. Start Login");
		
		// 2. Go to Cambio page
		driver.get(cambioUrl);

		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("trms-StreamElement-title")));

		// Get newspaper list
		List<WebElement> itemList = driver.findElements(By.className("trms-StreamElement-title"));

		System.out.println("Childs: " + itemList.size());

		nameUrlPaper = new HashMap<String, String>();

		int counter = itemList.size();

		for (int i = 0; i < counter; i++) {

			WebElement newspaper = itemList.get(i);

			System.out.println("#" + i + "  " + newspaper.getAttribute("href"));
			
			if (newspaper.getAttribute("href").contains(currentDate1)
					|| newspaper.getAttribute("href").contains(currentDate2)) {

				String href = newspaper.getAttribute("href");

				System.out.println("Match! - " + href);

				String newspaperName = href.substring(href.lastIndexOf("/")+1, href.length());
				String newspaperUrl = newspaper.getAttribute("href");

				nameUrlPaper.put(newspaperName, newspaperUrl);

			}

		}
		
		// Loop over papers
		Iterator it = nameUrlPaper.entrySet().iterator();
		urls = new ArrayList<String>();
		
		while(it.hasNext()) {
			
			Map.Entry pair = (Map.Entry)it.next();
			
			System.out.println("Entering " + pair.getKey());
			
			//Go to paper
			driver.get((String) pair.getValue());
			
			//
			WebElement og = driver.findElement(By.xpath("//meta[@property=\"og:image\"]"));
			
			String downloadUrl = og.getAttribute("content");
			
			downloadUrl = downloadUrl.substring(0, downloadUrl.lastIndexOf("/")) + "/";
			
			System.out.println("Download url: " + downloadUrl);
			
			String line = pair.getKey() + "|" + downloadUrl;
			
			urls.add(line);

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
		//driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

		return driver;

	}

	// Clean and Close
	private static void clearAndExit(WebDriver driver) {

		driver.manage().deleteAllCookies();
		driver.quit();

	}

}