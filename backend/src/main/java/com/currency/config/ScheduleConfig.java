package com.currency.config;

import com.currency.service.ExchangeRateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "exchange-rates.schedule.enabled", havingValue = "true")
public class ScheduleConfig {

    private final ExchangeRateService exchangeRateService;

    public ScheduleConfig(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    /**
     * Refresh exchange rates every hour to ensure fresh data
     * This will also trigger cache update based on cutoff times
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void refreshExchangeRates() {
        try {
            log.info("Scheduled task: Refreshing exchange rates");
            exchangeRateService.getLatestRates();
            log.info("Exchange rates refreshed successfully");
        } catch (Exception e) {
            log.error("Error refreshing exchange rates in scheduled task: ", e);
        }
    }

    /**
     * Refresh rates at specific times for key currencies
     * Runs every 30 minutes to check cutoff times
     */
    @Scheduled(fixedRate = 1800000) // 30 minutes
    public void checkAndRefreshByCutoffTime() {
        try {
            log.debug("Scheduled task: Checking cutoff times for currency rate refreshes");
            // This will be enhanced to refresh specific currencies based on their cutoff times
            exchangeRateService.getLatestRates();
        } catch (Exception e) {
            log.error("Error in cutoff time refresh task: ", e);
        }
    }
}
