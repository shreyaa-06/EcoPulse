package com.gen.ecopulse.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.gen.ecopulse.dto.ResourceMeterRequest;
import com.gen.ecopulse.dto.ResourceMeterResponse;
import com.gen.ecopulse.entity.ResourceType;
import com.gen.ecopulse.service.ResourceMeterService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/meters")
public class ResourceMeterController {

    private final ResourceMeterService meterService;

    public ResourceMeterController(ResourceMeterService meterService) {
        this.meterService = meterService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceMeterResponse create(@Valid @RequestBody ResourceMeterRequest request) {
        return meterService.create(request);
    }

    @GetMapping
    public List<ResourceMeterResponse> list(
            @RequestParam(required = false) Long facilityId,
            @RequestParam(required = false) ResourceType resourceType) {
        return meterService.findAll(facilityId, resourceType);
    }

    @GetMapping("/{id}")
    public ResourceMeterResponse getById(@PathVariable Long id) {
        return meterService.getById(id);
    }

    @PutMapping("/{id}")
    public ResourceMeterResponse update(@PathVariable Long id, @Valid @RequestBody ResourceMeterRequest request) {
        return meterService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        meterService.delete(id);
    }
}
