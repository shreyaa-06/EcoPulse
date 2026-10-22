package com.gen.ecopulse.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gen.ecopulse.entity.Alert;
import com.gen.ecopulse.entity.AlertSeverity;
import com.gen.ecopulse.entity.AlertStatus;
import com.gen.ecopulse.entity.ResourceMeter;
import com.gen.ecopulse.entity.ResourceReading;
import com.gen.ecopulse.entity.ResourceType;
import com.gen.ecopulse.repository.AlertRepository;

/**
 * Rule-based alert engine (see README for severity rules).
 * At most one alert per reading (idempotent).
 */
@Service
public class AlertEngineService {

    /** Readings up to 125% of threshold → WARNING; above that → HIGH. */
    private static final BigDecimal HIGH_SEVERITY_MULTIPLIER = new BigDecimal("1.25");

    private final AlertRepository alertRepository;
    private final RecommendationService recommendationService;

    public AlertEngineService(AlertRepository alertRepository, RecommendationService recommendationService) {
        this.alertRepository = alertRepository;
        this.recommendationService = recommendationService;
    }

    @Transactional
    public void evaluateReading(ResourceReading reading) {
        if (alertRepository.existsByReadingId(reading.getId())) {
            return;
        }

        ResourceMeter meter = reading.getMeter();
        BigDecimal threshold = meter.getAlertThreshold();
        BigDecimal value = reading.getReadingValue();

        if (value.compareTo(threshold) <= 0) {
            return;
        }

        AlertSeverity severity = determineSeverity(value, threshold);
        String message = buildMessage(meter, value, threshold, severity);

        Alert alert = new Alert();
        alert.setMeter(meter);
        alert.setReading(reading);
        alert.setAlertType("THRESHOLD_EXCEEDED");
        alert.setSeverity(severity);
        alert.setMessage(message);
        alert.setCreatedAt(LocalDateTime.now());
        alert.setStatus(AlertStatus.OPEN);
        alert = alertRepository.save(alert);

        recommendationService.createForAlert(alert);
    }

    static AlertSeverity determineSeverity(BigDecimal value, BigDecimal threshold) {
        BigDecimal highBoundary = threshold.multiply(HIGH_SEVERITY_MULTIPLIER);
        if (value.compareTo(highBoundary) > 0) {
            return AlertSeverity.HIGH;
        }
        return AlertSeverity.WARNING;
    }

    private static String buildMessage(ResourceMeter meter, BigDecimal value, BigDecimal threshold, AlertSeverity severity) {
        String resource = meter.getResourceType().name().toLowerCase();
        return String.format(
                "%s severity: %s consumption %.2f %s exceeds threshold %.2f %s for meter '%s'.",
                severity,
                resource,
                value.setScale(2, RoundingMode.HALF_UP),
                meter.getUnit(),
                threshold.setScale(2, RoundingMode.HALF_UP),
                meter.getUnit(),
                meter.getMeterName()
        );
    }

    static String recommendationFor(ResourceType type) {
        return switch (type) {
            case ENERGY -> "Inspect lighting, HVAC schedules, and idle equipment. Consider shifting high-load tasks to off-peak hours.";
            case WATER -> "Check for plumbing leaks, running taps, and unnecessary irrigation. Review water-intensive processes.";
            case WASTE -> "Review waste segregation, recycling practices, and bulk ordering to reduce packaging waste.";
        };
    }
}
