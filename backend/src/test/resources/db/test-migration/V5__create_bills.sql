CREATE TABLE bills (
    id           BIGSERIAL PRIMARY KEY,
    order_id     BIGINT UNIQUE REFERENCES orders(id),
    subtotal     NUMERIC(10, 2) NOT NULL,
    tax_percent  NUMERIC(5,  2) DEFAULT 0,
    tax_amount   NUMERIC(10, 2) DEFAULT 0,
    total        NUMERIC(10, 2) NOT NULL,
    payment_mode VARCHAR(30),
    is_paid      BOOLEAN DEFAULT FALSE,
    generated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    paid_at      TIMESTAMP WITH TIME ZONE
);
