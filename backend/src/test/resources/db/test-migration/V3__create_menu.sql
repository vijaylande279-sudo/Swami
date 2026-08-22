CREATE TABLE menu_categories (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    sort_order INT DEFAULT 0
);

CREATE TABLE menu_items (
    id           BIGSERIAL PRIMARY KEY,
    category_id  BIGINT REFERENCES menu_categories(id) ON DELETE SET NULL,
    name         VARCHAR(150) NOT NULL,
    description  TEXT,
    price        NUMERIC(10, 2) NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    image_url    TEXT,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
