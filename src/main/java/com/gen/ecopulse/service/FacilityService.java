
package com.gen.ecopulse.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.gen.ecopulse.entity.Facility;
import com.gen.ecopulse.repository.FacilityRepository;

@Service
public class FacilityService {

    private final FacilityRepository facilityRepository;

    public FacilityService(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

    public Facility createFacility(Facility facility) {
        return facilityRepository.save(facility);
    }

    public List<Facility> getAllFacilities() {
        return facilityRepository.findAll();
    }

    public Facility getFacilityById(Long id) {
        return facilityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Facility not found with ID: " + id
                ));
    }

    public Facility updateFacility(Long id, Facility updatedFacility) {
        Facility existingFacility = facilityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Facility not found with ID: " + id
                ));

        existingFacility.setName(updatedFacility.getName());
        existingFacility.setLocation(updatedFacility.getLocation());
        existingFacility.setType(updatedFacility.getType());

        return facilityRepository.save(existingFacility);
    }

    public void deleteFacility(Long id) {
        if (!facilityRepository.existsById(id)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Facility not found with ID: " + id
            );
        }

        facilityRepository.deleteById(id);
    }
}
