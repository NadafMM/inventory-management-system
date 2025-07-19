-- Seed data for Inventory Management System
-- Initial categories with hierarchical structure
-- Sample products and SKUs for development and testing

-- Root categories
INSERT INTO categories (name, description, parent_id, path, level, sort_order, is_active, metadata) VALUES 
('Electronics', 'Electronic devices and components', NULL, '/1/', 0, 1, TRUE, '{"color": "#1f77b4", "icon": "electronics"}'),
('Clothing', 'Apparel and fashion items', NULL, '/2/', 0, 2, TRUE, '{"color": "#ff7f0e", "icon": "clothing"}'),
('Books', 'Books and educational materials', NULL, '/3/', 0, 3, TRUE, '{"color": "#2ca02c", "icon": "book"}'),
('Home & Garden', 'Home improvement and gardening supplies', NULL, '/4/', 0, 4, TRUE, '{"color": "#d62728", "icon": "home"}'),
('Sports & Outdoors', 'Sports equipment and outdoor gear', NULL, '/5/', 0, 5, TRUE, '{"color": "#9467bd", "icon": "sports"}');

-- Electronics subcategories
INSERT INTO categories (name, description, parent_id, path, level, sort_order, is_active, metadata) VALUES 
('Computers', 'Desktop and laptop computers', 1, '/1/6/', 1, 1, TRUE, '{"color": "#1f77b4", "icon": "computer"}'),
('Mobile Phones', 'Smartphones and accessories', 1, '/1/7/', 1, 2, TRUE, '{"color": "#1f77b4", "icon": "phone"}'),
('Audio & Video', 'Headphones, speakers, and entertainment devices', 1, '/1/8/', 1, 3, TRUE, '{"color": "#1f77b4", "icon": "audio"}'),
('Gaming', 'Gaming consoles and accessories', 1, '/1/9/', 1, 4, TRUE, '{"color": "#1f77b4", "icon": "gaming"}');

-- Computers subcategories
INSERT INTO categories (name, description, parent_id, path, level, sort_order, is_active, metadata) VALUES 
('Laptops', 'Portable computers', 6, '/1/6/10/', 2, 1, TRUE, '{"color": "#1f77b4", "icon": "laptop"}'),
('Desktops', 'Desktop computers and workstations', 6, '/1/6/11/', 2, 2, TRUE, '{"color": "#1f77b4", "icon": "desktop"}'),
('Accessories', 'Computer accessories and peripherals', 6, '/1/6/12/', 2, 3, TRUE, '{"color": "#1f77b4", "icon": "accessories"}');

-- Clothing subcategories
INSERT INTO categories (name, description, parent_id, path, level, sort_order, is_active, metadata) VALUES 
('Men''s Clothing', 'Clothing for men', 2, '/2/13/', 1, 1, TRUE, '{"color": "#ff7f0e", "icon": "mens"}'),
('Women''s Clothing', 'Clothing for women', 2, '/2/14/', 1, 2, TRUE, '{"color": "#ff7f0e", "icon": "womens"}'),
('Kids'' Clothing', 'Clothing for children', 2, '/2/15/', 1, 3, TRUE, '{"color": "#ff7f0e", "icon": "kids"}'),
('Shoes', 'Footwear for all ages', 2, '/2/16/', 1, 4, TRUE, '{"color": "#ff7f0e", "icon": "shoes"}');

-- Sample products
INSERT INTO products (name, description, category_id, brand, manufacturer, weight, dimensions, color, material, is_active, metadata) VALUES 
('MacBook Pro 16"', 'Apple MacBook Pro 16-inch with M2 Pro chip', 10, 'Apple', 'Apple Inc.', 2.15, '35.57x24.81x1.68', 'Space Gray', 'Aluminum', TRUE, '{"warranty": "1 year", "model": "MK1E3LL/A"}'),
('Dell XPS 13', 'Dell XPS 13 Ultrabook with Intel Core i7', 10, 'Dell', 'Dell Technologies', 1.27, '29.6x19.9x1.48', 'Platinum Silver', 'Aluminum', TRUE, '{"warranty": "1 year", "model": "XPS9320-7302SLV-PUS"}'),
('iPhone 14 Pro', 'Apple iPhone 14 Pro with A16 Bionic chip', 7, 'Apple', 'Apple Inc.', 0.206, '14.75x7.15x0.79', 'Deep Purple', 'Titanium', TRUE, '{"warranty": "1 year", "model": "MQ0G3LL/A"}'),
('Samsung Galaxy S23', 'Samsung Galaxy S23 5G smartphone', 7, 'Samsung', 'Samsung Electronics', 0.168, '14.62x7.06x0.76', 'Phantom Black', 'Glass', TRUE, '{"warranty": "1 year", "model": "SM-S911U"}'),
('Sony WH-1000XM4', 'Sony WH-1000XM4 Wireless Noise Canceling Headphones', 8, 'Sony', 'Sony Corporation', 0.254, '25.4x20.3x7.6', 'Black', 'Plastic', TRUE, '{"warranty": "1 year", "model": "WH1000XM4/B"}'),
('Men''s Classic T-Shirt', 'Premium cotton t-shirt for men', 13, 'Generic', 'Textile Corp', 0.2, '60x45x1', 'Various', 'Cotton', TRUE, '{"material_blend": "100% Cotton", "care": "Machine wash"}'),
('Women''s Yoga Pants', 'High-waist yoga pants with moisture-wicking fabric', 14, 'FitWear', 'FitWear Inc.', 0.3, '90x30x1', 'Various', 'Polyester Blend', TRUE, '{"material_blend": "88% Polyester, 12% Spandex", "care": "Machine wash cold"}'),
('Programming Book Set', 'Complete guide to modern programming languages', 3, 'TechBooks', 'Educational Press', 1.5, '24x18x5', 'Multi-color', 'Paper', TRUE, '{"pages": 1200, "edition": "3rd Edition", "isbn": "978-0123456789"}');

