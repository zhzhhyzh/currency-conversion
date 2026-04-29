package com.currency.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversionResponseDto {
    private String from;
    private String to;
    private double amount;
    private double convertedAmount;
    private double rate;
    private LocalDateTime timestamp;
}
