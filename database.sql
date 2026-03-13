CREATE DATABASE IF NOT EXISTS generator_rental;
USE generator_rental;

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    role ENUM('TENANT', 'MERCHANT', 'ADMIN') NOT NULL,
    name VARCHAR(100),
    identity_card VARCHAR(50),
    business_license VARCHAR(100),
    status ENUM('ACTIVE', 'DISABLED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Generators Table
CREATE TABLE IF NOT EXISTS generators (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    power VARCHAR(50),
    fuel_consumption VARCHAR(50),
    weight VARCHAR(50),
    size VARCHAR(50),
    daily_rent DECIMAL(10, 2),
    weekly_rent DECIMAL(10, 2),
    monthly_rent DECIMAL(10, 2),
    deposit DECIMAL(10, 2),
    delivery_method VARCHAR(100),
    image_url VARCHAR(255),
    stock_status ENUM('AVAILABLE', 'RENTED', 'MAINTENANCE') DEFAULT 'AVAILABLE',
    description TEXT,
    address VARCHAR(255),
    latitude DECIMAL(10, 6),
    longitude DECIMAL(10, 6),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (merchant_id) REFERENCES users(id)
);

-- Orders Table
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    merchant_id BIGINT NOT NULL,
    generator_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    deposit_amount DECIMAL(10, 2) NOT NULL,
    service_fee DECIMAL(10, 2) DEFAULT 0.00,
    status ENUM('WAIT_CONFIRM', 'CONFIRMED', 'DELIVERED', 'RENTING', 'WAIT_RETURN', 'COMPLETED', 'CANCELLED') DEFAULT 'WAIT_CONFIRM',
    delivery_address VARCHAR(255),
    contact_name VARCHAR(50),
    contact_phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES users(id),
    FOREIGN KEY (merchant_id) REFERENCES users(id),
    FOREIGN KEY (generator_id) REFERENCES generators(id)
);

-- Contracts Table
CREATE TABLE IF NOT EXISTS contracts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    content TEXT,
    tenant_signed BOOLEAN DEFAULT FALSE,
    merchant_signed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- Reviews Table
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    generator_id BIGINT NOT NULL,
    rating INT CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (tenant_id) REFERENCES users(id),
    FOREIGN KEY (generator_id) REFERENCES generators(id)
);
