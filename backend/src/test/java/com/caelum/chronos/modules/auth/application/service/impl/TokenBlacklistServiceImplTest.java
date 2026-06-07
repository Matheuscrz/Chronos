package com.caelum.chronos.modules.auth.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.caelum.chronos.modules.auth.domain.model.TokenBlacklist;
import com.caelum.chronos.modules.auth.infra.repository.TokenBlacklistRepository;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;
    @Mock
    private TokenBlacklistRepository repository;

    @InjectMocks
    private TokenBlacklistServiceImpl service;

    private String jti;
    private UUID userId;
    private Instant expiresAt;

    @BeforeEach
    void setUp() {
        jti = UUID.randomUUID().toString();
        userId = UUID.randomUUID();
        expiresAt = Instant.now().plus(Duration.ofHours(1));
    }

    @Test
    void blacklistDeveSalvarNoRepositorioENoRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        service.blacklist(jti, userId, "Logout", expiresAt);

        verify(repository).save(any(TokenBlacklist.class));
        verify(valueOperations).set(eq("blacklist:" + jti), eq("revoked"), any(Duration.class));
    }

    @Test
    void isBlacklistedDeveRetornarTrueQuandoNoRedis() {
        when(redisTemplate.hasKey("blacklist:" + jti)).thenReturn(true);

        boolean blacklisted = service.isBlacklisted(jti);

        assertTrue(blacklisted);
    }

    @Test
    void isBlacklistedDeveRetornarFalseQuandoNaoNoRedis() {
        when(redisTemplate.hasKey("blacklist:" + jti)).thenReturn(false);

        boolean blacklisted = service.isBlacklisted(jti);

        assertFalse(blacklisted);
    }
}