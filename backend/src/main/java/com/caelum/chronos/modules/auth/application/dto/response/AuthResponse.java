package com.caelum.chronos.modules.auth.application.dto.response;

import com.caelum.chronos.modules.users.domain.model.User;
import com.caelum.chronos.shared.infra.security.dto.TokenPair;

public record AuthResponse(
    User user,
    TokenPair tokens
) {}