package com.caelum.chronos.modules.auth.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.caelum.chronos.modules.auth.application.dto.request.LoginRequest;
import com.caelum.chronos.modules.auth.application.service.AuthService;
import com.caelum.chronos.modules.users.application.service.UserService;
import com.caelum.chronos.modules.users.domain.enums.UserRole;
import com.caelum.chronos.modules.users.domain.model.User;
import com.caelum.chronos.shared.infra.security.JwtCookieService;
import com.caelum.chronos.shared.infra.security.JwtService;
import com.caelum.chronos.shared.infra.security.SecurityProperties;

class AuthControllerTest {

    private MockMvc mockMvc;

    private UserService userService;
    private AuthService authService;
    private JwtService jwtService;
    private JwtCookieService cookieService;
    private SecurityProperties securityProperties;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        authService = mock(AuthService.class);
        jwtService = mock(JwtService.class);
        cookieService = new JwtCookieService();

        securityProperties = new SecurityProperties(
                false,
                new SecurityProperties.Cors(
                        List.of("http://localhost:3000"),
                        List.of("GET", "POST", "OPTIONS"),
                        List.of("Content-Type"),
                        List.of("X-Correlation-Id"),
                        true),
                new SecurityProperties.Jwt(
                        "test-secret-test-secret-test-secret-test-secret",
                        15,
                        7,
                        false,
                        "Strict",
                        "chronos-backend"),
                new SecurityProperties.PasswordEncoder(16, 32, 1, 65536, 3));

        mockMvc = MockMvcBuilders.standaloneSetup(
                new AuthController(userService, authService, jwtService, cookieService, securityProperties)).build();
    }

    @Test
    void loginDeveRetornarUsuarioESetarCookies() throws Exception {
        User user = mock(User.class);
        UUID userId = UUID.randomUUID();

        when(user.getId()).thenReturn(userId);
        when(user.getUsername()).thenReturn("matheus");
        when(user.getFullName()).thenReturn("Matheus Silva");
        when(user.getEmail()).thenReturn("matheus@caelum.com");
        when(user.getRole()).thenReturn(UserRole.CLIENTE);

        when(authService.authenticate(any(LoginRequest.class))).thenReturn(user);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"login":"matheus","password":"123456"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("matheus"))
                .andExpect(header().stringValues("Set-Cookie",
                        hasItems(
                                containsString(JwtCookieService.ACCESS_COOKIE),
                                containsString(JwtCookieService.REFRESH_COOKIE))));
        verify(authService).authenticate(any(LoginRequest.class));
        verify(jwtService).generateAccessToken(user);
        verify(jwtService).generateRefreshToken(user);
    }

    @Test
    void refreshDeveRenovarCookies() throws Exception {
        User user = mock(User.class);
        UUID userId = UUID.randomUUID();

        when(user.getId()).thenReturn(userId);
        when(user.getUsername()).thenReturn("matheus");
        when(user.getFullName()).thenReturn("Matheus Silva");
        when(user.getEmail()).thenReturn("matheus@caelum.com");
        when(user.getRole()).thenReturn(UserRole.CLIENTE);

        when(authService.refresh("refresh-token")).thenReturn(user);
        when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("new-refresh-token");

        mockMvc.perform(post("/auth/refresh")
                .cookie(new jakarta.servlet.http.Cookie(JwtCookieService.REFRESH_COOKIE, "refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(header().stringValues("Set-Cookie",
                        hasItems(
                                containsString(JwtCookieService.ACCESS_COOKIE),
                                containsString(JwtCookieService.REFRESH_COOKIE))));
        verify(authService).refresh("refresh-token");
        verify(jwtService).generateAccessToken(user);
        verify(jwtService).generateRefreshToken(user);
    }

    @Test
    void refreshSemCookieDeveFalhar() throws Exception {
        mockMvc.perform(post("/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutDeveLimparCookies() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(header().stringValues("Set-Cookie",
                        hasItems(
                                containsString(JwtCookieService.ACCESS_COOKIE),
                                containsString(JwtCookieService.REFRESH_COOKIE))));
    }
}