package com.currency.controller;

import com.currency.dto.ConversionResponseDto;
import com.currency.dto.ExchangeRatesResponseDto;
import com.currency.exception.CurrencyException;
import com.currency.service.CurrencyConversionService;
import com.currency.service.ExchangeRateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
public class CurrencyController {

    private final CurrencyConversionService conversionService;
    private final ExchangeRateService exchangeRateService;

    public CurrencyController(CurrencyConversionService conversionService, 
                            ExchangeRateService exchangeRateService) {
        this.conversionService = conversionService;
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping("/convert")
    public ResponseEntity<ConversionResponseDto> convert(
            @RequestParam(required = true) String from,
            @RequestParam(required = true) String to,
            @RequestParam(defaultValue = "1.0") double amount) {
        
        log.info("Conversion request: from={}, to={}, amount={}", from, to, amount);
        
        if (from == null || from.trim().isEmpty() || to == null || to.trim().isEmpty()) {
            throw new CurrencyException("Both 'from' and 'to' currency parameters are required", 400);
        }

        ConversionResponseDto response = conversionService.convert(from, to, amount);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rates")
    public ResponseEntity<ExchangeRatesResponseDto> getExchangeRates() {
        log.info("Exchange rates request");
        ExchangeRatesResponseDto rates = exchangeRateService.getExchangeRates();
        return ResponseEntity.ok(rates);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Currency Conversion API is running");
    }
}
