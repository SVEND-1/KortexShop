-- liquibase formatted sql

-- changeset SVEND-1.1:1
CREATE INDEX idx_payments_user_id ON payments(user_id);