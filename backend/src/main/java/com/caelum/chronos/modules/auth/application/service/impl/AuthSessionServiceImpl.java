package com.caelum.chronos.modules.auth.application.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.caelum.chronos.modules.auth.application.dto.session.AuthSessionDTO;
import com.caelum.chronos.modules.auth.application.service.AuthSessionService;
import com.caelum.chronos.modules.auth.domain.model.AuthSession;
import com.caelum.chronos.modules.auth.infra.repository.AuthSessionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthSessionServiceImpl implements AuthSessionService {

    private static final String REDIS_PREFIX = "session:";
    private final RedisTemplate<String, Object> redisTemplate;
    private final AuthSessionRepository repository;

    @Override
    @Transactional
    @Retryable(retryFor = Exception.class, maxAttempts = 2, backoff = @Backoff(delay = 500))
    public void save(AuthSession session) {
        repository.save(session);
        try {
            String key = REDIS_PREFIX + session.getJti();
            AuthSessionDTO dto = AuthSessionDTO.fromEntity(session);
            long ttl = Duration.between(Instant.now(), session.getExpiresAt()).toSeconds();
            if (ttl > 0) {
                redisTemplate.opsForValue().set(key, dto, Duration.ofSeconds(ttl));
            }
        } catch (Exception e) {
            log.error("Failed to save session to Redis, but it was saved to DB. JTI: {}", session.getJti(), e);
            // Não bloqueia para falhas no redis
        }
    }

    @Override
    @Retryable(retryFor = Exception.class, maxAttempts = 2, backoff = @Backoff(delay = 500))
    public boolean isValid(String jti) {
        try {
            String key = REDIS_PREFIX + jti;
            AuthSessionDTO dto = (AuthSessionDTO) redisTemplate.opsForValue().get(key);
            if (dto != null) {
                return !dto.revoked() && dto.expiresAt().isAfter(Instant.now());
            }
        } catch (Exception e) {
            log.warn("Redis is down, falling back to database for JTI: {}", jti);
            throw e; // Trigger retry/recover
        }

        return repository.findByJti(jti)
                .map(s -> !s.isRevoked() && s.getExpiresAt().isAfter(Instant.now()))
                .orElse(false);
    }

    @Recover
    public boolean isValidRecover(Exception e, String jti) {
        log.info("Recovering from Redis failure for session validation. JTI: {}", jti);
        return repository.findByJti(jti)
                .map(s -> !s.isRevoked() && s.getExpiresAt().isAfter(Instant.now()))
                .orElse(false);
    }

    @Override
    @Transactional
    @Retryable(retryFor = Exception.class, maxAttempts = 2, backoff = @Backoff(delay = 500))
    public void revoke(String jti) {
        repository.findByJti(jti).ifPresent(session -> {
            session.revoke();
            repository.save(session);
            try {
                String key = REDIS_PREFIX + jti;
                redisTemplate.delete(key);
            } catch (Exception e) {
                log.error("Failed to revoke session in Redis. JTI: {}", jti, e);
            }
        });
    }

    @Override
    @Transactional
    @Retryable(retryFor = Exception.class, maxAttempts = 2, backoff = @Backoff(delay = 500))
    public void revokeAllByUserId(UUID userId) {
        repository.deleteByUserId(userId);
        // Em um cenário real, poderíamos querer iterar e deletar do Redis ou usar um set para rastrear sessões do usuário.
        // Para simplicidade, vamos apenas logar e talvez implementar um padrão de exclusão se necessário.
        // TODO: Implementar exclusão de sessões do Redis para o usuário, se necessário.
        log.warn("Revoking all sessions for user {} in DB. Redis sessions will expire or need manual cleanup.", userId);
    }
}