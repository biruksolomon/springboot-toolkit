package io.github.biruksolomon.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class RateLimitService {
    private final Map<String, RateLimitEntry> store = new ConcurrentHashMap<>();

    public boolean isAllowed(String key, int limit, long windowSeconds) {
        long now = Instant.now().getEpochSecond();

        RateLimitEntry entry = store.computeIfAbsent(key, k -> new RateLimitEntry());

        // Reset if window expired
        if (now >= entry.windowStart + windowSeconds) {
            entry.count = 0;
            entry.windowStart = now;
        }

        // Check limit
        if (entry.count >= limit) {
            return false;
        }

        entry.count++;
        return true;
    }

    public long getRemainingAttempts(String key, int limit, long windowSeconds) {
        long now = Instant.now().getEpochSecond();
        RateLimitEntry entry = store.get(key);

        if (entry == null) {
            return limit;
        }

        // Reset if window expired
        if (now >= entry.windowStart + windowSeconds) {
            return limit;
        }

        return Math.max(0, limit - entry.count);
    }

    public long getResetTime(String key, long windowSeconds) {
        RateLimitEntry entry = store.get(key);
        if (entry == null) {
            return Instant.now().getEpochSecond();
        }
        return entry.windowStart + windowSeconds;
    }

    public void reset(String key) {
        store.remove(key);
    }

    public void cleanup() {
        long now = Instant.now().getEpochSecond();
        store.entrySet().removeIf(e -> now >= e.getValue().windowStart + 3600); // Remove entries older than 1 hour
    }

    private static class RateLimitEntry {
        long windowStart = Instant.now().getEpochSecond();
        int count = 0;
    }
}
