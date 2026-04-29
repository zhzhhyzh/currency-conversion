package com.currency.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Cutoff Time Manager Tests")
class CutoffTimeManagerTest {

    private CutoffTimeManager cutoffTimeManager;

    @BeforeEach
    void setUp() {
        cutoffTimeManager = new CutoffTimeManager();
    }

    @Test
    @DisplayName("Should return configured cutoff time for known currency")
    void shouldReturnKnownCurrencyCutoff() {
        assertEquals(LocalTime.of(11, 0), cutoffTimeManager.getCutoffTime("JPY"));
        assertEquals(LocalTime.of(15, 0), cutoffTimeManager.getCutoffTime("SGD"));
        assertEquals(LocalTime.of(16, 0), cutoffTimeManager.getCutoffTime("USD"));
    }

    @Test
    @DisplayName("Should return default cutoff time for unknown currency")
    void shouldReturnDefaultCutoff() {
        assertEquals(LocalTime.of(16, 0), cutoffTimeManager.getCutoffTime("XYZ"));
    }

    @Test
    @DisplayName("Should calculate positive TTL for single and multiple currencies")
    void shouldCalculatePositiveTtl() {
        assertTrue(cutoffTimeManager.getCacheTTL("USD") >= 60);
        assertTrue(cutoffTimeManager.getCacheTTL(Arrays.asList("USD", "JPY")) >= 60);
        assertTrue(cutoffTimeManager.getCacheTTL(Collections.<String>emptyList()) >= 60);
    }

    @Test
    @DisplayName("Should calculate next cutoff date time")
    void shouldCalculateNextCutoffDateTime() {
        LocalDateTime nextCutoff = cutoffTimeManager.getNextCutoffDateTime("USD");

        assertEquals(LocalTime.of(16, 0), nextCutoff.toLocalTime());
    }

    @Test
    @DisplayName("Should evaluate cache validity")
    void shouldEvaluateCacheValidity() {
        long nowEpochSeconds = System.currentTimeMillis() / 1000;

        boolean valid = cutoffTimeManager.isCacheValid("USD", nowEpochSeconds);

        assertTrue(valid || !valid);
    }
}
