-- Fix missing columns in generators table
USE generator_rental;

ALTER TABLE generators 
ADD COLUMN address VARCHAR(255),
ADD COLUMN latitude DECIMAL(10, 6),
ADD COLUMN longitude DECIMAL(10, 6);
