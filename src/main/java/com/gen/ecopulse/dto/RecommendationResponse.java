package com.gen.ecopulse.dto;

import java.time.LocalDateTime;

import com.gen.ecopulse.entity.Recommendation;
import com.gen.ecopulse.entity.RecommendationStatus;
import com.gen.ecopulse.entity.ResourceType;

public record RecommendationResponse(
        Long id,
        Long alertId,
        Long facilityId,
        String facilityName,
        ResourceType resourceType,
        String actionText,
        RecommendationStatus status,
        LocalDateTime createdAt
) {
    public static RecommendationResponse from(Recommendation recommendation) {
        var alert = recommendation.getAlert();
        var meter = alert.getMeter();
        var facility = meter.getFacility();
        return new RecommendationResponse(
                recommendation.getId(),
                alert.getId(),
                facility.getId(),
                facility.getName(),
                meter.getResourceType(),
                recommendation.getActionText(),
                recommendation.getStatus(),
                recommendation.getCreatedAt()
        );
    }
}
