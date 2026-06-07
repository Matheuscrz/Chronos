package com.caelum.chronos.modules.auth.application.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
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

    private static final String SESSION_KEY_PREFIX = "session:";
    private static final String USER_SESSIONS_PREFIX = "user:sessions:";
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final AuthSessionRepository repository;

    @Override
    @Transactional
    @Retryable(retryFor = Exception.class, maxAttempts = 2, backoff = @Backoff(delay = 500))
    public void save(AuthSession session) {
        repository.save(session);
        try {
            String sessionKey = SESSION_KEY_PREFIX + session.getJti();
            String userSessionsKey = USER_SESSIONS_PREFIX + session.getUserId();
            
            AuthSessionDTO dto = AuthSessionDTO.fromEntity(session);
            long ttl = Duration.between(Instant.now(), session.getExpiresAt()).toSeconds();
            
            if (ttl > 0) {
                redisTemplate.opsForValue().set(sessionKey, dto, Duration.ofSeconds(ttl));
                redisTemplate.opsForSet().add(userSessionsKey, session.getJti());
                redisTemplate.expire(userSessionsKey, Duration.ofDays(7));
            }
        } catch (Exception e) {
            log.error("Failed to save session to Redis. JTI: {}", session.getJti(), e);
        }
    }

    @Override
    @Retryable(retryFor = Exception.class, maxAttempts = 2, backoff = @Backoff(delay = 500))
    public boolean isValid(String jti) {
        try {
            String key = SESSION_KEY_PREFIX + jti;
            AuthSessionDTO dto = (AuthSessionDTO) redisTemplate.opsForValue().get(key);
            if (dto != null) {
                return !dto.revoked() && dto.expiresAt().isAfter(Instant.now());
            }
        } catch (Exception e) {
            log.warn("Redis error during session validation, falling back to DB. JTI: {}", jti);
            throw e;
        }

        return repository.findByJti(jti)
                .map(s -> !s.isRevoked() && s.getExpiresAt().isAfter(Instant.now()))
                .orElse(false);
    }

    @Recover
    public boolean isValidRecover(Exception e, String jti) {
        log.info("Recovering session validation from DB for JTI: {}", jti);
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
                redisTemplate.delete(SESSION_KEY_PREFIX + jti);
                redisTemplate.opsForSet().remove(USER_SESSIONS_PREFIX + session.getUserId(), jti);
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
        try {
            String userSessionsKey = USER_SESSIONS_PREFIX + userId;
            Set<Object> jtis = redisTemplate.opsForSet().members(userSessionsKey);
            if (jtis != null) {
                jtis.forEach(jti -> redisTemplate.delete(SESSION_KEY_PREFIX + jti));
            }
            redisTemplate.delete(userSessionsKey);
        } catch (Exception e) {
            log.error("Failed to revoke all sessions in Redis for user: {}", userId, e);
        }
    }
}