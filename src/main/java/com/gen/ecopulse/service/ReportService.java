package com.gen.ecopulse.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.gen.ecopulse.dto.ReportSummaryResponse;
import com.gen.ecopulse.dto.ReportSummaryResponse.FacilityReportRow;
import com.gen.ecopulse.dto.ReportSummaryResponse.PeriodComparison;
import com.gen.ecopulse.dto.TrendReportResponse;
import com.gen.ecopulse.dto.TrendReportResponse.DailyTrend;
import com.gen.ecopulse.entity.RecommendationStatus;
import com.gen.ecopulse.entity.ResourceReading;
import com.gen.ecopulse.entity.ResourceType;
import com.gen.ecopulse.repository.AlertRepository;
import com.gen.ecopulse.repository.FacilityRepository;
import com.gen.ecopulse.repository.RecommendationRepository;
import com.gen.ecopulse.repository.ResourceReadingRepository;

@Service
public class ReportService {

    private final ResourceReadingRepository readingRepository;
    private final FacilityRepository facilityRepository;
    private final AlertRepository alertRepository;
    private final RecommendationRepository recommendationRepository;

    public ReportService(
            ResourceReadingRepository readingRepository,
            FacilityRepository facilityRepository,
            AlertRepository alertRepository,
            RecommendationRepository recommendationRepository) {
        this.readingRepository = readingRepository;
        this.facilityRepository = facilityRepository;
        this.alertRepository = alertRepository;
        this.recommendationRepository = recommendationRepository;
    }

    @Transactional(readOnly = true)
    public ReportSummaryResponse getSummary(LocalDate startDate, LocalDate endDate, Long facilityId) {
        DateRange range = resolveRange(startDate, endDate);
        List<ResourceReading> readings = filterByFacility(
                readingRepository.findAllInRange(range.start(), range.end()), facilityId);

        ReportSummaryResponse current = buildSummary(range.startDate(), range.endDate(), readings, facilityId);

        long days = ChronoUnit.DAYS.between(range.startDate(), range.endDate()) + 1;
        LocalDate prevEnd = range.startDate().minusDays(1);
        LocalDate prevStart = prevEnd.minusDays(days - 1);
        List<ResourceReading> previousReadings = filterByFacility(
                readingRepository.findAllInRange(prevStart.atStartOfDay(), prevEnd.plusDays(1).atStartOfDay()),
                facilityId);

        PeriodComparison comparison = new PeriodComparison(
                prevStart,
                prevEnd,
                sumByType(previousReadings, ResourceType.ENERGY),
                sumByType(previousReadings, ResourceType.WATER),
                sumByType(previousReadings, ResourceType.WASTE)
        );

        return new ReportSummaryResponse(
                current.startDate(),
                current.endDate(),
                current.totalEnergyKwh(),
                current.totalWaterLiters(),
                current.totalWasteKg(),
                current.alertsGenerated(),
                current.recommendationsCompleted(),
                current.byFacility(),
                comparison
        );
    }

