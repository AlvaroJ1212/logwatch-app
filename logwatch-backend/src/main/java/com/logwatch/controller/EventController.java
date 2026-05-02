package com.logwatch.controller;

import com.logwatch.dto.EventRequest;
import com.logwatch.dto.EventResponse;
import com.logwatch.entity.LogEvent;
import com.logwatch.service.EventService;
import com.logwatch.service.ImportExportService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final ImportExportService importExportService;

    public EventController(EventService eventService, ImportExportService importExportService) {
        this.eventService = eventService;
        this.importExportService = importExportService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> ingest(@Valid @RequestBody EventRequest request) {
        LogEvent saved = eventService.ingest(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", saved.getId(), "message", "Evento registrado"));
    }

    @GetMapping
    public ResponseEntity<Page<EventResponse>> list(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String severity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        PageRequest pageable = PageRequest.of(page, Math.min(size, 100), sort);

        Page<EventResponse> result = eventService
                .findFiltered(from, to, eventType, user, ip, status, source, severity, pageable)
                .map(EventResponse::from);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getById(@PathVariable Long id) {
        return eventService.findById(id)
                .map(EventResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/export/csv")
    public void exportCsv(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String severity,
            HttpServletResponse response) throws IOException {

        PageRequest pageable = PageRequest.of(0, 10000, Sort.by("timestamp").descending());
        var events = eventService.findFiltered(from, to, eventType, user, ip,
                status, source, severity, pageable).getContent();

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=events.csv");
        importExportService.exportCsv(events, response.getOutputStream());
    }

    @GetMapping("/export/json")
    public void exportJson(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String severity,
            HttpServletResponse response) throws IOException {

        PageRequest pageable = PageRequest.of(0, 10000, Sort.by("timestamp").descending());
        var events = eventService.findFiltered(from, to, eventType, user, ip,
                status, source, severity, pageable).getContent();

        response.setContentType("application/json");
        response.setHeader("Content-Disposition", "attachment; filename=events.json");
        importExportService.exportJson(events, response.getOutputStream());
    }
}
