-- Changeset 1: Create links table
CREATE TABLE IF NOT EXISTS links (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    long_url TEXT NOT NULL,
    short_code VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_short_code ON links(short_code);

-- Changeset 2: Add click_count column to links
ALTER TABLE links ADD COLUMN IF NOT EXISTS click_count INT DEFAULT 0 NOT NULL;

-- Changeset 4: Create clicks table
CREATE TABLE IF NOT EXISTS clicks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    link_id BIGINT NOT NULL,
    click_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    user_agent TEXT,
    ip_address VARCHAR(45),
    referer TEXT,
    CONSTRAINT fk_clicks_link_id FOREIGN KEY (link_id) REFERENCES links(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_link_id ON clicks(link_id);
CREATE INDEX IF NOT EXISTS idx_timestamp ON clicks(click_timestamp);
