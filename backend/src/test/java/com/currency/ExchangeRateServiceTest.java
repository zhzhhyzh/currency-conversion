package com.currency.service;

import com.currency.dto.ExchangeRatesResponseDto;
import com.currency.exception.CurrencyException;
import com.currency.model.ExchangeRateData;
import com.currency.util.CacheManager;
import com.currency.util.CutoffTimeManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Exchange Rate Service Tests")
class ExchangeRateServiceTest {

    private static final String CACHE_KEY = "exchange_rates_usd";
    private static final String API_RESPONSE = "{"
        + "\"timestamp\":1777449600,"
        + "\"base\":\"USD\","
        + "\"rates\":{\"USD\":1,\"EUR\":0.92,\"MYR\":3.95}"
        + "}";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private CutoffTimeManager cutoffTimeManager;

    private CacheManager cacheManager;
    private ExchangeRateService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cacheManager = new CacheManager();
        cacheManager.clear();
        service = new ExchangeRateService(
            restTemplate,
            cacheManager,
            redisCacheService,
            cutoffTimeManager,
            new ObjectMapper()
        );
        ReflectionTestUtils.setField(service, "apiKey", "test-key");
        ReflectionTestUtils.setField(service, "apiUrl", "https://openexchangerates.org/api/latest.json");
        ReflectionTestUtils.setField(service, "redisEnabled", true);
        when(cutoffTimeManager.getCacheTTL(any(Collection.class))).thenReturn(3600L);
    }

    @Test
    @DisplayName("Should return Redis cached rates without external API call")
    void shouldReturnRedisCacheHit() {
        ExchangeRateData redisData = createRateData();
        when(redisCacheService.get(CACHE_KEY)).thenReturn(redisData);

        ExchangeRateData result = service.getLatestRates();

        assertEquals(redisData, result);
        verify(restTemplate, never()).getForObject(any(String.class), eq(String.class));
    }

    @Test
    @DisplayName("Should return memory cache and rehydrate Redis")
    void shouldReturnMemoryCacheAndRehydrateRedis() {
        ExchangeRateData memoryData = createRateData();
        cacheManager.put(CACHE_KEY, memoryData, 3600);
        when(redisCacheService.get(CACHE_KEY)).thenReturn(null);
        when(redisCacheService.set(eq(CACHE_KEY), eq(memoryData), anyLong())).thenReturn(true);

        ExchangeRateData result = service.getLatestRates();

        assertEquals(memoryData, result);
        verify(redisCacheService).set(eq(CACHE_KEY), eq(memoryData), anyLong());
        verify(restTemplate, never()).getForObject(any(String.class), eq(String.class));
    }

    @Test
    @DisplayName("Should fetch from official API and refresh cache on cache miss")
    void shouldFetchApiAndRefreshCacheOnMiss() {
        when(redisCacheService.get(CACHE_KEY)).thenReturn(null);
        when(restTemplate.getForObject("https://openexchangerates.org/api/latest.json?app_id=test-key", String.class))
            .thenReturn(API_RESPONSE);
        when(redisCacheService.set(eq(CACHE_KEY), any(ExchangeRateData.class), eq(3600L))).thenReturn(true);

        ExchangeRateData result = service.getLatestRates();

        assertEquals("USD", result.getBase());
        assertEquals(0.92, result.getRates().get("EUR"), 0.000001);
        assertEquals(3.95, result.getRates().get("MYR"), 0.000001);
        verify(redisCacheService).set(eq(CACHE_KEY), any(ExchangeRateData.class), eq(3600L));
        assertTrue(cacheManager.exists(CACHE_KEY));
    }

    @Test
    @DisplayName("Should force refresh official rates for rates endpoint")
    void shouldRefreshRatesForRatesEndpoint() {
        when(restTemplate.getForObject("https://openexchangerates.org/api/latest.json?app_id=test-key", String.class))
            .thenReturn(API_RESPONSE);
        when(redisCacheService.set(eq(CACHE_KEY), any(ExchangeRateData.class), eq(3600L))).thenReturn(true);

        ExchangeRatesResponseDto result = service.getExchangeRates();

        assertEquals("USD", result.getBase());
        assertEquals(1777449600L, result.getTimestamp());
        assertEquals(1.0, result.getRates().get("USD"), 0.000001);
    }

    @Test
    @DisplayName("Should reject empty API response with readable error")
    void shouldRejectEmptyApiResponse() {
        when(redisCacheService.get(CACHE_KEY)).thenReturn(null);
        when(restTemplate.getForObject(any(String.class), eq(String.class))).thenReturn("");

        CurrencyException exception = assertThrows(CurrencyException.class, () -> service.getLatestRates());

        assertEquals(503, exception.getStatusCode());
        assertEquals("Empty response from exchange rates API", exception.getMessage());
    }

    @Test
    @DisplayName("Should surface official API error status")
    void shouldSurfaceOfficialApiError() {
        when(redisCacheService.get(CACHE_KEY)).thenReturn(null);
        when(restTemplate.getForObject(any(String.class), eq(String.class)))
            .thenReturn("{\"error\":true,\"status\":401}");

        CurrencyException exception = assertThrows(CurrencyException.class, () -> service.getLatestRates());

        assertEquals(401, exception.getStatusCode());
        assertEquals("API Error: true", exception.getMessage());
    }

    @Test
    @DisplayName("Should wrap HTTP client errors in readable service error")
    void shouldWrapRestClientError() {
        when(redisCacheService.get(CACHE_KEY)).thenReturn(null);
        when(restTemplate.getForObject(any(String.class), eq(String.class)))
            .thenThrow(new RestClientException("401 Unauthorized"));

        CurrencyException exception = assertThrows(CurrencyException.class, () -> service.getLatestRates());

        assertEquals(503, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Unable to fetch exchange rates from API"));
    }

    @Test
    @DisplayName("Should continue to memory cache when Redis fails")
    void shouldFallbackToMemoryWhenRedisFails() {
        ExchangeRateData memoryData = createRateData();
        cacheManager.put(CACHE_KEY, memoryData, 3600);
        when(redisCacheService.get(CACHE_KEY)).thenThrow(new IllegalStateException("redis down"));

        ExchangeRateData result = service.getLatestRates();

        assertEquals(memoryData, result);
    }

    @Test
    @DisplayName("Should use API when Redis is disabled")
    void shouldUseApiWhenRedisDisabled() {
        ReflectionTestUtils.setField(service, "redisEnabled", false);
        when(restTemplate.getForObject(any(String.class), eq(String.class))).thenReturn(API_RESPONSE);

        ExchangeRateData result = service.getLatestRates();

        assertEquals("USD", result.getBase());
        verify(redisCacheService, never()).get(CACHE_KEY);
        verify(redisCacheService, never()).set(eq(CACHE_KEY), any(ExchangeRateData.class), anyLong());
    }

    private ExchangeRateData createRateData() {
        Map<String, Double> rates = new HashMap<>();
        rates.put("USD", 1.0);
        rates.put("EUR", 0.92);
        rates.put("MYR", 3.95);
        return new ExchangeRateData("USD", rates, LocalDateTime.now(), System.currentTimeMillis() + 3600000);
    }
}
