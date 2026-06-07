CREATE TABLE IF NOT EXISTS rate_limit_buckets (
    bucket_key     varchar(255) PRIMARY KEY,
    tokens         integer NOT NULL,
    last_refill_at timestamptz NOT NULL DEFAULT now(),
    updated_at     timestamptz NOT NULL DEFAULT now()
);

CREATE TRIGGER trg_rate_limit_buckets_updated_at
BEFORE UPDATE ON rate_limit_buckets
FOR EACH ROW EXECUTE FUNCTION chronos_set_updated_at();