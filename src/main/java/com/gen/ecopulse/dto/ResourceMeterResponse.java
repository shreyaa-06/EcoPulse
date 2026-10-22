package com.gen.ecopulse.dto;

import java.math.BigDecimal;

import com.gen.ecopulse.entity.ResourceMeter;
import com.gen.ecopulse.entity.ResourceType;

public record ResourceMeterResponse(
        Long id,
        Long facilityId,
        String facilityName,
        String meterName,
        String meterCode,
        ResourceType resourceType,
        String unit,
        BigDecimal alertThreshold,
        boolean active
) {
    public static ResourceMeterResponse from(ResourceMeter meter) {
        return new ResourceMeterResponse(
                meter.getId(),
                meter.getFacility().getId(),
                meter.getFacility().getName(),
                meter.getMeterName(),
                meter.getMeterCode(),
                meter.getResourceType(),
                meter.getUnit(),
                meter.getAlertThreshold(),
                meter.isActive()
        );
    }
}
