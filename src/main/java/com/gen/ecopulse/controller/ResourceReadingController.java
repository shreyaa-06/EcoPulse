package com.gen.ecopulse.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.gen.ecopulse.dto.ResourceReadingRequest;
import com.gen.ecopulse.dto.ResourceReadingResponse;
import com.gen.ecopulse.entity.ResourceType;
import com.gen.ecopulse.service.ResourceReadingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/readings")
public class ResourceReadingController {

    private final ResourceReadingService readingService;

    public ResourceReadingController(ResourceReadingService readingService) {
        this.readingService = readingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceReadingResponse create(@Valid @RequestBody ResourceReadingRequest request) {
        return readingService.create(request);
    }

    @GetMapping
    public List<ResourceReadingResponse> list(
            @RequestParam(required = false) Long facilityId,
            @RequestParam(required = false) ResourceType resourceType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return readingService.findAll(facilityId, resourceType, start, end);
    }

    @GetMapping("/{id}")
    public ResourceReadingResponse getById(@PathVariable Long id) {
        return readingService.getById(id);
    }

    @GetMapping("/meter/{meterId}")
    public List<ResourceReadingResponse> getByMeter(@PathVariable Long meterId) {
        return readingService.getByMeterId(meterId);
    }

    @GetMapping("/facility/{facilityId}")
    public List<ResourceReadingResponse> getByFacility(@PathVariable Long facilityId) {
        return readingService.getByFacilityId(facilityId);
    }
}
