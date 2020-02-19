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
import java.util.Map.Entry;
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

public class LoginSeleniumFamily {

	private static final String geckoPath = "/usr/bin/geckodriver";
	// private static final String geckoPath = "C:\\Users\\Diego
	// Gonzalez\\git\\newspapers\\newspapers\\lib\\browserDrivers\\geckodriver.exe";
	private static final String baseUrl = "https://issuu.com";
	private static final String spotifyWebUrl = "https://open.spotify.com/browse";

	private static final Map<String, String> users = new HashMap<String, String>();

	public static void main(String[] args) {

		// 1. Configure Webriver
		WebDriver driver = setUpFirefox();

		System.out.println("1. Start Login");

		for(String userName : users.keySet()) {
			
			System.out.println("Usuario: " + userName);
			
			String password = users.get(userName);
			
		}
		
		// 2. Go to Spotify page
		driver.get(spotifyWebUrl);

		
		// 3. Login
		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("has-account")));

		driver.findElement(By.id("has-account")).click();
		
		driver.findElement(By.id("login-username")).clear();
		driver.findElement(By.id("login-username")).sendKeys();
		driver.findElement(By.id("login-password")).clear();
		driver.findElement(By.id("login-password")).sendKeys();
		driver.findElement(By.id("login-button")).click();
		
		
		//4. Go to settings
		wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("player-controls")));
		
		driver.get("https://open.spotify.com/settings/account");

		//5. 
		
		
		
		
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
