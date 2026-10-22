package com.gen.ecopulse.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gen.ecopulse.dto.ResourceMeterRequest;
import com.gen.ecopulse.dto.ResourceReadingRequest;
import com.gen.ecopulse.entity.Facility;
import com.gen.ecopulse.entity.ResourceType;
import com.gen.ecopulse.repository.AlertRepository;
import com.gen.ecopulse.repository.RecommendationRepository;

@SpringBootTest
class ReadingAlertFlowTest {

    @Autowired
    private FacilityService facilityService;
    @Autowired
    private ResourceMeterService meterService;
    @Autowired
    private ResourceReadingService readingService;
    @Autowired
    private AlertRepository alertRepository;
    @Autowired
    private RecommendationRepository recommendationRepository;

    @Test
    void highReadingCreatesAlertAndRecommendationOnce() {
        Facility facility = facilityService.createFacility(new Facility("Flow Test", "Campus", "College"));
        var meter = meterService.create(new ResourceMeterRequest(
                facility.getId(),
                "Main Energy",
                "ENG-FLOW-1",
                ResourceType.ENERGY,
                "kWh",
                new BigDecimal("50"),
                true
        ));

        readingService.create(new ResourceReadingRequest(meter.id(), new BigDecimal("30"), "kWh", null, "normal"));
        readingService.create(new ResourceReadingRequest(meter.id(), new BigDecimal("80"), "kWh", null, "peak day"));

        assertThat(alertRepository.count()).isEqualTo(1);
        assertThat(recommendationRepository.count()).isEqualTo(1);
    }
}
