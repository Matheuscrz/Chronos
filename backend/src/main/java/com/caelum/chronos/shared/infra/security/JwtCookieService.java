package com.caelum.chronos.shared.infra.security;

import java.time.Duration;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class JwtCookieService {

    public static final String ACCESS_COOKIE = "chronos_access_token";
    public static final String REFRESH_COOKIE = "chronos_refresh_token";

    public ResponseCookie createAccessCookie(String token, SecurityProperties properties) {
        return buildCookie(ACCESS_COOKIE, token, Duration.ofMinutes(properties.jwt().accessTtlMinutes()),
                properties.jwt().cookieSecure(), properties.jwt().cookieSameSite());
    }

    public ResponseCookie createRefreshCookie(String token, SecurityProperties properties) {
        return buildCookie(REFRESH_COOKIE, token, Duration.ofDays(properties.jwt().refreshTtlDays()),
                properties.jwt().cookieSecure(), properties.jwt().cookieSameSite());
    }

    public ResponseCookie clearAccessCookie(SecurityProperties properties) {
        return ResponseCookie.from(ACCESS_COOKIE, "")
                .httpOnly(true)
                .secure(properties.jwt().cookieSecure())
                .sameSite(properties.jwt().cookieSameSite())
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
    }

    public ResponseCookie clearRefreshCookie(SecurityProperties properties) {
        return ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(properties.jwt().cookieSecure())
                .sameSite(properties.jwt().cookieSameSite())
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
    }

    private ResponseCookie buildCookie(String name, String value, Duration ttl, boolean secure, String sameSite) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(ttl)
                .build();
    }
}