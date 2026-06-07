package com.caelum.chronos.shared.infra.security.audit;

import java.time.Instant;
import java.util.UUID;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.caelum.chronos.shared.domain.SecurityAuditLog;
import com.caelum.chronos.shared.infra.logging.LogContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityAuditService {

    private final SecurityAuditRepository repository;

    public enum SecurityEventType {
        LOGIN_SUCCESS,
        LOGIN_FAILURE,
        LOGOUT,
        REFRESH_TOKEN,
        SESSION_REVOKED,
        TOKEN_BLACKLISTED,
        RATE_LIMIT_EXCEEDED,
        REDIS_FALLBACK
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(SecurityEventType type, UUID userId, String username, String ipAddress, String userAgent, String status, String details) {
        try {
            SecurityAuditLog auditLog = SecurityAuditLog.builder()
                    .eventTimestamp(Instant.now())
                    .eventType(type.name())
                    .userId(userId)
                    .username(username)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .status(status)
                    .details(details)
                    .correlationId(LogContext.getCorrelationId())
                    .build();

            repository.save(auditLog);
            log.debug("Security Audit Log saved: {} for user {}", type, username != null ? username : userId);
        } catch (Exception e) {
            log.error("Failed to save Security Audit Log: {}", e.getMessage(), e);
        }
    }

    public void logRedisFallback(String component, String key, String error) {
        log(SecurityEventType.REDIS_FALLBACK, null, null, null, null, "FALLBACK", 
            String.format("Component: %s, Key: %s, Error: %s", component, key, error));
    }
}
