package com.example.quanly.service;

import com.example.quanly.domain.dto.PendingBookingData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class PendingBookingCache {

    private final ConcurrentHashMap<Long, PendingBookingData> cache = new ConcurrentHashMap<>();

    public long store(PendingBookingData data) {
        long id;
        do {
            id = ThreadLocalRandom.current().nextLong(1_000_000L, Long.MAX_VALUE);
        } while (cache.containsKey(id));
        cache.put(id, data);
        log.debug("Lưu pending booking vào cache: pendingId={}", id);
        return id;
    }

    public Optional<PendingBookingData> get(long pendingId) {
        return Optional.ofNullable(cache.get(pendingId));
    }

    public void remove(long pendingId) {
        cache.remove(pendingId);
        log.debug("Xóa pending booking khỏi cache: pendingId={}", pendingId);
    }
}
