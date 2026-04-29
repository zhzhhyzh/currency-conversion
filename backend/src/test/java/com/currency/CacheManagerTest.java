package com.currency.util;

import com.currency.model.ExchangeRateData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("In-memory Cache Manager Tests")
class CacheManagerTest {

    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager = new CacheManager();
        cacheManager.clear();
    }

    @Test
    @DisplayName("Should store and retrieve non-expired exchange rates")
    void shouldStoreAndRetrieveExchangeRates() {
        cacheManager.put("exchange_rates_usd", createRateData(), 120);

        assertNotNull(cacheManager.get("exchange_rates_usd"));
        assertTrue(cacheManager.exists("exchange_rates_usd"));
    }

    @Test
    @DisplayName("Should remove expired exchange rates")
    void shouldRemoveExpiredExchangeRates() {
        ExchangeRateData data = createRateData();
        data.setCacheExpiryTime(System.currentTimeMillis() - 1000);
        cacheManager.put("exchange_rates_usd", data, -1);
        data.setCacheExpiryTime(System.currentTimeMillis() - 1000);

        assertNull(cacheManager.get("exchange_rates_usd"));
        assertFalse(cacheManager.exists("exchange_rates_usd"));
    }

    @Test
    @DisplayName("Should clear cache")
    void shouldClearCache() {
        cacheManager.put("exchange_rates_usd", createRateData());

        cacheManager.clear();

        assertNull(cacheManager.get("exchange_rates_usd"));
    }

    private ExchangeRateData createRateData() {
        Map<String, Double> rates = new HashMap<>();
        rates.put("USD", 1.0);
        return new ExchangeRateData("USD", rates, LocalDateTime.now(), System.currentTimeMillis() + 3600000);
    }
}
