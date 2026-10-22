package com.gen.ecopulse.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gen.ecopulse.dto.AlertResponse;
import com.gen.ecopulse.dto.StatusUpdateRequest;
import com.gen.ecopulse.entity.AlertStatus;
import com.gen.ecopulse.service.AlertService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public List<AlertResponse> getAll() {
        return alertService.getAll();
    }

    @GetMapping("/active")
    public List<AlertResponse> getActive() {
        return alertService.getActive();
    }

    @GetMapping("/{id}")
    public AlertResponse getById(@PathVariable Long id) {
        return alertService.getById(id);
    }

    @PutMapping("/{id}/status")
    public AlertResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        AlertStatus status = AlertStatus.valueOf(request.status().trim().toUpperCase());
        return alertService.updateStatus(id, status);
    }
}
