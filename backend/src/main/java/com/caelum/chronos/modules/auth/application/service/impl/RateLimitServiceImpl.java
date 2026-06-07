package com.caelum.chronos.modules.auth.application.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.caelum.chronos.modules.auth.application.service.RateLimitService;
import com.caelum.chronos.modules.auth.domain.model.RateLimitBuckets;
import com.caelum.chronos.modules.auth.infra.repository.RateLimitRepository;
import com.caelum.chronos.shared.infra.security.audit.SecurityAuditService;
import com.caelum.chronos.shared.infra.security.audit.SecurityAuditService.SecurityEventType;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.api.StatefulRedisConnection;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {

    private final RedisConnectionFactory redisConnectionFactory;
    private final RateLimitRepository repository;
    private final SecurityAuditService auditService;
    private ProxyManager<String> proxyManager;
    private final Map<String, Bucket> localBuckets = new ConcurrentHashMap<>();

    private static final int DEFAULT_CAPACITY = 10;
    private static final int DEFAULT_REFILL_TOKENS = 10;
    private static final Duration DEFAULT_REFILL_DURATION = Duration.ofMinutes(1);

    @PostConstruct
    public void init() {
        try {
            if (redisConnectionFactory instanceof LettuceConnectionFactory lettuceFactory) {
                try (RedisConnection connection = lettuceFactory.getConnection()) {
                    Object nativeConnection = connection.getNativeConnection();
                    
                    if (nativeConnection instanceof StatefulRedisConnection<?, ?> statefulConnection) {
                        @SuppressWarnings("unchecked")
                        StatefulRedisConnection<String, byte[]> lettuceConnection = (StatefulRedisConnection<String, byte[]>) statefulConnection;
                        
                        ClientSideConfig clientSideConfig = ClientSideConfig.getDefault()
                                .withExpirationAfterWriteStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(1)));

                        this.proxyManager = LettuceBasedProxyManager.builderFor(lettuceConnection)
                                .withClientSideConfig(clientSideConfig)
                                .build();
                        log.info("Redis Rate Limiting initialized successfully.");
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to initialize Redis Rate Limiting, falling back to database/local: {}", e.getMessage());
            auditService.logRedisFallback("RateLimitInit", "global", e.getMessage());
        }
    }

    @Override
    public Bucket resolveBucket(String key) {
        BucketConfiguration config = createDefaultConfig();

        if (proxyManager != null) {
            try {
                return proxyManager.builder().build(key, config);
            } catch (Exception e) {
                log.warn("Redis Rate Limiting failed for key {}, falling back to local: {}", key, e.getMessage());
            }
        }

        return localBuckets.computeIfAbsent(key, k -> Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(DEFAULT_CAPACITY)
                        .refillGreedy(DEFAULT_REFILL_TOKENS, DEFAULT_REFILL_DURATION)
                        .build())
                .build());
    }

    @Override
    @Retryable(retryFor = Exception.class, maxAttempts = 2, backoff = @Backoff(delay = 500))
    public boolean tryConsume(String key) {
        try {
            Bucket bucket = resolveBucket(key);
            boolean allowed = bucket.tryConsume(1);
            if (!allowed) {
                auditService.log(SecurityEventType.RATE_LIMIT_EXCEEDED, null, null, null, null, "BLOCKED", "Key: " + key);
            }
            return allowed;
        } catch (Exception e) {
            log.warn("Rate limit check failed in Redis/Local for key {}, falling back to database.", key);
            throw e;
        }
    }

    @Recover
    @Transactional
    public boolean tryConsumeRecover(Exception e, String key) {
        log.info("Recovering rate limit check from database for key: {}", key);
        auditService.logRedisFallback("RateLimitConsume", key, e.getMessage());
        
        RateLimitBuckets bucket = repository.findById(key)
                .orElseGet(() -> RateLimitBuckets.builder()
                        .bucketKey(key)
                        .tokens(DEFAULT_CAPACITY)
                        .lastRefillAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build());

        refill(bucket);

        if (bucket.getTokens() >= 1) {
            bucket.setTokens(bucket.getTokens() - 1);
            repository.save(bucket);
            return true;
        }

        auditService.log(SecurityEventType.RATE_LIMIT_EXCEEDED, null, null, null, null, "BLOCKED_DB", "Key: " + key);
        return false;
    }

    private void refill(RateLimitBuckets bucket) {
        Instant now = Instant.now();
        long secondsSinceLastRefill = Duration.between(bucket.getLastRefillAt(), now).toSeconds();
        
        if (secondsSinceLastRefill <= 0) return;

        double refillRate = (double) DEFAULT_REFILL_TOKENS / DEFAULT_REFILL_DURATION.toSeconds();
        int tokensToAdd = (int) (secondsSinceLastRefill * refillRate);
        
        if (tokensToAdd > 0) {
            bucket.setTokens(Math.min(DEFAULT_CAPACITY, bucket.getTokens() + tokensToAdd));
            bucket.setLastRefillAt(now);
        }
    }

    private BucketConfiguration createDefaultConfig() {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(DEFAULT_CAPACITY)
                        .refillGreedy(DEFAULT_REFILL_TOKENS, DEFAULT_REFILL_DURATION)
                        .build())
                .build();
    }
}