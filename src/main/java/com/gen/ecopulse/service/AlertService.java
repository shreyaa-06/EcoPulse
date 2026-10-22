package com.gen.ecopulse.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.gen.ecopulse.dto.AlertResponse;
import com.gen.ecopulse.entity.Alert;
import com.gen.ecopulse.entity.AlertStatus;
import com.gen.ecopulse.repository.AlertRepository;

@Service
public class AlertService {

    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getAll() {
        return alertRepository.findAllWithDetailsOrderByCreatedAtDesc().stream()
                .map(AlertResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AlertResponse getById(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found: " + id));
        return AlertResponse.from(alert);
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getActive() {
        return alertRepository.findByStatusInOrderByCreatedAtDesc(
                        List.of(AlertStatus.OPEN, AlertStatus.ACKNOWLEDGED))
                .stream()
                .map(AlertResponse::from)
                .toList();
    }

    @Transactional
    public AlertResponse updateStatus(Long id, AlertStatus status) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found: " + id));
        alert.setStatus(status);
        return AlertResponse.from(alertRepository.save(alert));
    }
}
