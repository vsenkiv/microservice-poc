-- Drop tables in reverse order of dependencies
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;

-- Create orders table
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CHECK (total_amount >= 0)
);

-- Create order_items table
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(19,2) NOT NULL CHECK (unit_price >= 0),
    total_price DECIMAL(19,2) NOT NULL CHECK (total_price >= 0),

    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Create indexes for better performance
DROP INDEX IF EXISTS idx_orders_user_id;
CREATE INDEX idx_orders_user_id ON orders(user_id);

DROP INDEX IF EXISTS idx_orders_status;
CREATE INDEX idx_orders_status ON orders(status);

DROP INDEX IF EXISTS idx_orders_created_at;
CREATE INDEX idx_orders_created_at ON orders(created_at);

DROP INDEX IF EXISTS idx_order_items_order_id;
CREATE INDEX idx_order_items_order_id ON order_items(order_id);

DROP INDEX IF EXISTS idx_order_items_product_id;
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

-- Drop tables if they exist for user-related schema
DROP TABLE IF EXISTS user_addresses CASCADE;
DROP TABLE IF EXISTS user_profiles CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    phone_number VARCHAR(20),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create user_profiles table
CREATE TABLE user_profiles (
    id BIGINT PRIMARY KEY,
    date_of_birth DATE,
    gender VARCHAR(10),
    occupation VARCHAR(255),
    bio TEXT,
    preferred_language VARCHAR(10) DEFAULT 'en',
    marketing_consent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create user_addresses table
CREATE TABLE user_addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(50),
    postal_code VARCHAR(20),
    country VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('HOME', 'WORK', 'BILLING')),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for better performance
DROP INDEX IF EXISTS idx_users_username;
CREATE INDEX idx_users_username ON users(username);

DROP INDEX IF EXISTS idx_users_email;
CREATE INDEX idx_users_email ON users(email);

DROP INDEX IF EXISTS idx_users_status;
CREATE INDEX idx_users_status ON users(status);

DROP INDEX IF EXISTS idx_user_addresses_user_id;
CREATE INDEX idx_user_addresses_user_id ON user_addresses(user_id);

DROP INDEX IF EXISTS idx_user_addresses_type;
CREATE INDEX idx_user_addresses_type ON user_addresses(type);

DROP INDEX IF EXISTS idx_user_addresses_default;
CREATE INDEX idx_user_addresses_default ON user_addresses(is_default);

