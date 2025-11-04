-- Insert sample users
INSERT INTO users (id, username, email, first_name, last_name, phone_number, status, created_at) VALUES
(1001, 'john_doe', 'john.doe@email.com', 'John', 'Doe', '+1234567890', 'ACTIVE', CURRENT_TIMESTAMP),
(1002, 'jane_smith', 'jane.smith@email.com', 'Jane', 'Smith', '+1234567891', 'ACTIVE', CURRENT_TIMESTAMP),
(1003, 'bob_johnson', 'bob.johnson@email.com', 'Bob', 'Johnson', '+1234567892', 'ACTIVE', CURRENT_TIMESTAMP),
(1004, 'alice_wilson', 'alice.wilson@email.com', 'Alice', 'Wilson', '+1234567893', 'ACTIVE', CURRENT_TIMESTAMP),
(1005, 'charlie_brown', 'charlie.brown@email.com', 'Charlie', 'Brown', '+1234567894', 'INACTIVE', CURRENT_TIMESTAMP);

-- Insert sample user profiles
INSERT INTO user_profiles (id, date_of_birth, gender, occupation, bio, preferred_language, marketing_consent) VALUES
(1001, '1990-05-15', 'MALE', 'Software Engineer', 'Passionate about technology and innovation.', 'en', TRUE),
(1002, '1985-08-22', 'FEMALE', 'Product Manager', 'Love creating amazing user experiences.', 'en', TRUE),
(1003, '1978-12-10', 'MALE', 'Data Scientist', 'Analytics enthusiast and problem solver.', 'en', FALSE),
(1004, '1992-03-07', 'FEMALE', 'UX Designer', 'Designer with a passion for user-centered design.', 'en', TRUE),
(1005, '1988-11-30', 'MALE', 'Marketing Specialist', 'Creative marketer with digital expertise.', 'en', FALSE);

-- Insert sample addresses
INSERT INTO user_addresses (user_id, street, city, state, postal_code, country, type, is_default) VALUES
(1001, '123 Main St', 'New York', 'NY', '10001', 'USA', 'HOME', TRUE),
(1001, '456 Broadway', 'New York', 'NY', '10002', 'USA', 'WORK', FALSE),
(1002, '789 Oak Ave', 'Los Angeles', 'CA', '90210', 'USA', 'HOME', TRUE),
(1003, '321 Pine St', 'Chicago', 'IL', '60601', 'USA', 'HOME', TRUE),
(1003, '654 Elm St', 'Chicago', 'IL', '60602', 'USA', 'BILLING', FALSE),
(1004, '987 Maple Dr', 'Seattle', 'WA', '98101', 'USA', 'HOME', TRUE),
(1005, '147 Cedar Ln', 'Miami', 'FL', '33101', 'USA', 'HOME', TRUE);

-- Update sequence to avoid conflicts
SELECT setval('users_id_seq', 1005, true);
SELECT setval('user_addresses_id_seq', 7, true);