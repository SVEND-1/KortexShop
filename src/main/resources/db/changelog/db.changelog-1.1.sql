-- liquibase formatted sql

-- changeset SVEND-1.1:1
CREATE TABLE payments (
                          idempotency_key VARCHAR(255) PRIMARY KEY,
                          payment_id VARCHAR(255),
                          receipt_id VARCHAR(255),
                          use BOOLEAN DEFAULT FALSE,
                          user_id BIGINT NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- changeset SVEND-1.1:2
ALTER TABLE payments
    ADD CONSTRAINT fk_payments_user
        FOREIGN KEY (user_id) REFERENCES users(id);

-- changeset SVEND-1.1:3
CREATE INDEX idx_payments_user_id ON payments(user_id);