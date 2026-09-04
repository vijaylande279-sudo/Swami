package com.swamisuite.hello;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of(
                "service", "hello-service",
                "message", "Swami Suite platform is alive"
        );
    }
}
