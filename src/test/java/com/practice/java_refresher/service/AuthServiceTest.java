package com.practice.java_refresher.service;

import com.practice.java_refresher.dto.RegisterRequestDto;
import com.practice.java_refresher.dto.UserProfileDto;
import com.practice.java_refresher.model.User;
import com.practice.java_refresher.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private RegisterRequestDto requestDto;

    @BeforeEach
    void setUp() {
        requestDto = RegisterRequestDto.builder()
                .username("newbuyer")
                .email("newbuyer@example.com")
                .password("secret123")
                .fullName("New Buyer")
                .build();
    }

    @Test
    void testRegister_Success() {
        when(userRepository.existsByUsername("newbuyer")).thenReturn(false);
        when(userRepository.existsByEmail("newbuyer@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(50L);
            return u;
        });

        UserProfileDto response = authService.register(requestDto);

        assertNotNull(response);
        assertEquals("newbuyer", response.getUsername());
        assertEquals("newbuyer@example.com", response.getEmail());
        assertTrue(response.getRoles().contains("ROLE_USER"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegister_DuplicateUsername() {
        when(userRepository.existsByUsername("newbuyer")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(requestDto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegister_DuplicateEmail() {
        when(userRepository.existsByUsername("newbuyer")).thenReturn(false);
        when(userRepository.existsByEmail("newbuyer@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(requestDto));
        verify(userRepository, never()).save(any());
    }
}

