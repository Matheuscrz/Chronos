package com.caelum.chronos.modules.auth.application.service;

import io.github.bucket4j.Bucket;

public interface RateLimitService {
    Bucket resolveBucket(String key);
}