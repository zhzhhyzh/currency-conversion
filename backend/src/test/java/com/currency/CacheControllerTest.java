package com.currency.controller;

import com.currency.service.RedisCacheService;
import com.currency.util.CutoffTimeManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CacheController.class)
@DisplayName("Cache Controller API Tests")
class CacheControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RedisCacheService redisCacheService;

    @MockBean
    private CutoffTimeManager cutoffTimeManager;

    @Test
    @DisplayName("Should return cache status")
    void shouldReturnCacheStatus() throws Exception {
        when(redisCacheService.isConnected()).thenReturn(true);
        when(cutoffTimeManager.getCutoffTime(anyString())).thenReturn(LocalTime.of(16, 0));
        when(cutoffTimeManager.getNextCutoffDateTime(anyString())).thenReturn(LocalDateTime.of(2026, 4, 29, 16, 0));
        when(cutoffTimeManager.getCacheTTL(anyString())).thenReturn(3600L);

        mockMvc.perform(get("/api/cache/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.redisConnected").value(true))
            .andExpect(jsonPath("$.currencyCutoffInfo.USD.cutoffTime").value("16:00"))
            .andExpect(jsonPath("$.currencyCutoffInfo.EUR.ttlSeconds").value(3600));
    }

    @Test
    @DisplayName("Should return cutoff info for a currency")
    void shouldReturnCurrencyCutoffInfo() throws Exception {
        when(cutoffTimeManager.getCutoffTime("myr")).thenReturn(LocalTime.of(16, 0));
        when(cutoffTimeManager.getNextCutoffDateTime("myr")).thenReturn(LocalDateTime.of(2026, 4, 29, 16, 0));
        when(cutoffTimeManager.getCacheTTL("myr")).thenReturn(3600L);
        when(redisCacheService.getTimeToLive("exchange_rates_usd")).thenReturn(3000L);

        mockMvc.perform(get("/api/cache/cutoff/myr"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.currencyCode").value("MYR"))
            .andExpect(jsonPath("$.cutoffTime").value("16:00"))
            .andExpect(jsonPath("$.ttlSeconds").value(3600))
            .andExpect(jsonPath("$.cacheKeyTTL").value(3000));
    }

    @Test
    @DisplayName("Should clear cache")
    void shouldClearCache() throws Exception {
        mockMvc.perform(delete("/api/cache/clear"))
            .andExpect(status().isOk())
            .andExpect(content().string("Cache cleared successfully"));

        verify(redisCacheService).clearAll();
    }

    @Test
    @DisplayName("Should return Redis health")
    void shouldReturnRedisHealth() throws Exception {
        when(redisCacheService.isConnected()).thenReturn(false);

        mockMvc.perform(get("/api/cache/redis/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.redisConnected").value(false))
            .andExpect(jsonPath("$.status").value("DOWN"));

        verify(redisCacheService, Mockito.times(1)).isConnected();
    }
}
