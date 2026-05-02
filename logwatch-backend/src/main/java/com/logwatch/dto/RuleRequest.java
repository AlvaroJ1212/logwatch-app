package com.logwatch.dto;

import jakarta.validation.constraints.NotBlank;

public class RuleRequest {

    @NotBlank(message = "El campo 'name' es obligatorio")
    private String name;

    @NotBlank(message = "El campo 'severity' es obligatorio")
    private String severity;

    @NotBlank(message = "El campo 'definitionYaml' es obligatorio")
    private String definitionYaml;

    private Boolean enabled = true;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getDefinitionYaml() { return definitionYaml; }
    public void setDefinitionYaml(String definitionYaml) { this.definitionYaml = definitionYaml; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
