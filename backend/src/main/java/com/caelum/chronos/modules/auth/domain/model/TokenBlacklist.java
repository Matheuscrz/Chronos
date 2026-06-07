package com.caelum.chronos.modules.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

import com.caelum.chronos.shared.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "token_blacklist")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class TokenBlacklist extends BaseEntity {

    @Column(name = "jti", nullable = false, unique = true, length = 255)
    private String jti;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "reason", length = 100)
    private String reason;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}