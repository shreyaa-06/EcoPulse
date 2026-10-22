package com.gen.ecopulse.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gen.ecopulse.entity.Alert;
import com.gen.ecopulse.entity.AlertStatus;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    boolean existsByReadingId(Long readingId);

    Optional<Alert> findByReadingId(Long readingId);

    List<Alert> findByStatusInOrderByCreatedAtDesc(List<AlertStatus> statuses);

    long countByStatus(AlertStatus status);

    @Query("""
            SELECT COUNT(a) FROM Alert a
            WHERE a.createdAt >= :start AND a.createdAt < :end
            """)
    long countCreatedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
            SELECT a FROM Alert a
            JOIN FETCH a.meter m
            JOIN FETCH m.facility
            JOIN FETCH a.reading
            ORDER BY a.createdAt DESC
            """)
    List<Alert> findAllWithDetailsOrderByCreatedAtDesc();
}
