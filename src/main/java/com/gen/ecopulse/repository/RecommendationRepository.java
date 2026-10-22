package com.gen.ecopulse.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gen.ecopulse.entity.Recommendation;
import com.gen.ecopulse.entity.RecommendationStatus;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    Optional<Recommendation> findByAlertId(Long alertId);

    List<Recommendation> findByStatusInOrderByCreatedAtDesc(List<RecommendationStatus> statuses);

    long countByStatusAndCreatedAtBetween(
            RecommendationStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
            SELECT r FROM Recommendation r
            JOIN FETCH r.alert a
            JOIN FETCH a.meter m
            JOIN FETCH m.facility
            ORDER BY r.createdAt DESC
            """)
    List<Recommendation> findAllWithDetailsOrderByCreatedAtDesc();
}
