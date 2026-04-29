package com.currency.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateData {
    private String base;
    private Map<String, Double> rates;
    private LocalDateTime timestamp;
    private long cacheExpiryTime;
    
    public boolean isExpired() {
        return System.currentTimeMillis() > cacheExpiryTime;
    }
}
