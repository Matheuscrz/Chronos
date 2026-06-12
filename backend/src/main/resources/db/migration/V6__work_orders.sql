CREATE TABLE IF NOT EXISTS work_orders (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id uuid NOT NULL,
    technician_id uuid,
    status varchar(50) NOT NULL,
    description text NOT NULL,
    completed_at timestamptz,
    cancel_reason text,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    created_by varchar(100),
    updated_by varchar(100),
    version bigint
);

CREATE TABLE IF NOT EXISTS work_order_items (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    work_order_id uuid NOT NULL REFERENCES work_orders(id) ON DELETE CASCADE,
    inventory_item_id uuid NOT NULL,
    quantity integer NOT NULL,
    unit_price numeric(19,2) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    created_by varchar(100),
    updated_by varchar(100),
    version bigint
);

CREATE INDEX IF NOT EXISTS idx_work_orders_client ON work_orders(client_id);
CREATE INDEX IF NOT EXISTS idx_work_orders_technician ON work_orders(technician_id);
CREATE INDEX IF NOT EXISTS idx_work_order_items_order ON work_order_items(work_order_id);

CREATE TRIGGER trg_work_orders_updated_at
BEFORE UPDATE ON work_orders
FOR EACH ROW
EXECUTE FUNCTION chronos_set_updated_at();

CREATE TRIGGER trg_work_order_items_updated_at
BEFORE UPDATE ON work_order_items
FOR EACH ROW
EXECUTE FUNCTION chronos_set_updated_at();
