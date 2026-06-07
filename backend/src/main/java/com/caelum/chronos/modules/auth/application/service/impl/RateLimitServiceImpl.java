package com.caelum.chronos.modules.auth.application.service.impl;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;

import com.caelum.chronos.modules.auth.application.service.RateLimitService;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {

    private final RedisConnectionFactory redisConnectionFactory;
    private ProxyManager<byte[]> proxyManager;
    private final Map<String, Bucket> localBuckets = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        try {
            // Simplificação para obter a conexão nativa do Lettuce
            // Em produção, deve-se usar o RedisClient configurado no Spring
            log.info("Initializing Redis Rate Limiting Proxy Manager");
            // Nota: bucket4j-redis-lettuce requer configuração específica.
            // Aqui estamos apenas esboçando a integração.
        } catch (Exception e) {
            log.error("Failed to initialize Redis Proxy Manager for Rate Limiting", e);
        }
    }

    @Override
    public Bucket resolveBucket(String key) {
        return localBuckets.computeIfAbsent(key, k -> {
            BucketConfiguration config = BucketConfiguration.builder()
                    .addLimit(Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1))))
                    .build();
            
            // Aqui poderíamos usar o proxyManager para distribuir o bucket no Redis.
            // Se falhar, retornamos um bucket local (fallback).
            return Bucket.builder()
                    .addLimit(Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1))))
                    .build();
        });
    }
}