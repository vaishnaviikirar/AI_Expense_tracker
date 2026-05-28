-- ============================================================
-- SpendWise - MySQL Database Schema
-- Run this script before starting the application
-- ============================================================

-- Step 1: Create the database
CREATE DATABASE IF NOT EXISTS expense_tracker_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Step 2: Select the database
USE expense_tracker_db;

-- ============================================================
-- TABLE: users
-- Stores user account information
-- Passwords are stored as BCrypt hashes (never plain text!)
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id         BIGINT          NOT NULL AUTO_INCREMENT,
    name       VARCHAR(100)    NOT NULL,
    email      VARCHAR(100)    NOT NULL UNIQUE,
    password   VARCHAR(255)    NOT NULL,   -- BCrypt hash is ~60 chars, give extra space
    PRIMARY KEY (id),
    INDEX idx_user_email (email)           -- Index for faster login lookups
);

-- ============================================================
-- TABLE: expenses
-- Stores individual expense records
-- Links to users via user_id foreign key
-- ============================================================
CREATE TABLE IF NOT EXISTS expenses (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    title       VARCHAR(200)    NOT NULL,
    amount      DECIMAL(10, 2)  NOT NULL,  -- 10 digits total, 2 decimal places
    category    VARCHAR(100)    NOT NULL,
    date        DATE            NOT NULL,
    user_id     BIGINT          NOT NULL,  -- Foreign key to users table
    PRIMARY KEY (id),
    INDEX idx_expense_user_id (user_id),   -- Index for faster user expense queries
    CONSTRAINT fk_expense_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE                   -- If user is deleted, delete their expenses too
);

-- ============================================================
-- SAMPLE DATA (Optional - for testing)
-- NOTE: The password hash below is BCrypt of "password123"
-- ============================================================

-- Insert a test user (password: password123)
-- INSERT INTO users (name, email, password) VALUES
-- ('Test User', 'test@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');

-- Insert sample expenses for the test user
-- INSERT INTO expenses (title, amount, category, date, user_id) VALUES
-- ('Lunch at restaurant',    450.00, 'Food',          '2024-01-15', 1),
-- ('Metro card recharge',   500.00, 'Travel',        '2024-01-14', 1),
-- ('Grocery shopping',      1200.00, 'Groceries',    '2024-01-13', 1),
-- ('Netflix subscription',  649.00, 'Entertainment', '2024-01-12', 1),
-- ('Electricity bill',      1800.00, 'Bills',        '2024-01-10', 1),
-- ('Online course',         2999.00, 'Education',    '2024-01-08', 1),
-- ('Medicine',              350.00, 'Healthcare',    '2024-01-05', 1);

-- ============================================================
-- VERIFY TABLES WERE CREATED
-- ============================================================
SHOW TABLES;
DESCRIBE users;
DESCRIBE expenses;
