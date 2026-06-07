package com.caelum.chronos.shared.infra.security;

import java.io.IOException;
import java.time.Instant;
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
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String preferredUsername = oAuth2User.getAttribute("preferred_username");

        if (email == null) {
            log.error("Email not found in OAuth2 attributes");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email não fornecido pelo provedor de identidade");
            return;
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createOAuth2User(email, name, preferredUsername));

        TokenPair tokens = jwtService.generateTokenPair(user);
        
        createSession(user, tokens, request);

        String accessCookie = cookieService.createAccessCookie(tokens.accessToken(), securityProperties).toString();
        String refreshCookie = cookieService.createRefreshCookie(tokens.refreshToken(), securityProperties).toString();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie);

        auditService.log(SecurityEventType.LOGIN_SUCCESS, user.getId(), user.getUsername(), request.getRemoteAddr(), request.getHeader("User-Agent"), "SUCCESS", "Login via OAuth2 (Keycloak)");

        // Redireciona para o frontend (ajustar conforme necessário)
        String targetUrl = securityProperties.cors().allowedOrigins().get(0);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private User createOAuth2User(String email, String name, String username) {
        String finalUsername = username != null ? username : email.split("@")[0];
        
        // Garante que o username é único se já existir um com o mesmo nome
        if (userRepository.existsByUsername(finalUsername)) {
            finalUsername = finalUsername + "_" + Instant.now().getEpochSecond();
        }

        User user = User.builder()
                .email(email)
                .fullName(name)
                .username(finalUsername)
                .role(UserRole.CLIENTE) // Papel padrão
                .build();

        return userRepository.save(user);
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
