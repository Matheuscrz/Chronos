package com.caelum.chronos.modules.auth.application.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.caelum.chronos.modules.auth.application.dto.request.LoginRequest;
import com.caelum.chronos.modules.auth.application.service.AuthService;
import com.caelum.chronos.modules.users.domain.model.User;
import com.caelum.chronos.modules.users.infra.UserRepository;
import com.caelum.chronos.shared.exception.InvalidCredentialsException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    private String sanitize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }
}