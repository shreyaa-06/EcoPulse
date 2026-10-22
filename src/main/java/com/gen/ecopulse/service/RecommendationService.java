package com.gen.ecopulse.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.gen.ecopulse.dto.RecommendationResponse;
import com.gen.ecopulse.entity.Alert;
import com.gen.ecopulse.entity.Recommendation;
import com.gen.ecopulse.entity.RecommendationStatus;
import com.gen.ecopulse.repository.RecommendationRepository;

@Service
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;

    public RecommendationService(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    @Transactional
    public void createForAlert(Alert alert) {
        if (recommendationRepository.findByAlertId(alert.getId()).isPresent()) {
            return;
        }

        Recommendation recommendation = new Recommendation();
        recommendation.setAlert(alert);
        recommendation.setActionText(AlertEngineService.recommendationFor(alert.getMeter().getResourceType()));
        recommendation.setStatus(RecommendationStatus.PENDING);
        recommendation.setCreatedAt(LocalDateTime.now());
        recommendationRepository.save(recommendation);
    }

    @Transactional(readOnly = true)
    public List<RecommendationResponse> getAll() {
        return recommendationRepository.findAllWithDetailsOrderByCreatedAtDesc().stream()
                .map(RecommendationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecommendationResponse getById(Long id) {
        Recommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recommendation not found: " + id));
        return RecommendationResponse.from(recommendation);
    }

    @Transactional
    public RecommendationResponse updateStatus(Long id, RecommendationStatus status) {
        Recommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recommendation not found: " + id));
        recommendation.setStatus(status);
        return RecommendationResponse.from(recommendationRepository.save(recommendation));
    }
}
