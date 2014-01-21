package net.onrc.onos.ofcontroller.bgproute;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RestClient {
	protected final static Logger log = LoggerFactory.getLogger(RestClient.class);

	public static String get(String str) {
		StringBuilder response = new StringBuilder();

		try {

			URL url = new URL(str);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(2 * 1000); //2 seconds
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			if (!conn.getContentType().equals("application/json")){	
				log.warn("The content received from {} is not json", str);
			}		

            String contentType = conn.getContentType();
            String regex = ".*charset=(.+)";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(contentType);

            String charSet;
            if (! m.find()) {
                    log.debug("charset not specified in HTTP header.");
                    charSet = "ISO-8859-1";                // assume ISO-8859-1
            } else {
                    charSet = m.group(1);
            }
            
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), charSet)); 
            
			String line;
			while ((line = br.readLine()) != null) {
				response.append(line);
			}
			
			br.close();
			conn.disconnect();
			
		} catch (MalformedURLException e) {
			log.error("Malformed URL for GET request", e);
		} catch (ConnectTimeoutException e) {
			log.warn("Couldn't connect remote REST server");
		} catch (IOException e) {
			log.warn("Couldn't connect remote REST server");
		}
		
		return response.toString();
	}

	public static void post (String str) {

		try {
			URL url = new URL(str);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");		

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			conn.disconnect();

		} catch (MalformedURLException e) {
			log.error("Malformed URL for GET request", e);
		} catch (IOException e) {
			log.warn("Couldn't connect remote REST server");
		}
	}


	public static void delete (String str) {

		try {
			URL url = new URL(str);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("DELETE");
			conn.setRequestProperty("Accept", "application/json");


			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			conn.disconnect();

		} catch (MalformedURLException e) {
			log.error("Malformed URL for GET request", e);
		} catch (IOException e) {
			log.warn("Couldn't connect remote REST server");
		}
	}
}
