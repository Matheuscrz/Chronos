package com.caelum.chronos.modules.users.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationRequest(
        @NotBlank @Size(min = 3, max = 100) String username,

        @NotBlank @Size(min = 3, max = 255) String fullName,

        @NotBlank @Email @Size(max = 255) String email,

        @NotBlank @Size(min = 6, max = 255) String password) {
}
