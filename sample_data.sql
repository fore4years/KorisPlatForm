-- Test Data for Generator Rental Platform
USE generator_rental;

-- 1. Create Users (Merchants and Tenants)
-- Merchant 1: Power Rent Co. (Beijing)
INSERT INTO users (username, password, phone, role, name, business_license, status)
VALUES ('merchant_bj', 'password123', '13800100001', 'MERCHANT', 'Beijing Power Rent Co.', 'BL_BJ_001', 'ACTIVE');

-- Merchant 2: Shanghai Energy Solutions (Shanghai)
INSERT INTO users (username, password, phone, role, name, business_license, status)
VALUES ('merchant_sh', 'password123', '13800210001', 'MERCHANT', 'Shanghai Energy Solutions', 'BL_SH_001', 'ACTIVE');

-- Tenant 1: Construction Corp
INSERT INTO users (username, password, phone, role, name, identity_card, status)
VALUES ('tenant_corp', 'password123', '13900100001', 'TENANT', 'Mega Construction Corp', 'ID_CORP_001', 'ACTIVE');

-- Tenant 2: Event Planner
INSERT INTO users (username, password, phone, role, name, identity_card, status)
VALUES ('tenant_event', 'password123', '13900100002', 'TENANT', 'Star Event Planning', 'ID_EVENT_001', 'ACTIVE');

-- Admin 1
INSERT INTO users (username, password, phone, role, name, status)
VALUES ('admin_01', 'password123', '13800009991', 'ADMIN', 'System Admin 1', 'ACTIVE');

-- Admin 2
INSERT INTO users (username, password, phone, role, name, status)
VALUES ('admin_02', 'password123', '13800009992', 'ADMIN', 'System Admin 2', 'ACTIVE');

-- 2. Create Generators
-- Note: Assuming merchant_bj has ID 1 and merchant_sh has ID 2 based on insertion order.

-- Merchant 1 (Beijing) Generators
INSERT INTO generators (merchant_id, name, power, daily_rent, weekly_rent, monthly_rent, deposit, delivery_method, stock_status, address, latitude, longitude, image_url, description, fuel_consumption, weight, size)
VALUES
-- 50KW Gen in Chaoyang
(1, '50KW Silent Generator (Chaoyang)', '50KW', 450.00, 2800.00, 9000.00, 2000.00, 'Seller Delivery', 'AVAILABLE', 'Chaoyang Park, Beijing', 39.9427, 116.4795, 'https://placehold.co/600x400?text=50KW+Chaoyang', 'Perfect for outdoor events, low noise.', '10L/h', '1100kg', '200x100x140cm'),
-- 100KW Gen in Haidian
(1, '100KW Heavy Duty (Haidian)', '100KW', 800.00, 5000.00, 16000.00, 5000.00, 'Self Pickup', 'AVAILABLE', 'Zhongguancun, Haidian, Beijing', 39.9833, 116.3167, 'https://placehold.co/600x400?text=100KW+Haidian', 'Reliable power for construction.', '22L/h', '2100kg', '260x130x180cm'),
-- 500KW Gen in Daxing
(1, '500KW Industrial Power (Daxing)', '500KW', 3000.00, 18000.00, 60000.00, 20000.00, 'Seller Delivery', 'AVAILABLE', 'Daxing District, Beijing', 39.7266, 116.3414, 'https://placehold.co/600x400?text=500KW+Daxing', 'Massive power for large scale industrial needs.', '80L/h', '5500kg', '450x200x250cm');

-- Merchant 2 (Shanghai) Generators
INSERT INTO generators (merchant_id, name, power, daily_rent, weekly_rent, monthly_rent, deposit, delivery_method, stock_status, address, latitude, longitude, image_url, description, fuel_consumption, weight, size)
VALUES
-- 50KW Gen in Pudong
(2, '50KW Event Generator (Pudong)', '50KW', 500.00, 3200.00, 10500.00, 2000.00, 'Seller Delivery', 'AVAILABLE', 'Lujiazui, Pudong, Shanghai', 31.2375, 121.4986, 'https://placehold.co/600x400?text=50KW+Pudong', 'Compact and quiet.', '9L/h', '1050kg', '190x90x130cm'),
-- 200KW Gen in Minhang
(2, '200KW Backup Power (Minhang)', '200KW', 1400.00, 8500.00, 28000.00, 8000.00, 'Self Pickup', 'AVAILABLE', 'Minhang District, Shanghai', 31.1128, 121.3817, 'https://placehold.co/600x400?text=200KW+Minhang', 'Emergency backup power.', '35L/h', '3200kg', '320x140x190cm');

-- 3. Create Orders and Reviews to simulate history
-- Order 1: Tenant 1 rented Merchant 1's 50KW Gen (Completed)
-- Assuming IDs: Tenant 1 = 3, Merchant 1 = 1, Gen 1 = 1
INSERT INTO orders (tenant_id, merchant_id, generator_id, start_time, end_time, total_amount, deposit_amount, status, created_at)
VALUES (3, 1, 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY), 900.00, 2000.00, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 11 DAY));

-- Review for Order 1
INSERT INTO reviews (order_id, tenant_id, generator_id, rating, comment, created_at)
VALUES (1, 3, 1, 5, 'Excellent service and machine!', DATE_SUB(NOW(), INTERVAL 8 DAY));

-- Order 2: Tenant 2 rented Merchant 1's 100KW Gen (Completed)
-- Assuming IDs: Tenant 2 = 4, Merchant 1 = 1, Gen 2 = 2
INSERT INTO orders (tenant_id, merchant_id, generator_id, start_time, end_time, total_amount, deposit_amount, status, created_at)
VALUES (4, 1, 2, DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY), 4000.00, 5000.00, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 21 DAY));

-- Review for Order 2
INSERT INTO reviews (order_id, tenant_id, generator_id, rating, comment, created_at)
VALUES (2, 4, 2, 4, 'Good machine but delivery was slightly late.', DATE_SUB(NOW(), INTERVAL 15 DAY));

-- Order 3: Tenant 1 rented Merchant 2's 50KW Gen (Completed)
-- Assuming IDs: Tenant 1 = 3, Merchant 2 = 2, Gen 4 = 4
INSERT INTO orders (tenant_id, merchant_id, generator_id, start_time, end_time, total_amount, deposit_amount, status, created_at)
VALUES (3, 2, 4, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), 1000.00, 2000.00, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 6 DAY));

-- Review for Order 3
INSERT INTO reviews (order_id, tenant_id, generator_id, rating, comment, created_at)
VALUES (3, 3, 4, 5, 'Very professional merchant.', DATE_SUB(NOW(), INTERVAL 3 DAY));

-- Admin 1
INSERT INTO users (username, password, phone, role, name, status)
VALUES ('admin_01', 'password123', '13800009991', 'ADMIN', 'System Admin 1', 'ACTIVE');

-- Admin 2
INSERT INTO users (username, password, phone, role, name, status)
VALUES ('admin_02', 'password123', '13800009992', 'ADMIN', 'System Admin 2', 'ACTIVE');
