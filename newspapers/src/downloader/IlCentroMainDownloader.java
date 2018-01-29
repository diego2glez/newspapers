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

public class IlCentroMainDownloader {


	//private static final String geckoPath = "/usr/bin/geckodriver";
	private static final String geckoPath = "C:\\Users\\dgonzalezgon\\Desktop\\Workspace Beto\\geckodriver.exe";
	private static final String baseUrl = "http://digital.ilcentro.it/ilcentro/index.jsp";

	private static String downloadPath = null;
	private static String urlsFilePath = null;

	private static String orgCookiesPath = null;
	private static String destCookiesPath = null;
	
	private static DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");
	private static DateFormat dateFormat2 = new SimpleDateFormat("yyyy");

	private static String currentDate1;
	private static String currentYear;
	
	public static void main(String[] args) {

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
			currentYear = dateFormat2.format(date);

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
		driver.get(baseUrl);

		// Wait for login textbox
		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(@name,\"myprofile\")]")));

		driver.findElement(By.xpath("//a[contains(@name,\"myprofile\")]")).click();
		
		driver.switchTo().frame("iframe_login");
		
		driver.findElement(By.id("input_username")).clear();
		driver.findElement(By.id("input_username")).sendKeys("jramongil@hotmail.com");
		driver.findElement(By.id("input_password")).clear();
		driver.findElement(By.id("input_password")).sendKeys("art59ba3");
		driver.findElement(By.xpath("//p[contains(@rel,\"login_form\")]")).click();
		
		//wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(@name,\"myprofile\")]")));
		
		try {
			Thread.sleep(10000);			
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		driver.get("http://digital.ilcentro.it/ilcentro/books/pescara/"+currentYear+"/"+currentDate1+"pescara/index.html");
		
		driver.findElement(By.xpath("//p[contains(@id,\"activate\")]//a")).click();
		
		//2. Get Firefox profile path
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
			
		}catch (Exception e) {
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
		
		ProcessBuilder pb = new ProcessBuilder("/bin/bash","-c","rm " + destCookiesPath);
		
		pb.start();
		
		ProcessBuilder pb2 = new ProcessBuilder("/bin/bash", "-c",
				"echo \".dump\" | sqlite3 " + orgCookiesPath
				+ " | sqlite3 " + destCookiesPath);
		
		pb2.start();
		
	}

	// Clean and Close
	private static void clearAndExit(WebDriver driver) {

		driver.manage().deleteAllCookies();
		driver.quit();
		

	}

}