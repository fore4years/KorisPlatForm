-- Update Orders Table for Payment
USE generator_rental;

ALTER TABLE orders
ADD COLUMN payment_method ENUM('ONLINE', 'OFFLINE') DEFAULT 'ONLINE',
ADD COLUMN payment_status ENUM('PENDING', 'PAID') DEFAULT 'PENDING';
