package com.logwatch.service;

import com.logwatch.entity.Alert;
import com.logwatch.entity.LogEvent;
import com.logwatch.entity.Rule;
import com.logwatch.repository.AlertRepository;
import com.logwatch.repository.LogEventRepository;
import com.logwatch.repository.RuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.Yaml;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Motor de reglas que evalua periodicamente las reglas activas contra los eventos
 * almacenados, generando alertas cuando se superan los umbrales configurados.
 */
@Component
public class RuleEngine {

    private static final Logger log = LoggerFactory.getLogger(RuleEngine.class);

    private final RuleRepository ruleRepository;
    private final LogEventRepository eventRepository;
    private final AlertRepository alertRepository;

    public RuleEngine(RuleRepository ruleRepository,
                      LogEventRepository eventRepository,
                      AlertRepository alertRepository) {
        this.ruleRepository = ruleRepository;
        this.eventRepository = eventRepository;
        this.alertRepository = alertRepository;
    }

    @Scheduled(fixedDelayString = "${logwatch.rule-engine.interval-ms:30000}")
    @Transactional
    public void evaluate() {
        List<Rule> enabledRules = ruleRepository.findByEnabledTrue();
        if (enabledRules.isEmpty()) return;

        log.debug("Evaluando {} regla(s) activa(s)", enabledRules.size());
        Instant now = Instant.now();

        for (Rule rule : enabledRules) {
            try {
                evaluateRule(rule, now);
                rule.setLastEvaluatedAt(now);
                ruleRepository.save(rule);
            } catch (Exception e) {
                log.error("Error evaluando regla '{}': {}", rule.getName(), e.getMessage(), e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void evaluateRule(Rule rule, Instant now) {
        Yaml yaml = new Yaml();
        Map<String, Object> def = yaml.load(rule.getDefinitionYaml());

        Map<String, Object> match = (Map<String, Object>) def.get("match");
        String eventType = (String) match.get("eventType");

        List<Integer> httpStatuses = null;
        Object statusObj = match.get("httpStatus");
        if (statusObj instanceof List) {
            httpStatuses = ((List<?>) statusObj).stream()
                    .map(o -> ((Number) o).intValue())
                    .collect(Collectors.toList());
        } else if (statusObj instanceof Number) {
            httpStatuses = List.of(((Number) statusObj).intValue());
        }

        String groupBy = (String) def.get("groupBy");
        int windowSeconds = ((Number) def.get("windowSeconds")).intValue();
        int threshold = ((Number) def.get("threshold")).intValue();
        int cooldownSeconds = def.containsKey("cooldownSeconds")
                ? ((Number) def.get("cooldownSeconds")).intValue()
                : 0;
        String description = (String) def.getOrDefault("description", rule.getName());

        Instant windowStart = now.minusSeconds(windowSeconds);

        List<LogEvent> candidates = eventRepository.findForRuleEvaluation(
                windowStart, now, eventType, httpStatuses);

        Map<String, List<LogEvent>> grouped = groupEvents(candidates, groupBy);

        for (Map.Entry<String, List<LogEvent>> entry : grouped.entrySet()) {
            String groupValue = entry.getKey();
            List<LogEvent> events = entry.getValue();

            if (events.size() >= threshold) {
                if (cooldownSeconds > 0) {
                    Instant cooldownThreshold = now.minusSeconds(cooldownSeconds);
                    boolean recentExists = alertRepository.existsRecentAlert(
                            rule.getId(), groupValue, cooldownThreshold);
                    if (recentExists) {
                        log.debug("Cooldown activo para regla '{}', grupo '{}'", rule.getName(), groupValue);
                        continue;
                    }
                }

                Alert alert = new Alert();
                alert.setRule(rule);
                alert.setWindowStart(windowStart);
                alert.setWindowEnd(now);
                alert.setGroupBy(groupBy);
                alert.setGroupValue(groupValue);
                alert.setSeverity(rule.getSeverity());
                alert.setEventCount(events.size());
                alert.setDescription(description);
                alert.setEvidenceEvents(new ArrayList<>(events));

                alertRepository.save(alert);
                log.info("ALERTA generada: regla='{}', grupo='{}', count={}, severidad={}",
                        rule.getName(), groupValue, events.size(), rule.getSeverity());
            }
        }
    }

    private Map<String, List<LogEvent>> groupEvents(List<LogEvent> events, String groupBy) {
        if (groupBy == null || groupBy.isBlank()) {
            return Map.of("_all", events);
        }

        return events.stream().collect(Collectors.groupingBy(event -> {
            String value = switch (groupBy.toLowerCase()) {
                case "ip", "sourceip", "source_ip" -> event.getSourceIp();
                case "user", "username", "user_name" -> event.getUserName();
                case "source" -> event.getSource();
                default -> "_unknown";
            };
            return value != null ? value : "_null";
        }));
    }
}
