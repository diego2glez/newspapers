package downloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;

public class FinancialTimesDownloader {

	private static final String geckoPath = "/usr/bin/geckodriver";
	private static final String epaperUrl = "https://digital.olivesoftware.com/Olive/ODN/FTEurope/Default.aspx";
	private static final String logoutUrl = "https://id.handelsblatt.com/logout?service=https%3A%2F%2Fepaper.handelsblatt.com%2F";

	private static String downloadPath = null;

	private static String orgCookiesPath = null;
	private static String destCookiesPath = null;

	public static void main(String[] args) throws InterruptedException {

		// 0. Get args
		if (args.length > 0) {

			downloadPath = args[0];

			if (downloadPath == null || downloadPath.isEmpty()) {

				System.err.println("Need params: java -jar xxxx DOWNLOADPATH");

				return;

			}

		} else {

			System.err.println("Need params: java -jar xxxx DOWNLOADPATH");

			return;

		}

		// 1. Configure Webriver

		WebDriver driver = setUpFirefox();

		// 1. Login
		driver.get(epaperUrl);

		// Wait for login textbox
		WebDriverWait wait = new WebDriverWait(driver, 30);

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Sign in here")));
		
		driver.findElement(By.linkText("Sign in here")).click();
	    driver.findElement(By.id("enter-email")).clear();
	    driver.findElement(By.id("enter-email")).sendKeys("ramon@tenao.com");
	    driver.findElement(By.id("enter-email-next")).click();
	    driver.findElement(By.id("enter-password")).clear();
	    driver.findElement(By.id("enter-password")).sendKeys("art59ba3");
	    driver.findElement(By.xpath("//button[@type='submit']")).click();

	    wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Sign in here")));

	    driver.get(epaperUrl);
	    
	    //Lo bajamos de aqui directamente en el script de bash
	    //https://digital.olivesoftware.com/Olive/ODN/FTEurope/get/image.ashx?kind=page&href=FTE%2F2020%2F02%2F29&page=1&res=120

		try {

			Thread.sleep(30000);

		} catch (Exception e) {

		}

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

		// Cerramos sesion
		//driver.get(logoutUrl);

		//WebElement logout = driver.findElement(By.xpath("//a[contains(@data-command,\"loadInto\")]"));

		/*if (logout != null) {

			logout.click();

		}*/

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

		//driver.manage().deleteAllCookies();
		driver.quit();

	}

}