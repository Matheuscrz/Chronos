package com.caelum.chronos.modules.auth.application.service;

import java.time.Instant;
import java.util.UUID;

public interface TokenBlacklistService {
    void blacklist(String jti, UUID userId, String reason, Instant expiresAt);
    boolean isBlacklisted(String jti);
}