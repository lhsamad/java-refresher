package com.practice.java_refresher.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<Authentication> getAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }

    public static Optional<Jwt> getCurrentJwt() {
        return getAuthentication()
                .map(Authentication::getPrincipal)
                .filter(Jwt.class::isInstance)
                .map(Jwt.class::cast);
    }

    public static Optional<OAuth2User> getCurrentOAuth2User() {
        return getAuthentication()
                .map(Authentication::getPrincipal)
                .filter(OAuth2User.class::isInstance)
                .map(OAuth2User.class::cast);
    }

    public static String getCurrentUserId() {
        return getCurrentJwt()
                .map(Jwt::getSubject)
                .or(() -> getCurrentOAuth2User().map(OAuth2User::getName))
                .orElseGet(() -> getAuthentication()
                        .map(Authentication::getName)
                        .orElse("anonymous"));
    }

    public static String getCurrentUsername() {
        return getCurrentJwt()
                .map(jwt -> {
                    String username = jwt.getClaimAsString("preferred_username");
                    return (username != null && !username.isBlank()) ? username : jwt.getSubject();
                })
                .or(() -> getCurrentOAuth2User().map(user -> {
                    if (user instanceof OidcUser oidcUser && oidcUser.getPreferredUsername() != null) {
                        return oidcUser.getPreferredUsername();
                    }
                    Object pref = user.getAttributes().get("preferred_username");
                    return pref != null ? pref.toString() : user.getName();
                }))
                .orElseGet(() -> getAuthentication()
                        .map(Authentication::getName)
                        .orElse("anonymous"));
    }

    public static String getCurrentUserEmail() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString("email"))
                .or(() -> getCurrentOAuth2User().map(user -> {
                    if (user instanceof OidcUser oidcUser && oidcUser.getEmail() != null) {
                        return oidcUser.getEmail();
                    }
                    Object email = user.getAttributes().get("email");
                    return email != null ? email.toString() : null;
                }))
                .orElse("unknown@example.com");
    }

    public static List<String> getCurrentUserRoles() {
        return getAuthentication()
                .map(auth -> auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    public static boolean hasRole(String role) {
        String targetAuthority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return getCurrentUserRoles().contains(targetAuthority);
    }
}

