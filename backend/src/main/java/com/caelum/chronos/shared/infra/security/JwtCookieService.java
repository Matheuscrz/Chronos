package com.caelum.chronos.shared.infra.security;

import java.time.Duration;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Serviço responsável por criar e limpar os cookies de autenticação (access
 * token e refresh token) utilizados para manter a sessão do usuário. Ele
 * utiliza as
 * propriedades de segurança para configurar os cookies de acordo com as
 * necessidades do sistema, como tempo de vida, segurança e política de
 * SameSite. Este serviço é utilizado pelo controlador de autenticação para
 * gerenciar os cookies durante as
 * operações de login, refresh e logout, garantindo que os cookies sejam
 * configurados corretamente para proteger a sessão do usuário e facilitar a
 * autenticação em requisições subsequentes.
 */
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