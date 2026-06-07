package com.caelum.chronos.modules.auth.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import io.github.bucket4j.Bucket;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceImplTest {

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

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
}