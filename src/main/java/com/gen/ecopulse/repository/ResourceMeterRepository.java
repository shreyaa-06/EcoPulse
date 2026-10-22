package com.gen.ecopulse.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gen.ecopulse.entity.ResourceMeter;
import com.gen.ecopulse.entity.ResourceType;

public interface ResourceMeterRepository extends JpaRepository<ResourceMeter, Long> {

    List<ResourceMeter> findByFacilityId(Long facilityId);

    List<ResourceMeter> findByResourceType(ResourceType resourceType);

    List<ResourceMeter> findByFacilityIdAndResourceType(Long facilityId, ResourceType resourceType);

    boolean existsByMeterCode(String meterCode);

    boolean existsByMeterCodeAndIdNot(String meterCode, Long id);
}
