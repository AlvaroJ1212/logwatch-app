package com.logwatch.controller;

import com.logwatch.dto.AlertResponse;
import com.logwatch.service.AlertService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public ResponseEntity<Page<AlertResponse>> list(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) Long ruleId,
            @RequestParam(required = false) String severity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageable = PageRequest.of(page, Math.min(size, 100),
                Sort.by("createdAt").descending());

        Page<AlertResponse> result = alertService
                .findFiltered(from, to, ruleId, severity, pageable)
                .map(AlertResponse::from);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertResponse> getById(@PathVariable Long id) {
        return alertService.findByIdWithEvidence(id)
                .map(AlertResponse::fromWithEvidence)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
