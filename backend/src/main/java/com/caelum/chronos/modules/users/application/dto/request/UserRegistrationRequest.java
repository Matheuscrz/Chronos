package com.caelum.chronos.modules.users.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationRequest(
        @Valid @NotBlank(message = "Nome de usuário é obrigatório") @Size(min = 3, max = 100, message = "Nome de usuário deve ter entre 3 e 100 caracteres") String username,
        @Valid @NotBlank(message = "Nome completo é obrigatório") @Size(min = 3, max = 255, message = "Nome completo deve ter entre 3 e 255 caracteres") String fullName,
        @Valid @NotBlank(message = "Email é obrigatório") @Size(max = 255, message = "Email deve ter no máximo 255 caracteres") String email,
        @Valid @NotBlank(message = "Senha é obrigatória") @Size(min = 6, max = 255, message = "Senha deve ter entre 6 e 255 caracteres") String password) {
}