-- Sample SKUs
INSERT INTO skus (sku_code, product_id, variant_name, size, color, price, cost, stock_quantity, reserved_quantity, reorder_point, reorder_quantity, barcode, location, is_active, metadata) VALUES 
-- MacBook Pro variants
('MBP16-M2-512-SG', 1, '512GB SSD', '16"', 'Space Gray', 2499.00, 2000.00, 25, 3, 5, 10, '123456789012', 'A1-B2-C3', TRUE, '{"storage": "512GB", "ram": "16GB"}'),
('MBP16-M2-1TB-SG', 1, '1TB SSD', '16"', 'Space Gray', 2799.00, 2200.00, 15, 2, 5, 10, '123456789013', 'A1-B2-C4', TRUE, '{"storage": "1TB", "ram": "16GB"}'),
('MBP16-M2-512-SV', 1, '512GB SSD', '16"', 'Silver', 2499.00, 2000.00, 20, 1, 5, 10, '123456789014', 'A1-B2-C5', TRUE, '{"storage": "512GB", "ram": "16GB"}'),

-- Dell XPS variants
('XPS13-I7-512-SV', 2, 'i7 512GB', '13"', 'Platinum Silver', 1299.00, 1000.00, 30, 5, 10, 15, '123456789015', 'A2-B1-C1', TRUE, '{"processor": "Intel i7", "storage": "512GB"}'),
('XPS13-I7-1TB-SV', 2, 'i7 1TB', '13"', 'Platinum Silver', 1599.00, 1200.00, 20, 2, 10, 15, '123456789016', 'A2-B1-C2', TRUE, '{"processor": "Intel i7", "storage": "1TB"}'),

-- iPhone variants
('IP14P-128-DP', 3, '128GB', 'Standard', 'Deep Purple', 999.00, 700.00, 50, 8, 20, 25, '123456789017', 'B1-A1-C1', TRUE, '{"storage": "128GB", "network": "5G"}'),
('IP14P-256-DP', 3, '256GB', 'Standard', 'Deep Purple', 1099.00, 750.00, 40, 5, 20, 25, '123456789018', 'B1-A1-C2', TRUE, '{"storage": "256GB", "network": "5G"}'),
('IP14P-128-GD', 3, '128GB', 'Standard', 'Gold', 999.00, 700.00, 45, 3, 20, 25, '123456789019', 'B1-A1-C3', TRUE, '{"storage": "128GB", "network": "5G"}'),

-- Samsung Galaxy variants
('SGS23-128-PB', 4, '128GB', 'Standard', 'Phantom Black', 799.00, 550.00, 60, 10, 25, 30, '123456789020', 'B1-A2-C1', TRUE, '{"storage": "128GB", "network": "5G"}'),
('SGS23-256-PB', 4, '256GB', 'Standard', 'Phantom Black', 899.00, 600.00, 35, 4, 25, 30, '123456789021', 'B1-A2-C2', TRUE, '{"storage": "256GB", "network": "5G"}'),

-- Sony Headphones variants
('WH1000XM4-BLK', 5, 'Standard', 'Over-ear', 'Black', 349.00, 250.00, 75, 12, 30, 40, '123456789022', 'C1-A1-B1', TRUE, '{"noise_canceling": true, "wireless": true}'),
('WH1000XM4-SLV', 5, 'Standard', 'Over-ear', 'Silver', 349.00, 250.00, 50, 8, 30, 40, '123456789023', 'C1-A1-B2', TRUE, '{"noise_canceling": true, "wireless": true}'),

