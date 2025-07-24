/*
 * Use ONLY for testing/develop purpose DON'T use it on production
 * SpringBoot takes resources/data.sql and executes it when the application starts
 */
CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,  # Más estándar que GENERATED ALWAYS AS IDENTITY
  email VARCHAR(255) UNIQUE NOT NULL,
  alias VARCHAR(120),
  avatar_url VARCHAR(1024),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Inserts para tabla users
INSERT INTO users (email, alias, avatar_url, created_at, updated_at) VALUES
('alice@example.com', 'Alice', 'https://example.com/avatars/alice.jpg', '2024-01-15 10:00:00', '2024-01-15 10:00:00'),
('bob@example.org', 'Bob', 'https://example.com/avatars/bob.png', '2024-01-14 11:30:00', '2024-01-15 10:00:00'),
('charlie@test.com', 'Charlie', NULL, '2024-01-15 08:15:00', '2024-01-15 10:00:00'),
('admin@example.com', 'Admin', 'https://example.com/avatars/admin.webp', '2023-01-01 12:00:00', '2023-01-01 12:00:00'),
('jane.doe@mail.com', 'Jane', NULL, '2024-01-15 09:30:00', '2024-01-15 10:00:00'),
('john.doe@mail.com', 'John', 'https://example.com/avatars/john.jpg', '2023-06-15 08:30:00', '2023-06-15 08:30:00'),
('support@company.com', 'Support', 'https://example.com/avatars/support.png', '2024-01-15 10:00:00', '2024-01-15 10:00:00'),
('test.users@demo.com', 'Tester', NULL, '2024-01-15 05:00:00', '2024-01-15 10:00:00'),
('guest@example.com', 'Guest', 'https://example.com/avatars/guest.webp', '2024-01-01 00:00:00', '2024-01-01 00:00:00'),
('developer@h2.org', 'Dev', NULL, '2024-01-08 10:00:00', '2024-01-15 10:00:00'),
('sales@business.com', 'Sales', 'https://example.com/avatars/sales.jpg', '2024-01-15 10:00:00', '2024-01-15 10:00:00'),
('no-reply@newsletter.com', 'Newsletter', NULL, '2023-03-10 10:00:00', '2023-03-10 10:00:00'),
('contact@website.com', 'Contact', 'https://example.com/avatars/contact.png', '2024-01-13 10:00:00', '2024-01-15 10:00:00'),
('api.users@service.com', 'API', NULL, '2024-01-15 10:00:00', '2024-01-15 10:00:00'),
('hr@enterprise.com', 'HR', 'https://example.com/avatars/hr.webp', '2023-12-31 23:59:59', '2023-12-31 23:59:59'),
('mobile.app@client.com', 'Mobile', NULL, '2024-01-15 09:45:00', '2024-01-15 10:00:00'),
('webmaster@site.com', 'Webmaster', 'https://example.com/avatars/webmaster.jpg', '2024-01-15 10:00:00', '2024-01-15 10:00:00'),
('backup@data.com', 'Backup', NULL, '2023-07-04 18:30:00', '2023-07-04 18:30:00'),
('notifications@system.com', 'System', 'https://example.com/avatars/system.png', '2024-01-15 04:00:00', '2024-01-15 10:00:00'),
('demo.account@h2.com', 'Demo', NULL, '2024-01-15 10:00:00', '2024-01-15 10:00:00');
