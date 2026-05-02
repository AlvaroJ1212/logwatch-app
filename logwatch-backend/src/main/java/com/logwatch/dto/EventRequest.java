package com.logwatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Map;

public class EventRequest {

    @NotNull(message = "El campo 'timestamp' es obligatorio")
    private Instant timestamp;

    @NotBlank(message = "El campo 'source' es obligatorio")
    private String source;

    @NotBlank(message = "El campo 'eventType' es obligatorio")
    private String eventType;

    @NotBlank(message = "El campo 'severity' es obligatorio")
    private String severity;

    private String userName;
    private String sourceIp;
    private Integer httpStatus;
    private String message;
    private Map<String, Object> rawPayload;

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getSourceIp() { return sourceIp; }
    public void setSourceIp(String sourceIp) { this.sourceIp = sourceIp; }

    public Integer getHttpStatus() { return httpStatus; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Map<String, Object> getRawPayload() { return rawPayload; }
    public void setRawPayload(Map<String, Object> rawPayload) { this.rawPayload = rawPayload; }
}
