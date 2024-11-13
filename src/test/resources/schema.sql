-- Table for Budgets
CREATE TABLE budgets (
    budget_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

-- Table for Categories
CREATE TABLE categories (
    category_id BIGSERIAL PRIMARY KEY,
    budget_id BIGINT REFERENCES budgets(budget_id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL
);

-- Table for Transactors
CREATE TABLE transactors (
    transactor_id BIGSERIAL PRIMARY KEY,
    budget_id BIGINT REFERENCES budgets(budget_id) ON DELETE CASCADE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100)
);

-- Table for Transactions
CREATE TABLE transactions (
    transaction_id BIGSERIAL PRIMARY KEY,
    transactor_id BIGINT REFERENCES transactors(transactor_id) ON DELETE CASCADE,
    category_id BIGINT REFERENCES categories(category_id) ON DELETE SET NULL,
    description VARCHAR(500) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    date DATE NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE'))
);
