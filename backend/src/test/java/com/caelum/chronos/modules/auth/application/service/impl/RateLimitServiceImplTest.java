package com.caelum.chronos.modules.auth.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.caelum.chronos.modules.auth.domain.model.RateLimitBuckets;
import com.caelum.chronos.modules.auth.infra.repository.RateLimitRepository;

import io.github.bucket4j.Bucket;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceImplTest {

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    @Mock
    private RateLimitRepository repository;

    @InjectMocks
    private RateLimitServiceImpl service;

    @Test
    void resolveBucketDeveRetornarBucket() {
        Bucket bucket = service.resolveBucket("test-key");
        
        assertNotNull(bucket);
        assertEquals(10, bucket.getAvailableTokens());
    }

    @Test
    void resolveBucketDeveRetornarMesmoBucketParaMesmaChave() {
        Bucket bucket1 = service.resolveBucket("test-key");
        Bucket bucket2 = service.resolveBucket("test-key");
        
        assertSame(bucket1, bucket2);
    }

    @Test
    void tryConsumeDeveRetornarTrueQuandoTemTokens() {
        boolean consumed = service.tryConsume("test-key");
        assertTrue(consumed);
    }

    @Test
    void tryConsumeRecoverDeveConsumirDoBanco() {
        String key = "test-key";
        RateLimitBuckets bucket = RateLimitBuckets.builder()
                .bucketKey(key)
                .tokens(5)
                .lastRefillAt(Instant.now())
                .build();

        when(repository.findById(key)).thenReturn(Optional.of(bucket));

        boolean consumed = service.tryConsumeRecover(new RuntimeException("Redis error"), key);

        assertTrue(consumed);
        assertEquals(4, bucket.getTokens());
        verify(repository).save(bucket);
    }

    @Test
    void tryConsumeRecoverDeveRetornarFalseQuandoSemTokens() {
        String key = "test-key";
        RateLimitBuckets bucket = RateLimitBuckets.builder()
                .bucketKey(key)
                .tokens(0)
                .lastRefillAt(Instant.now())
                .build();

        when(repository.findById(key)).thenReturn(Optional.of(bucket));

        boolean consumed = service.tryConsumeRecover(new RuntimeException("Redis error"), key);

        assertFalse(consumed);
        verify(repository, never()).save(any());
    }
}