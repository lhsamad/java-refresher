package com.practice.java_refresher.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Maps Keycloak realm roles into Spring Security GrantedAuthority instances (ROLE_USER, ROLE_ADMIN)
 * during OAuth2 / OIDC login.
 */
@Component
public class KeycloakOidcAuthoritiesMapper implements GrantedAuthoritiesMapper {

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<GrantedAuthority> mappedAuthorities = new HashSet<>(authorities);

        for (GrantedAuthority authority : authorities) {
            if (authority instanceof OidcUserAuthority oidcAuth) {
                extractRolesFromClaims(oidcAuth.getIdToken().getClaims(), mappedAuthorities);
                if (oidcAuth.getUserInfo() != null) {
                    extractRolesFromClaims(oidcAuth.getUserInfo().getClaims(), mappedAuthorities);
                }
            } else if (authority instanceof OAuth2UserAuthority oauth2Auth) {
                extractRolesFromClaims(oauth2Auth.getAttributes(), mappedAuthorities);
            }
        }

        return mappedAuthorities;
    }

    private void extractRolesFromClaims(Map<String, Object> claims, Set<GrantedAuthority> mappedAuthorities) {
        if (claims == null || claims.isEmpty()) {
            return;
        }

        // 1. Extract realm_access.roles
        Object realmAccessObj = claims.get("realm_access");
        if (realmAccessObj instanceof Map<?, ?> realmAccess) {
            Object rolesObj = realmAccess.get("roles");
            if (rolesObj instanceof Collection<?> rolesCollection) {
                for (Object roleItem : rolesCollection) {
                    if (roleItem instanceof String roleName) {
                        String prefixedRole = roleName.toUpperCase().startsWith("ROLE_")
                                ? roleName.toUpperCase()
                                : "ROLE_" + roleName.toUpperCase();
                        mappedAuthorities.add(new SimpleGrantedAuthority(prefixedRole));
                    }
                }
            }
        }

        // 2. Extract resource_access.<client>.roles
        Object resourceAccessObj = claims.get("resource_access");
        if (resourceAccessObj instanceof Map<?, ?> resourceAccess) {
            for (Map.Entry<?, ?> entry : resourceAccess.entrySet()) {
                if (entry.getValue() instanceof Map<?, ?> clientMap) {
                    Object rolesObj = clientMap.get("roles");
                    if (rolesObj instanceof Collection<?> rolesCollection) {
                        for (Object roleItem : rolesCollection) {
                            if (roleItem instanceof String roleName) {
                                String prefixedRole = roleName.toUpperCase().startsWith("ROLE_")
                                        ? roleName.toUpperCase()
                                        : "ROLE_" + roleName.toUpperCase();
                                mappedAuthorities.add(new SimpleGrantedAuthority(prefixedRole));
                            }
                        }
                    }
                }
            }
        }
    }
}

