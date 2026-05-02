package com.logwatch.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "alert", indexes = {
        @Index(name = "idx_alert_created_at", columnList = "created_at"),
        @Index(name = "idx_alert_rule_id", columnList = "rule_id"),
        @Index(name = "idx_alert_severity", columnList = "severity")
})
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private Rule rule;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "window_start", nullable = false)
    private Instant windowStart;

    @Column(name = "window_end", nullable = false)
    private Instant windowEnd;

    @Column(name = "group_by", length = 50)
    private String groupBy;

    @Column(name = "group_value", length = 200)
    private String groupValue;

    @Column(nullable = false, length = 20)
    private String severity;

    @Column(name = "event_count", nullable = false)
    private Integer eventCount;

    @Column(length = 500)
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "alert_event",
            joinColumns = @JoinColumn(name = "alert_id"),
            inverseJoinColumns = @JoinColumn(name = "log_event_id")
    )
    private List<LogEvent> evidenceEvents = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Rule getRule() { return rule; }
    public void setRule(Rule rule) { this.rule = rule; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getWindowStart() { return windowStart; }
    public void setWindowStart(Instant windowStart) { this.windowStart = windowStart; }

    public Instant getWindowEnd() { return windowEnd; }
    public void setWindowEnd(Instant windowEnd) { this.windowEnd = windowEnd; }

    public String getGroupBy() { return groupBy; }
    public void setGroupBy(String groupBy) { this.groupBy = groupBy; }

    public String getGroupValue() { return groupValue; }
    public void setGroupValue(String groupValue) { this.groupValue = groupValue; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public Integer getEventCount() { return eventCount; }
    public void setEventCount(Integer eventCount) { this.eventCount = eventCount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<LogEvent> getEvidenceEvents() { return evidenceEvents; }
    public void setEvidenceEvents(List<LogEvent> evidenceEvents) { this.evidenceEvents = evidenceEvents; }
}
