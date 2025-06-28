package io.nwdaf.analytics.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PrometheusClient {

    public static String queryMetric(String serverIp, String metricName) {
        String uri =  serverIp + "/api/v1/query?query=" + metricName;
        StringBuilder response = new StringBuilder();

        try {
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }
        } catch (Exception e) {
            System.err.println("Error querying Prometheus: " + e.getMessage());
        }

        return response.toString();
    }
}
