package com.pitchbooking.app.service;

import com.pitchbooking.app.domain.dto.PendingBookingData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Test-profile implementation backed by an in-process map. Avoids requiring a
 * Redis server when running the test suite.
 */
@Slf4j
@Service
@Profile("test")
public class InMemoryPendingBookingCache implements PendingBookingCache {

    private final ConcurrentHashMap<Long, PendingBookingData> cache = new ConcurrentHashMap<>();

    @Override
    public long store(PendingBookingData data) {
        long id;
        do {
            id = ThreadLocalRandom.current().nextLong(1_000_000L, Long.MAX_VALUE);
        } while (cache.containsKey(id));
        cache.put(id, data);
        log.debug("Lưu pending booking vào cache (in-memory): pendingId={}", id);
        return id;
    }

    @Override
    public Optional<PendingBookingData> get(long pendingId) {
        return Optional.ofNullable(cache.get(pendingId));
    }

    @Override
    public void remove(long pendingId) {
        cache.remove(pendingId);
        log.debug("Xóa pending booking khỏi cache (in-memory): pendingId={}", pendingId);
    }
}
