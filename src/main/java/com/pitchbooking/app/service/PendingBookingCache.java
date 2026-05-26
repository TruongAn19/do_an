package com.pitchbooking.app.service;

import com.pitchbooking.app.domain.dto.PendingBookingData;

import java.util.Optional;

/**
 * Stores a {@link PendingBookingData} snapshot during the VNPay payment window.
 *
 * <p>Two implementations:
 * <ul>
 *   <li>{@link RedisPendingBookingCache} (dev/prod, default profile) — distributed,
 *       20-minute TTL, survives restarts and works across instances.</li>
 *   <li>{@link InMemoryPendingBookingCache} (test profile) — in-process map so the
 *       test suite has no Redis dependency.</li>
 * </ul>
 */
public interface PendingBookingCache {

    long store(PendingBookingData data);

    Optional<PendingBookingData> get(long pendingId);

    void remove(long pendingId);
}
