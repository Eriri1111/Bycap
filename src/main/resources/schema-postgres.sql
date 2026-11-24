-- PostgreSQL Schema for Bycap Commercial Payment Gateway
CREATE TABLE IF NOT EXISTS employees (
    id UUID PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    merchant_id UUID NOT NULL,
    role VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY,
    merchant_id UUID NOT NULL,
    reference VARCHAR(100) NOT NULL UNIQUE,
    amount_in_cents BIGINT NOT NULL CHECK (amount_in_cents >= 100000),
    currency VARCHAR(10) NOT NULL DEFAULT 'COP',
    status VARCHAR(20) NOT NULL,
    payment_method_code VARCHAR(30) NOT NULL,
    customer_email VARCHAR(150),
    customer_name VARCHAR(150),
    gateway_transaction_id VARCHAR(120),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_transactions_merchant ON transactions(merchant_id);
CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_employees_merchant ON employees(merchant_id);
