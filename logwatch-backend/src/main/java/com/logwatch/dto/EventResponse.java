package com.logwatch.dto;

import com.logwatch.entity.LogEvent;
import java.time.Instant;

public class EventResponse {

    private Long id;
    private Instant timestamp;
    private String source;
    private String eventType;
    private String severity;
    private String userName;
    private String sourceIp;
    private Integer httpStatus;
    private String message;
    private String rawPayload;
    private Instant createdAt;

    public static EventResponse from(LogEvent entity) {
        EventResponse dto = new EventResponse();
        dto.id = entity.getId();
        dto.timestamp = entity.getTimestamp();
        dto.source = entity.getSource();
        dto.eventType = entity.getEventType();
        dto.severity = entity.getSeverity();
        dto.userName = entity.getUserName();
        dto.sourceIp = entity.getSourceIp();
        dto.httpStatus = entity.getHttpStatus();
        dto.message = entity.getMessage();
        dto.rawPayload = entity.getRawPayload();
        dto.createdAt = entity.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public String getRawPayload() { return rawPayload; }
    public void setRawPayload(String rawPayload) { this.rawPayload = rawPayload; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
