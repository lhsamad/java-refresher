package com.practice.java_refresher.service;

import com.practice.java_refresher.dto.RegisterRequestDto;
import com.practice.java_refresher.dto.UserProfileDto;
import com.practice.java_refresher.model.User;
import com.practice.java_refresher.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Set;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserProfileDto register(RegisterRequestDto request) {
        if (userRepository.existsByUsername(request.getUsername().trim())) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' is already taken.");
        }

        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' is already registered.");
        }

        User newUser = User.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName() != null ? request.getFullName().trim() : null)
                .roles(Set.of("ROLE_USER"))
                .build();

        User saved = userRepository.save(newUser);

        return UserProfileDto.builder()
                .userId(saved.getId().toString())
                .username(saved.getUsername())
                .email(saved.getEmail())
                .roles(new ArrayList<>(saved.getRoles()))
                .build();
    }
}

