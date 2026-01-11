-- liquibase formatted sql

-- changeset SVEND-1:1
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       address VARCHAR(255),
                       email VARCHAR(128) NOT NULL UNIQUE,
                       name VARCHAR(64) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(255) NOT NULL
);

-- changeset SVEND-1:2
CREATE TABLE products (
                          id BIGSERIAL PRIMARY KEY,
                          category VARCHAR(255),
                          count INTEGER NOT NULL,
                          description VARCHAR(3000) NOT NULL,
                          image VARCHAR(255) NOT NULL,
                          name VARCHAR(255) NOT NULL,
                          price NUMERIC(19,2) NOT NULL,
                          seller_id BIGINT NOT NULL
);

ALTER TABLE products
    ADD CONSTRAINT fkbgw3lyxhsm13kfqnfr4560vbj
        FOREIGN KEY (seller_id) REFERENCES users(id);

-- changeset SVEND-1:3
CREATE TABLE carts (
                       id BIGSERIAL PRIMARY KEY,
                       user_id BIGINT NOT NULL UNIQUE
);

ALTER TABLE carts
    ADD CONSTRAINT fkb5o626f86h46m4s7ms6ginnop
        FOREIGN KEY (user_id) REFERENCES users(id);

-- changeset SVEND-1:4
CREATE TABLE cart_items (
                            id BIGSERIAL PRIMARY KEY,
                            price NUMERIC(19,2) NOT NULL,
                            quantity INTEGER NOT NULL,
                            cart_id BIGINT NOT NULL,
                            product_id BIGINT NOT NULL
);

ALTER TABLE cart_items
    ADD CONSTRAINT fkpcttvuq4mxppo8sxsgjtn512c
        FOREIGN KEY (cart_id) REFERENCES carts(id);

ALTER TABLE cart_items
    ADD CONSTRAINT fklre40cjegsfvw58xrkdp6bac6
        FOREIGN KEY (product_id) REFERENCES products(id);

-- changeset SVEND-1:5
CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        message VARCHAR(500),
                        order_date TIMESTAMP NOT NULL,
                        shipping_address VARCHAR(255),
                        status VARCHAR(255),
                        total_amount NUMERIC(19,2) NOT NULL,
                        courier_id BIGINT,
                        user_id BIGINT NOT NULL
);

ALTER TABLE orders
    ADD CONSTRAINT fkkda753b42924ldhhnyxt75n6c
        FOREIGN KEY (courier_id) REFERENCES users(id);

ALTER TABLE orders
    ADD CONSTRAINT fk32q18ubntj5uh44ph9659t1ih
        FOREIGN KEY (user_id) REFERENCES users(id);

-- changeset SVEND-1:6
CREATE TABLE order_items (
                             id BIGSERIAL PRIMARY KEY,
                             price NUMERIC(19,2) NOT NULL,
                             quantity INTEGER NOT NULL,
                             order_id BIGINT NOT NULL,
                             product_id BIGINT NOT NULL
);

ALTER TABLE order_items
    ADD CONSTRAINT fkbioxgbv59vetrxe0efjubepiw
        FOREIGN KEY (order_id) REFERENCES orders(id);

ALTER TABLE order_items
    ADD CONSTRAINT fkocime7dtr037rh41s4195n1fi
        FOREIGN KEY (product_id) REFERENCES products(id);

-- changeset SVEND-1:7
CREATE TABLE role_requests (
                               id BIGSERIAL PRIMARY KEY,
                               create_at TIMESTAMP NOT NULL,
                               message VARCHAR(500),
                               request_role VARCHAR(255) NOT NULL,
                               status VARCHAR(255) NOT NULL,
                               type_action VARCHAR(255) NOT NULL,
                               user_id BIGINT NOT NULL
);

ALTER TABLE role_requests
    ADD CONSTRAINT fkljihuio58bmdk3jjmtm31h72j
        FOREIGN KEY (user_id) REFERENCES users(id);