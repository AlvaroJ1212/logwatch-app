package com.logwatch.repository;

import com.logwatch.entity.LogEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface LogEventRepository extends JpaRepository<LogEvent, Long> {

    @Query("""
            SELECT e FROM LogEvent e
            WHERE (CAST(:from AS TIMESTAMP) IS NULL OR e.timestamp >= :from)
              AND (CAST(:to AS TIMESTAMP) IS NULL OR e.timestamp <= :to)
              AND (:eventType IS NULL OR e.eventType = :eventType)
              AND (:userName IS NULL OR e.userName = :userName)
              AND (:sourceIp IS NULL OR e.sourceIp = :sourceIp)
              AND (CAST(:httpStatus AS INTEGER) IS NULL OR e.httpStatus = :httpStatus)
              AND (:source IS NULL OR e.source = :source)
              AND (:severity IS NULL OR e.severity = :severity)
            """)
    Page<LogEvent> findFiltered(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("eventType") String eventType,
            @Param("userName") String userName,
            @Param("sourceIp") String sourceIp,
            @Param("httpStatus") Integer httpStatus,
            @Param("source") String source,
            @Param("severity") String severity,
            Pageable pageable);

    @Query("""
            SELECT e FROM LogEvent e
            WHERE e.timestamp BETWEEN :windowStart AND :windowEnd
              AND (:eventType IS NULL OR e.eventType = :eventType)
              AND (:httpStatuses IS NULL OR e.httpStatus IN :httpStatuses)
            """)
    List<LogEvent> findForRuleEvaluation(
            @Param("windowStart") Instant windowStart,
            @Param("windowEnd") Instant windowEnd,
            @Param("eventType") String eventType,
            @Param("httpStatuses") List<Integer> httpStatuses);
}
