package net.onrc.onos.apps.bgproute;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RestClient {
    private final static Logger log = LoggerFactory.getLogger(RestClient.class);

    private RestClient() {
        // Private constructor to prevent instantiation
    }
    
    public static String get(String str) {
        StringBuilder response = new StringBuilder();

        try {
            URL url = new URL(str);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(2 * 1000); // 2 seconds
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                // XXX bad. RestClient API needs to be redesigned
                throw new IOException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            if (!conn.getContentType().equals("application/json")) {
                log.warn("The content received from {} is not json", str);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            br.close();
            conn.disconnect();

        } catch (MalformedURLException e) {
            log.error("Malformed URL for GET request", e);
        } catch (ConnectTimeoutException e) {
            log.warn("Couldn't connect to the remote REST server", e);
        } catch (IOException e) {
            log.warn("Couldn't connect to the remote REST server", e);
        }

        return response.toString();
    }

    public static void post(String str) {

        try {
            URL url = new URL(str);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            if (conn.getResponseCode() != 200) {
                // XXX bad. RestClient API needs to be redesigned
                throw new IOException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            conn.disconnect();

        } catch (MalformedURLException e) {
            log.error("Malformed URL for GET request", e);
        } catch (IOException e) {
            log.warn("Couldn't connect to the remote REST server", e);
        }
    }

    public static void delete(String str) {

        try {
            URL url = new URL(str);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                // XXX bad. RestClient API needs to be redesigned
                throw new IOException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            conn.disconnect();

        } catch (MalformedURLException e) {
            log.error("Malformed URL for GET request", e);
        } catch (IOException e) {
            log.warn("Couldn't connect to the remote REST server", e);
        }
    }
}
