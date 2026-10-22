package com.gen.ecopulse.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gen.ecopulse.dto.RecommendationResponse;
import com.gen.ecopulse.dto.StatusUpdateRequest;
import com.gen.ecopulse.entity.RecommendationStatus;
import com.gen.ecopulse.service.RecommendationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public List<RecommendationResponse> getAll() {
        return recommendationService.getAll();
    }

    @GetMapping("/{id}")
    public RecommendationResponse getById(@PathVariable Long id) {
        return recommendationService.getById(id);
    }

    @PutMapping("/{id}/status")
    public RecommendationResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        RecommendationStatus status = RecommendationStatus.valueOf(request.status().trim().toUpperCase());
        return recommendationService.updateStatus(id, status);
    }
}
