CREATE TABLE IF NOT EXISTS urls (
    id         UUID PRIMARY KEY,
    user_id    UUID NOT NULL,
    title      TEXT,
    url        VARCHAR NOT NULL UNIQUE,
    body_hash  VARCHAR,
    selector   VARCHAR,
    type_check VARCHAR(20) NOT NULL DEFAULT 'ANY',
    price_min  DECIMAL(12,2),
    price_max  DECIMAL(12,2),
    has_changed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (type_check IN ('ANY', 'PAGE_SECTION', 'PRICE'))
);

CREATE INDEX IF NOT EXISTS idx_user_urls ON urls (id, user_id);

INSERT INTO urls (id, user_id, url, body_hash, selector, type_check, price_min, price_max)
SELECT *
FROM (
    VALUES
        ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'bdefbc99-9c0b-4ef8-bb6d-6bb9bd380a31',
         'https://example.com/product/1', 'hash1', '.price', 'PRICE', 10.50, 20.00),
        ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'bdefbc99-9c0b-4ef8-bb6d-6bb9bd380a31',
         'https://example.com/product/2', 'hash2', '.price', 'ANY', NULL, NULL),
        ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'bdefbc99-9c0b-4ef8-bb6d-6bb9bd380a31',
         'https://example.com/product/3', 'hash3', '.stock', 'ANY', NULL, NULL),
        ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'bdefbc99-9c0b-4ef8-bb6d-6bb9bd380a31',
         'https://example.com/product/4', 'hash4', '.price', 'PRICE', 5.00, 15.00),
        ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'bdefbc99-9c0b-4ef8-bb6d-6bb9bd380a31',
         'https://example.com/product/5', 'hash5', '.title', 'ANY', NULL, NULL)
) AS t(id, user_id, url, body_hash, selector, type_check, price_min, price_max)
WHERE NOT EXISTS (SELECT 1 FROM urls);
