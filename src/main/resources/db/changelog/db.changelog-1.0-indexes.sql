-- liquibase formatted sql

-- changeset SVEND-1:1
CREATE INDEX idx_products_category ON products(category);