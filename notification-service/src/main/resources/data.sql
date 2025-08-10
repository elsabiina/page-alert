CREATE TABLE IF NOT EXISTS email_confirmation_tokens (
    id          UUID         NOT NULL,
    user_email  VARCHAR(255) NOT NULL,
    token       VARCHAR(255) NOT NULL UNIQUE,
    created_at  TIMESTAMP    NOT NULL,
    expires_at  TIMESTAMP    NOT NULL,
    confirmed   BOOLEAN      NOT NULL DEFAULT FALSE,
    confirmed_at TIMESTAMP,
    PRIMARY KEY (id)
);

INSERT INTO email_confirmation_tokens
(id, user_email, token, created_at, expires_at, confirmed, confirmed_at)
SELECT
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'test@example.com',
    'sample-token-123',
    CURRENT_TIMESTAMP,
    DATEADD('DAY', 1, CURRENT_TIMESTAMP),
    TRUE,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM email_confirmation_tokens
    WHERE id   = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'
       OR token = 'sample-token-123'
);
