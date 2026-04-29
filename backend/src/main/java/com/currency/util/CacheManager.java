package com.currency.util;

import com.currency.model.ExchangeRateData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class CacheManager {
    private static final long DEFAULT_CACHE_DURATION_SECONDS = 300;
    private static final ConcurrentHashMap<String, ExchangeRateData> cache = new ConcurrentHashMap<>();

    public void put(String key, ExchangeRateData data) {
        put(key, data, DEFAULT_CACHE_DURATION_SECONDS);
    }

    public void put(String key, ExchangeRateData data, long ttlSeconds) {
        long expiryTime = System.currentTimeMillis() + (Math.max(ttlSeconds, 60) * 1000);
        data.setCacheExpiryTime(expiryTime);
        cache.put(key, data);
        log.debug("Cached exchange rates with key: {} for {} seconds", key, ttlSeconds);
    }

    public ExchangeRateData get(String key) {
        ExchangeRateData data = cache.get(key);
        if (data != null && !data.isExpired()) {
            log.debug("Retrieved exchange rates from cache with key: {}", key);
            return data;
        }
        if (data != null) {
            cache.remove(key);
            log.debug("Cache expired for key: {}", key);
        }
        return null;
    }

    public void clear() {
        cache.clear();
        log.debug("Cache cleared");
    }

    public boolean exists(String key) {
        ExchangeRateData data = cache.get(key);
        return data != null && !data.isExpired();
    }
}
