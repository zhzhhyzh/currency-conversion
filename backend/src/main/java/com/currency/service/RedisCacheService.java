package com.currency.service;

import com.currency.model.ExchangeRateData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RedisCacheService {

    private static final String EXCHANGE_RATES_KEY_PREFIX = "exchange_rates:";
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Store exchange rate data in Redis with TTL
     */
    public boolean set(String key, ExchangeRateData data, long ttlSeconds) {
        try {
            String redisKey = EXCHANGE_RATES_KEY_PREFIX + key;
            redisTemplate.opsForValue().set(redisKey, data, ttlSeconds, TimeUnit.SECONDS);
            log.info("Cached exchange rates for key: {} with TTL: {} seconds", key, ttlSeconds);
            return true;
        } catch (Exception e) {
            log.error("Error caching exchange rates: ", e);
            return false;
        }
    }

    /**
     * Retrieve exchange rate data from Redis
     */
    public ExchangeRateData get(String key) {
        try {
            String redisKey = EXCHANGE_RATES_KEY_PREFIX + key;
            Object data = redisTemplate.opsForValue().get(redisKey);
            
            if (data instanceof ExchangeRateData) {
                ExchangeRateData rateData = (ExchangeRateData) data;
                log.debug("Retrieved exchange rates from Redis for key: {}", key);
                return rateData;
            }
            
            return null;
        } catch (Exception e) {
            log.error("Error retrieving exchange rates from Redis: ", e);
            return null;
        }
    }

    /**
     * Check if key exists in Redis
     */
    public boolean exists(String key) {
        try {
            String redisKey = EXCHANGE_RATES_KEY_PREFIX + key;
            Boolean hasKey = redisTemplate.hasKey(redisKey);
            return hasKey != null && hasKey;
        } catch (Exception e) {
            log.error("Error checking Redis key existence: ", e);
            return false;
        }
    }

    /**
     * Delete key from Redis
     */
    public void delete(String key) {
        try {
            String redisKey = EXCHANGE_RATES_KEY_PREFIX + key;
            redisTemplate.delete(redisKey);
            log.debug("Deleted exchange rates from Redis for key: {}", key);
        } catch (Exception e) {
            log.error("Error deleting from Redis: ", e);
        }
    }

    /**
     * Clear all exchange rate cache
     */
    public void clearAll() {
        try {
            redisTemplate.delete(redisTemplate.keys(EXCHANGE_RATES_KEY_PREFIX + "*"));
            log.info("Cleared all exchange rate cache from Redis");
        } catch (Exception e) {
            log.error("Error clearing Redis cache: ", e);
        }
    }

    /**
     * Get remaining TTL for a key
     */
    public long getTimeToLive(String key) {
        try {
            String redisKey = EXCHANGE_RATES_KEY_PREFIX + key;
            Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
            return ttl != null ? ttl : -1;
        } catch (Exception e) {
            log.error("Error getting TTL from Redis: ", e);
            return -1;
        }
    }

    /**
     * Check Redis connection
     */
    public boolean isConnected() {
        try {
            redisTemplate.opsForValue().set("health_check", "pong", 10, TimeUnit.SECONDS);
            Boolean result = redisTemplate.opsForValue().get("health_check") != null;
            redisTemplate.delete("health_check");
            return result != null && result;
        } catch (Exception e) {
            log.error("Redis connection check failed: ", e);
            return false;
        }
    }
}
