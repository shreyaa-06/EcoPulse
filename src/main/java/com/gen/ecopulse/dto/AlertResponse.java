package com.gen.ecopulse.dto;

import java.time.LocalDateTime;

import com.gen.ecopulse.entity.Alert;
import com.gen.ecopulse.entity.AlertSeverity;
import com.gen.ecopulse.entity.AlertStatus;
import com.gen.ecopulse.entity.ResourceType;

public record AlertResponse(
        Long id,
        Long meterId,
        String meterName,
        Long readingId,
        Long facilityId,
        String facilityName,
        ResourceType resourceType,
        String alertType,
        AlertSeverity severity,
        String message,
        LocalDateTime createdAt,
        AlertStatus status
) {
    public static AlertResponse from(Alert alert) {
        var meter = alert.getMeter();
        var facility = meter.getFacility();
        return new AlertResponse(
                alert.getId(),
                meter.getId(),
                meter.getMeterName(),
                alert.getReading().getId(),
                facility.getId(),
                facility.getName(),
                meter.getResourceType(),
                alert.getAlertType(),
                alert.getSeverity(),
                alert.getMessage(),
                alert.getCreatedAt(),
                alert.getStatus()
        );
    }
}
