package com.pitchbooking.app.service;

import com.pitchbooking.app.domain.dto.PendingBookingData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Redis-backed implementation. Used in any non-test profile.
 *
 * <p>Keys: {@value #KEY_PREFIX}{pendingId}. Values: JSON-encoded {@link PendingBookingData}.
 * Entries auto-expire after {@link #TTL} so abandoned pending bookings don't accumulate.
 */
@Slf4j
@Service
@Profile("!test")
public class RedisPendingBookingCache implements PendingBookingCache {

    static final String KEY_PREFIX = "pending-booking:";
    static final Duration TTL = Duration.ofMinutes(20);

    private final RedisTemplate<String, PendingBookingData> redisTemplate;

    public RedisPendingBookingCache(RedisTemplate<String, PendingBookingData> pendingBookingRedisTemplate) {
        this.redisTemplate = pendingBookingRedisTemplate;
    }

    private String key(long pendingId) {
        return KEY_PREFIX + pendingId;
    }

    @Override
    public long store(PendingBookingData data) {
        long id;
        // setIfAbsent — re-roll on the (vanishingly rare) collision.
        do {
            id = ThreadLocalRandom.current().nextLong(1_000_000L, Long.MAX_VALUE);
        } while (Boolean.FALSE.equals(redisTemplate.opsForValue().setIfAbsent(key(id), data, TTL)));
        log.debug("Lưu pending booking vào Redis: pendingId={}", id);
        return id;
    }

    @Override
    public Optional<PendingBookingData> get(long pendingId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(pendingId)));
    }

    @Override
    public void remove(long pendingId) {
        redisTemplate.delete(key(pendingId));
        log.debug("Xóa pending booking khỏi Redis: pendingId={}", pendingId);
    }
}
