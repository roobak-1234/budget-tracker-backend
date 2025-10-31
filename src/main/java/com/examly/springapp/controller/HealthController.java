package com.examly.springapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    
    @GetMapping("/")
    public String health() {
        return "Budget Tracker API is running";
    }
    
    @GetMapping("/health")
    public String healthCheck() {
        return "OK";
    }
}