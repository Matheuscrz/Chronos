package com.caelum.chronos.modules.users.application.dto.response;

import java.util.UUID;

import com.caelum.chronos.modules.users.domain.enums.UserRole;

public record UserResponse(
        UUID id,
        String username,
        String fullName,
        String email,
        UserRole role) {

}
