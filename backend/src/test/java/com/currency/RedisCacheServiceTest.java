package com.currency.service;

import com.currency.model.ExchangeRateData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Redis Cache Service Tests")
class RedisCacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private RedisCacheService redisCacheService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        redisCacheService = new RedisCacheService(redisTemplate);
    }

    @Test
    @DisplayName("Should cache exchange rates with TTL")
    void shouldCacheExchangeRates() {
        boolean result = redisCacheService.set("exchange_rates_usd", createRateData(), 3600);

        assertTrue(result);
        verify(valueOperations).set(eq("exchange_rates:exchange_rates_usd"), any(ExchangeRateData.class), eq(3600L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Should return false when cache write fails")
    void shouldReturnFalseWhenCacheWriteFails() {
        doThrow(new RuntimeException("redis write failed"))
            .when(valueOperations).set(eq("exchange_rates:exchange_rates_usd"), any(ExchangeRateData.class), eq(3600L), eq(TimeUnit.SECONDS));

        assertFalse(redisCacheService.set("exchange_rates_usd", createRateData(), 3600));
    }

    @Test
    @DisplayName("Should retrieve cached exchange rates")
    void shouldRetrieveExchangeRates() {
        ExchangeRateData data = createRateData();
        when(valueOperations.get("exchange_rates:exchange_rates_usd")).thenReturn(data);

        assertEquals(data, redisCacheService.get("exchange_rates_usd"));
    }

    @Test
    @DisplayName("Should return null when cached value is not exchange rate data")
    void shouldReturnNullForUnexpectedCachedValue() {
        when(valueOperations.get("exchange_rates:exchange_rates_usd")).thenReturn("unexpected");

        assertNull(redisCacheService.get("exchange_rates_usd"));
    }

    @Test
    @DisplayName("Should return null when cache read fails")
    void shouldReturnNullWhenCacheReadFails() {
        when(valueOperations.get("exchange_rates:exchange_rates_usd")).thenThrow(new RuntimeException("redis read failed"));

        assertNull(redisCacheService.get("exchange_rates_usd"));
    }

    @Test
    @DisplayName("Should check key existence")
    void shouldCheckKeyExists() {
        when(redisTemplate.hasKey("exchange_rates:exchange_rates_usd")).thenReturn(Boolean.TRUE);

        assertTrue(redisCacheService.exists("exchange_rates_usd"));
    }

    @Test
    @DisplayName("Should return false when key existence check fails")
    void shouldReturnFalseWhenExistsFails() {
        when(redisTemplate.hasKey("exchange_rates:exchange_rates_usd")).thenThrow(new RuntimeException("redis exists failed"));

        assertFalse(redisCacheService.exists("exchange_rates_usd"));
    }

    @Test
    @DisplayName("Should delete single cache key")
    void shouldDeleteCacheKey() {
        redisCacheService.delete("exchange_rates_usd");

        verify(redisTemplate).delete("exchange_rates:exchange_rates_usd");
    }

    @Test
    @DisplayName("Should clear all exchange-rate keys")
    void shouldClearAllExchangeRateKeys() {
        Set<String> keys = Collections.singleton("exchange_rates:exchange_rates_usd");
        when(redisTemplate.keys("exchange_rates:*")).thenReturn(keys);

        redisCacheService.clearAll();

        verify(redisTemplate).delete(keys);
    }

    @Test
    @DisplayName("Should return TTL")
    void shouldReturnTtl() {
        when(redisTemplate.getExpire("exchange_rates:exchange_rates_usd", TimeUnit.SECONDS)).thenReturn(123L);

        assertEquals(123L, redisCacheService.getTimeToLive("exchange_rates_usd"));
    }

    @Test
    @DisplayName("Should return fallback TTL when Redis returns null")
    void shouldReturnFallbackTtlWhenNull() {
        when(redisTemplate.getExpire("exchange_rates:exchange_rates_usd", TimeUnit.SECONDS)).thenReturn(null);

        assertEquals(-1L, redisCacheService.getTimeToLive("exchange_rates_usd"));
    }

    @Test
    @DisplayName("Should check Redis health")
    void shouldCheckRedisHealth() {
        when(valueOperations.get("health_check")).thenReturn("pong");

        assertTrue(redisCacheService.isConnected());
        verify(valueOperations).set("health_check", "pong", 10, TimeUnit.SECONDS);
        verify(redisTemplate).delete("health_check");
    }

    @Test
    @DisplayName("Should return false when Redis health check fails")
    void shouldReturnFalseWhenHealthCheckFails() {
        doThrow(new RuntimeException("redis down"))
            .when(valueOperations).set("health_check", "pong", 10, TimeUnit.SECONDS);

        assertFalse(redisCacheService.isConnected());
    }

    private ExchangeRateData createRateData() {
        Map<String, Double> rates = new HashMap<>();
        rates.put("USD", 1.0);
        return new ExchangeRateData("USD", rates, LocalDateTime.now(), System.currentTimeMillis() + 3600000);
    }
}
