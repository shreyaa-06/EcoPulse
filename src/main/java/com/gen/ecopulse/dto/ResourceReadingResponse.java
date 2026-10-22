package com.gen.ecopulse.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.gen.ecopulse.entity.ResourceReading;
import com.gen.ecopulse.entity.ResourceType;

public record ResourceReadingResponse(
        Long id,
        Long meterId,
        String meterName,
        Long facilityId,
        String facilityName,
        ResourceType resourceType,
        BigDecimal readingValue,
        String unit,
        LocalDateTime readingTimestamp,
        String notes
) {
    public static ResourceReadingResponse from(ResourceReading reading) {
        var meter = reading.getMeter();
        var facility = meter.getFacility();
        return new ResourceReadingResponse(
                reading.getId(),
                meter.getId(),
                meter.getMeterName(),
                facility.getId(),
                facility.getName(),
                meter.getResourceType(),
                reading.getReadingValue(),
                reading.getUnit(),
                reading.getReadingTimestamp(),
                reading.getNotes()
        );
    }
}
