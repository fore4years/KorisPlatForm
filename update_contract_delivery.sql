-- Update Tables for Contract and Delivery
USE generator_rental;

-- Update Contracts Table
ALTER TABLE contracts
ADD COLUMN sign_date DATETIME;

-- Update Orders Table
ALTER TABLE orders
ADD COLUMN delivery_type ENUM('DELIVERY', 'PICKUP'),
ADD COLUMN delivery_evidence_url VARCHAR(255);
