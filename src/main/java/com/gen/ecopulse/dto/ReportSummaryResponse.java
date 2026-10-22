package com.gen.ecopulse.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReportSummaryResponse(
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalEnergyKwh,
        BigDecimal totalWaterLiters,
        BigDecimal totalWasteKg,
        long alertsGenerated,
        long recommendationsCompleted,
        List<FacilityReportRow> byFacility,
        PeriodComparison comparison
) {
    public record FacilityReportRow(
            Long facilityId,
            String facilityName,
            BigDecimal energyKwh,
            BigDecimal waterLiters,
            BigDecimal wasteKg
    ) {
    }

    public record PeriodComparison(
            LocalDate previousStartDate,
            LocalDate previousEndDate,
            BigDecimal previousEnergyKwh,
            BigDecimal previousWaterLiters,
            BigDecimal previousWasteKg
    ) {
    }
}
