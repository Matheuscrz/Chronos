package com.caelum.chronos.shared.infra.security.dto;

import java.time.Instant;

public record TokenPair(
    String accessToken,
    String refreshToken,
    String accessJti,
    String refreshJti,
    Instant accessExpiresAt,
    Instant refreshExpiresAt
) {}