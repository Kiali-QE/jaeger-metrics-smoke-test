package io.jaegertracing.qe.micrometer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@RestController
public class HelloController {
    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("/hello")
    public String singleSpan() throws InterruptedException {
        return "Hello from /hello at " + Instant.now();
    }
}
