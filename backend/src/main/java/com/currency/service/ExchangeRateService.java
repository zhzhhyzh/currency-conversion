package com.currency.service;

import com.currency.dto.ExchangeRatesResponseDto;
import com.currency.exception.CurrencyException;
import com.currency.model.ExchangeRateData;
import com.currency.util.CacheManager;
import com.currency.util.CutoffTimeManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ExchangeRateService {

    @Value("${openexchangerates.api.key}")
    private String apiKey;

    @Value("${openexchangerates.api.url:https://openexchangerates.org/api/latest.json}")
    private String apiUrl;

    @Value("${redis.enabled:true}")
    private boolean redisEnabled;

    private final RestTemplate restTemplate;
    private final CacheManager cacheManager;
    private final RedisCacheService redisCacheService;
    private final CutoffTimeManager cutoffTimeManager;
    private final ObjectMapper objectMapper;

    public ExchangeRateService(RestTemplate restTemplate, 
                             CacheManager cacheManager,
                             RedisCacheService redisCacheService,
                             CutoffTimeManager cutoffTimeManager,
                             ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.cacheManager = cacheManager;
        this.redisCacheService = redisCacheService;
        this.cutoffTimeManager = cutoffTimeManager;
        this.objectMapper = objectMapper;
    }

    public ExchangeRateData getLatestRates() {
        String cacheKey = "exchange_rates_usd";
        
        // Try Redis first. On a hit, no external API request is made.
        if (redisEnabled) {
            try {
                ExchangeRateData cachedData = redisCacheService.get(cacheKey);
                if (cachedData != null) {
                    log.info("Returning cached exchange rates from Redis");
                    return cachedData;
                }
            } catch (Exception e) {
                log.warn("Redis access failed, falling back to memory cache or API: ", e);
            }
        }
        
        // Fallback to in-memory cache
        ExchangeRateData cachedData = cacheManager.get(cacheKey);
        if (cachedData != null) {
            if (redisEnabled) {
                cacheRedisFromMemory(cacheKey, cachedData);
            }
            log.info("Returning cached exchange rates from memory");
            return cachedData;
        }

        return fetchLatestRatesFromApiAndRefreshCache(cacheKey);
    }

    public ExchangeRateData refreshLatestRates() {
        return fetchLatestRatesFromApiAndRefreshCache("exchange_rates_usd");
    }

    private ExchangeRateData fetchLatestRatesFromApiAndRefreshCache(String cacheKey) {
        log.info("Fetching fresh exchange rates from Open Exchange Rates API");
        try {
            String url = apiUrl + "?app_id=" + apiKey;
            String response = restTemplate.getForObject(url, String.class);
            
            if (response == null || response.isEmpty()) {
                throw new CurrencyException("Empty response from exchange rates API", 503);
            }

            ExchangeRateData data = parseExchangeRateResponse(response);
            long ttl = cutoffTimeManager.getCacheTTL(data.getRates().keySet());
            data.setCacheExpiryTime(System.currentTimeMillis() + (Math.max(ttl, 60) * 1000));
            
            // Refresh both Redis and memory cache with the official latest rates.
            if (redisEnabled) {
                try {
                    if (redisCacheService.set(cacheKey, data, ttl)) {
                        log.info("Refreshed latest exchange rates in Redis with TTL: {} seconds", ttl);
                    }
                } catch (Exception e) {
                    log.warn("Failed to cache in Redis: ", e);
                }
            }
            
            cacheManager.put(cacheKey, data, ttl);
            
            return data;
        } catch (CurrencyException e) {
            throw e;
        } catch (RestClientException e) {
            log.error("Error fetching exchange rates from API: ", e);
            throw new CurrencyException("Unable to fetch exchange rates from API: " + e.getMessage(), 503, e);
        } catch (Exception e) {
            log.error("Error processing exchange rates: ", e);
            throw new CurrencyException("Error processing exchange rates: " + e.getMessage(), 500, e);
        }
    }

    private void cacheRedisFromMemory(String cacheKey, ExchangeRateData data) {
        try {
            long ttl = getRemainingTtlSeconds(data);
            if (ttl > 0 && redisCacheService.set(cacheKey, data, ttl)) {
                log.info("Rehydrated Redis exchange rates cache from memory with TTL: {} seconds", ttl);
            }
        } catch (Exception e) {
            log.warn("Failed to rehydrate Redis cache from memory: ", e);
        }
    }

    private long getRemainingTtlSeconds(ExchangeRateData data) {
        long remainingMillis = data.getCacheExpiryTime() - System.currentTimeMillis();
        return Math.max(remainingMillis / 1000, 0);
    }

    private ExchangeRateData parseExchangeRateResponse(String response) throws Exception {
        try {
            JsonNode root = objectMapper.readTree(response);
            
            if (root.has("error")) {
                String errorMessage = root.get("error").asText();
                if (root.has("status")) {
                    int status = root.get("status").asInt();
                    throw new CurrencyException("API Error: " + errorMessage, status);
                }
                throw new CurrencyException("API Error: " + errorMessage, 400);
            }

            String base = root.path("base").asText("USD");
            long apiTimestamp = root.path("timestamp").asLong(Instant.now().getEpochSecond());
            Map<String, Double> rates = new HashMap<>();
            
            JsonNode ratesNode = root.get("rates");
            ratesNode.fields().forEachRemaining(entry -> {
                rates.put(entry.getKey(), entry.getValue().asDouble());
            });

            return new ExchangeRateData(
                base,
                rates,
                LocalDateTime.ofInstant(Instant.ofEpochSecond(apiTimestamp), ZoneOffset.UTC),
                0 // Will be set by cache manager
            );
        } catch (CurrencyException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error parsing API response: ", e);
            throw new CurrencyException("Error parsing API response", 500, e);
        }
    }

    public ExchangeRatesResponseDto getExchangeRates() {
        ExchangeRateData data = refreshLatestRates();
        return new ExchangeRatesResponseDto(
            data.getBase(),
            data.getRates(),
            data.getTimestamp().toEpochSecond(ZoneOffset.UTC)
        );
    }
}
