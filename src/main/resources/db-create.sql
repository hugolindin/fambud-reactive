-- Role: fambud
-- DROP ROLE IF EXISTS fambud;

CREATE ROLE fambud WITH
  LOGIN
  NOSUPERUSER
  INHERIT
  NOCREATEDB
  NOCREATEROLE
  NOREPLICATION
  NOBYPASSRLS
  ENCRYPTED PASSWORD 'SCRAM-SHA-256$4096:onOOCVeNNgkoZFnSXKtcWw==$cvw5QdheQLY2L6HH1xtWlQvjtSwuDiPWY/fLbOJqtYI=:9UgHpCme5solAHi28DId8DPopPFJSpvpQdEs1iyxxAM=';

-- Database: fambud

-- DROP DATABASE IF EXISTS fambud;

CREATE DATABASE fambud
    WITH
    OWNER = fambud
    ENCODING = 'UTF8'
    LC_COLLATE = 'C'
    LC_CTYPE = 'C'
    LOCALE_PROVIDER = 'libc'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    IS_TEMPLATE = False;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO fambud;

GRANT USAGE ON SEQUENCE budgets_budget_id_seq TO fambud;
GRANT USAGE ON SEQUENCE categories_category_id_seq TO fambud;
GRANT USAGE ON SEQUENCE transactors_transactor_id_seq TO fambud;
GRANT USAGE ON SEQUENCE transactions_transaction_id_seq TO fambud;