    @Transactional(readOnly = true)
    public ReportSummaryResponse getFacilityReport(Long facilityId, LocalDate startDate, LocalDate endDate) {
        if (!facilityRepository.existsById(facilityId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Facility not found: " + facilityId);
        }
        return getSummary(startDate, endDate, facilityId);
    }

    @Transactional(readOnly = true)
    public TrendReportResponse getTrends(LocalDate startDate, LocalDate endDate, Long facilityId) {
        DateRange range = resolveRange(startDate, endDate);
        List<ResourceReading> readings = filterByFacility(
                readingRepository.findAllInRange(range.start(), range.end()), facilityId);

        Map<LocalDate, DailyTrend> byDay = new HashMap<>();
        for (ResourceReading r : readings) {
            LocalDate day = r.getReadingTimestamp().toLocalDate();
            DailyTrend existing = byDay.getOrDefault(day,
                    new DailyTrend(day, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
            BigDecimal e = existing.energyKwh();
            BigDecimal w = existing.waterLiters();
            BigDecimal wa = existing.wasteKg();
            switch (r.getMeter().getResourceType()) {
                case ENERGY -> e = e.add(r.getReadingValue());
                case WATER -> w = w.add(r.getReadingValue());
                case WASTE -> wa = wa.add(r.getReadingValue());
            }
            byDay.put(day, new DailyTrend(day, e, w, wa));
        }

        List<DailyTrend> trends = new ArrayList<>(byDay.values());
        trends.sort(Comparator.comparing(DailyTrend::date));
        return new TrendReportResponse(range.startDate(), range.endDate(), trends);
    }

    @Transactional(readOnly = true)
    public byte[] exportCsv(LocalDate startDate, LocalDate endDate, Long facilityId) {
        ReportSummaryResponse summary = getSummary(startDate, endDate, facilityId);
        StringBuilder sb = new StringBuilder();
        sb.append("startDate,endDate,totalEnergyKwh,totalWaterLiters,totalWasteKg,alertsGenerated,recommendationsCompleted\n");
        sb.append(summary.startDate()).append(',')
                .append(summary.endDate()).append(',')
                .append(summary.totalEnergyKwh()).append(',')
                .append(summary.totalWaterLiters()).append(',')
                .append(summary.totalWasteKg()).append(',')
                .append(summary.alertsGenerated()).append(',')
                .append(summary.recommendationsCompleted()).append('\n');
        sb.append("\nfacilityId,facilityName,energyKwh,waterLiters,wasteKg\n");
        for (FacilityReportRow row : summary.byFacility()) {
            sb.append(row.facilityId()).append(',')
                    .append(escapeCsv(row.facilityName())).append(',')
                    .append(row.energyKwh()).append(',')
                    .append(row.waterLiters()).append(',')
                    .append(row.wasteKg()).append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private ReportSummaryResponse buildSummary(
            LocalDate startDate,
            LocalDate endDate,
            List<ResourceReading> readings,
            Long facilityFilter) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        Map<Long, FacilityReportRow> byFacility = new HashMap<>();
        for (ResourceReading r : readings) {
            var facility = r.getMeter().getFacility();
            FacilityReportRow row = byFacility.getOrDefault(facility.getId(),
                    new FacilityReportRow(facility.getId(), facility.getName(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
            BigDecimal e = row.energyKwh();
            BigDecimal w = row.waterLiters();
            BigDecimal wa = row.wasteKg();
            switch (r.getMeter().getResourceType()) {
                case ENERGY -> e = e.add(r.getReadingValue());
                case WATER -> w = w.add(r.getReadingValue());
                case WASTE -> wa = wa.add(r.getReadingValue());
            }
            byFacility.put(facility.getId(), new FacilityReportRow(facility.getId(), facility.getName(), e, w, wa));
        }

        return new ReportSummaryResponse(
                startDate,
                endDate,
                sumByType(readings, ResourceType.ENERGY),
                sumByType(readings, ResourceType.WATER),
                sumByType(readings, ResourceType.WASTE),
                alertRepository.countCreatedBetween(start, end),
                recommendationRepository.countByStatusAndCreatedAtBetween(RecommendationStatus.COMPLETED, start, end),
                byFacility.values().stream().sorted(Comparator.comparing(FacilityReportRow::facilityName)).toList(),
                null
        );
    }

    private static List<ResourceReading> filterByFacility(List<ResourceReading> readings, Long facilityId) {
        if (facilityId == null) {
            return readings;
        }
        return readings.stream()
                .filter(r -> r.getMeter().getFacility().getId().equals(facilityId))
                .toList();
    }

    private static BigDecimal sumByType(List<ResourceReading> readings, ResourceType type) {
        return readings.stream()
                .filter(r -> r.getMeter().getResourceType() == type)
                .map(ResourceReading::getReadingValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static DateRange resolveRange(LocalDate startDate, LocalDate endDate) {
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        LocalDate start = startDate != null ? startDate : end.minusDays(29);
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("startDate must not be after endDate");
        }
        return new DateRange(start, end, start.atStartOfDay(), end.plusDays(1).atStartOfDay());
    }

    private static String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private record DateRange(LocalDate startDate, LocalDate endDate, LocalDateTime start, LocalDateTime end) {
    }
}
