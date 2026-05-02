package com.logwatch.repository;

import com.logwatch.entity.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    @Query("""
            SELECT a FROM Alert a JOIN FETCH a.rule
            WHERE (CAST(:from AS TIMESTAMP) IS NULL OR a.createdAt >= :from)
              AND (CAST(:to AS TIMESTAMP) IS NULL OR a.createdAt <= :to)
              AND (CAST(:ruleId AS LONG) IS NULL OR a.rule.id = :ruleId)
              AND (:severity IS NULL OR a.severity = :severity)
            """)
    Page<Alert> findFiltered(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("ruleId") Long ruleId,
            @Param("severity") String severity,
            Pageable pageable);

    @Query("SELECT a FROM Alert a JOIN FETCH a.rule LEFT JOIN FETCH a.evidenceEvents WHERE a.id = :id")
    Optional<Alert> findByIdWithEvidence(@Param("id") Long id);

    @Query("""
            SELECT COUNT(a) > 0 FROM Alert a
            WHERE a.rule.id = :ruleId
              AND a.groupValue = :groupValue
              AND a.createdAt > :cooldownThreshold
            """)
    boolean existsRecentAlert(
            @Param("ruleId") Long ruleId,
            @Param("groupValue") String groupValue,
            @Param("cooldownThreshold") Instant cooldownThreshold);
}
