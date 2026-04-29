package com.currency.service;

import com.currency.dto.ConversionResponseDto;
import com.currency.exception.CurrencyException;
import com.currency.model.ExchangeRateData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Slf4j
@Service
public class CurrencyConversionService {

    private final ExchangeRateService exchangeRateService;

    public CurrencyConversionService(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    public ConversionResponseDto convert(String fromCurrency, String toCurrency, double amount) {
        String normalizedFromCurrency = normalizeCurrencyCode(fromCurrency);
        String normalizedToCurrency = normalizeCurrencyCode(toCurrency);

        if (!isValidCurrencyCode(normalizedFromCurrency)) {
            throw new CurrencyException("Source currency must be a 3-letter ISO currency code", 400);
        }

        if (!isValidCurrencyCode(normalizedToCurrency)) {
            throw new CurrencyException("Target currency must be a 3-letter ISO currency code", 400);
        }

        if (!Double.isFinite(amount)) {
            throw new CurrencyException("Amount must be a finite number", 400);
        }

        if (amount < 0) {
            throw new CurrencyException("Amount cannot be negative", 400);
        }

        if (amount == 0) {
            log.warn("Zero amount conversion requested");
            return createConversionResponse(normalizedFromCurrency, normalizedToCurrency, amount, 0, 0);
        }

        if (normalizedFromCurrency.equals(normalizedToCurrency)) {
            return createConversionResponse(normalizedFromCurrency, normalizedToCurrency, amount, amount, 1.0);
        }

        try {
            ExchangeRateData rateData = exchangeRateService.getLatestRates();
            double rate = calculateExchangeRate(normalizedFromCurrency, normalizedToCurrency, rateData);
            double convertedAmount = amount * rate;

            log.info("Converted {} {} to {} {} at rate {}", amount, normalizedFromCurrency, convertedAmount, normalizedToCurrency, rate);
            return createConversionResponse(normalizedFromCurrency, normalizedToCurrency, amount, convertedAmount, rate);
        } catch (CurrencyException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during currency conversion: ", e);
            throw new CurrencyException("Error performing currency conversion: " + e.getMessage(), 500, e);
        }
    }

    private double calculateExchangeRate(String fromCurrency, String toCurrency, ExchangeRateData rateData) {
        String base = rateData.getBase();
        Double fromRate = rateData.getRates().get(fromCurrency.toUpperCase());
        Double toRate = rateData.getRates().get(toCurrency.toUpperCase());

        if (fromRate == null) {
            throw new CurrencyException("Exchange rate not found for currency: " + fromCurrency, 400);
        }

        if (toRate == null) {
            throw new CurrencyException("Exchange rate not found for currency: " + toCurrency, 400);
        }

        if (fromRate <= 0 || toRate <= 0) {
            throw new CurrencyException("Exchange rate must be greater than zero", 500);
        }

        // If source currency is the base currency (USD), just use the target rate
        if (base.equalsIgnoreCase(fromCurrency)) {
            return toRate;
        }

        // If target currency is the base currency, invert the source rate
        if (base.equalsIgnoreCase(toCurrency)) {
            return 1.0 / fromRate;
        }

        // Otherwise, calculate cross-rate: (1 / fromRate) * toRate
        return toRate / fromRate;
    }

    private boolean isValidCurrencyCode(String code) {
        return code != null && code.matches("^[A-Z]{3}$");
    }

    private String normalizeCurrencyCode(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }

    private ConversionResponseDto createConversionResponse(String from, String to, double amount, 
                                                           double convertedAmount, double rate) {
        return new ConversionResponseDto(
            from.toUpperCase(),
            to.toUpperCase(),
            amount,
            convertedAmount,
            rate,
            LocalDateTime.now()
        );
    }
}
