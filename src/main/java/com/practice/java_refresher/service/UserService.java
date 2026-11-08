package com.practice.java_refresher.service;

import com.practice.java_refresher.dto.UserProfileDto;
import com.practice.java_refresher.security.SecurityUtils;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    public UserProfileDto getCurrentUserProfile() {
        return UserProfileDto.builder()
                .userId(SecurityUtils.getCurrentUserId())
                .username(SecurityUtils.getCurrentUsername())
                .email(SecurityUtils.getCurrentUserEmail())
                .roles(SecurityUtils.getCurrentUserRoles())
                .build();
    }
}

