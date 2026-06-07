package com.caelum.chronos.modules.auth.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.SetOperations;

import com.caelum.chronos.modules.auth.application.dto.session.AuthSessionDTO;
import com.caelum.chronos.modules.auth.domain.model.AuthSession;
import com.caelum.chronos.modules.auth.infra.repository.AuthSessionRepository;

@ExtendWith(MockitoExtension.class)
class AuthSessionServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;
    @Mock
    private SetOperations<String, Object> setOperations;
    @Mock
    private AuthSessionRepository repository;

    @InjectMocks
    private AuthSessionServiceImpl service;

    private UUID userId;
    private String jti;
    private AuthSession session;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        jti = UUID.randomUUID().toString();
        session = AuthSession.builder()
                .userId(userId)
                .jti(jti)
                .expiresAt(Instant.now().plus(Duration.ofHours(1)))
                .build();
    }

    @Test
    void saveDeveSalvarNoRepositorioENoRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);

        service.save(session);

        verify(repository).save(session);
        verify(valueOperations).set(eq("session:" + jti), any(AuthSessionDTO.class), any(Duration.class));
        verify(setOperations).add(eq("user:sessions:" + userId), eq(jti));
    }

    @Test
    void isValidDeveRetornarTrueQuandoNoRedis() {
        AuthSessionDTO dto = new AuthSessionDTO(UUID.randomUUID(), userId, jti, null, null, null, Instant.now(), Instant.now().plus(Duration.ofHours(1)), false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("session:" + jti)).thenReturn(dto);

        boolean valid = service.isValid(jti);

        assertTrue(valid);
        verifyNoInteractions(repository);
    }

    @Test
    void isValidDeveBuscarNoRepositorioQuandoNaoNoRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("session:" + jti)).thenReturn(null);
        when(repository.findByJti(jti)).thenReturn(Optional.of(session));

        boolean valid = service.isValid(jti);

        assertTrue(valid);
        verify(repository).findByJti(jti);
    }

    @Test
    void revokeDeveInvalidarNoRepositorioENoRedis() {
        when(repository.findByJti(jti)).thenReturn(Optional.of(session));
        when(redisTemplate.opsForSet()).thenReturn(setOperations);

        service.revoke(jti);

        assertTrue(session.isRevoked());
        verify(repository).save(session);
        verify(redisTemplate).delete("session:" + jti);
        verify(setOperations).remove("user:sessions:" + userId, jti);
    }

    @Test
    void revokeAllByUserIdDeveLimparTudo() {
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members("user:sessions:" + userId)).thenReturn(Set.of(jti));

        service.revokeAllByUserId(userId);

        verify(repository).deleteByUserId(userId);
        verify(redisTemplate).delete("session:" + jti);
        verify(redisTemplate).delete("user:sessions:" + userId);
    }
}