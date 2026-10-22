package com.gen.ecopulse.dto;

import java.math.BigDecimal;

import com.gen.ecopulse.entity.ResourceType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ResourceMeterRequest(
        @NotNull Long facilityId,
        @NotBlank String meterName,
        @NotBlank String meterCode,
        @NotNull ResourceType resourceType,
        String unit,
        @NotNull @Positive BigDecimal alertThreshold,
        Boolean active
) {
}
