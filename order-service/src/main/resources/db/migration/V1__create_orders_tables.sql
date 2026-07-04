CREATE TABLE orders
(
    id           UUID PRIMARY KEY,
    customer_id  VARCHAR(100)   NOT NULL,
    status       VARCHAR(30)    NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE order_items
(
    id         UUID PRIMARY KEY,
    order_id   UUID           NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id VARCHAR(100)   NOT NULL,
    quantity   INTEGER        NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL
);
