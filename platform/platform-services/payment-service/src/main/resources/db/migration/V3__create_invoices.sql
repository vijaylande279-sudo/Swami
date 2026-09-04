CREATE TABLE invoices (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    subscription_id UUID NOT NULL,
    invoice_number  VARCHAR(50) NOT NULL UNIQUE,
    amount_paise    BIGINT NOT NULL,
    gst_paise       BIGINT NOT NULL,
    total_paise     BIGINT NOT NULL,
    pdf_bytes       BYTEA,
    issued_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_invoices_tenant_id ON invoices (tenant_id);
