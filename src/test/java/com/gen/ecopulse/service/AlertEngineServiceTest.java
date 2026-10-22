package com.gen.ecopulse.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.gen.ecopulse.entity.AlertSeverity;

class AlertEngineServiceTest {

    @Test
    void severityWarningWhenSlightlyAboveThreshold() {
        BigDecimal threshold = new BigDecimal("100");
        BigDecimal value = new BigDecimal("110");
        assertThat(AlertEngineService.determineSeverity(value, threshold)).isEqualTo(AlertSeverity.WARNING);
    }

    @Test
    void severityHighWhenWellAboveThreshold() {
        BigDecimal threshold = new BigDecimal("100");
        BigDecimal value = new BigDecimal("130");
        assertThat(AlertEngineService.determineSeverity(value, threshold)).isEqualTo(AlertSeverity.HIGH);
    }
}
