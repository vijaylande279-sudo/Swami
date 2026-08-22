CREATE TABLE orders (
    id           BIGSERIAL PRIMARY KEY,
    table_id     BIGINT REFERENCES dining_tables(id),
    waiter_id    BIGINT REFERENCES users(id),
    session_type VARCHAR(20),
    status       VARCHAR(30) DEFAULT 'OPEN',
    notes        TEXT,
    created_at   TIMESTAMPTZ DEFAULT NOW(),
    closed_at    TIMESTAMPTZ
);

CREATE TABLE order_items (
    id           BIGSERIAL PRIMARY KEY,
    order_id     BIGINT REFERENCES orders(id) ON DELETE CASCADE,
    menu_item_id BIGINT REFERENCES menu_items(id),
    quantity     INT NOT NULL DEFAULT 1,
    unit_price   NUMERIC(10, 2) NOT NULL,
    notes        TEXT,
    status       VARCHAR(30) DEFAULT 'PENDING',
    created_at   TIMESTAMPTZ DEFAULT NOW()
);
