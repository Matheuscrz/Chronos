package com.caelum.chronos.modules.users.application.dto.response;

import java.util.UUID;

import com.caelum.chronos.modules.users.domain.enums.UserRole;

import lombok.Builder;

/**
 * DTO de resposta para informações de usuário, contendo o ID, nome de usuário,
 * nome completo, email e papel do usuário. Este DTO é utilizado para retornar
 * os dados de um usuário no sistema, permitindo que os clientes visualizem as
 * informações relevantes sobre os usuários, como seu nome de usuário, nome
 * completo, email e papel (ADMIN, TECNICO, CLIENTE). O campo id é um UUID,
 * garantindo a unicidade de cada usuário no sistema e facilitando a
 * identificação e o gerenciamento dos usuários.
 */
@Builder
public record UserResponse(
        UUID id,
        String username,
        String fullName,
        String email,
        UserRole role) {
}
