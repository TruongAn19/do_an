package com.pitchbooking.app.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Default rate limiter — Bucket4j buckets persisted in Redis via
 * {@code LettuceBasedProxyManager}, so the 5 req/min budget is shared
 * across every application instance behind the load balancer.
 */
@Service
@Profile("!test")
public class RedisRateLimitService implements RateLimitService {

    private final ProxyManager<String> proxyManager;
    private final Supplier<BucketConfiguration> bucketConfigSupplier;

    public RedisRateLimitService(ProxyManager<String> bucket4jProxyManager) {
        this.proxyManager = bucket4jProxyManager;
        this.bucketConfigSupplier = () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.builder().capacity(5).refillGreedy(5, Duration.ofMinutes(1)).build())
                .build();
    }

    @Override
    public Bucket resolveBucket(String key) {
        return proxyManager.builder().build("rate-limit:" + key, bucketConfigSupplier);
    }
}
