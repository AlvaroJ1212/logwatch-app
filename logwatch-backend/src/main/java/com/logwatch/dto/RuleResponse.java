package com.logwatch.dto;

import com.logwatch.entity.Rule;
import java.time.Instant;

public class RuleResponse {

    private Long id;
    private String name;
    private Boolean enabled;
    private String severity;
    private String definitionYaml;
    private Instant updatedAt;
    private Instant lastEvaluatedAt;
    private Instant createdAt;

    public static RuleResponse from(Rule entity) {
        RuleResponse dto = new RuleResponse();
        dto.id = entity.getId();
        dto.name = entity.getName();
        dto.enabled = entity.getEnabled();
        dto.severity = entity.getSeverity();
        dto.definitionYaml = entity.getDefinitionYaml();
        dto.updatedAt = entity.getUpdatedAt();
        dto.lastEvaluatedAt = entity.getLastEvaluatedAt();
        dto.createdAt = entity.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getDefinitionYaml() { return definitionYaml; }
    public void setDefinitionYaml(String definitionYaml) { this.definitionYaml = definitionYaml; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getLastEvaluatedAt() { return lastEvaluatedAt; }
    public void setLastEvaluatedAt(Instant lastEvaluatedAt) { this.lastEvaluatedAt = lastEvaluatedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
