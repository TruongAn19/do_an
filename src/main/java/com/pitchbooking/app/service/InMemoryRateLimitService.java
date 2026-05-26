package com.pitchbooking.app.service;

import io.github.bucket4j.Bucket;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test-profile rate limiter — in-process {@link ConcurrentHashMap}, no Redis required.
 */
@Service
@Profile("test")
public class InMemoryRateLimitService implements RateLimitService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, this::newBucket);
    }

    private Bucket newBucket(String key) {
        // 5 requests per minute per client key.
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(5).refillGreedy(5, Duration.ofMinutes(1)))
                .build();
    }
}
