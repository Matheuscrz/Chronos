package com.caelum.chronos.shared.infra.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.caelum.chronos.modules.auth.application.service.TokenBlacklistService;
import com.caelum.chronos.shared.infra.logging.LogContext;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Filtro de autenticação JWT que valida tokens e verifica a blacklist.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlacklistService blacklistService;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        return HttpMethod.OPTIONS.name().equals(method)
                || path.equals("/auth")
                || path.startsWith("/auth/")
                || path.equals("/v3/api-docs")
                || path.startsWith("/v3/api-docs/")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/webjars/")
                || path.startsWith("/actuator/health")
                || path.startsWith("/actuator/info");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = resolveCookie(request, JwtCookieService.ACCESS_COOKIE);
            if (token != null && !token.isBlank()) {
                var jwt = jwtService.decode(token);
                String jti = jwt.getId();

                if (jti != null && blacklistService.isBlacklisted(jti)) {
                    throw new JwtException("Token blacklisted");
                }

                String role = jwt.getClaimAsString("role");

                var auth = new UsernamePasswordAuthenticationToken(
                        jwt.getSubject(),
                        null,
                        List.of(new SimpleGrantedAuthority(role)));

                SecurityContextHolder.getContext().setAuthentication(auth);
                LogContext.setUserId(jwt.getSubject());
            }

            filterChain.doFilter(request, response);
        } catch (JwtException ex) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
        }
    }

    private String resolveCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}