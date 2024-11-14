-- Table for Budgets
CREATE TABLE budgets (
    budget_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

-- Table for Categories
CREATE TABLE categories (
    category_id BIGSERIAL PRIMARY KEY,
    budget_id BIGINT,
    name VARCHAR(255) NOT NULL
);

-- Table for Transactors
CREATE TABLE transactors (
    transactor_id BIGSERIAL PRIMARY KEY,
    budget_id BIGINT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100)
);

-- Table for Transactions
CREATE TABLE transactions (
    transaction_id BIGSERIAL PRIMARY KEY,
    transactor_id BIGINT,
    category_id BIGINT,
    description VARCHAR(500) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    date DATE NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE'))
);

-- Sequences for ID Generation (Optional if using PostgreSQL auto-increment)
CREATE SEQUENCE budget_sequence START 1 INCREMENT BY 1;
CREATE SEQUENCE category_sequence START 1 INCREMENT BY 1;
CREATE SEQUENCE transactor_sequence START 1 INCREMENT BY 1;
CREATE SEQUENCE transaction_sequence START 1 INCREMENT BY 1;
