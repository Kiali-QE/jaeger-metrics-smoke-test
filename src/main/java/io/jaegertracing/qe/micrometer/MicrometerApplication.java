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
        Configuration.ReporterConfiguration reporterConfiguration  = new Configuration.ReporterConfiguration()
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
