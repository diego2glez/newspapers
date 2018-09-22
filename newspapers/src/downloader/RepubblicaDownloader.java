package downloader;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;

public class RepubblicaDownloader {

	private static final String geckoPath = "/usr/bin/geckodriver";
	// private static final String geckoPath = "C:\\Users\\Diego
	// Gonzalez\\git\\newspapers\\newspapers\\lib\\browserDrivers\\geckodriver.exe";
	private static final String loginUrl = "https://login.kataweb.it/registrazione/repubblica.it/login.jsp?ssoOnly=false&backurl=https%3A%2F%2Fwww.repubblica.it%2Fsocial%2Fsites%2Frepubblica%2Fnazionale%2Floader.php%3FmClose%3D2%26backUrl%3Dhttps%253A%2F%2Fquotidiano.repubblica.it%2Fedicola%2Fmanager%253Fservice%253Dlogin.social&origin=null&optbackurl=https%3A%2F%2Fwww.repubblica.it%2Fsocial%2Fsites%2Frepubblica%2Fnazionale%2Floader.php%3FmClose%3D2%26backUrl%3Dhttps%253A%2F%2Fquotidiano.repubblica.it%2Fedicola%2Fmanager%253Fservice%253Dlogin.social";

	private static String downloadPath = null;

	private static String orgCookiesPath = null;
	private static String destCookiesPath = null;

	private static DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	private static DateFormat dateFormat2 = new SimpleDateFormat("ddMMyyyy");
	private static String currentDate;
	private static String currentDate2;

	public static void main(String[] args) {

		// 0. Get args
		if (args.length > 0) {

			downloadPath = args[0];

			if (downloadPath == null || downloadPath.isEmpty()) {

				System.err.println("Need params: java -jar xxxx DOWNLOADPATH");

				return;

			}

			// Current date
			Date date = new Date();
			currentDate = dateFormat.format(date);
			currentDate2 = dateFormat2.format(date);
			System.out.println("Current date: " + currentDate);

		} else {

			System.err.println("Need params: java -jar xxxx DOWNLOADPATH");

			return;

		}

		// 1. Configure Webriver

		WebDriver driver = setUpFirefox();

		System.out.println("1. Start Login");

		// 1. Login
		driver.get(loginUrl);

		// Wait for login textbox
		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("userid")));

		driver.findElement(By.name("userid")).clear();
		driver.findElement(By.name("userid")).sendKeys("jramongil@hotmail.com");
		driver.findElement(By.name("userpw")).clear();
		driver.findElement(By.name("userpw")).sendKeys("art59ba3");
		driver.findElement(By.name("submit")).click();

		// 2. Check if todays paper exists
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("select-1")));

		WebElement version = driver.findElement(By.className("select-1"));

		if (version.getAttribute("data-image").contains(currentDate)) {

			// Click en descarga pdf
			driver.findElement(By.id("extra-content")).click();

			try {

				driver.get("https://quotidiano.repubblica.it/edicola/manager?service=download.pdf&data=" + currentDate2
						+ "&issue=" + currentDate + "&testata=repubblica&sezione=nz_all");

			} catch (Exception e) {

				System.out.println("Cerramos sesion");

			}

			try {

				driver.findElement(By.id("cont-login")).click();

				// LogOut
				driver.findElements(By.id("signin_submit")).get(1).click();

			} catch (Exception e) {

				System.out.println("Matamos sin cerrar sesion");

			}

		} else {

			System.err.println("NO HAY VERSION DE " + currentDate);

		}

		clearAndExit(driver);

		System.out.println("DONE");

	}

	private static WebDriver setUpFirefox() {

		// 0. Creacion de directorio y configuracion del webdriver
		System.out.println("0. Creating directory and configuration");

		System.setProperty("webdriver.gecko.driver", geckoPath);

		FirefoxProfile firefoxProfile = new FirefoxProfile();

		firefoxProfile.setPreference("pdfjs.disabled", true);
		firefoxProfile.setPreference("browser.download.folderList", 2);
		firefoxProfile.setPreference("browser.download.manager.showWhenStarting", false);
		firefoxProfile.setPreference("browser.download.dir", downloadPath);
		firefoxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/pdf");

		DesiredCapabilities dc = DesiredCapabilities.firefox();
		dc.setAcceptInsecureCerts(true);
		dc.setJavascriptEnabled(true);
		dc.setCapability(FirefoxDriver.MARIONETTE, true);
		dc.setCapability(FirefoxDriver.PROFILE, firefoxProfile);

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