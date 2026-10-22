package com.gen.ecopulse.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TrendReportResponse(
        LocalDate startDate,
        LocalDate endDate,
        List<DailyTrend> trends
) {
    public record DailyTrend(
            LocalDate date,
            BigDecimal energyKwh,
            BigDecimal waterLiters,
            BigDecimal wasteKg
    ) {
    }
}
