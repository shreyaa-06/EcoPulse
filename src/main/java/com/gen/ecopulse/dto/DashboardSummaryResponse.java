package com.gen.ecopulse.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryResponse(
        long totalFacilities,
        BigDecimal totalEnergyKwh,
        BigDecimal totalWaterLiters,
        BigDecimal totalWasteKg,
        long activeAlerts,
        List<TrendPoint> energyTrend,
        List<TrendPoint> waterTrend,
        List<TrendPoint> wasteTrend,
        List<ResourceReadingResponse> recentReadings,
        List<AlertResponse> recentAlerts,
        List<RecommendationResponse> recommendationsNeedingAttention,
        List<FacilityConsumptionSummary> facilitySummaries
) {
    public record TrendPoint(String date, BigDecimal total) {
    }

    public record FacilityConsumptionSummary(
            Long facilityId,
            String facilityName,
            BigDecimal energyKwh,
            BigDecimal waterLiters,
            BigDecimal wasteKg
    ) {
    }
}
