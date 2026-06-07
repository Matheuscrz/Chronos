CREATE TABLE IF NOT EXISTS security_audit_logs (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    event_timestamp timestamptz NOT NULL DEFAULT now(),
    event_type varchar(100) NOT NULL,
    user_id uuid,
    username varchar(100),
    ip_address varchar(45),
    user_agent varchar(512),
    status varchar(50),
    details text,
    correlation_id varchar(100),
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_audit_event_type ON security_audit_logs(event_type);
CREATE INDEX IF NOT EXISTS idx_audit_user_id ON security_audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON security_audit_logs(event_timestamp);
