package com.caelum.chronos.modules.auth.infra.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caelum.chronos.modules.auth.domain.model.AuthSession;

@Repository
public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {
    Optional<AuthSession> findByJti(String jti);
    void deleteByUserId(UUID userId);
}