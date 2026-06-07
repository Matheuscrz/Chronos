package com.caelum.chronos.modules.auth.application.service;

import java.util.UUID;
import com.caelum.chronos.modules.auth.domain.model.AuthSession;

public interface AuthSessionService {
    void save(AuthSession session);
    boolean isValid(String jti);
    void revoke(String jti);
    void revokeAllByUserId(UUID userId);
}