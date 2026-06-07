CREATE TABLE IF NOT EXISTS auth_sessions (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    jti         varchar(255) NOT NULL UNIQUE,
    device_name varchar(255),
    ip_address  varchar(45),
    user_agent  varchar(512),
    issued_at   timestamptz NOT NULL DEFAULT now(),
    expires_at  timestamptz NOT NULL,
    revoked     boolean NOT NULL DEFAULT false,
    revoked_at  timestamptz,
    created_at  timestamptz NOT NULL DEFAULT now(),
    updated_at  timestamptz NOT NULL DEFAULT now(),
    created_by  varchar(100),
    updated_by  varchar(100),
    version     bigint
);

CREATE INDEX idx_auth_sessions_user_id ON auth_sessions(user_id);
CREATE INDEX idx_auth_sessions_jti     ON auth_sessions(jti);
CREATE INDEX idx_auth_sessions_expires ON auth_sessions(expires_at);

CREATE TRIGGER trg_auth_sessions_updated_at
BEFORE UPDATE ON auth_sessions
FOR EACH ROW EXECUTE FUNCTION chronos_set_updated_at();