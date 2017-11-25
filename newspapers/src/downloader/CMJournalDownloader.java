package downloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CMJournalDownloader {

	private static final String downloadPath = "/home/vnc/Escritorio/Periodicos/PT-Correiodamanha/";

	public static void main(String[] args) {

		// 0. Creacion de directorio y configuracion del webdriver
		System.out.println("0. Creating directory and configuration");
		
		File dir = new File(downloadPath);
		if(!dir.exists()) {

			dir.mkdir();
			
		}

		if (!dir.exists()) {

			System.err.println("No existe el directorio " + downloadPath);

		}

		System.setProperty("webdriver.gecko.driver", "/usr/bin/geckodriver");

		DesiredCapabilities dc = DesiredCapabilities.firefox();

		dc.setAcceptInsecureCerts(true);
		dc.setJavascriptEnabled(true);
		dc.setCapability("marionette", true);

		WebDriver driver = new FirefoxDriver(dc);

		System.out.println("1. Start Login");

		// 1. Login
		driver.get("https://aminhaconta.xl.pt/LoginNonio?returnUrl=http%3a%2f%2fwww.cmjornal.pt%2fepaper");

		driver.findElement(By.id("email")).clear();
		driver.findElement(By.id("email")).sendKeys("jramongil@hotmail.com");
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys("art59ba3");
		driver.findElement(By.id("loginBtn")).click();

		WebDriverWait wait = new WebDriverWait(driver, 10);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("date_epaper")));

		// 2. Go to ViewPaper site
		System.out.println("2. GoTo ViewPaper");

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		String viewPaperDate = dateFormat.format(date);

		driver.get("http://www.cmjornal.pt/epaper/viewepaper/?isFlash=False&date=" + viewPaperDate);

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// 3. Get PDF Id
		WebElement titleEl = driver.findElement(By.xpath("//meta[@property='og:url']"));
		String titleContent = titleEl.getAttribute("content");

		String id = titleContent.split("docid=")[1];

		System.out.println("3. PDF ID: " + id);

		dateFormat = new SimpleDateFormat("yyyyMMdd");

		String date2 = dateFormat.format(date);

		String urlDownload = "https://docs.epaperflip.com/Cofinamedia/Correio-da-Manha/" + id + "/" + date2 + ".pdf";

		System.out.println("4. URLDownload: " + urlDownload);

		URL website;

		try {
			website = new URL(urlDownload);

			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			FileOutputStream fos = new FileOutputStream(downloadPath + "correiodamanha" + date2 + ".pdf");
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		driver.quit();

		System.out.println("DONE");

	}

}