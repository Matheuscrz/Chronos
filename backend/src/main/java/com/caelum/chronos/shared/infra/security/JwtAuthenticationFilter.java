package com.caelum.chronos.shared.infra.security;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.caelum.chronos.modules.auth.application.service.TokenBlacklistService;
import com.caelum.chronos.modules.users.domain.enums.UserRole;
import com.caelum.chronos.modules.users.domain.model.User;
import com.caelum.chronos.modules.users.infra.UserRepository;
import com.caelum.chronos.shared.infra.logging.LogContext;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Filtro de autenticação JWT que valida tokens e verifica a blacklist.
 * Suporta tokens via Cookie (fluxo local) e Header Bearer (fluxo externo/Swagger).
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final JwtDecoder localJwtDecoder;
    private final JwtDecoder jwtDecoder;
    private final TokenBlacklistService blacklistService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            @Qualifier("localJwtDecoder") JwtDecoder localJwtDecoder,
            JwtDecoder jwtDecoder,
            TokenBlacklistService blacklistService,
            UserRepository userRepository) {
        this.jwtService = jwtService;
        this.localJwtDecoder = localJwtDecoder;
        this.jwtDecoder = jwtDecoder;
        this.blacklistService = blacklistService;
        this.userRepository = userRepository;
    }

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

        String token = resolveToken(request);

        if (token != null && !token.isBlank()) {
            try {
                Jwt jwt = decodeToken(token);
                String jti = jwt.getId();

                if (jti != null && blacklistService.isBlacklisted(jti)) {
                    throw new JwtException("Token blacklisted");
                }

                UserRole role = mapRole(jwt);
                UUID userId = getOrProvisionUser(jwt, role);

                var auth = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));

                SecurityContextHolder.getContext().setAuthentication(auth);
                LogContext.setUserId(userId.toString());
            } catch (Exception ex) {
                log.debug("JWT authentication failed: {}", ex.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        // 1. Tenta extrair do Header Authorization (Swagger / Postman)
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 2. Tenta extrair do Cookie (Frontend)
        return resolveCookie(request, JwtCookieService.ACCESS_COOKIE);
    }

    private Jwt decodeToken(String token) {
        try {
            // Tenta decodificar como token local (HS256)
            return localJwtDecoder.decode(token);
        } catch (Exception e) {
            // Se falhar, tenta com o JwtDecoder padrão (Keycloak - RS256)
            return jwtDecoder.decode(token);
        }
    }

    private UUID getOrProvisionUser(Jwt jwt, UserRole role) {
        String email = jwt.getClaimAsString("email");
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        String sub = jwt.getSubject();

        Optional<User> userOpt = userRepository.findByEmail(email != null ? email : "none")
                .or(() -> preferredUsername != null ? userRepository.findByUsername(preferredUsername) : Optional.empty());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Atualiza role se houver mudança
            if (role != null && user.getRole() != role) {
                user.setRole(role);
                userRepository.save(user);
            }
            return user.getId();
        }

        // Provisiona usuário se não existir localmente
        String finalUsername = preferredUsername != null ? preferredUsername : (email != null ? email.split("@")[0] : "user_" + sub.substring(0, 8));
        
        User newUser = User.builder()
                .email(email)
                .username(finalUsername)
                .fullName(jwt.getClaimAsString("name") != null ? jwt.getClaimAsString("name") : finalUsername)
                .role(role != null ? role : UserRole.CLIENTE)
                .build();

        newUser = userRepository.save(newUser);
        log.info("Automatically provisioned user from JWT: {} with role: {}", newUser.getUsername(), newUser.getRole());
        return newUser.getId();
    }

    private UserRole mapRole(Jwt jwt) {
        // 1. Tenta do claim 'role' (formato local)
        String roleStr = jwt.getClaimAsString("role");
        if (roleStr != null) {
            try { return UserRole.valueOf(roleStr.toUpperCase()); } catch (Exception e) {}
        }

        // 2. Tenta do claim 'realm_access.roles' (formato Keycloak)
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null) {
            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles != null) {
                if (roles.contains("admin")) return UserRole.ADMIN;
                if (roles.contains("tecnico")) return UserRole.TECNICO;
                if (roles.contains("cliente")) return UserRole.CLIENTE;
            }
        }

        return UserRole.CLIENTE;
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