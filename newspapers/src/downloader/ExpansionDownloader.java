package downloader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;

public class ExpansionDownloader {

	private static final String geckoPath = "/usr/bin/geckodriver";
	// private static final String geckoPath =
	// "D:\\Workstation\\LIBRARY\\geckodriver\\geckodriver.exe";
	private static final String loginUrl = "https://seguro.orbyt.es/registro/registro.html";

	private static String rootXMLUrl = "http://quiosco.expansionpro.orbyt.es/epaper/xml_epaper/Expansi%C3%B3n/%DATE%/init_pub.xml";

	private static String galiciaXMLUrl = "http://quiosco.expansionpro.orbyt.es/epaper/xml_epaper/Expansi%C3%B3n/";

	private static DateFormat dateFormat1 = new SimpleDateFormat("dd_MM_yyyy");

	private static String currentDate1;

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
				System.out.println(e.getMessage());
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

			// Current Date
			Date date = new Date();
			currentDate1 = dateFormat1.format(date);

			rootXMLUrl = rootXMLUrl.replaceFirst("%DATE%", currentDate1);

			System.out.println("rootXMLUrl: " + rootXMLUrl);

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
		driver.get(loginUrl);

		// Wait for login textbox
		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("usuario")));

		driver.findElement(By.id("usuario")).clear();
		driver.findElement(By.id("usuario")).sendKeys("jramongil@hotmail.com");
		driver.findElement(By.id("pass")).clear();
		driver.findElement(By.id("pass")).sendKeys("art59ba3");
		driver.findElement(By.id("botonLogin")).click();

		wait = new WebDriverWait(driver, 15);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dni")));

		// 3. Open actual edition xml info
		driver.get(rootXMLUrl);

		String rootXMLSource = driver.getPageSource();

		String[] lines = rootXMLSource.split("PLANILLO");

		String galiciaXMLUrlTMP = null;

		for (String line : lines) {

			if (line.contains("Galicia.xml")) {

				galiciaXMLUrlTMP = StringUtils.substringBetween(line, "FILE_XML=\"", "\" ");

			}

		}

		galiciaXMLUrlTMP = galiciaXMLUrl + galiciaXMLUrlTMP;

		System.out.println("Galicia XML Url: " + galiciaXMLUrlTMP);

		// 4. Open Galicia XML info
		driver.get(galiciaXMLUrlTMP);

		String galiciaXMLSource = driver.getPageSource();

		lines = galiciaXMLSource.split("PAGINA ");

		ArrayList<String> pdfPages = new ArrayList<String>();

		int count = 0;

		for (String line : lines) {

			if (line.contains("FILE_PDF")) {

				count++;

				galiciaXMLSource = StringUtils.substringBetween(line, "FILE_PDF=\"", "\" ");

				pdfPages.add(galiciaXMLUrl + galiciaXMLSource);

				System.out.println("Page " + count + " : " + galiciaXMLUrl + galiciaXMLSource);

			}

		}

		if (pdfPages.size() != pdfPages.size()) {

			System.out.println(" NO SE HAN RECUPERADO IMAGENES ");

			throw new Exception();

		}

		// 8. Iterate over pages and write to temp file
		Iterator<String> pagesIterator = pdfPages.iterator();

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
		driver.get(loginUrl);

		// Wait for login textbox
		wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("usuario")));

		driver.findElement(By.id("usuario")).clear();
		driver.findElement(By.id("usuario")).sendKeys("jramongil@hotmail.com");
		driver.findElement(By.id("pass")).clear();
		driver.findElement(By.id("pass")).sendKeys("art59ba3");
		driver.findElement(By.id("botonLogin")).click();

		wait = new WebDriverWait(driver, 15);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dni")));

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