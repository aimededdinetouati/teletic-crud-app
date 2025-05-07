INSERT INTO roles (name, created_date, last_modified_date)
VALUES
    ('ROLE_ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ROLE_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert default admin user (password: poi321)
INSERT INTO users (full_name, email, password, locked, enabled, created_date, last_modified_date)
VALUES
    ('Administrator', 'admin@teletic.com', '$2a$10$MrDHlV18B8UItSV1QpvtZOLXQwTnOHQDfFsECqeVH7xLrQlK1Yo0O', false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert default regular user (password: user123)
INSERT INTO users (full_name, email, password, locked, enabled, created_date, last_modified_date)
VALUES
    ('Regular User', 'user@teletic.com', '$2a$10$GjB2PiEblGJGklCS7i0XWuQgDCVq35xJV6FZQ0sRnNFCGVLvRpJ7a', false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Assign roles to users
INSERT INTO users_roles (users_id, roles_id)
VALUES
    (1, 1), -- Admin user gets admin role
    (2, 2); -- Regular user gets user role

-- Insert sample tasks
INSERT INTO tasks (title, description, status, due_date, assignee_id, created_date, last_modified_date)
VALUES
    ('Develop Mobile App', 'Create a new mobile application for client project', 'TO_DO', CURRENT_TIMESTAMP + INTERVAL '30 days', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Optimize Website Performance', 'Analyze and improve website loading time', 'IN_PROGRESS', CURRENT_TIMESTAMP + INTERVAL '15 days', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Update Documentation', 'Review and update API documentation', 'COMPLETED', CURRENT_TIMESTAMP + INTERVAL '5 days', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Fix Authentication Bug', 'Resolve issues with user login system', 'TO_DO', CURRENT_TIMESTAMP + INTERVAL '7 days', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Implement New Feature', 'Add dark mode to the application', 'TO_DO', CURRENT_TIMESTAMP + INTERVAL '20 days', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
