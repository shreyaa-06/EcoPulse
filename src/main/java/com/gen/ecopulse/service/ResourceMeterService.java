package com.gen.ecopulse.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.gen.ecopulse.dto.ResourceMeterRequest;
import com.gen.ecopulse.dto.ResourceMeterResponse;
import com.gen.ecopulse.entity.Facility;
import com.gen.ecopulse.entity.ResourceMeter;
import com.gen.ecopulse.entity.ResourceType;
import com.gen.ecopulse.repository.FacilityRepository;
import com.gen.ecopulse.repository.ResourceMeterRepository;

@Service
public class ResourceMeterService {

    private final ResourceMeterRepository meterRepository;
    private final FacilityRepository facilityRepository;

    public ResourceMeterService(ResourceMeterRepository meterRepository, FacilityRepository facilityRepository) {
        this.meterRepository = meterRepository;
        this.facilityRepository = facilityRepository;
    }

    @Transactional
    public ResourceMeterResponse create(ResourceMeterRequest request) {
        Facility facility = facilityRepository.findById(request.facilityId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Facility not found: " + request.facilityId()));

        if (meterRepository.existsByMeterCode(request.meterCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Meter code already exists: " + request.meterCode());
        }

        ResourceMeter meter = mapToEntity(new ResourceMeter(), request, facility);
        return ResourceMeterResponse.from(meterRepository.save(meter));
    }

    @Transactional(readOnly = true)
    public List<ResourceMeterResponse> findAll(Long facilityId, ResourceType resourceType) {
        List<ResourceMeter> meters;
        if (facilityId != null && resourceType != null) {
            meters = meterRepository.findByFacilityIdAndResourceType(facilityId, resourceType);
        } else if (facilityId != null) {
            meters = meterRepository.findByFacilityId(facilityId);
        } else if (resourceType != null) {
            meters = meterRepository.findByResourceType(resourceType);
        } else {
            meters = meterRepository.findAll();
        }
        return meters.stream().map(ResourceMeterResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ResourceMeterResponse getById(Long id) {
        return ResourceMeterResponse.from(getEntity(id));
    }

    @Transactional
    public ResourceMeterResponse update(Long id, ResourceMeterRequest request) {
        ResourceMeter meter = getEntity(id);
        Facility facility = facilityRepository.findById(request.facilityId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Facility not found: " + request.facilityId()));

        if (meterRepository.existsByMeterCodeAndIdNot(request.meterCode(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Meter code already exists: " + request.meterCode());
        }

        mapToEntity(meter, request, facility);
        return ResourceMeterResponse.from(meterRepository.save(meter));
    }

    @Transactional
    public void delete(Long id) {
        if (!meterRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Meter not found: " + id);
        }
        meterRepository.deleteById(id);
    }

    ResourceMeter getEntity(Long id) {
        return meterRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meter not found: " + id));
    }

    private ResourceMeter mapToEntity(ResourceMeter meter, ResourceMeterRequest request, Facility facility) {
        meter.setFacility(facility);
        meter.setMeterName(request.meterName());
        meter.setMeterCode(request.meterCode());
        meter.setResourceType(request.resourceType());
        String unit = request.unit();
        if (unit == null || unit.isBlank()) {
            unit = request.resourceType().defaultUnit();
        }
        validateUnit(request.resourceType(), unit);
        meter.setUnit(unit);
        meter.setAlertThreshold(request.alertThreshold());
        meter.setActive(request.active() == null || request.active());
        return meter;
    }

    static void validateUnit(ResourceType type, String unit) {
        String expected = type.defaultUnit();
        if (!expected.equalsIgnoreCase(unit.trim())) {
            throw new IllegalArgumentException(
                    "Unit '" + unit + "' is not valid for " + type + ". Expected " + expected + ".");
        }
    }
}
