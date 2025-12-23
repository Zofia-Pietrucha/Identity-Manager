-- Initialize roles
INSERT INTO roles (id, name) VALUES (1, 'USER');
INSERT INTO roles (id, name) VALUES (2, 'ADMIN');

-- Initialize users (password is "password123" hashed with BCrypt)
-- BCrypt hash for "password123": $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

INSERT INTO users (id, email, password, first_name, last_name, phone, is_privacy_enabled, created_at, updated_at)
VALUES (1, 'admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'User', '123456789', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (id, email, password, first_name, last_name, phone, is_privacy_enabled, created_at, updated_at)
VALUES (2, 'john@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'John', 'Doe', '987654321', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (id, email, password, first_name, last_name, phone, is_privacy_enabled, created_at, updated_at)
VALUES (3, 'jane@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Jane', 'Smith', NULL, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id) VALUES (1, 2); -- Admin has ADMIN role
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1); -- Admin also has USER role
INSERT INTO user_roles (user_id, role_id) VALUES (2, 1); -- John has USER role
INSERT INTO user_roles (user_id, role_id) VALUES (3, 1); -- Jane has USER role

-- Create sample support tickets
INSERT INTO support_tickets (id, subject, description, status, user_id, created_at)
VALUES (1, 'Cannot login', 'I forgot my password and cannot login to my account', 'OPEN', 2, CURRENT_TIMESTAMP);

INSERT INTO support_tickets (id, subject, description, status, user_id, created_at)
VALUES (2, 'Profile update issue', 'When I try to update my profile, I get an error', 'IN_PROGRESS', 2, CURRENT_TIMESTAMP);

INSERT INTO support_tickets (id, subject, description, status, user_id, created_at)
VALUES (3, 'Privacy settings', 'How do I enable privacy settings?', 'RESOLVED', 3, CURRENT_TIMESTAMP);

ALTER TABLE roles ALTER COLUMN id RESTART WITH 3;
ALTER TABLE users ALTER COLUMN id RESTART WITH 4;
ALTER TABLE support_tickets ALTER COLUMN id RESTART WITH 4;