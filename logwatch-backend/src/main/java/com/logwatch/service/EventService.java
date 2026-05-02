package com.logwatch.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logwatch.dto.EventRequest;
import com.logwatch.entity.LogEvent;
import com.logwatch.repository.LogEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);
    private final LogEventRepository repository;
    private final ObjectMapper objectMapper;

    public EventService(LogEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public LogEvent ingest(EventRequest request) {
        LogEvent event = new LogEvent();
        event.setTimestamp(request.getTimestamp());
        event.setSource(request.getSource().trim());
        event.setEventType(normalizeSeverityOrType(request.getEventType()));
        event.setSeverity(normalizeSeverityOrType(request.getSeverity()));
        event.setUserName(request.getUserName());
        event.setSourceIp(request.getSourceIp());
        event.setHttpStatus(request.getHttpStatus());
        event.setMessage(request.getMessage());

        if (request.getRawPayload() != null) {
            try {
                event.setRawPayload(objectMapper.writeValueAsString(request.getRawPayload()));
            } catch (JsonProcessingException e) {
                log.warn("No se pudo serializar rawPayload: {}", e.getMessage());
            }
        }

        LogEvent saved = repository.save(event);
        log.info("Evento ingestado: id={}, source={}, type={}", saved.getId(), saved.getSource(), saved.getEventType());
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<LogEvent> findFiltered(Instant from, Instant to, String eventType,
                                       String userName, String sourceIp, Integer httpStatus,
                                       String source, String severity, Pageable pageable) {
        return repository.findFiltered(from, to, eventType, userName, sourceIp,
                httpStatus, source, severity, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<LogEvent> findById(Long id) {
        return repository.findById(id);
    }

    private String normalizeSeverityOrType(String value) {
        return value != null ? value.trim().toUpperCase() : null;
    }
}
