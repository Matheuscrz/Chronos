package com.caelum.chronos.modules.auth.api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.*;

import com.caelum.chronos.modules.auth.application.dto.request.LoginRequest;
import com.caelum.chronos.modules.auth.application.dto.response.AuthResponse;
import com.caelum.chronos.modules.auth.application.service.AuthService;
import com.caelum.chronos.modules.users.application.dto.request.UserRegistrationRequest;
import com.caelum.chronos.modules.users.application.dto.response.UserResponse;
import com.caelum.chronos.modules.users.application.service.UserService;
import com.caelum.chronos.modules.users.domain.model.User;
import com.caelum.chronos.shared.infra.security.JwtCookieService;
import com.caelum.chronos.shared.infra.security.SecurityProperties;
import com.caelum.chronos.shared.infra.security.dto.TokenPair;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador responsável por gerenciar as operações de autenticação.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints relacionados à autenticação de usuários")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final JwtCookieService cookieService;
    private final SecurityProperties securityProperties;

    @PostMapping("/register")
    @Operation(summary = "Registra um novo usuário")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid UserRegistrationRequest request) {
        return ResponseEntity.status(201).body(userService.createUser(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica um usuário")
    public ResponseEntity<UserResponse> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletRequest httpRequest) {
        
        AuthResponse response = authService.authenticate(
                request, 
                httpRequest.getRemoteAddr(), 
                httpRequest.getHeader("User-Agent")
        );

        return setAuthCookiesAndResponse(response.user(), response.tokens());
    }

    @PostMapping("/refresh")
    @Operation(summary = "Atualiza o token de acesso")
    public ResponseEntity<UserResponse> refresh(
            @CookieValue(name = JwtCookieService.REFRESH_COOKIE) String refreshToken,
            HttpServletRequest httpRequest) {
        
        AuthResponse response = authService.refresh(
                refreshToken, 
                httpRequest.getRemoteAddr(), 
                httpRequest.getHeader("User-Agent")
        );

        return setAuthCookiesAndResponse(response.user(), response.tokens());
    }

    @PostMapping("/logout")
    @Operation(summary = "Realiza o logout do usuário")
    public ResponseEntity<Void> logout(
            @CookieValue(name = JwtCookieService.REFRESH_COOKIE, required = false) String refreshToken) {
        
        authService.logout(refreshToken);

        String accessCookie = cookieService.clearAccessCookie(securityProperties).toString();
        String refreshCookie = cookieService.clearRefreshCookie(securityProperties).toString();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, accessCookie)
                .header(HttpHeaders.SET_COOKIE, refreshCookie)
                .build();
    }

    @GetMapping("/oauth2/logout")
    @Operation(summary = "Endpoint para o logout via Keycloak (Front channel logout)")
    public ResponseEntity<Void> oauth2Logout() {
        return ResponseEntity.ok().build();
    }

    private ResponseEntity<UserResponse> setAuthCookiesAndResponse(User user, TokenPair tokens) {
        String accessCookie = cookieService.createAccessCookie(tokens.accessToken(), securityProperties)
                .toString();
        String refreshCookie = cookieService.createRefreshCookie(tokens.refreshToken(), securityProperties)
                .toString();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie)
                .header(HttpHeaders.SET_COOKIE, refreshCookie)
                .body(toResponse(user));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}