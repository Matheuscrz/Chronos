package com.caelum.chronos.modules.auth.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.caelum.chronos.modules.auth.application.dto.request.LoginRequest;
import com.caelum.chronos.modules.auth.application.dto.response.AuthResponse;
import com.caelum.chronos.modules.auth.application.service.AuthService;
import com.caelum.chronos.modules.users.application.service.UserService;
import com.caelum.chronos.modules.users.domain.enums.UserRole;
import com.caelum.chronos.modules.users.domain.model.User;
import com.caelum.chronos.shared.infra.security.JwtCookieService;
import com.caelum.chronos.shared.infra.security.SecurityProperties;
import com.caelum.chronos.shared.infra.security.dto.TokenPair;
import com.caelum.chronos.shared.api.error.GlobalExceptionHandler;

class AuthControllerTest {

    private MockMvc mockMvc;

    private UserService userService;
    private AuthService authService;
    private JwtCookieService cookieService;
    private SecurityProperties securityProperties;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        authService = mock(AuthService.class);
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
                new AuthController(userService, authService, cookieService, securityProperties))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void loginDeveRetornarUsuarioESetarCookies() throws Exception {
        User user = mock(User.class);
        UUID userId = UUID.randomUUID();

        when(user.getId()).thenReturn(userId);
        when(user.getUsername()).thenReturn("matheus");
        when(user.getRole()).thenReturn(UserRole.CLIENTE);

        TokenPair tokens = new TokenPair("access", "refresh", "ajti", "rjti", Instant.now(), Instant.now().plusSeconds(3600));
        AuthResponse authResponse = new AuthResponse(user, tokens);

        when(authService.authenticate(any(LoginRequest.class), any(), any())).thenReturn(authResponse);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"login":"matheus","password":"123456"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(header().stringValues("Set-Cookie",
                        hasItems(
                                containsString(JwtCookieService.ACCESS_COOKIE),
                                containsString(JwtCookieService.REFRESH_COOKIE))));
    }

    @Test
    void refreshDeveRenovarCookies() throws Exception {
        User user = mock(User.class);
        UUID userId = UUID.randomUUID();
        when(user.getId()).thenReturn(userId);
        when(user.getRole()).thenReturn(UserRole.CLIENTE);

        TokenPair tokens = new TokenPair("new-access", "new-refresh", "najti", "nrjti", Instant.now(), Instant.now().plusSeconds(3600));
        AuthResponse authResponse = new AuthResponse(user, tokens);

        when(authService.refresh(eq("refresh-token"), any(), any())).thenReturn(authResponse);

        mockMvc.perform(post("/auth/refresh")
                .cookie(new jakarta.servlet.http.Cookie(JwtCookieService.REFRESH_COOKIE, "refresh-token")))
                .andExpect(status().isOk())
                .andExpect(header().stringValues("Set-Cookie",
                        hasItems(
                                containsString(JwtCookieService.ACCESS_COOKIE),
                                containsString(JwtCookieService.REFRESH_COOKIE))));
    }

    @Test
    void logoutDeveInvalidaSessaoELimparCookies() throws Exception {
        mockMvc.perform(post("/auth/logout")
                .cookie(new jakarta.servlet.http.Cookie(JwtCookieService.REFRESH_COOKIE, "refresh-token")))
                .andExpect(status().isNoContent())
                .andExpect(header().stringValues("Set-Cookie",
                        hasItems(
                                containsString(JwtCookieService.ACCESS_COOKIE),
                                containsString(JwtCookieService.REFRESH_COOKIE))));
        
        verify(authService).logout("refresh-token");
    }
}