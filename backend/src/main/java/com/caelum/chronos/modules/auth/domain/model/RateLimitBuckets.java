package com.caelum.chronos.modules.auth.domain.model;

import java.time.Instant;

import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rate_limit_buckets")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class RateLimitBuckets {

    @Id
    @Column(name = "bucket_key", nullable = false, length = 255)
    private String bucketKey;

    @Column(name = "tokens", nullable = false)
    private Integer tokens;

    @Column(name = "last_refill_at", nullable = false)
    private Instant lastRefillAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}