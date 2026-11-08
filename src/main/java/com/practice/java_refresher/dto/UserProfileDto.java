package com.practice.java_refresher.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {

    private String userId;
    private String username;
    private String email;
    private List<String> roles;
}

