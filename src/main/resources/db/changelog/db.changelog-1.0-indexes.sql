-- liquibase formatted sql

-- changeset SVEND-1:1
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_seller_id ON products(seller_id);

-- changeset SVEND-1:2
CREATE INDEX idx_carts_user_id ON carts(user_id);

-- changeset SVEND-1:3
CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);

-- changeset SVEND-1:4
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_courier_id ON orders(courier_id);

-- changeset SVEND-1:5
CREATE INDEX idx_order_items_order_id ON order_items(order_id);

-- changeset SVEND-1:7
CREATE INDEX idx_role_requests_user_id ON role_requests(user_id);