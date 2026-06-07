package com.caelum.chronos.shared.infra.security;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.caelum.chronos.modules.auth.application.service.AuthSessionService;
import com.caelum.chronos.modules.auth.domain.model.AuthSession;
import com.caelum.chronos.modules.users.domain.enums.UserRole;
import com.caelum.chronos.modules.users.domain.model.User;
import com.caelum.chronos.modules.users.infra.UserRepository;
import com.caelum.chronos.shared.infra.security.audit.SecurityAuditService;
import com.caelum.chronos.shared.infra.security.audit.SecurityAuditService.SecurityEventType;
import com.caelum.chronos.shared.infra.security.dto.TokenPair;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final JwtCookieService cookieService;
    private final SecurityProperties securityProperties;
    private final AuthSessionService sessionService;
    private final SecurityAuditService auditService;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        log.info("OAuth2 Login Success. Attributes: {}", oAuth2User.getAttributes());
        
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String preferredUsername = oAuth2User.getAttribute("preferred_username");
        UserRole mappedRole = mapRole(oAuth2User);

        // Tenta encontrar por email ou username para sincronizar
        User user = userRepository.findByEmail(email != null ? email : "none")
                .or(() -> preferredUsername != null ? userRepository.findByUsername(preferredUsername) : Optional.empty())
                .map(existingUser -> syncUser(existingUser, email, name, mappedRole))
                .orElseGet(() -> createOAuth2User(email, name, preferredUsername, oAuth2User.getName(), mappedRole));

        TokenPair tokens = jwtService.generateTokenPair(user);
        
        createSession(user, tokens, request);

        String accessCookie = cookieService.createAccessCookie(tokens.accessToken(), securityProperties).toString();
        String refreshCookie = cookieService.createRefreshCookie(tokens.refreshToken(), securityProperties).toString();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie);

        auditService.log(SecurityEventType.LOGIN_SUCCESS, user.getId(), user.getUsername(), request.getRemoteAddr(), request.getHeader("User-Agent"), "SUCCESS", "Login via OAuth2 (Keycloak) - Sync complete");

        // Redireciona para o frontend
        String targetUrl = securityProperties.cors().allowedOrigins().get(0);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private User syncUser(User user, String email, String name, UserRole role) {
        log.info("Syncing existing local user: {} with role: {}", user.getUsername(), role);
        
        if (name != null && !name.isBlank()) {
            user.setFullName(name);
        }
        
        if (email != null && !email.equalsIgnoreCase(user.getEmail())) {
            user.setEmail(email);
        }

        if (role != null) {
            user.setRole(role);
        }
        
        return userRepository.save(user);
    }

    private User createOAuth2User(String email, String name, String username, String sub, UserRole role) {
        String finalUsername = username != null ? username : (email != null ? email.split("@")[0] : "user_" + sub.substring(0, 8));
        
        if (userRepository.existsByUsername(finalUsername)) {
            finalUsername = finalUsername + "_" + Instant.now().getEpochSecond();
        }

        User user = User.builder()
                .email(email)
                .fullName(name != null ? name : finalUsername)
                .username(finalUsername)
                .role(role != null ? role : UserRole.CLIENTE)
                .build();

        log.info("Provisioning new local user from OAuth2: {} with role: {}", finalUsername, user.getRole());
        return userRepository.save(user);
    }

    @SuppressWarnings("unchecked")
    private UserRole mapRole(OAuth2User oAuth2User) {
        // No Keycloak as roles costumam vir em 'realm_access.roles'
        Map<String, Object> realmAccess = oAuth2User.getAttribute("realm_access");
        if (realmAccess != null) {
            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles != null) {
                // Procura por nossas roles conhecidas (admin, tecnico, cliente)
                if (roles.contains("admin")) return UserRole.ADMIN;
                if (roles.contains("tecnico")) return UserRole.TECNICO;
                if (roles.contains("cliente")) return UserRole.CLIENTE;
            }
        }
        return UserRole.CLIENTE;
    }

    private void createSession(User user, TokenPair tokens, HttpServletRequest request) {
        AuthSession session = AuthSession.builder()
                .userId(user.getId())
                .jti(tokens.refreshJti())
                .ipAddress(request.getRemoteAddr())
                .userAgent(request.getHeader("User-Agent"))
                .issuedAt(Instant.now())
                .expiresAt(tokens.refreshExpiresAt())
                .build();
        
        sessionService.save(session);
    }
}
