package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.apache.http.params.HttpParams;
import org.openqa.selenium.WebDriver;

public class JBrowserDriverUtil {

	 public static File downloadFile(WebDriver webDriver, String downloadUrl, String downloadPath) throws IOException {
	        File file = new File(downloadPath + System.currentTimeMillis());
	        file.deleteOnExit();
	        BasicCookieStore cookieStore = getBasicCookieStore(webDriver);

	        // Setup HttpClient and ProxyConfiguration
	        DefaultHttpClient httpClient = new DefaultHttpClient();

	        // Setup Cookie configuration
	        httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
	                org.apache.http.client.params.CookiePolicy.BROWSER_COMPATIBILITY);
	        httpClient.setCookieStore(cookieStore);
	        HttpGet httpGet = new HttpGet(downloadUrl);

	        HttpParams requestParams = httpGet.getParams();
	        requestParams.setParameter(ClientPNames.HANDLE_REDIRECTS, true);

	        httpGet.setParams(requestParams);
	        httpGet.setHeader("User-Agent", "My user agent");
	        httpGet.setHeader("Accept-Language", "de,en-US;q=0.7,en;q=0.3");

	        // gets response as file
	        HttpResponse response = httpClient.execute(httpGet);
	        FileUtils.copyInputStreamToFile(response.getEntity().getContent(), file);
	        response.getEntity().getContent().close();

	        Scanner fileScanner = new Scanner(file);
	        fileScanner.nextLine();

	        String newFileName = downloadPath + System.currentTimeMillis();
	        FileWriter fileStream = new FileWriter(newFileName);
	        BufferedWriter out = new BufferedWriter(fileStream);
	        while (fileScanner.hasNextLine()) {
	            String next = fileScanner.nextLine();
	            if (next.equals("\n")) {
	                out.newLine();
	            } else {
	                out.write(next);
	                out.newLine();
	            }
	        }
	        out.close();

	        return new File(newFileName);
	    }

	public static BasicCookieStore getBasicCookieStore(WebDriver webDriver) {
		BasicCookieStore cookieStore = new BasicCookieStore();

		System.out.println("JBrowserDriver Cookies:");

		webDriver.manage().getCookies().forEach(it -> {
			System.out.println(it.toString());
			BasicClientCookie2 cookie = new BasicClientCookie2(it.getName(), it.getValue());
			cookie.setDomain(it.getDomain());
			cookie.setSecure(it.isSecure());
			cookie.setExpiryDate(it.getExpiry());
			cookie.setPath(it.getPath());
			cookieStore.addCookie(cookie);
		});

		System.out.println("Converted Cookies: \n " + cookieStore.toString());

		return cookieStore;
	}

}
