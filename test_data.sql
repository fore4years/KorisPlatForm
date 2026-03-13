-- Test Data for Verification (Idempotent)
USE generator_rental;

-- Create Merchant if not exists
INSERT IGNORE INTO users (username, password, phone, role, name, business_license, status) 
VALUES ('merchant_01', 'password123', '13800000001', 'MERCHANT', 'Power Rent Co.', 'BL12345678', 'ACTIVE');

-- Create Tenant if not exists
INSERT IGNORE INTO users (username, password, phone, role, name, identity_card, status) 
VALUES ('tenant_01', 'password123', '13900000001', 'TENANT', 'John Doe', 'ID12345678', 'ACTIVE');

-- Get IDs
SET @merchant_id = (SELECT id FROM users WHERE username = 'merchant_01');
SET @tenant_id = (SELECT id FROM users WHERE username = 'tenant_01');

-- Create Generators (Check existence by name/merchant to avoid duplicates if possible, or just insert)
-- For simplicity, we use INSERT IGNORE based on some unique key if available, but generators don't have unique names usually.
-- Let's just add them if not present.
INSERT INTO generators (merchant_id, name, power, daily_rent, weekly_rent, monthly_rent, deposit, delivery_method, stock_status, address, latitude, longitude, image_url, description, fuel_consumption, weight, size)
SELECT @merchant_id, '50KW Silent Diesel Generator', '50KW', 500.00, 3000.00, 10000.00, 2000.00, 'Seller Delivery', 'AVAILABLE', 'Beijing Chaoyang District', 39.9042, 116.4074, 'https://placehold.co/600x400?text=50KW+Gen', 'High efficiency silent generator suitable for events.', '10L/h', '1200kg', '200x100x150cm'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM generators WHERE name = '50KW Silent Diesel Generator' AND merchant_id = @merchant_id);

INSERT INTO generators (merchant_id, name, power, daily_rent, weekly_rent, monthly_rent, deposit, delivery_method, stock_status, address, latitude, longitude, image_url, description, fuel_consumption, weight, size)
SELECT @merchant_id, '100KW Heavy Duty Generator', '100KW', 800.00, 4800.00, 15000.00, 5000.00, 'Self Pickup', 'AVAILABLE', 'Beijing Haidian District', 39.9542, 116.3074, 'https://placehold.co/600x400?text=100KW+Gen', 'Robust power for construction sites.', '20L/h', '2000kg', '250x120x180cm'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM generators WHERE name = '100KW Heavy Duty Generator' AND merchant_id = @merchant_id);

INSERT INTO generators (merchant_id, name, power, daily_rent, weekly_rent, monthly_rent, deposit, delivery_method, stock_status, address, latitude, longitude, image_url, description, fuel_consumption, weight, size)
SELECT @merchant_id, '200KW Industrial Generator', '200KW', 1500.00, 9000.00, 25000.00, 8000.00, 'Seller Delivery', 'AVAILABLE', 'Shanghai Pudong', 31.2244, 121.5350, 'https://placehold.co/600x400?text=200KW+Gen', 'Massive power output for factories.', '40L/h', '3500kg', '350x150x200cm'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM generators WHERE name = '200KW Industrial Generator' AND merchant_id = @merchant_id);

-- Create Order if not exists
SET @gen_id = (SELECT id FROM generators WHERE name = '50KW Silent Diesel Generator' AND merchant_id = @merchant_id LIMIT 1);

INSERT INTO orders (tenant_id, merchant_id, generator_id, start_time, end_time, total_amount, deposit_amount, status, payment_method, payment_status, contact_name, contact_phone, delivery_address)
SELECT @tenant_id, @merchant_id, @gen_id, NOW(), NOW(), 500.00, 2000.00, 'COMPLETED', 'ONLINE', 'PAID', 'John Doe', '13900000001', 'Beijing'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM orders WHERE tenant_id = @tenant_id AND generator_id = @gen_id);

-- Create Reviews
SET @order_id = (SELECT id FROM orders WHERE tenant_id = @tenant_id AND generator_id = @gen_id LIMIT 1);

INSERT INTO reviews (order_id, tenant_id, generator_id, rating, comment)
SELECT @order_id, @tenant_id, @gen_id, 5, 'Great condition and silent!'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE order_id = @order_id);
