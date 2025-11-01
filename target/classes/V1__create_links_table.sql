-- Create links table
CREATE TABLE links (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    long_url TEXT NOT NULL,
    short_code VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add index on short_code for faster lookups
CREATE INDEX idx_short_code ON links(short_code);