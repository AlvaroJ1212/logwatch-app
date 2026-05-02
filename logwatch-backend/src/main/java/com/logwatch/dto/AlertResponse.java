package com.logwatch.dto;

import com.logwatch.entity.Alert;
import java.time.Instant;
import java.util.List;

public class AlertResponse {

    private Long id;
    private Long ruleId;
    private String ruleName;
    private Instant createdAt;
    private Instant windowStart;
    private Instant windowEnd;
    private String groupBy;
    private String groupValue;
    private String severity;
    private Integer eventCount;
    private String description;
    private List<EventResponse> evidenceEvents;

    public static AlertResponse from(Alert entity) {
        AlertResponse dto = new AlertResponse();
        dto.id = entity.getId();
        dto.ruleId = entity.getRule().getId();
        dto.ruleName = entity.getRule().getName();
        dto.createdAt = entity.getCreatedAt();
        dto.windowStart = entity.getWindowStart();
        dto.windowEnd = entity.getWindowEnd();
        dto.groupBy = entity.getGroupBy();
        dto.groupValue = entity.getGroupValue();
        dto.severity = entity.getSeverity();
        dto.eventCount = entity.getEventCount();
        dto.description = entity.getDescription();
        return dto;
    }

    public static AlertResponse fromWithEvidence(Alert entity) {
        AlertResponse dto = from(entity);
        dto.evidenceEvents = entity.getEvidenceEvents().stream()
                .map(EventResponse::from)
                .toList();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

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

    public List<EventResponse> getEvidenceEvents() { return evidenceEvents; }
    public void setEvidenceEvents(List<EventResponse> evidenceEvents) { this.evidenceEvents = evidenceEvents; }
}
