package com.gen.ecopulse.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gen.ecopulse.entity.ResourceReading;
import com.gen.ecopulse.entity.ResourceType;

public interface ResourceReadingRepository extends JpaRepository<ResourceReading, Long> {

    List<ResourceReading> findByMeterIdOrderByReadingTimestampDesc(Long meterId);

    @Query("""
            SELECT r FROM ResourceReading r
            JOIN r.meter m
            WHERE m.facility.id = :facilityId
            ORDER BY r.readingTimestamp DESC
            """)
    List<ResourceReading> findByFacilityId(@Param("facilityId") Long facilityId);

    @Query("""
            SELECT r FROM ResourceReading r
            JOIN r.meter m
            WHERE (:facilityId IS NULL OR m.facility.id = :facilityId)
              AND (:resourceType IS NULL OR m.resourceType = :resourceType)
              AND r.readingTimestamp >= :start
              AND r.readingTimestamp < :end
            ORDER BY r.readingTimestamp DESC
            """)
    List<ResourceReading> findFiltered(
            @Param("facilityId") Long facilityId,
            @Param("resourceType") ResourceType resourceType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            SELECT r FROM ResourceReading r
            JOIN FETCH r.meter m
            JOIN FETCH m.facility
            WHERE r.readingTimestamp >= :start AND r.readingTimestamp < :end
            ORDER BY r.readingTimestamp DESC
            """)
    List<ResourceReading> findAllInRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
