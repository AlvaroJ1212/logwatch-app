CREATE TABLE log_event (
    id              BIGSERIAL       PRIMARY KEY,
    timestamp       TIMESTAMPTZ     NOT NULL,
    source          VARCHAR(100)    NOT NULL,
    event_type      VARCHAR(50)     NOT NULL,
    severity        VARCHAR(20)     NOT NULL,
    user_name       VARCHAR(100),
    source_ip       VARCHAR(45),
    http_status     INTEGER,
    message         VARCHAR(500),
    raw_payload     TEXT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_log_event_timestamp   ON log_event (timestamp);
CREATE INDEX idx_log_event_source_ip   ON log_event (source_ip);
CREATE INDEX idx_log_event_user_name   ON log_event (user_name);
CREATE INDEX idx_log_event_event_type  ON log_event (event_type);
CREATE INDEX idx_log_event_http_status ON log_event (http_status);

CREATE TABLE rule (
    id                  BIGSERIAL       PRIMARY KEY,
    name                VARCHAR(100)    NOT NULL UNIQUE,
    enabled             BOOLEAN         NOT NULL DEFAULT TRUE,
    severity            VARCHAR(20)     NOT NULL,
    definition_yaml     TEXT            NOT NULL,
    updated_at          TIMESTAMPTZ,
    last_evaluated_at   TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE TABLE alert (
    id              BIGSERIAL       PRIMARY KEY,
    rule_id         BIGINT          NOT NULL REFERENCES rule(id),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    window_start    TIMESTAMPTZ     NOT NULL,
    window_end      TIMESTAMPTZ     NOT NULL,
    group_by        VARCHAR(50),
    group_value     VARCHAR(200),
    severity        VARCHAR(20)     NOT NULL,
    event_count     INTEGER         NOT NULL,
    description     VARCHAR(500)
);

CREATE INDEX idx_alert_created_at ON alert (created_at);
CREATE INDEX idx_alert_rule_id    ON alert (rule_id);
CREATE INDEX idx_alert_severity   ON alert (severity);

CREATE TABLE alert_event (
    alert_id        BIGINT NOT NULL REFERENCES alert(id) ON DELETE CASCADE,
    log_event_id    BIGINT NOT NULL REFERENCES log_event(id) ON DELETE CASCADE,
    PRIMARY KEY (alert_id, log_event_id)
);
