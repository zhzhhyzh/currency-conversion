package com.currency.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class CutoffTimeManager {

    private static final ZoneId GMT_PLUS_8 = ZoneId.of("Asia/Shanghai"); // GMT+8
    private static final LocalTime DEFAULT_CUTOFF_TIME = LocalTime.of(16, 0); // 4 PM

    // Currency-specific cutoff times
    private static final Map<String, LocalTime> CURRENCY_CUTOFF_TIMES = new HashMap<>();

    static {
        // Initialize cutoff times for each currency
        CURRENCY_CUTOFF_TIMES.put("AED", LocalTime.of(16, 0)); // Emirati Dirham
        CURRENCY_CUTOFF_TIMES.put("AUD", LocalTime.of(12, 0)); // Australian Dollar
        CURRENCY_CUTOFF_TIMES.put("BDT", LocalTime.of(16, 0)); // Bangladeshi Taka
        CURRENCY_CUTOFF_TIMES.put("BND", LocalTime.of(15, 0)); // Bruneian Dollar
        CURRENCY_CUTOFF_TIMES.put("CAD", LocalTime.of(16, 0)); // Canadian Dollar
        CURRENCY_CUTOFF_TIMES.put("CHF", LocalTime.of(16, 0)); // Swiss Franc
        CURRENCY_CUTOFF_TIMES.put("CNY", LocalTime.of(15, 0)); // Chinese Yuan
        CURRENCY_CUTOFF_TIMES.put("DKK", LocalTime.of(16, 0)); // Danish Krone
        CURRENCY_CUTOFF_TIMES.put("EUR", LocalTime.of(16, 0)); // Euro
        CURRENCY_CUTOFF_TIMES.put("GBP", LocalTime.of(16, 0)); // British Pound
        CURRENCY_CUTOFF_TIMES.put("HKD", LocalTime.of(15, 0)); // Hong Kong Dollar
        CURRENCY_CUTOFF_TIMES.put("IDR", LocalTime.of(15, 0)); // Indonesian Rupiah
        CURRENCY_CUTOFF_TIMES.put("INR", LocalTime.of(16, 0)); // Indian Rupee
        CURRENCY_CUTOFF_TIMES.put("JPY", LocalTime.of(11, 0)); // Japanese Yen
        CURRENCY_CUTOFF_TIMES.put("LKR", LocalTime.of(16, 0)); // Sri Lankan Rupee
        CURRENCY_CUTOFF_TIMES.put("NOK", LocalTime.of(16, 0)); // Norwegian Krone
        CURRENCY_CUTOFF_TIMES.put("NZD", LocalTime.of(11, 0)); // New Zealand Dollar
        CURRENCY_CUTOFF_TIMES.put("PHP", LocalTime.of(12, 0)); // Philippine Piso
        CURRENCY_CUTOFF_TIMES.put("PKR", LocalTime.of(16, 0)); // Pakistani Rupee
        CURRENCY_CUTOFF_TIMES.put("SAR", LocalTime.of(16, 0)); // Saudi Arabian Riyal
        CURRENCY_CUTOFF_TIMES.put("SEK", LocalTime.of(16, 0)); // Swedish Krona
        CURRENCY_CUTOFF_TIMES.put("SGD", LocalTime.of(15, 0)); // Singapore Dollar
        CURRENCY_CUTOFF_TIMES.put("THB", LocalTime.of(15, 0)); // Thai Baht
        CURRENCY_CUTOFF_TIMES.put("USD", LocalTime.of(16, 0)); // United States Dollar
        CURRENCY_CUTOFF_TIMES.put("ZAR", LocalTime.of(16, 0)); // South Africa Rand
    }

    /**
     * Get the cache TTL (Time To Live) in seconds for a specific currency
     * TTL is calculated based on the cutoff time in GMT+8 timezone
     */
    public long getCacheTTL(String currencyCode) {
        LocalTime cutoffTime = CURRENCY_CUTOFF_TIMES.getOrDefault(
            currencyCode.toUpperCase(),
            DEFAULT_CUTOFF_TIME
        );

        ZonedDateTime nowGMT8 = ZonedDateTime.now(GMT_PLUS_8);
        LocalDateTime nowLocal = nowGMT8.toLocalDateTime();
        LocalDateTime cutoffDateTime = LocalDateTime.of(nowLocal.toLocalDate(), cutoffTime);

        // If cutoff time has already passed today, schedule for next day
        if (nowLocal.isAfter(cutoffDateTime)) {
            cutoffDateTime = cutoffDateTime.plusDays(1);
        }

        long ttlSeconds = ChronoUnit.SECONDS.between(nowLocal, cutoffDateTime);
        
        log.debug("Currency: {}, Cutoff Time: {}, TTL: {} seconds", 
            currencyCode, cutoffTime, ttlSeconds);
        
        return Math.max(ttlSeconds, 60); // Minimum 60 seconds TTL
    }

    public long getCacheTTL(Collection<String> currencyCodes) {
        if (currencyCodes == null || currencyCodes.isEmpty()) {
            return getCacheTTL("USD");
        }

        return currencyCodes.stream()
            .mapToLong(this::getCacheTTL)
            .min()
            .orElseGet(() -> getCacheTTL("USD"));
    }

    /**
     * Get cutoff time for a specific currency
     */
    public LocalTime getCutoffTime(String currencyCode) {
        return CURRENCY_CUTOFF_TIMES.getOrDefault(
            currencyCode.toUpperCase(),
            DEFAULT_CUTOFF_TIME
        );
    }

    /**
     * Get next cutoff time in GMT+8
     */
    public LocalDateTime getNextCutoffDateTime(String currencyCode) {
        LocalTime cutoffTime = getCutoffTime(currencyCode);
        ZonedDateTime nowGMT8 = ZonedDateTime.now(GMT_PLUS_8);
        LocalDateTime nowLocal = nowGMT8.toLocalDateTime();
        LocalDateTime cutoffDateTime = LocalDateTime.of(nowLocal.toLocalDate(), cutoffTime);

        if (nowLocal.isAfter(cutoffDateTime)) {
            cutoffDateTime = cutoffDateTime.plusDays(1);
        }

        return cutoffDateTime;
    }

    /**
     * Check if cache is still valid based on cutoff time
     */
    public boolean isCacheValid(String currencyCode, long cacheTimestamp) {
        ZonedDateTime nowGMT8 = ZonedDateTime.now(GMT_PLUS_8);
        LocalDateTime nowLocal = nowGMT8.toLocalDateTime();
        LocalDateTime cachedTime = LocalDateTime.ofEpochSecond(
            cacheTimestamp,
            0,
            java.time.ZoneOffset.UTC
        );

        LocalTime cutoffTime = getCutoffTime(currencyCode);
        LocalDateTime cutoffDateTime = LocalDateTime.of(nowLocal.toLocalDate(), cutoffTime);

        // If current time is before cutoff, cache is valid
        if (nowLocal.isBefore(cutoffDateTime)) {
            return cachedTime.toLocalDate().isEqual(nowLocal.toLocalDate());
        }

        // After cutoff, need new cache entry for next day
        return false;
    }
}
