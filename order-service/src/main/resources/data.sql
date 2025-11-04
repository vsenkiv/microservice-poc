-- src/main/resources/data.sql
INSERT INTO orders (id, user_id, total_amount, status, created_at, updated_at) VALUES
(1, 1001, 250.50, 'PENDING', '2024-01-15 10:30:00', NULL),
(2, 1002, 350.75, 'CONFIRMED', '2024-01-16 14:45:00', '2024-01-16 15:00:00'),
(3, 1003, 125.00, 'COMPLETED', '2024-01-17 09:15:00', '2024-01-17 16:30:00'),
(4, 1004, 199.99, 'CANCELLED', '2024-01-18 11:20:00', '2024-01-18 12:00:00'),
(5, 1001, 89.99, 'CONFIRMED', '2024-01-19 13:10:00', '2024-01-19 13:25:00'),
(6, 1002, 445.50, 'PENDING', '2024-01-20 16:40:00', NULL),
(7, 1005, 299.99, 'COMPLETED', '2024-01-21 08:30:00', '2024-01-21 18:45:00'),
(8, 1003, 175.25, 'CONFIRMED', '2024-01-22 12:15:00', '2024-01-22 12:30:00');

-- Insert sample order items
INSERT INTO order_items (id, order_id, product_id, quantity, unit_price, total_price) VALUES
-- Order 1 items (user 1001, total: 250.50)
(1, 1, 501, 2, 100.00, 200.00),
(2, 1, 502, 1, 50.50, 50.50),

-- Order 2 items (user 1002, total: 350.75)
(3, 2, 503, 3, 75.00, 225.00),
(4, 2, 504, 1, 125.75, 125.75),

-- Order 3 items (user 1003, total: 125.00)
(5, 3, 505, 5, 25.00, 125.00),

-- Order 4 items (user 1004, total: 199.99)
(6, 4, 506, 2, 99.99, 199.98),

-- Order 5 items (user 1001, total: 89.99)
(7, 5, 507, 1, 89.99, 89.99),

-- Order 6 items (user 1002, total: 445.50)
(8, 6, 508, 3, 120.00, 360.00),
(9, 6, 509, 1, 85.50, 85.50),

-- Order 7 items (user 1005, total: 299.99)
(10, 7, 510, 2, 149.99, 299.98),

-- Order 8 items (user 1003, total: 175.25)
(11, 8, 511, 1, 75.25, 75.25),
(12, 8, 512, 2, 50.00, 100.00);

-- Update sequences to avoid conflicts
SELECT setval('orders_id_seq', 8, true);
SELECT setval('order_items_id_seq', 12, true);

-- Verify data integrity
-- This query should show order totals matching the sum of order items
SELECT
    o.id as order_id,
    o.user_id,
    o.total_amount as declared_total,
    COALESCE(SUM(oi.total_price), 0) as calculated_total,
    o.status,
    COUNT(oi.id) as item_count
FROM orders o
LEFT JOIN order_items oi ON o.id = oi.order_id
GROUP BY o.id, o.user_id, o.total_amount, o.status
ORDER BY o.id;


-- Insert sample users (ensure users table has phone_number and status columns)
INSERT INTO users (id, username, email, first_name, last_name, phone_number, status, created_at) VALUES
(1001, 'john_doe', 'john.doe@example.com', 'John', 'Doe', '+1234567890', 'ACTIVE', CURRENT_TIMESTAMP),
(1002, 'jane_smith', 'jane.smith@example.com', 'Jane', 'Smith', '+1234567891', 'ACTIVE', CURRENT_TIMESTAMP),
(1003, 'bob_johnson', 'bob.johnson@example.com', 'Bob', 'Johnson', '+1234567892', 'ACTIVE', CURRENT_TIMESTAMP),
(1004, 'alice_wilson', 'alice.wilson@example.com', 'Alice', 'Wilson', '+1234567893', 'ACTIVE', CURRENT_TIMESTAMP),
(1005, 'charlie_brown', 'charlie.brown@example.com', 'Charlie', 'Brown', '+1234567894', 'INACTIVE', CURRENT_TIMESTAMP);

-- Insert sample user profiles
INSERT INTO user_profiles (id, date_of_birth, gender, occupation, bio, preferred_language, marketing_consent) VALUES
(1001, '1990-05-15', 'MALE', 'Software Engineer', 'Passionate about technology and innovation.', 'en', TRUE),
(1002, '1985-08-22', 'FEMALE', 'Product Manager', 'Love creating amazing user experiences.', 'en', TRUE),
(1003, '1978-12-10', 'MALE', 'Data Scientist', 'Analytics enthusiast and problem solver.', 'en', FALSE),
(1004, '1992-03-07', 'FEMALE', 'UX Designer', 'Designer with a passion for user-centered design.', 'en', TRUE),
(1005, '1988-11-30', 'MALE', 'Marketing Specialist', 'Creative marketer with digital expertise.', 'en', FALSE);

-- Insert sample addresses
INSERT INTO user_addresses (user_id, street, city, state, postal_code, country, type, is_default) VALUES
(1001, '123 Main St', 'New York', 'NY', '10001', 'USA', 'HOME', TRUE),
(1001, '456 Broadway', 'New York', 'NY', '10002', 'USA', 'WORK', FALSE),
(1002, '789 Oak Ave', 'Los Angeles', 'CA', '90210', 'USA', 'HOME', TRUE),
(1003, '321 Pine St', 'Chicago', 'IL', '60601', 'USA', 'HOME', TRUE),
(1003, '654 Elm St', 'Chicago', 'IL', '60602', 'USA', 'BILLING', FALSE),
(1004, '987 Maple Dr', 'Seattle', 'WA', '98101', 'USA', 'HOME', TRUE),
(1005, '147 Cedar Ln', 'Miami', 'FL', '33101', 'USA', 'HOME', TRUE);

-- Update sequences (verify these sequence names exist first)
SELECT setval('users_id_seq', 1005, true);
-- user_addresses uses SERIAL, so get the max id
SELECT setval('user_addresses_id_seq', (SELECT MAX(id) FROM user_addresses), true);
