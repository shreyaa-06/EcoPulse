package com.gen.ecopulse.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.gen.ecopulse.dto.ResourceReadingRequest;
import com.gen.ecopulse.dto.ResourceReadingResponse;
import com.gen.ecopulse.entity.ResourceMeter;
import com.gen.ecopulse.entity.ResourceReading;
import com.gen.ecopulse.entity.ResourceType;
import com.gen.ecopulse.repository.ResourceReadingRepository;

@Service
public class ResourceReadingService {

    private final ResourceReadingRepository readingRepository;
    private final ResourceMeterService meterService;
    private final AlertEngineService alertEngineService;

    public ResourceReadingService(
            ResourceReadingRepository readingRepository,
            ResourceMeterService meterService,
            AlertEngineService alertEngineService) {
        this.readingRepository = readingRepository;
        this.meterService = meterService;
        this.alertEngineService = alertEngineService;
    }

    @Transactional
    public ResourceReadingResponse create(ResourceReadingRequest request) {
        ResourceMeter meter = meterService.getEntity(request.meterId());
        if (!meter.isActive()) {
            throw new IllegalArgumentException("Cannot record reading for inactive meter: " + meter.getId());
        }

        String unit = request.unit();
        if (unit == null || unit.isBlank()) {
            unit = meter.getUnit();
        }
        ResourceMeterService.validateUnit(meter.getResourceType(), unit);

        ResourceReading reading = new ResourceReading();
        reading.setMeter(meter);
        reading.setReadingValue(request.readingValue());
        reading.setUnit(unit);
        reading.setReadingTimestamp(
                request.readingTimestamp() != null ? request.readingTimestamp() : LocalDateTime.now());
        reading.setNotes(request.notes());

        reading = readingRepository.save(reading);
        alertEngineService.evaluateReading(reading);

        return ResourceReadingResponse.from(reading);
    }

    @Transactional(readOnly = true)
    public List<ResourceReadingResponse> findAll(
            Long facilityId,
            ResourceType resourceType,
            LocalDateTime start,
            LocalDateTime end) {
        LocalDateTime rangeStart = start != null ? start : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime rangeEnd = end != null ? end : LocalDateTime.now().plusYears(10);
        return readingRepository.findFiltered(facilityId, resourceType, rangeStart, rangeEnd).stream()
                .map(ResourceReadingResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResourceReadingResponse getById(Long id) {
        ResourceReading reading = readingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reading not found: " + id));
        return ResourceReadingResponse.from(reading);
    }

    @Transactional(readOnly = true)
    public List<ResourceReadingResponse> getByMeterId(Long meterId) {
        meterService.getEntity(meterId);
        return readingRepository.findByMeterIdOrderByReadingTimestampDesc(meterId).stream()
                .map(ResourceReadingResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ResourceReadingResponse> getByFacilityId(Long facilityId) {
        return readingRepository.findByFacilityId(facilityId).stream()
                .map(ResourceReadingResponse::from)
                .toList();
    }
}
