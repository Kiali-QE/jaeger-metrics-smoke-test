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

import io.jaegertracing.Configuration;
import io.jaegertracing.Tracer;
import io.jaegertracing.micrometer.MicrometerMetricsFactory;
import io.jaegertracing.samplers.ConstSampler;
import io.opentracing.util.GlobalTracer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class MicrometerApplication {
    private static final Logger logger = LoggerFactory.getLogger(MicrometerApplication.class);

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    public static void main(String[] args) {
        MicrometerMetricsFactory metricsFactory = new MicrometerMetricsFactory();
        Configuration configuration = new Configuration("jaeger-client-java-tester");
        Configuration.ReporterConfiguration reporterConfiguration = new Configuration.ReporterConfiguration()
                .withLogSpans(true);
        Configuration.SamplerConfiguration samplerConfiguration = new Configuration.SamplerConfiguration()
                .withType(ConstSampler.TYPE)
                .withParam(1.0);

        Tracer tracer = configuration
                .withReporter(reporterConfiguration)
                .withSampler(samplerConfiguration)
                .getTracerBuilder()
                .withMetricsFactory(metricsFactory)
                .build();

        GlobalTracer.register(tracer);

        SpringApplication.run(MicrometerApplication.class, args);
    }
}
