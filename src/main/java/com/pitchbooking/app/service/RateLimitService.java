package com.pitchbooking.app.service;

import io.github.bucket4j.Bucket;

/**
 * Resolves the Bucket4j {@link Bucket} for a given client key (e.g. IP).
 *
 * <p>Two implementations:
 * <ul>
 *   <li>{@link InMemoryRateLimitService} (test profile) — per-process map, ok for tests.</li>
 *   <li>{@link RedisRateLimitService} (default) — Bucket4j {@code LettuceBasedProxyManager}
 *       backed by Redis so counters are shared across instances.</li>
 * </ul>
 */
public interface RateLimitService {
    Bucket resolveBucket(String key);
}
