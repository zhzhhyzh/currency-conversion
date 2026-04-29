package com.currency.service;

import com.currency.dto.ConversionResponseDto;
import com.currency.exception.CurrencyException;
import com.currency.model.ExchangeRateData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Currency Conversion Service Tests")
class CurrencyConversionServiceTest {

    private CurrencyConversionService conversionService;
    private TestExchangeRateService exchangeRateService;

    @BeforeEach
    void setUp() {
        exchangeRateService = new TestExchangeRateService();
        conversionService = new CurrencyConversionService(exchangeRateService);
    }

    @Test
    @DisplayName("Should convert from USD base currency to target currency")
    void shouldConvertFromUsdBaseCurrency() {
        ConversionResponseDto result = conversionService.convert("USD", "EUR", 100);

        assertEquals("USD", result.getFrom());
        assertEquals("EUR", result.getTo());
        assertEquals(92.5, result.getConvertedAmount(), 0.000001);
        assertEquals(0.925, result.getRate(), 0.000001);
    }

    @Test
    @DisplayName("Should convert target currency back to USD")
    void shouldConvertToUsdBaseCurrency() {
        ConversionResponseDto result = conversionService.convert("EUR", "USD", 92.5);

        assertEquals(100.0, result.getConvertedAmount(), 0.000001);
        assertEquals(1.0 / 0.925, result.getRate(), 0.000001);
    }

    @Test
    @DisplayName("Should calculate cross-currency conversion")
    void shouldConvertCrossCurrency() {
        ConversionResponseDto result = conversionService.convert("GBP", "JPY", 10);

        assertEquals(10 * (149.50 / 0.79), result.getConvertedAmount(), 0.000001);
        assertEquals(149.50 / 0.79, result.getRate(), 0.000001);
    }

    @Test
    @DisplayName("Should normalize lowercase and padded currency codes")
    void shouldNormalizeCurrencyCodes() {
        ConversionResponseDto result = conversionService.convert(" usd ", " eur ", 100);

        assertEquals("USD", result.getFrom());
        assertEquals("EUR", result.getTo());
        assertEquals(92.5, result.getConvertedAmount(), 0.000001);
    }

    @Test
    @DisplayName("Should handle zero amount conversion")
    void shouldHandleZeroAmount() {
        ConversionResponseDto result = conversionService.convert("USD", "EUR", 0);

        assertEquals(0, result.getConvertedAmount());
        assertEquals(0, result.getRate());
    }

    @Test
    @DisplayName("Should return same currency conversion as 1:1")
    void shouldReturnSameCurrencyConversion() {
        ConversionResponseDto result = conversionService.convert("USD", "USD", 100);

        assertEquals(100, result.getConvertedAmount());
        assertEquals(1.0, result.getRate());
    }

    @Test
    @DisplayName("Should reject negative amount with readable error")
    void shouldRejectNegativeAmount() {
        CurrencyException exception = assertThrows(CurrencyException.class, () ->
            conversionService.convert("USD", "EUR", -100)
        );

        assertEquals(400, exception.getStatusCode());
        assertEquals("Amount cannot be negative", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject non-finite amount with readable error")
    void shouldRejectNonFiniteAmount() {
        CurrencyException exception = assertThrows(CurrencyException.class, () ->
            conversionService.convert("USD", "EUR", Double.NaN)
        );

        assertEquals(400, exception.getStatusCode());
        assertEquals("Amount must be a finite number", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject invalid source currency code with readable error")
    void shouldRejectInvalidSourceCurrencyCode() {
        CurrencyException exception = assertThrows(CurrencyException.class, () ->
            conversionService.convert("INVALID", "EUR", 100)
        );

        assertEquals(400, exception.getStatusCode());
        assertEquals("Source currency must be a 3-letter ISO currency code", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject invalid target currency code with readable error")
    void shouldRejectInvalidTargetCurrencyCode() {
        CurrencyException exception = assertThrows(CurrencyException.class, () ->
            conversionService.convert("USD", "EURO", 100)
        );

        assertEquals(400, exception.getStatusCode());
        assertEquals("Target currency must be a 3-letter ISO currency code", exception.getMessage());
    }

    @Test
    @DisplayName("Should return readable error when source rate is missing")
    void shouldRejectMissingSourceRate() {
        CurrencyException exception = assertThrows(CurrencyException.class, () ->
            conversionService.convert("CAD", "EUR", 100)
        );

        assertEquals(400, exception.getStatusCode());
        assertEquals("Exchange rate not found for currency: CAD", exception.getMessage());
    }

    @Test
    @DisplayName("Should return readable error when target rate is missing")
    void shouldRejectMissingTargetRate() {
        CurrencyException exception = assertThrows(CurrencyException.class, () ->
            conversionService.convert("USD", "CAD", 100)
        );

        assertEquals(400, exception.getStatusCode());
        assertEquals("Exchange rate not found for currency: CAD", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject zero or negative exchange rates")
    void shouldRejectInvalidExchangeRateValue() {
        exchangeRateService.getRates().put("EUR", 0.0);

        CurrencyException exception = assertThrows(CurrencyException.class, () ->
            conversionService.convert("USD", "EUR", 100)
        );

        assertEquals(500, exception.getStatusCode());
        assertEquals("Exchange rate must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Should wrap unexpected conversion errors in readable exception")
    void shouldWrapUnexpectedConversionErrors() {
        exchangeRateService.setFailUnexpectedly(true);

        CurrencyException exception = assertThrows(CurrencyException.class, () ->
            conversionService.convert("USD", "EUR", 100)
        );

        assertEquals(500, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Error performing currency conversion"));
    }

    static class TestExchangeRateService extends ExchangeRateService {
        private final Map<String, Double> rates = new HashMap<>();
        private boolean failUnexpectedly;

        TestExchangeRateService() {
            super(null, null, null, null, null);
            rates.put("USD", 1.0);
            rates.put("EUR", 0.925);
            rates.put("GBP", 0.79);
            rates.put("JPY", 149.50);
        }

        Map<String, Double> getRates() {
            return rates;
        }

        void setFailUnexpectedly(boolean failUnexpectedly) {
            this.failUnexpectedly = failUnexpectedly;
        }

        @Override
        public ExchangeRateData getLatestRates() {
            if (failUnexpectedly) {
                throw new IllegalStateException("rate provider unavailable");
            }
            return new ExchangeRateData("USD", rates, LocalDateTime.now(), System.currentTimeMillis() + 3600000);
        }
    }
}
