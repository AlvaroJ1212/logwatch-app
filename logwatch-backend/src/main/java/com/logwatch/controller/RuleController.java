package com.logwatch.controller;

import com.logwatch.dto.RuleRequest;
import com.logwatch.dto.RuleResponse;
import com.logwatch.service.RuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
public class RuleController {

    private final RuleService ruleService;

    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @GetMapping
    public ResponseEntity<List<RuleResponse>> list() {
        List<RuleResponse> rules = ruleService.findAll().stream()
                .map(RuleResponse::from)
                .toList();
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RuleResponse> getById(@PathVariable Long id) {
        return ruleService.findById(id)
                .map(RuleResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RuleResponse> create(@Valid @RequestBody RuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RuleResponse.from(ruleService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RuleResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody RuleRequest request) {
        return ResponseEntity.ok(RuleResponse.from(ruleService.update(id, request)));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<RuleResponse> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(RuleResponse.from(ruleService.toggle(id)));
    }
}
