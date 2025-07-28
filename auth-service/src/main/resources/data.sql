CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL
);

INSERT INTO users (id, email, password)
SELECT 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
       'test@example.com',
       '$2a$12$FMVHO88LVWLLeovp0piTF.npMjYuHHPlZOlX0PZtM7hGuOmXyKcmK'
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'
       OR email = 'test@example.com'
);
-- It might login with:
-- email: test@example.com
-- password: secret
-- https://bcrypt-generator.com/

