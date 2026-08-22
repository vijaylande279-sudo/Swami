CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(150) UNIQUE NOT NULL,
    password   TEXT NOT NULL,
    role       VARCHAR(20)  NOT NULL DEFAULT 'WAITER',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
