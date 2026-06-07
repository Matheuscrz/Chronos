package com.caelum.chronos.modules.users.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.caelum.chronos.modules.users.application.dto.request.UserRegistrationRequest;
import com.caelum.chronos.modules.users.application.dto.response.UserResponse;
import com.caelum.chronos.modules.users.domain.enums.UserRole;
import com.caelum.chronos.modules.users.domain.model.User;
import com.caelum.chronos.modules.users.infra.UserRepository;
import com.caelum.chronos.shared.exception.NotFoundException;

class UserServiceImplTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        service = new UserServiceImpl(userRepository, passwordEncoder);
    }

    @Test
    void deveCriarUsuarioComSanitizacao() {
        var req = new UserRegistrationRequest("  MatHeus  ", "  Matheus   Silva  ", "  MATHEUS@MAIL.COM ", "123456");

        when(userRepository.existsByUsername("matheus")).thenReturn(false);
        when(userRepository.existsByEmail("matheus@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("hash");

        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return User.builder()
                    .username(u.getUsername())
                    .fullName(u.getFullName())
                    .email(u.getEmail())
                    .passwordHash(u.getPasswordHash())
                    .role(u.getRole())
                    .build();
        });

        UserResponse response = service.createUser(req);

        assertThat(response.username()).isEqualTo("matheus");
        assertThat(response.fullName()).isEqualTo("Matheus Silva");
        assertThat(response.email()).isEqualTo("matheus@mail.com");
        assertThat(response.role()).isEqualTo(UserRole.CLIENTE);
    }

    @Test
    void deveFalharSeUsernameExistir() {
        var req = new UserRegistrationRequest("matheus", "Matheus Silva", "m@a.com", "123456");
        when(userRepository.existsByUsername("matheus")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Já existe uma conta com este nome de usuário!");
    }

    @Test
    void deveFalharSeEmailExistir() {
        var req = new UserRegistrationRequest("matheus", "Matheus Silva", "m@a.com", "123456");
        when(userRepository.existsByUsername("matheus")).thenReturn(false);
        when(userRepository.existsByEmail("m@a.com")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Já existe um usuário com este e-mail!");
    }

    @Test
    void deveBuscarUsuarioPorId() {
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .username("matheus")
                .fullName("Matheus Silva")
                .email("m@a.com")
                .role(UserRole.CLIENTE)
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserResponse response = service.findById(id);

        assertThat(response.username()).isEqualTo("matheus");
        assertThat(response.role()).isEqualTo(UserRole.CLIENTE);
    }

    @Test
    void deveFalharQuandoUsuarioNaoExistir() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Usuário não encontrado!");
    }
}