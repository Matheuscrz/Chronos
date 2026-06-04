package com.caelum.chronos.modules.auth.api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.*;

import com.caelum.chronos.modules.auth.application.dto.request.LoginRequest;
import com.caelum.chronos.modules.auth.application.service.AuthService;
import com.caelum.chronos.modules.users.application.dto.request.UserRegistrationRequest;
import com.caelum.chronos.modules.users.application.dto.response.UserResponse;
import com.caelum.chronos.modules.users.application.service.UserService;
import com.caelum.chronos.modules.users.domain.model.User;
import com.caelum.chronos.shared.infra.security.JwtCookieService;
import com.caelum.chronos.shared.infra.security.JwtService;
import com.caelum.chronos.shared.infra.security.SecurityProperties;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador responsável por gerenciar as operações de autenticação, incluindo
 * registro, login, refresh de tokens e logout. Ele utiliza os serviços de
 * autenticação
 * e de usuários para realizar as operações necessárias e manipula os cookies de
 * autenticação para manter a sessão do usuário. O controlador expõe endpoints
 * REST para cada uma dessas operações, garantindo que apenas usuários
 * autenticados possam acessar os recursos protegidos.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints relacionados à autenticação de usuários")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final JwtService jwtService;
    private final JwtCookieService cookieService;
    private final SecurityProperties securityProperties;

    @PostMapping("/register")
    @Operation(summary = "Registra um novo usuário", description = "Cria um novo usuário no sistema com base nos dados fornecidos.")
    @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Requisição inválida, como dados de registro incompletos ou email já em uso")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid UserRegistrationRequest request) {
        return ResponseEntity.status(201).body(userService.createUser(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica um usuário", description = "Realiza o login do usuário e retorna os tokens de acesso e refresh em cookies.")
    @ApiResponse(responseCode = "200", description = "Login bem-sucedido")
    @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    public ResponseEntity<UserResponse> login(@RequestBody @Valid LoginRequest request) {
        User user = authService.authenticate(request);

        String accessCookie = cookieService.createAccessCookie(jwtService.generateAccessToken(user), securityProperties)
                .toString();
        String refreshCookie = cookieService
                .createRefreshCookie(jwtService.generateRefreshToken(user), securityProperties).toString();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie)
                .header(HttpHeaders.SET_COOKIE, refreshCookie)
                .body(toResponse(user));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Atualiza o token de acesso", description = "Usa o refresh token (enviado via cookie) para gerar um novo access token.")
    @ApiResponse(responseCode = "200", description = "Sessão atualizada com sucesso")
    @ApiResponse(responseCode = "401", description = "Token de refresh inválido ou expirado")
    public ResponseEntity<UserResponse> refresh(
            @CookieValue(name = JwtCookieService.REFRESH_COOKIE) String refreshToken) {
        User user = authService.refresh(refreshToken);

        String accessCookie = cookieService.createAccessCookie(jwtService.generateAccessToken(user), securityProperties)
                .toString();
        String refreshCookie = cookieService
                .createRefreshCookie(jwtService.generateRefreshToken(user), securityProperties).toString();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie)
                .header(HttpHeaders.SET_COOKIE, refreshCookie)
                .body(toResponse(user));
    }

    @PostMapping("/logout")
    @Operation(summary = "Realiza o logout do usuário", description = "Invalida a sessão do usuário limpando os cookies de autenticação.")
    @ApiResponse(responseCode = "204", description = "Logout bem-sucedido, cookies de autenticação limpos")
    public ResponseEntity<Void> logout() {
        String accessCookie = cookieService.clearAccessCookie(securityProperties).toString();
        String refreshCookie = cookieService.clearRefreshCookie(securityProperties).toString();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, accessCookie)
                .header(HttpHeaders.SET_COOKIE, refreshCookie)
                .build();
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