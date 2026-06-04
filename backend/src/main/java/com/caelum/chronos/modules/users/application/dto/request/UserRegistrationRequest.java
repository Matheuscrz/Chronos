package com.caelum.chronos.modules.users.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para registro de usuário, contendo os campos necessários para criar um
 * novo usuário no sistema. O campo username é obrigatório e deve ter entre 3 e
 * 100 caracteres, o campo fullName é obrigatório e deve ter entre 3 e 255
 * caracteres, o campo email é obrigatório, deve ser um email válido e ter no
 * máximo 255 caracteres, e o campo password é obrigatório e deve ter entre 6 e
 * 255 caracteres.
 */
public record UserRegistrationRequest(
        @NotBlank @Size(min = 3, max = 100) String username,

        @NotBlank @Size(min = 3, max = 255) String fullName,

        @NotBlank @Email @Size(max = 255) String email,

        @NotBlank @Size(min = 6, max = 255) String password) {
}
