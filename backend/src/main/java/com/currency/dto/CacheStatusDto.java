package com.currency.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheStatusDto {
    private boolean redisConnected;
    private String redisHost;
    private int redisPort;
    private long cacheSize;
    private Map<String, CurrencyCutoffInfoDto> currencyCutoffInfo;
    private String timestamp;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class CurrencyCutoffInfoDto {
    private String currencyCode;
    private LocalTime cutoffTime;
    private LocalDateTime nextCutoffDateTime;
    private long ttlSeconds;
}
