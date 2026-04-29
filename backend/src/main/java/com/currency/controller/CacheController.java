package com.currency.controller;

import com.currency.dto.CacheStatusDto;
import com.currency.service.RedisCacheService;
import com.currency.util.CutoffTimeManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/cache")
public class CacheController {

    private final RedisCacheService redisCacheService;
    private final CutoffTimeManager cutoffTimeManager;

    public CacheController(RedisCacheService redisCacheService, 
                         CutoffTimeManager cutoffTimeManager) {
        this.redisCacheService = redisCacheService;
        this.cutoffTimeManager = cutoffTimeManager;
    }

    /**
     * Get cache status and cutoff times
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getCacheStatus() {
        log.info("Cache status request");
        
        Map<String, Object> status = new HashMap<>();
        status.put("redisConnected", redisCacheService.isConnected());
        status.put("timestamp", LocalDateTime.now().toString());
        
        // Add cutoff times for popular currencies
        Map<String, Map<String, Object>> cutoffInfo = new HashMap<>();
        String[] popularCurrencies = {"USD", "EUR", "GBP", "JPY", "AUD", "CAD", "SGD", "INR", "CHF", "CNY"};
        
        for (String currency : popularCurrencies) {
            Map<String, Object> currencyInfo = new HashMap<>();
            currencyInfo.put("cutoffTime", cutoffTimeManager.getCutoffTime(currency).toString());
            currencyInfo.put("nextCutoffDateTime", cutoffTimeManager.getNextCutoffDateTime(currency).toString());
            currencyInfo.put("ttlSeconds", cutoffTimeManager.getCacheTTL(currency));
            cutoffInfo.put(currency, currencyInfo);
        }
        
        status.put("currencyCutoffInfo", cutoffInfo);
        
        return ResponseEntity.ok(status);
    }

    /**
     * Get cutoff information for a specific currency
     */
    @GetMapping("/cutoff/{currencyCode}")
    public ResponseEntity<Map<String, Object>> getCurrencyCutoffInfo(@PathVariable String currencyCode) {
        log.info("Cutoff info request for currency: {}", currencyCode);
        
        Map<String, Object> info = new HashMap<>();
        info.put("currencyCode", currencyCode.toUpperCase());
        info.put("cutoffTime", cutoffTimeManager.getCutoffTime(currencyCode).toString());
        info.put("nextCutoffDateTime", cutoffTimeManager.getNextCutoffDateTime(currencyCode).toString());
        info.put("ttlSeconds", cutoffTimeManager.getCacheTTL(currencyCode));
        info.put("cacheKeyTTL", redisCacheService.getTimeToLive("exchange_rates_usd"));
        
        return ResponseEntity.ok(info);
    }

    /**
     * Clear cache manually
     */
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCache() {
        log.info("Cache clear request");
        redisCacheService.clearAll();
        return ResponseEntity.ok("Cache cleared successfully");
    }

    /**
     * Check Redis connection
     */
    @GetMapping("/redis/health")
    public ResponseEntity<Map<String, Object>> redisHealth() {
        log.info("Redis health check request");
        
        Map<String, Object> health = new HashMap<>();
        boolean connected = redisCacheService.isConnected();
        health.put("redisConnected", connected);
        health.put("status", connected ? "UP" : "DOWN");
        health.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(health);
    }
}
