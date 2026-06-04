package com.caelum.chronos.modules.auth.application.service;

import com.caelum.chronos.modules.auth.application.dto.request.LoginRequest;
import com.caelum.chronos.modules.users.domain.model.User;

public interface AuthService {
    User authenticate(LoginRequest request);
}