/**
 * Copyright 2018 The Jaeger Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.jaegertracing.qe.micrometer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Category(io.jaegertracing.qe.micrometer.IntegrationTest.class)
public class SimpleMetricsTest {
    private static final Map<String, String> envs = System.getenv();
    private static final String TARGET_URL = envs.getOrDefault("TARGET_URL", "http://localhost:8080/");

    private static final Logger logger = LoggerFactory.getLogger(SimpleMetricsTest.class);
    private static List<String> expectedMetricNames = new ArrayList<>();

    @BeforeClass
    public static void initial() {
        expectedMetricNames.add("jaeger:baggage_restrictions_updates_total{result=\"err\",}");
        expectedMetricNames.add("jaeger:baggage_restrictions_updates_total{result=\"ok\",}");
        expectedMetricNames.add("jaeger:baggage_truncations_total");
        expectedMetricNames.add("jaeger:baggage_updates_total{result=\"err\",}");
        expectedMetricNames.add("jaeger:baggage_updates_total{result=\"ok\",}");
        expectedMetricNames.add("jaeger:finished_spans_total");
        expectedMetricNames.add("jaeger:reporter_spans_total{result=\"dropped\",}");
        expectedMetricNames.add("jaeger:reporter_spans_total{result=\"err\",}");
        expectedMetricNames.add("jaeger:reporter_spans_total{result=\"ok\",}");
        expectedMetricNames.add("jaeger:sampler_queries_total{result=\"err\",}");
        expectedMetricNames.add("jaeger:sampler_queries_total{result=\"ok\",}");
        expectedMetricNames.add("jaeger:sampler_updates_total{result=\"err\",}");
        expectedMetricNames.add("jaeger:sampler_updates_total{result=\"ok\",}");
        expectedMetricNames.add("jaeger:span_context_decoding_errors_total");
        expectedMetricNames.add("jaeger:started_spans_total{sampled=\"n\",}");
        expectedMetricNames.add("jaeger:started_spans_total{sampled=\"y\",}");
        expectedMetricNames.add("jaeger:traces_total{sampled=\"n\",state=\"joined\",}");
        expectedMetricNames.add("jaeger:traces_total{sampled=\"n\",state=\"started\",}");
        expectedMetricNames.add("jaeger:traces_total{sampled=\"y\",state=\"joined\",}");
        expectedMetricNames.add("jaeger:traces_total{sampled=\"y\",state=\"started\",}");

        Collections.sort(expectedMetricNames);
    }


    @Test
    public void testMetricsExist() throws IOException, MalformedURLException {
        Map<String, Double> metricCounts = getMetrics();
        List<String> metricNames = new ArrayList<>(metricCounts.keySet());
        Collections.sort(metricNames);
        assertEquals(expectedMetricNames, metricNames);
    }

    @Test
    public void simpleMetricsCountTest() throws InterruptedException, IOException, MalformedURLException {
        Map<String, Double> metricCounts = getMetrics();
        final Double startStartedSpansTotal = metricCounts.get("jaeger:started_spans_total{sampled=\"y\",}");
        final Double startTracesTotal = metricCounts.get("jaeger:traces_total{sampled=\"y\",state=\"started\",}");
        final Double startFinishedSpansTotal = metricCounts.get("jaeger:finished_spans_total");

        String helloUrlString = TARGET_URL + "/hello";
        URL helloUrl = new URL(helloUrlString);
        final int iterations = 10;
        for (int i = 0; i < iterations; i++) {
            HttpURLConnection connection = (HttpURLConnection) helloUrl.openConnection();
            logger.debug("Got response " + connection.getResponseCode() + " from " + helloUrlString);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = bufferedReader.readLine();
            logger.debug("line: " + line);
            bufferedReader.close();
        }

        Thread.sleep(2000);  //  sleep at least for FLUSH interval;
        metricCounts = getMetrics();

        Double endStartedSpansTotal = metricCounts.get("jaeger:started_spans_total{sampled=\"y\",}");
        Double endTracesTotal = metricCounts.get("jaeger:traces_total{sampled=\"y\",state=\"started\",}");
        Double endFinishedSpansTotal = metricCounts.get("jaeger:finished_spans_total");

        assertTrue(endFinishedSpansTotal >= startFinishedSpansTotal + iterations);
        assertTrue(endStartedSpansTotal >= startStartedSpansTotal + iterations);
        assertTrue(endTracesTotal >= startTracesTotal + iterations);
    }

    private Map<String, Double> getMetrics() throws IOException, MalformedURLException {
        String urlString = TARGET_URL + "/actuator/prometheus";
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        logger.info("Got response " + connection.getResponseCode() + " from " + urlString);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        Map<String, Double> metricCounts = new HashMap<>();
        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith("jaeger")) {
                String metricName = line.substring(0, line.indexOf(" "));
                Double count = Double.valueOf(line.substring(line.indexOf(" ")));
                metricCounts.put(metricName, count);
            }
        }
        bufferedReader.close();

        return metricCounts;
    }
}
