package com.caelum.chronos.modules.auth.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.caelum.chronos.modules.auth.domain.model.RateLimitBuckets;

@Repository
public interface RateLimitRepository extends JpaRepository<RateLimitBuckets, String> {
}