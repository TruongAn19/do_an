package com.example.quanly.service;

import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {
    // Trong môi trường thực tế, ta nên dùng RedisProxyManager của Bucket4j để đồng
    // bộ giữa các node.
    // Ở đây ta mô phỏng bằng Map, nhưng logic có thể dễ dàng chuyển sang Redis.
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, this::newBucket);
    }

    private Bucket newBucket(String key) {
        // Cho phép 5 yêu cầu mỗi phút
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(5).refillGreedy(5, Duration.ofMinutes(1)))
                .build();
    }

}
