package com.example.webcompiler.global.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealCheckController {
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        boolean isHealthy = true;

        if (isHealthy) {
            return new ResponseEntity<>("OK", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Service Unavailable", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
