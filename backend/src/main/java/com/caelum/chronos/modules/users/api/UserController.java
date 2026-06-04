package com.caelum.chronos.modules.users.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.caelum.chronos.modules.users.application.dto.response.UserResponse;
import com.caelum.chronos.modules.users.application.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Users", description = "Endpoints relacionados à gestão de usuários, incluindo consulta de perfil e busca por ID")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(description = "Endpoint para consultar o perfil do usuário autenticado. Retorna um UserResponse com os detalhes do usuário autenticado.")
    @ApiResponse(responseCode = "200", description = "Perfil do usuário consultado com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    public ResponseEntity<UserResponse> me(@Parameter(hidden = true) Authentication authentication) {
        return ResponseEntity.ok(userService.findById(UUID.fromString(authentication.getName())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(description = "Endpoint para consultar o perfil de um usuário por ID. Requer o ID do usuário e retorna um UserResponse com os detalhes do usuário encontrado.")
    @ApiResponse(responseCode = "200", description = "Perfil do usuário consultado com sucesso")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado com o ID fornecido")
    public ResponseEntity<UserResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }
}