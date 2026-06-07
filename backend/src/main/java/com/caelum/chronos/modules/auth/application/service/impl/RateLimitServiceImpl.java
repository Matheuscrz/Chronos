package com.caelum.chronos.modules.auth.application.service.impl;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Service;

import com.caelum.chronos.modules.auth.application.service.RateLimitService;

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
    private ProxyManager<String> proxyManager;
    private final Map<String, Bucket> localBuckets = new ConcurrentHashMap<>();

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
            log.warn("Failed to initialize Redis Rate Limiting, falling back to local memory: {}", e.getMessage());
        }
    }

    @Override
    public Bucket resolveBucket(String key) {
        BucketConfiguration config = BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(10)
                        .refillGreedy(10, Duration.ofMinutes(1))
                        .build())
                .build();

        if (proxyManager != null) {
            try {
                return proxyManager.builder().build(key, config);
            } catch (Exception e) {
                log.warn("Redis Rate Limiting failed for key {}, falling back to local: {}", key, e.getMessage());
            }
        }

        return localBuckets.computeIfAbsent(key, k -> Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(10)
                        .refillGreedy(10, Duration.ofMinutes(1))
                        .build())
                .build());
    }
}