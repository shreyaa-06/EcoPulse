package com.gen.ecopulse.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gen.ecopulse.dto.AlertResponse;
import com.gen.ecopulse.dto.DashboardSummaryResponse;
import com.gen.ecopulse.dto.DashboardSummaryResponse.FacilityConsumptionSummary;
import com.gen.ecopulse.dto.DashboardSummaryResponse.TrendPoint;
import com.gen.ecopulse.dto.RecommendationResponse;
import com.gen.ecopulse.dto.ResourceReadingResponse;
import com.gen.ecopulse.entity.AlertStatus;
import com.gen.ecopulse.entity.RecommendationStatus;
import com.gen.ecopulse.entity.ResourceReading;
import com.gen.ecopulse.entity.ResourceType;
import com.gen.ecopulse.repository.AlertRepository;
import com.gen.ecopulse.repository.FacilityRepository;
import com.gen.ecopulse.repository.RecommendationRepository;
import com.gen.ecopulse.repository.ResourceReadingRepository;

@Service
public class DashboardService {

    private final FacilityRepository facilityRepository;
    private final ResourceReadingRepository readingRepository;
    private final AlertRepository alertRepository;
    private final RecommendationRepository recommendationRepository;

    public DashboardService(
            FacilityRepository facilityRepository,
            ResourceReadingRepository readingRepository,
            AlertRepository alertRepository,
            RecommendationRepository recommendationRepository) {
        this.facilityRepository = facilityRepository;
        this.readingRepository = readingRepository;
        this.alertRepository = alertRepository;
        this.recommendationRepository = recommendationRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        LocalDateTime end = LocalDateTime.now().plusSeconds(1);
        LocalDateTime start = LocalDate.now().minusDays(29).atStartOfDay();
        List<ResourceReading> readings = readingRepository.findAllInRange(start, end);

        BigDecimal energy = sumByType(readings, ResourceType.ENERGY);
        BigDecimal water = sumByType(readings, ResourceType.WATER);
        BigDecimal waste = sumByType(readings, ResourceType.WASTE);

        long activeAlerts = alertRepository.countByStatus(AlertStatus.OPEN)
                + alertRepository.countByStatus(AlertStatus.ACKNOWLEDGED);

        List<ResourceReadingResponse> recentReadings = readings.stream()
                .limit(10)
                .map(ResourceReadingResponse::from)
                .toList();

        List<AlertResponse> recentAlerts = alertRepository.findAllWithDetailsOrderByCreatedAtDesc().stream()
                .limit(10)
                .map(AlertResponse::from)
                .toList();

        List<RecommendationResponse> recs = recommendationRepository.findAllWithDetailsOrderByCreatedAtDesc().stream()
                .filter(r -> r.getStatus() == RecommendationStatus.PENDING || r.getStatus() == RecommendationStatus.IN_PROGRESS)
                .limit(10)
                .map(RecommendationResponse::from)
                .toList();

        return new DashboardSummaryResponse(
                facilityRepository.count(),
                energy,
                water,
                waste,
                activeAlerts,
                buildTrend(readings, ResourceType.ENERGY),
                buildTrend(readings, ResourceType.WATER),
                buildTrend(readings, ResourceType.WASTE),
                recentReadings,
                recentAlerts,
                recs,
                buildFacilitySummaries(readings)
        );
    }

    private static BigDecimal sumByType(List<ResourceReading> readings, ResourceType type) {
        return readings.stream()
                .filter(r -> r.getMeter().getResourceType() == type)
                .map(ResourceReading::getReadingValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static List<TrendPoint> buildTrend(List<ResourceReading> readings, ResourceType type) {
        Map<LocalDate, BigDecimal> byDay = new HashMap<>();
        readings.stream()
                .filter(r -> r.getMeter().getResourceType() == type)
                .forEach(r -> {
                    LocalDate day = r.getReadingTimestamp().toLocalDate();
                    byDay.merge(day, r.getReadingValue(), BigDecimal::add);
                });
        return byDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new TrendPoint(e.getKey().toString(), e.getValue()))
                .toList();
    }

    private static List<FacilityConsumptionSummary> buildFacilitySummaries(List<ResourceReading> readings) {
        Map<Long, FacilityConsumptionSummary> map = new HashMap<>();
        for (ResourceReading r : readings) {
            var facility = r.getMeter().getFacility();
            Long fid = facility.getId();
            FacilityConsumptionSummary current = map.getOrDefault(fid,
                    new FacilityConsumptionSummary(fid, facility.getName(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
            BigDecimal e = current.energyKwh();
            BigDecimal w = current.waterLiters();
            BigDecimal wa = current.wasteKg();
            switch (r.getMeter().getResourceType()) {
                case ENERGY -> e = e.add(r.getReadingValue());
                case WATER -> w = w.add(r.getReadingValue());
                case WASTE -> wa = wa.add(r.getReadingValue());
            }
            map.put(fid, new FacilityConsumptionSummary(fid, facility.getName(), e, w, wa));
        }
        return map.values().stream()
                .sorted(Comparator.comparing(FacilityConsumptionSummary::facilityName))
                .toList();
    }
}
