CREATE TABLE IF NOT EXISTS token_blacklist (
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    jti        varchar(255) NOT NULL UNIQUE,
    user_id    uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reason     varchar(100),
    expires_at timestamptz NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    created_by varchar(100),
    updated_by varchar(100),
    version    bigint
);

CREATE INDEX idx_token_blacklist_jti     ON token_blacklist(jti);
CREATE INDEX idx_token_blacklist_expires ON token_blacklist(expires_at);
CREATE INDEX idx_token_blacklist_user_id ON token_blacklist(user_id);

CREATE TRIGGER trg_token_blacklist_updated_at
BEFORE UPDATE ON token_blacklist
FOR EACH ROW EXECUTE FUNCTION chronos_set_updated_at();