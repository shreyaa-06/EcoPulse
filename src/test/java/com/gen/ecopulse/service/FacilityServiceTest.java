package com.gen.ecopulse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import com.gen.ecopulse.entity.Facility;

@SpringBootTest
class FacilityServiceTest {

    @Autowired
    private FacilityService facilityService;

    @Test
    void crudFacility() {
        Facility created = facilityService.createFacility(new Facility("Test Office", "Block A", "Office"));
        assertThat(created.getId()).isNotNull();

        Facility fetched = facilityService.getFacilityById(created.getId());
        assertThat(fetched.getName()).isEqualTo("Test Office");

        fetched.setLocation("Block B");
        Facility updated = facilityService.updateFacility(created.getId(), fetched);
        assertThat(updated.getLocation()).isEqualTo("Block B");

        facilityService.deleteFacility(created.getId());
        assertThatThrownBy(() -> facilityService.getFacilityById(created.getId()))
                .isInstanceOf(ResponseStatusException.class);
    }
}