-- T-Shirt variants
('TSHIRT-M-BLK', 6, 'Classic Fit', 'M', 'Black', 24.99, 12.00, 100, 15, 50, 75, '123456789024', 'D1-A1-B1', TRUE, '{"fit": "Classic", "sleeve": "Short"}'),
('TSHIRT-M-WHT', 6, 'Classic Fit', 'M', 'White', 24.99, 12.00, 120, 20, 50, 75, '123456789025', 'D1-A1-B2', TRUE, '{"fit": "Classic", "sleeve": "Short"}'),
('TSHIRT-L-BLK', 6, 'Classic Fit', 'L', 'Black', 24.99, 12.00, 90, 12, 50, 75, '123456789026', 'D1-A1-B3', TRUE, '{"fit": "Classic", "sleeve": "Short"}'),
('TSHIRT-L-WHT', 6, 'Classic Fit', 'L', 'White', 24.99, 12.00, 110, 18, 50, 75, '123456789027', 'D1-A1-B4', TRUE, '{"fit": "Classic", "sleeve": "Short"}'),

-- Yoga Pants variants
('YOGA-S-BLK', 7, 'High Waist', 'S', 'Black', 59.99, 30.00, 80, 10, 40, 60, '123456789028', 'D2-A1-B1', TRUE, '{"waist": "High", "length": "Full"}'),
('YOGA-M-BLK', 7, 'High Waist', 'M', 'Black', 59.99, 30.00, 95, 15, 40, 60, '123456789029', 'D2-A1-B2', TRUE, '{"waist": "High", "length": "Full"}'),
('YOGA-L-BLK', 7, 'High Waist', 'L', 'Black', 59.99, 30.00, 70, 8, 40, 60, '123456789030', 'D2-A1-B3', TRUE, '{"waist": "High", "length": "Full"}'),
('YOGA-S-GRY', 7, 'High Waist', 'S', 'Gray', 59.99, 30.00, 85, 12, 40, 60, '123456789031', 'D2-A1-B4', TRUE, '{"waist": "High", "length": "Full"}'),

-- Book variants
('PROG-BOOK-SET', 8, 'Complete Set', 'Standard', 'Multi-color', 149.99, 75.00, 200, 25, 100, 150, '123456789032', 'E1-A1-B1', TRUE, '{"volumes": 3, "format": "Hardcover"}'),
('PROG-BOOK-EBOOK', 8, 'Digital Edition', 'Digital', 'Digital', 99.99, 50.00, 999, 0, 500, 0, '123456789033', 'DIGITAL', TRUE, '{"format": "PDF", "drm": false}');

-- Sample inventory transactions
INSERT INTO inventory_transactions (sku_id, transaction_type, quantity, reference_id, reference_type, reason, performed_by) VALUES 
-- Initial stock entries
(1, 'IN', 25, 'INIT-001', 'INITIAL_STOCK', 'Initial inventory setup', 'system'),
(2, 'IN', 15, 'INIT-002', 'INITIAL_STOCK', 'Initial inventory setup', 'system'),
(3, 'IN', 20, 'INIT-003', 'INITIAL_STOCK', 'Initial inventory setup', 'system'),
(4, 'IN', 30, 'INIT-004', 'INITIAL_STOCK', 'Initial inventory setup', 'system'),
(5, 'IN', 20, 'INIT-005', 'INITIAL_STOCK', 'Initial inventory setup', 'system'),
(6, 'IN', 50, 'INIT-006', 'INITIAL_STOCK', 'Initial inventory setup', 'system'),
(7, 'IN', 40, 'INIT-007', 'INITIAL_STOCK', 'Initial inventory setup', 'system'),
(8, 'IN', 45, 'INIT-008', 'INITIAL_STOCK', 'Initial inventory setup', 'system'),
(9, 'IN', 60, 'INIT-009', 'INITIAL_STOCK', 'Initial inventory setup', 'system'),
(10, 'IN', 35, 'INIT-010', 'INITIAL_STOCK', 'Initial inventory setup', 'system'),

-- Some sample transactions
(1, 'RESERVED', 3, 'ORD-001', 'ORDER', 'Customer order reservation', 'admin'),
(6, 'OUT', 5, 'ORD-002', 'ORDER', 'Product sold', 'admin'),
(9, 'RESERVED', 10, 'ORD-003', 'ORDER', 'Bulk order reservation', 'admin'),
(15, 'OUT', 20, 'ORD-004', 'ORDER', 'Clothing sale', 'staff'),
(20, 'ADJUSTMENT', -5, 'ADJ-001', 'ADJUSTMENT', 'Damaged items removed', 'manager'); 