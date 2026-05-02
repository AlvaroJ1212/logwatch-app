package com.logwatch.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "rule")
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false, length = 20)
    private String severity;

    @Column(name = "definition_yaml", nullable = false, columnDefinition = "TEXT")
    private String definitionYaml;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "last_evaluated_at")
    private Instant lastEvaluatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
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
