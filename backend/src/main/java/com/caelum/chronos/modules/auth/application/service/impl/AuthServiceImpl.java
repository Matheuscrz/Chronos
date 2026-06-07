package com.caelum.chronos.modules.auth.application.service.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.caelum.chronos.modules.auth.application.dto.request.LoginRequest;
import com.caelum.chronos.modules.auth.application.dto.response.AuthResponse;
import com.caelum.chronos.modules.auth.application.service.AuthService;
import com.caelum.chronos.modules.auth.application.service.AuthSessionService;
import com.caelum.chronos.modules.auth.application.service.TokenBlacklistService;
import com.caelum.chronos.modules.auth.domain.model.AuthSession;
import com.caelum.chronos.modules.users.domain.model.User;
import com.caelum.chronos.modules.users.infra.UserRepository;
import com.caelum.chronos.shared.exception.InvalidCredentialsException;
import com.caelum.chronos.shared.infra.security.audit.SecurityAuditService;
import com.caelum.chronos.shared.infra.security.audit.SecurityAuditService.SecurityEventType;
import com.caelum.chronos.shared.infra.security.JwtService;
import com.caelum.chronos.shared.infra.security.dto.TokenPair;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthSessionService sessionService;
    private final TokenBlacklistService blacklistService;
    private final SecurityAuditService auditService;

    @Override
    @Transactional
    public AuthResponse authenticate(LoginRequest request, String ipAddress, String userAgent) {
        String login = sanitize(request.login());

        try {
            User user = userRepository.findByUsername(login)
                    .or(() -> userRepository.findByEmail(login))
                    .orElseThrow(InvalidCredentialsException::new);

            if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
                auditService.log(SecurityEventType.LOGIN_FAILURE, null, login, ipAddress, userAgent, "FAILURE", "Senha incorreta");
                throw new InvalidCredentialsException();
            }

            TokenPair tokens = jwtService.generateTokenPair(user);
            
            createSession(user, tokens, ipAddress, userAgent);

            auditService.log(SecurityEventType.LOGIN_SUCCESS, user.getId(), user.getUsername(), ipAddress, userAgent, "SUCCESS", "Autenticação realizada com sucesso");

            return new AuthResponse(user, tokens);
        } catch (InvalidCredentialsException e) {
            if (!login.contains("@")) { // Log generic failure if user not found to prevent enumeration, or log specifics if needed
                auditService.log(SecurityEventType.LOGIN_FAILURE, null, login, ipAddress, userAgent, "FAILURE", "Usuário ou senha inválidos");
            }
            throw e;
        }
    }

    @Override
    @Transactional
    public AuthResponse refresh(String refreshToken, String ipAddress, String userAgent) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidCredentialsException();
        }

        var jwt = jwtService.decode(refreshToken);
        String jti = jwt.getId();

        if (jti == null || !jwtService.isRefreshToken(jwt) || !sessionService.isValid(jti) || blacklistService.isBlacklisted(jti)) {
            auditService.log(SecurityEventType.REFRESH_TOKEN, null, null, ipAddress, userAgent, "FAILURE", "Token de refresh inválido ou revogado: " + jti);
            throw new InvalidCredentialsException();
        }

        UUID userId = Objects.requireNonNull(jwtService.getUserId(jwt), "User ID cannot be null");
        User user = userRepository.findById(userId)
            .orElseThrow(InvalidCredentialsException::new);

        // Invalida a sessão antiga
        sessionService.revoke(jti);
        blacklistService.blacklist(jti, userId, "Refreshed", jwt.getExpiresAt());

        // Cria nova sessão
        TokenPair tokens = jwtService.generateTokenPair(user);
        createSession(user, tokens, ipAddress, userAgent);

        auditService.log(SecurityEventType.REFRESH_TOKEN, user.getId(), user.getUsername(), ipAddress, userAgent, "SUCCESS", "Token de refresh atualizado: " + jti);

        return new AuthResponse(user, tokens);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;

        try {
            var jwt = jwtService.decode(refreshToken);
            String jti = jwt.getId();
            UUID userId = jwtService.getUserId(jwt);
            if (jti != null) {
                sessionService.revoke(jti);
                blacklistService.blacklist(jti, userId, "Logout", jwt.getExpiresAt());
                auditService.log(SecurityEventType.LOGOUT, userId, null, null, null, "SUCCESS", "Logout realizado: " + jti);
            }
        } catch (Exception e) {
            log.warn("Failed to process logout for token: {}", e.getMessage());
        }
    }

    private void createSession(User user, TokenPair tokens, String ipAddress, String userAgent) {
        AuthSession session = AuthSession.builder()
                .userId(user.getId())
                .jti(tokens.refreshJti())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .issuedAt(Instant.now())
                .expiresAt(tokens.refreshExpiresAt())
                .build();
        
        sessionService.save(session);
    }

    private String sanitize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }
}