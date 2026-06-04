package com.caelum.chronos.modules.users.application.service.impl;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.caelum.chronos.modules.users.application.dto.request.UserRegistrationRequest;
import com.caelum.chronos.modules.users.application.dto.response.UserResponse;
import com.caelum.chronos.modules.users.application.service.UserService;
import com.caelum.chronos.modules.users.domain.model.User;
import com.caelum.chronos.modules.users.domain.enums.UserRole;
import com.caelum.chronos.modules.users.infra.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(UserRegistrationRequest req) {
        String username = sanitizeUsername(req.username());
        String fullName = sanitizeFullName(req.fullName());
        String email = sanitizeEmail(req.email());

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("email already exists");
        }

        String hash = passwordEncoder.encode(req.password());

        User user = User.builder()
                .username(username)
                .fullName(fullName)
                .email(email)
                .passwordHash(hash)
                .role(UserRole.CLIENTE)
                .build();

        user = userRepository.save(user);

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional
    public UserResponse findById(UUID id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        return UserResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .role(u.getRole())
                .build();
    }

    private String sanitizeUsername(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }

    private String sanitizeFullName(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }

    private String sanitizeEmail(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }
}
