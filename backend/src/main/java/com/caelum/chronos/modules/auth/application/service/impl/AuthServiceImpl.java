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

    @Override
    @Transactional
    public AuthResponse authenticate(LoginRequest request, String ipAddress, String userAgent) {
        String login = sanitize(request.login());

        User user = userRepository.findByUsername(login)
                .or(() -> userRepository.findByEmail(login))
                .orElseThrow(InvalidCredentialsException::new);

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        TokenPair tokens = jwtService.generateTokenPair(user);
        
        createSession(user, tokens, ipAddress, userAgent);

        return new AuthResponse(user, tokens);
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

        return new AuthResponse(user, tokens);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;

        try {
            var jwt = jwtService.decode(refreshToken);
            String jti = jwt.getId();
            if (jti != null) {
                sessionService.revoke(jti);
                blacklistService.blacklist(jti, jwtService.getUserId(jwt), "Logout", jwt.getExpiresAt());
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