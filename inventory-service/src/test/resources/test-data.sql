-- Insert sample products matching the product IDs from order-service
INSERT INTO products (id, sku, name, description, category, price, quantity, reserved_quantity, reorder_level, reorder_quantity, status, weight, weight_unit) VALUES
(501, 'ELEC-LAP-001', 'Premium Laptop', '15-inch laptop with 16GB RAM and 512GB SSD', 'Electronics', 1200.00, 50, 0, 10, 20, 'ACTIVE', 2.5, 'kg'),
(502, 'ELEC-PHO-001', 'Smartphone Pro', 'Latest flagship smartphone with 5G support', 'Electronics', 899.99, 100, 0, 15, 30, 'ACTIVE', 0.2, 'kg'),
(503, 'HOME-CHA-001', 'Ergonomic Office Chair', 'Adjustable office chair with lumbar support', 'Furniture', 299.99, 30, 0, 5, 10, 'ACTIVE', 15.0, 'kg'),
(504, 'HOME-DES-001', 'Standing Desk', 'Electric height-adjustable standing desk', 'Furniture', 499.99, 20, 0, 3, 5, 'ACTIVE', 35.0, 'kg'),
(505, 'BOOK-FIC-001', 'Bestseller Novel', 'Award-winning fiction book', 'Books', 24.99, 200, 0, 20, 50, 'ACTIVE', 0.5, 'kg'),
(506, 'ELEC-TAB-001', 'Tablet Device', '10-inch tablet with stylus support', 'Electronics', 449.99, 75, 0, 10, 25, 'ACTIVE', 0.5, 'kg'),
(507, 'CLOT-TSH-001', 'Premium T-Shirt', '100% cotton comfortable t-shirt', 'Clothing', 29.99, 150, 0, 25, 50, 'ACTIVE', 0.2, 'kg'),
(508, 'ELEC-MON-001', '4K Monitor', '27-inch 4K UHD monitor with HDR', 'Electronics', 399.99, 40, 0, 8, 15, 'ACTIVE', 6.5, 'kg'),
(509, 'HOME-LAM-001', 'LED Desk Lamp', 'Adjustable LED desk lamp with USB charging', 'Home', 49.99, 80, 0, 15, 30, 'ACTIVE', 1.0, 'kg'),
(510, 'ELEC-HEA-001', 'Wireless Headphones', 'Noise-canceling wireless headphones', 'Electronics', 249.99, 60, 0, 12, 25, 'ACTIVE', 0.3, 'kg'),
(511, 'BOOK-TEC-001', 'Programming Guide', 'Comprehensive programming tutorial book', 'Books', 59.99, 100, 0, 10, 30, 'ACTIVE', 0.8, 'kg'),
(512, 'HOME-MUG-001', 'Coffee Mug Set', 'Set of 4 ceramic coffee mugs', 'Home', 34.99, 120, 0, 20, 40, 'ACTIVE', 1.5, 'kg');

-- Insert additional products for variety
INSERT INTO products (id, sku, name, description, category, price, quantity, reserved_quantity, reorder_level, reorder_quantity, status, weight, weight_unit) VALUES
(513, 'ELEC-KEY-001', 'Mechanical Keyboard', 'RGB mechanical gaming keyboard', 'Electronics', 129.99, 45, 0, 10, 20, 'ACTIVE', 1.2, 'kg'),
(514, 'ELEC-MOU-001', 'Wireless Mouse', 'Ergonomic wireless mouse with precision sensor', 'Electronics', 49.99, 90, 0, 15, 30, 'ACTIVE', 0.1, 'kg'),
(515, 'HOME-PLA-001', 'Indoor Plant', 'Low-maintenance indoor plant with pot', 'Home', 19.99, 50, 0, 10, 20, 'ACTIVE', 2.0, 'kg'),
(516, 'SPOR-MAT-001', 'Yoga Mat', 'Non-slip exercise yoga mat', 'Sports', 39.99, 70, 0, 10, 25, 'ACTIVE', 1.0, 'kg'),
(517, 'SPOR-BOT-001', 'Water Bottle', 'Insulated stainless steel water bottle', 'Sports', 24.99, 100, 0, 20, 40, 'ACTIVE', 0.4, 'kg'),
(518, 'BOOK-COO-001', 'Cookbook Collection', 'International cuisine cookbook', 'Books', 44.99, 60, 0, 8, 20, 'ACTIVE', 1.2, 'kg'),
(519, 'ELEC-CAM-001', 'Webcam HD', '1080p HD webcam with microphone', 'Electronics', 79.99, 55, 0, 10, 20, 'ACTIVE', 0.3, 'kg'),
(520, 'HOME-CUS-001', 'Throw Pillow Set', 'Decorative throw pillows set of 2', 'Home', 29.99, 80, 0, 15, 30, 'ACTIVE', 0.8, 'kg');

-- Update sequences
SELECT setval('products_id_seq', 520, true);
SELECT setval('inventory_transactions_id_seq', 1, false);
SELECT setval('stock_reservations_id_seq', 1, false);

-- Insert initial inventory transactions for audit trail
INSERT INTO inventory_transactions (product_id, type, quantity, previous_quantity, new_quantity, reference_type, notes, performed_by)
SELECT id, 'RESTOCK', quantity, 0, quantity, 'INITIAL_STOCK', 'Initial stock creation', 'SYSTEM'
FROM products;

-- Verify data integrity
SELECT
    p.id,
    p.sku,
    p.name,
    p.category,
    p.price,
    p.quantity,
    p.reserved_quantity,
    (p.quantity - p.reserved_quantity) as available_quantity,
    p.status
FROM products
ORDER BY p.id;