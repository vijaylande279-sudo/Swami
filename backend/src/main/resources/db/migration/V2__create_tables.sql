CREATE TABLE dining_tables (
    id           BIGSERIAL PRIMARY KEY,
    table_number VARCHAR(10) UNIQUE NOT NULL,
    capacity     INT NOT NULL,
    status       VARCHAR(20) DEFAULT 'AVAILABLE'
);
