-- Insert data into orders table
INSERT INTO orders (id, user_id, total_amount, status, created_at, updated_at)
VALUES
    (1, 1001, 250.50, 'PENDING', CURRENT_TIMESTAMP, NULL),
    (2, 1002, 350.75, 'CONFIRMED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 1003, 125.00, 'COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 1004, 199.99, 'CANCELLED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert data into order_items table (child entities linked with 'order_id')
INSERT INTO order_items (id, order_id, product_id, quantity, unit_price, total_price)
VALUES
    (1, 1, 501, 2, 100.00, 200.00),  -- Linked with order_id = 1
    (2, 1, 502, 1, 50.50, 50.50),   -- Linked with order_id = 1

    (3, 2, 503, 3, 75.00, 225.00),  -- Linked with order_id = 2
    (4, 2, 504, 1, 125.75, 125.75), -- Linked with order_id = 2

    (5, 3, 505, 5, 25.00, 125.00),  -- Linked with order_id = 3

    (6, 4, 506, 2, 99.99, 199.98);  -- Linked with order_id = 4