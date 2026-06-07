package com.caelum.chronos.modules.auth.application.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.Objects;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.caelum.chronos.modules.auth.application.service.TokenBlacklistService;
import com.caelum.chronos.modules.auth.domain.model.TokenBlacklist;
import com.caelum.chronos.modules.auth.infra.repository.TokenBlacklistRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final String REDIS_PREFIX = "blacklist:";
    private final RedisTemplate<String, Object> redisTemplate;
    private final TokenBlacklistRepository repository;

    @Override
    @Transactional
    @Retryable(retryFor = Exception.class, maxAttempts = 2, backoff = @Backoff(delay = 500))
    public void blacklist(String jti, UUID userId, String reason, Instant expiresAt) {
        TokenBlacklist blacklistEntry = TokenBlacklist.builder()
                .jti(jti)
                .userId(userId)
                .reason(reason)
                .expiresAt(expiresAt)
                .build();

        Objects.requireNonNull(blacklistEntry, "Blacklist entry cannot be null");
        repository.save(blacklistEntry);

        try {
            String key = REDIS_PREFIX + jti;
            long ttl = Duration.between(Instant.now(), expiresAt).toSeconds();
            if (ttl > 0) {
                redisTemplate.opsForValue().set(key, "revoked", Duration.ofSeconds(ttl));
            }
        } catch (Exception e) {
            log.error("Failed to add token to Redis blacklist. JTI: {}", jti, e);
        }
    }

    @Override
    @Retryable(retryFor = Exception.class, maxAttempts = 2, backoff = @Backoff(delay = 500))
    public boolean isBlacklisted(String jti) {
        try {
            String key = REDIS_PREFIX + jti;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.warn("Redis is down, falling back to database for blacklist check. JTI: {}", jti);
            throw e;
        }
    }

    @Recover
    public boolean isBlacklistedRecover(Exception e, String jti) {
        log.info("Recovering from Redis failure for blacklist check. JTI: {}", jti);
        return repository.existsByJti(jti);
    }
}