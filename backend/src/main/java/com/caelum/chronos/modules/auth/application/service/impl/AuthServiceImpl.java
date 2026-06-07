package com.caelum.chronos.modules.auth.application.service.impl;

import java.util.UUID;
import java.util.Objects;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.caelum.chronos.modules.auth.application.dto.request.LoginRequest;
import com.caelum.chronos.modules.auth.application.service.AuthService;
import com.caelum.chronos.modules.users.domain.model.User;
import com.caelum.chronos.modules.users.infra.UserRepository;
import com.caelum.chronos.shared.exception.InvalidCredentialsException;
import com.caelum.chronos.shared.infra.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public User authenticate(LoginRequest request) {
        String login = sanitize(request.login());

        User user = userRepository.findByUsername(login)
                .or(() -> userRepository.findByEmail(login))
                .orElseThrow(InvalidCredentialsException::new);

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }

    @Override
    public User refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidCredentialsException();
        }

        var jwt = jwtService.decode(refreshToken);
        if (!jwtService.isRefreshToken(jwt)) {
            throw new InvalidCredentialsException();
        }

        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new InvalidCredentialsException();
        }

        UUID userId = Objects.requireNonNull(UUID.fromString(subject));
        return userRepository.findById(userId)
            .orElseThrow(InvalidCredentialsException::new);
    }

    private String sanitize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }
}