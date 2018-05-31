package io.jaegertracing.qe.micrometer;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Category(io.jaegertracing.qe.micrometer.IntegrationTest.class)
public class SimpleMetricsTest {
    private static final Logger logger = LoggerFactory.getLogger(SimpleMetricsTest.class);

    @Test
    public void check() throws Exception {
        String urlString = " http://localhost:8080/actuator/prometheus";
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        int responseCode = connection.getResponseCode();
        logger.info("Got response " + connection.getResponseCode() + " from " + urlString);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith("jaeger")) {
                System.out.println(line);
            }
        }
        bufferedReader.close();
    }
}
