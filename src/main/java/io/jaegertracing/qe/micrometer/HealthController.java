package io.jaegertracing.qe.micrometer;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


@RestController
public class HealthController {
    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("/health")
    public String singleSpan() throws InterruptedException {
        return "I am healthy at " + Instant.now();
    }
}
