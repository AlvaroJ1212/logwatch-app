package com.logwatch.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "log_event", indexes = {
        @Index(name = "idx_log_event_timestamp", columnList = "timestamp"),
        @Index(name = "idx_log_event_source_ip", columnList = "source_ip"),
        @Index(name = "idx_log_event_user_name", columnList = "user_name"),
        @Index(name = "idx_log_event_event_type", columnList = "event_type"),
        @Index(name = "idx_log_event_http_status", columnList = "http_status")
})
public class LogEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false, length = 100)
    private String source;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(nullable = false, length = 20)
    private String severity;

    @Column(name = "user_name", length = 100)
    private String userName;

    @Column(name = "source_ip", length = 45)
    private String sourceIp;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(length = 500)
    private String message;

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
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
