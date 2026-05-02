package com.logwatch.desktop.model;

import java.time.Instant;
import java.util.List;

public class AlertModel {

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
    private List<EventModel> evidenceEvents;

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

    public List<EventModel> getEvidenceEvents() { return evidenceEvents; }
    public void setEvidenceEvents(List<EventModel> evidenceEvents) { this.evidenceEvents = evidenceEvents; }
}
