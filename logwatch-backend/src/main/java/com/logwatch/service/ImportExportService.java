package com.logwatch.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logwatch.dto.EventRequest;
import com.logwatch.dto.EventResponse;
import com.logwatch.dto.ImportResult;
import com.logwatch.entity.LogEvent;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Service
public class ImportExportService {

    private static final Logger log = LoggerFactory.getLogger(ImportExportService.class);
    private static final String[] CSV_HEADERS = {
            "id", "timestamp", "source", "eventType", "severity",
            "userName", "sourceIp", "httpStatus", "message"
    };

    private final EventService eventService;
    private final ObjectMapper objectMapper;

    public ImportExportService(EventService eventService, ObjectMapper objectMapper) {
        this.eventService = eventService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ImportResult importCsv(InputStream inputStream) {
        ImportResult result = new ImportResult();
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            String[] headers = reader.readNext();
            if (headers == null) {
                result.addError("El fichero CSV está vacío");
                return result;
            }

            String[] line;
            int row = 1;
            while ((line = reader.readNext()) != null) {
                row++;
                result.setTotalRows(result.getTotalRows() + 1);
                try {
                    EventRequest req = parseCsvLine(headers, line);
                    eventService.ingest(req);
                    result.setImportedRows(result.getImportedRows() + 1);
                } catch (Exception e) {
                    result.setRejectedRows(result.getRejectedRows() + 1);
                    result.addError("Fila " + row + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            result.addError("Error leyendo CSV: " + e.getMessage());
        }
        log.info("Importación CSV: total={}, importadas={}, rechazadas={}",
                result.getTotalRows(), result.getImportedRows(), result.getRejectedRows());
        return result;
    }

    @Transactional
    public ImportResult importJson(InputStream inputStream) {
        ImportResult result = new ImportResult();
        try {
            List<Map<String, Object>> entries = objectMapper.readValue(
                    inputStream, new TypeReference<>() {});

            int row = 0;
            for (Map<String, Object> entry : entries) {
                row++;
                result.setTotalRows(result.getTotalRows() + 1);
                try {
                    EventRequest req = objectMapper.convertValue(entry, EventRequest.class);
                    if (req.getTimestamp() == null || req.getSource() == null ||
                            req.getEventType() == null || req.getSeverity() == null) {
                        throw new IllegalArgumentException("Campos obligatorios faltantes");
                    }
                    req.setRawPayload(entry);
                    eventService.ingest(req);
                    result.setImportedRows(result.getImportedRows() + 1);
                } catch (Exception e) {
                    result.setRejectedRows(result.getRejectedRows() + 1);
                    result.addError("Entrada " + row + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            result.addError("Error leyendo JSON: " + e.getMessage());
        }
        log.info("Importación JSON: total={}, importadas={}, rechazadas={}",
                result.getTotalRows(), result.getImportedRows(), result.getRejectedRows());
        return result;
    }

    public void exportCsv(List<LogEvent> events, OutputStream outputStream) throws IOException {
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream))) {
            writer.writeNext(CSV_HEADERS);
            for (LogEvent event : events) {
                writer.writeNext(new String[]{
                        String.valueOf(event.getId()),
                        event.getTimestamp().toString(),
                        event.getSource(),
                        event.getEventType(),
                        event.getSeverity(),
                        event.getUserName() != null ? event.getUserName() : "",
                        event.getSourceIp() != null ? event.getSourceIp() : "",
                        event.getHttpStatus() != null ? String.valueOf(event.getHttpStatus()) : "",
                        event.getMessage() != null ? event.getMessage() : ""
                });
            }
        }
    }

    public void exportJson(List<LogEvent> events, OutputStream outputStream) throws IOException {
        List<EventResponse> dtos = events.stream()
                .map(EventResponse::from)
                .toList();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputStream, dtos);
    }

    private EventRequest parseCsvLine(String[] headers, String[] values) {
        EventRequest req = new EventRequest();
        for (int i = 0; i < headers.length && i < values.length; i++) {
            String header = headers[i].trim().toLowerCase();
            String value = values[i].trim();
            if (value.isEmpty()) continue;

            switch (header) {
                case "timestamp" -> {
                    try {
                        req.setTimestamp(Instant.parse(value));
                    } catch (DateTimeParseException e) {
                        throw new IllegalArgumentException("Formato de timestamp inválido: " + value);
                    }
                }
                case "source" -> req.setSource(value);
                case "eventtype", "event_type" -> req.setEventType(value);
                case "severity" -> req.setSeverity(value);
                case "username", "user_name" -> req.setUserName(value);
                case "sourceip", "source_ip" -> req.setSourceIp(value);
                case "httpstatus", "http_status" -> req.setHttpStatus(Integer.parseInt(value));
                case "message" -> req.setMessage(value);
            }
        }

        if (req.getTimestamp() == null) throw new IllegalArgumentException("'timestamp' es obligatorio");
        if (req.getSource() == null) throw new IllegalArgumentException("'source' es obligatorio");
        if (req.getEventType() == null) throw new IllegalArgumentException("'eventType' es obligatorio");
        if (req.getSeverity() == null) throw new IllegalArgumentException("'severity' es obligatorio");

        return req;
    }
}
