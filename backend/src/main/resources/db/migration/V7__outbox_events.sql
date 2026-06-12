CREATE TABLE IF NOT EXISTS outbox_events (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_id uuid NOT NULL,
    aggregate_type varchar(100) NOT NULL,
    event_type varchar(100) NOT NULL,
    payload text NOT NULL,
    status varchar(50) NOT NULL,
    processed_at timestamptz,
    error_message text,
    retry_count integer NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    created_by varchar(100),
    updated_by varchar(100),
    version bigint
);

CREATE INDEX IF NOT EXISTS idx_outbox_status_created ON outbox_events(status, created_at);

CREATE TRIGGER trg_outbox_updated_at
BEFORE UPDATE ON outbox_events
FOR EACH ROW
EXECUTE FUNCTION chronos_set_updated_at();
