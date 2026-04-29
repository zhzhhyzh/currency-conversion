package com.currency.controller;

import com.currency.dto.ConversionResponseDto;
import com.currency.dto.ExchangeRatesResponseDto;
import com.currency.exception.CurrencyException;
import com.currency.service.CurrencyConversionService;
import com.currency.service.ExchangeRateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CurrencyController.class)
@DisplayName("Currency Controller API Tests")
class CurrencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrencyConversionService conversionService;

    @MockBean
    private ExchangeRateService exchangeRateService;

    @Test
    @DisplayName("Should return conversion response")
    void shouldReturnConversionResponse() throws Exception {
        when(conversionService.convert(eq("USD"), eq("MYR"), anyDouble()))
            .thenReturn(new ConversionResponseDto("USD", "MYR", 100, 395.15, 3.9515, LocalDateTime.now()));

        mockMvc.perform(get("/api/convert")
                .param("from", "USD")
                .param("to", "MYR")
                .param("amount", "100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.from").value("USD"))
            .andExpect(jsonPath("$.to").value("MYR"))
            .andExpect(jsonPath("$.amount").value(100.0))
            .andExpect(jsonPath("$.convertedAmount").value(395.15))
            .andExpect(jsonPath("$.rate").value(3.9515));
    }

    @Test
    @DisplayName("Should return readable error for missing source currency")
    void shouldReturnReadableErrorForMissingFromParameter() throws Exception {
        mockMvc.perform(get("/api/convert")
                .param("to", "MYR")
                .param("amount", "100"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Required query parameter 'from' is missing"))
            .andExpect(jsonPath("$.details", containsString("/api/convert")))
            .andExpect(jsonPath("$.timestamp", greaterThan(0L)));
    }

    @Test
    @DisplayName("Should return readable error for invalid amount")
    void shouldReturnReadableErrorForInvalidAmount() throws Exception {
        mockMvc.perform(get("/api/convert")
                .param("from", "USD")
                .param("to", "MYR")
                .param("amount", "abc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Query parameter 'amount' must be a valid number"));
    }

    @Test
    @DisplayName("Should return readable domain error from service")
    void shouldReturnReadableCurrencyException() throws Exception {
        when(conversionService.convert(eq("BAD"), eq("MYR"), anyDouble()))
            .thenThrow(new CurrencyException("Source currency must be a 3-letter ISO currency code", 400));

        mockMvc.perform(get("/api/convert")
                .param("from", "BAD")
                .param("to", "MYR")
                .param("amount", "100"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Source currency must be a 3-letter ISO currency code"));
    }

    @Test
    @DisplayName("Should hide unexpected details behind readable generic error")
    void shouldReturnReadableGenericError() throws Exception {
        when(conversionService.convert(eq("USD"), eq("MYR"), anyDouble()))
            .thenThrow(new RuntimeException("database password leaked"));

        mockMvc.perform(get("/api/convert")
                .param("from", "USD")
                .param("to", "MYR")
                .param("amount", "100"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.message").value("Internal Server Error"));
    }

    @Test
    @DisplayName("Should refresh rates through rates endpoint")
    void shouldReturnRatesFromRefreshEndpoint() throws Exception {
        Map<String, Double> rates = new HashMap<>();
        rates.put("USD", 1.0);
        rates.put("MYR", 3.9515);
        when(exchangeRateService.getExchangeRates())
            .thenReturn(new ExchangeRatesResponseDto("USD", rates, 1777449600L));

        mockMvc.perform(get("/api/rates"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.base").value("USD"))
            .andExpect(jsonPath("$.rates.USD").value(1.0))
            .andExpect(jsonPath("$.rates.MYR").value(3.9515))
            .andExpect(jsonPath("$.timestamp").value(1777449600L));

        verify(exchangeRateService, Mockito.times(1)).getExchangeRates();
    }

    @Test
    @DisplayName("Should return health check response")
    void shouldReturnHealthCheck() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(content().string("Currency Conversion API is running"));
    }
}
