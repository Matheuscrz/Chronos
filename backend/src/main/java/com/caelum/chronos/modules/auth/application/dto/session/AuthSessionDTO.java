package com.caelum.chronos.modules.auth.application.dto.session;

import java.time.Instant;
import java.util.UUID;

import com.caelum.chronos.modules.auth.domain.model.AuthSession;

public record AuthSessionDTO(
    UUID id,
    UUID userId,
    String jti,
    String deviceName,
    String ipAddress,
    String userAgent,
    Instant issuedAt,
    Instant expiresAt,
    boolean revoked
) {
    public static AuthSessionDTO fromEntity(AuthSession entity) {
        return new AuthSessionDTO(
            entity.getId(),
            entity.getUserId(),
            entity.getJti(),
            entity.getDeviceName(),
            entity.getIpAddress(),
            entity.getUserAgent(),
            entity.getIssuedAt(),
            entity.getExpiresAt(),
            entity.isRevoked()
        );
    }
}