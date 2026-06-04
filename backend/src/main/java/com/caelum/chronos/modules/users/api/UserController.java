package com.caelum.chronos.modules.users.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.caelum.chronos.modules.users.application.dto.response.UserResponse;
import com.caelum.chronos.modules.users.application.service.UserService;

import lombok.RequiredArgsConstructor;

/**
 * Controlador responsável por gerenciar as operações relacionadas aos usuários,
 * incluindo consulta de perfil e busca por ID. Ele utiliza o serviço de
 * usuários para realizar as operações necessárias e expõe endpoints REST para
 * cada uma dessas operações, garantindo que apenas usuários autenticados possam
 * acessar os recursos protegidos. O endpoint "/me" permite que o usuário
 * autenticado consulte seu próprio perfil, enquanto o endpoint "/{id}" permite
 * que
 * um administrador consulte o perfil de qualquer usuário por ID.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        return ResponseEntity.ok(userService.findById(UUID.fromString(authentication.getName())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }
}