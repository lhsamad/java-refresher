package com.practice.java_refresher.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@Tag(name = "Public", description = "Publicly accessible service discovery and health information")
public class PublicController {

    @GetMapping("/info")
    @Operation(summary = "Service metadata", description = "Publicly check API service status, version, and time.")
    public ResponseEntity<Map<String, Object>> getServiceInfo() {
        return ResponseEntity.ok(Map.of(
                "service", "ecommerce-service",
                "status", "UP",
                "timestamp", Instant.now().toString(),
                "documentation", "/swagger-ui.html"
        ));
    }
}

