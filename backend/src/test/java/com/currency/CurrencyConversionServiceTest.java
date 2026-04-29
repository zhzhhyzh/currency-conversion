package com.currency.service;

import com.currency.exception.CurrencyException;
import com.currency.model.ExchangeRateData;
import com.currency.dto.ConversionResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Currency Conversion Service Tests")
class CurrencyConversionServiceTest {

    private CurrencyConversionService conversionService;
    private ExchangeRateService exchangeRateService;

    @BeforeEach
    void setUp() {
        exchangeRateService = new TestExchangeRateService();
        conversionService = new CurrencyConversionService(exchangeRateService);
    }

    @Test
    @DisplayName("Should convert USD to EUR successfully")
    void testConvertUsdToEur() {
        double result = conversionService.convert("USD", "EUR", 100).getConvertedAmount();
        assertTrue(result > 0);
    }

    @Test
    @DisplayName("Should handle zero amount conversion")
    void testConvertZeroAmount() {
        ConversionResponseDto result = conversionService.convert("USD", "EUR", 0);
        assertEquals(0, result.getConvertedAmount());
    }

    @Test
    @DisplayName("Should return same currency conversion as 1:1")
    void testConvertSameCurrency() {
        ConversionResponseDto result = conversionService.convert("USD", "USD", 100);
        assertEquals(100, result.getConvertedAmount());
        assertEquals(1.0, result.getRate());
    }

    @Test
    @DisplayName("Should throw exception for negative amount")
    void testConvertNegativeAmount() {
        assertThrows(CurrencyException.class, () -> {
            conversionService.convert("USD", "EUR", -100);
        });
    }

    @Test
    @DisplayName("Should throw exception for invalid currency code")
    void testConvertInvalidCurrency() {
        assertThrows(CurrencyException.class, () -> {
            conversionService.convert("INVALID", "EUR", 100);
        });
    }

    static class TestExchangeRateService extends ExchangeRateService {
        TestExchangeRateService() {
            super(null, null, null, null, null);
        }

        @Override
        public ExchangeRateData getLatestRates() {
            Map<String, Double> rates = new HashMap<>();
            rates.put("USD", 1.0);
            rates.put("EUR", 0.925);
            rates.put("GBP", 0.79);
            rates.put("JPY", 149.50);
            return new ExchangeRateData("USD", rates, LocalDateTime.now(), System.currentTimeMillis() + 3600000);
        }
    }
}
