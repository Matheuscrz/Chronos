CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS users (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    username varchar(100) NOT NULL UNIQUE,
    full_name varchar(255),
    email varchar(255) UNIQUE,
    password_hash varchar(255),
    role varchar(50) NOT NULL DEFAULT 'CLIENTE',
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    created_by varchar(100),
    updated_by varchar(100),
    version bigint
);

CREATE TABLE IF NOT EXISTS billing_accounts (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    balance numeric(19,2) NOT NULL DEFAULT 0,
    status varchar(50) NOT NULL DEFAULT 'ACTIVE',
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    created_by varchar(100),
    updated_by varchar(100),
    version bigint,
    CONSTRAINT balance_non_negative CHECK (balance >= 0)
);

CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_billing_owner ON billing_accounts(owner_id);

-- trigger para atualizar updated_at automaticamente
CREATE OR REPLACE FUNCTION chronos_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION chronos_set_updated_at();

CREATE TRIGGER trg_billing_updated_at
BEFORE UPDATE ON billing_accounts
FOR EACH ROW
EXECUTE FUNCTION chronos_set_updated_at();