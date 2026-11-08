package com.practice.java_refresher.controller;

import com.practice.java_refresher.dto.RegisterRequestDto;
import com.practice.java_refresher.dto.UserProfileDto;
import com.practice.java_refresher.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication & Registration", description = "Endpoints for user registration and account creation")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Public endpoint to register a new user account with credentials and email.")
    public ResponseEntity<UserProfileDto> registerUser(@Valid @RequestBody RegisterRequestDto request) {
        UserProfileDto created = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}

