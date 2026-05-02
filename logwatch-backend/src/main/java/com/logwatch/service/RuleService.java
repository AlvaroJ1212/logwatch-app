package com.logwatch.service;

import com.logwatch.dto.RuleRequest;
import com.logwatch.entity.Rule;
import com.logwatch.repository.RuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RuleService {

    private static final Logger log = LoggerFactory.getLogger(RuleService.class);
    private final RuleRepository repository;

    public RuleService(RuleRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Rule> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Rule> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Rule> findEnabled() {
        return repository.findByEnabledTrue();
    }

    @Transactional
    public Rule create(RuleRequest request) {
        validateYaml(request.getDefinitionYaml());

        Rule rule = new Rule();
        rule.setName(request.getName().trim());
        rule.setSeverity(request.getSeverity().trim().toUpperCase());
        rule.setDefinitionYaml(request.getDefinitionYaml());
        rule.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);

        Rule saved = repository.save(rule);
        log.info("Regla creada: id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Transactional
    public Rule update(Long id, RuleRequest request) {
        Rule rule = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Regla no encontrada: " + id));

        validateYaml(request.getDefinitionYaml());

        rule.setName(request.getName().trim());
        rule.setSeverity(request.getSeverity().trim().toUpperCase());
        rule.setDefinitionYaml(request.getDefinitionYaml());
        if (request.getEnabled() != null) {
            rule.setEnabled(request.getEnabled());
        }

        Rule saved = repository.save(rule);
        log.info("Regla actualizada: id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Transactional
    public Rule toggle(Long id) {
        Rule rule = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Regla no encontrada: " + id));
        rule.setEnabled(!rule.getEnabled());
        Rule saved = repository.save(rule);
        log.info("Regla {}: id={}, name={}", saved.getEnabled() ? "activada" : "desactivada",
                saved.getId(), saved.getName());
        return saved;
    }

    @SuppressWarnings("unchecked")
    private void validateYaml(String yamlContent) {
        try {
            Yaml yaml = new Yaml();
            Object parsed = yaml.load(yamlContent);
            if (!(parsed instanceof Map)) {
                throw new IllegalArgumentException("La definición YAML debe ser un mapa de propiedades");
            }
            Map<String, Object> def = (Map<String, Object>) parsed;

            if (!def.containsKey("match")) {
                throw new IllegalArgumentException("La definición debe incluir un bloque 'match'");
            }
            if (!def.containsKey("windowSeconds")) {
                throw new IllegalArgumentException("La definición debe incluir 'windowSeconds'");
            }
            if (!def.containsKey("threshold")) {
                throw new IllegalArgumentException("La definición debe incluir 'threshold'");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("YAML inválido: " + e.getMessage());
        }
    }
}
