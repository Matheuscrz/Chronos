package com.caelum.chronos.modules.auth.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;

import com.caelum.chronos.modules.auth.application.dto.request.LoginRequest;
import com.caelum.chronos.modules.auth.application.dto.response.AuthResponse;
import com.caelum.chronos.modules.auth.application.service.AuthSessionService;
import com.caelum.chronos.modules.auth.application.service.TokenBlacklistService;
import com.caelum.chronos.modules.auth.domain.model.AuthSession;
import com.caelum.chronos.modules.users.domain.model.User;
import com.caelum.chronos.modules.users.infra.UserRepository;
import com.caelum.chronos.shared.exception.InvalidCredentialsException;
import com.caelum.chronos.shared.infra.security.JwtService;
import com.caelum.chronos.shared.infra.security.dto.TokenPair;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthSessionService sessionService;
    @Mock
    private TokenBlacklistService blacklistService;

    @InjectMocks
    private AuthServiceImpl service;

    private User user;
    private LoginRequest loginRequest;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = mock(User.class);
        lenient().when(user.getId()).thenReturn(userId);
        lenient().when(user.getPasswordHash()).thenReturn("hashed-password");
        
        loginRequest = new LoginRequest("user", "password");
    }

    @Test
    void authenticateDeveRetornarResponseQuandoCredenciaisValidas() {
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("user")).thenReturn(Optional.empty());
        when(passwordEncoder.matches("password", "hashed-password")).thenReturn(true);
        
        TokenPair tokens = new TokenPair("access", "refresh", "ajti", "rjti", Instant.now(), Instant.now().plusSeconds(3600));
        when(jwtService.generateTokenPair(user)).thenReturn(tokens);

        AuthResponse response = service.authenticate(loginRequest, "127.0.0.1", "agent");

        assertNotNull(response);
        assertEquals(user, response.user());
        assertEquals(tokens, response.tokens());
        verify(sessionService).save(any(AuthSession.class));
    }

    @Test
    void authenticateDeveLancarExcecaoQuandoSenhaInvalida() {
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashed-password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> service.authenticate(loginRequest, "127.0.0.1", "agent"));
    }

    @Test
    void refreshDeveRetornarNovosTokensQuandoRefreshTokenValido() {
        String refreshToken = "valid-refresh-token";
        Jwt jwt = mock(Jwt.class);
        when(jwt.getId()).thenReturn("rjti");
        when(jwt.getExpiresAt()).thenReturn(Instant.now().plusSeconds(3600));
        
        when(jwtService.decode(refreshToken)).thenReturn(jwt);
        when(jwtService.isRefreshToken(jwt)).thenReturn(true);
        when(sessionService.isValid("rjti")).thenReturn(true);
        when(blacklistService.isBlacklisted("rjti")).thenReturn(false);
        
        when(jwtService.getUserId(jwt)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        TokenPair tokens = new TokenPair("new-access", "new-refresh", "najti", "nrjti", Instant.now(), Instant.now().plusSeconds(3600));
        when(jwtService.generateTokenPair(user)).thenReturn(tokens);

        AuthResponse response = service.refresh(refreshToken, "127.0.0.1", "agent");

        assertNotNull(response);
        verify(sessionService).revoke("rjti");
        verify(blacklistService).blacklist(eq("rjti"), eq(userId), anyString(), any());
    }

    @Test
    void logoutDeveInvalidarSessaoEBlacklistarToken() {
        String refreshToken = "valid-refresh-token";
        Jwt jwt = mock(Jwt.class);
        when(jwt.getId()).thenReturn("rjti");
        when(jwt.getExpiresAt()).thenReturn(Instant.now().plusSeconds(3600));
        
        when(jwtService.decode(refreshToken)).thenReturn(jwt);
        when(jwtService.getUserId(jwt)).thenReturn(userId);

        service.logout(refreshToken);

        verify(sessionService).revoke("rjti");
        verify(blacklistService).blacklist(eq("rjti"), eq(userId), eq("Logout"), any());
    }
}