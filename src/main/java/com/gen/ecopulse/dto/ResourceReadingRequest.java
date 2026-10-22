package com.gen.ecopulse.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ResourceReadingRequest(
        @NotNull Long meterId,
        @NotNull @PositiveOrZero BigDecimal readingValue,
        String unit,
        LocalDateTime readingTimestamp,
        String notes
) {
}
