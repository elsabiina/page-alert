/*
 * Use ONLY for testing/develop purpose DON'T use it on production
 * SpringBoot takes resources/data.sql and executes it when the application starts
 */
CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  alias VARCHAR(120),
  avatar_url VARCHAR(1024),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  urls_last_viewed_at TIMESTAMP
);


-- Use the inserts only to populate it the first time or for test

-- Inserts para tabla users
-- 20 inserts con UUIDs explícitos
INSERT INTO users (id, email, alias, avatar_url) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'alice@example.com',  'Alice',  'https://i.pravatar.cc/150?u=alice'),
('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'bob@example.com',    'Bob',    'https://i.pravatar.cc/150?u=bob'),
('c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'carol@example.com',  'Carol',  'https://i.pravatar.cc/150?u=carol'),
('d3eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'dave@example.com',   'Dave',   'https://i.pravatar.cc/150?u=dave'),
('e4eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'eve@example.com',    'Eve',    'https://i.pravatar.cc/150?u=eve'),
('f5eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', 'frank@example.com',  'Frank',  'https://i.pravatar.cc/150?u=frank'),
('a6eebc99-9c0b-4ef8-bb6d-6bb9bd380a17', 'grace@example.com',  'Grace',  'https://i.pravatar.cc/150?u=grace'),
('b7eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', 'heidi@example.com',  'Heidi',  'https://i.pravatar.cc/150?u=heidi'),
('c8eebc99-9c0b-4ef8-bb6d-6bb9bd380a19', 'ivan@example.com',   'Ivan',   'https://i.pravatar.cc/150?u=ivan'),
('d9eebc99-9c0b-4ef8-bb6d-6bb9bd380a20', 'judy@example.com',   'Judy',   'https://i.pravatar.cc/150?u=judy'),
('eaeebc99-9c0b-4ef8-bb6d-6bb9bd380a21', 'mallory@example.com','Mallory','https://i.pravatar.cc/150?u=mallory'),
('fbeebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'nancy@example.com',  'Nancy',  'https://i.pravatar.cc/150?u=nancy'),
('acdebc99-9c0b-4ef8-bb6d-6bb9bd380a23', 'olivia@example.com', 'Olivia', 'https://i.pravatar.cc/150?u=olivia'),
('bcdebc99-9c0b-4ef8-bb6d-6bb9bd380a24', 'paul@example.com',   'Paul',   'https://i.pravatar.cc/150?u=paul'),
('ccdebc99-9c0b-4ef8-bb6d-6bb9bd380a25', 'quinn@example.com',  'Quinn',  'https://i.pravatar.cc/150?u=quinn'),
('dcdebc99-9c0b-4ef8-bb6d-6bb9bd380a26', 'rose@example.com',   'Rose',   'https://i.pravatar.cc/150?u=rose'),
('ecdebc99-9c0b-4ef8-bb6d-6bb9bd380a27', 'steve@example.com',  'Steve',  'https://i.pravatar.cc/150?u=steve'),
('fcdebc99-9c0b-4ef8-bb6d-6bb9bd380a28', 'tina@example.com',   'Tina',   'https://i.pravatar.cc/150?u=tina'),
('adefbc99-9c0b-4ef8-bb6d-6bb9bd380a29', 'ursula@example.com', 'Ursula', 'https://i.pravatar.cc/150?u=ursula'),
('bdefbc99-9c0b-4ef8-bb6d-6bb9bd380a30', 'victor@example.com', 'Victor', 'https://i.pravatar.cc/150?u=victor');
