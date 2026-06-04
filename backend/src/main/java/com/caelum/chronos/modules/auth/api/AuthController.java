package com.caelum.chronos.modules.auth.api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final JwtService jwtService;
    private final JwtCookieService cookieService;
    private final SecurityProperties securityProperties;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid UserRegistrationRequest request) {
        return ResponseEntity.status(201).body(userService.createUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody @Valid LoginRequest request,
            HttpServletResponse response) {
        User user = authService.authenticate(request);
        setAuthCookies(user, response);

        return ResponseEntity.ok(UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookieService.clearAccessCookie(securityProperties).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieService.clearRefreshCookie(securityProperties).toString());
        return ResponseEntity.noContent().build();
    }

    private void setAuthCookies(User user, HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE,
                cookieService.createAccessCookie(jwtService.generateAccessToken(user), securityProperties).toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                cookieService.createRefreshCookie(jwtService.generateRefreshToken(user), securityProperties)
                        .toString());
    }
}