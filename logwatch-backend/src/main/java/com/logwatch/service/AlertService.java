package com.logwatch.service;

import com.logwatch.entity.Alert;
import com.logwatch.repository.AlertRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class AlertService {

    private final AlertRepository repository;

    public AlertService(AlertRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<Alert> findFiltered(Instant from, Instant to, Long ruleId,
                                     String severity, Pageable pageable) {
        return repository.findFiltered(from, to, ruleId, severity, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Alert> findByIdWithEvidence(Long id) {
        return repository.findByIdWithEvidence(id);
    }
}
