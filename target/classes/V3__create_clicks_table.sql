-- Create clicks table for telemetry data
CREATE TABLE clicks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    link_id BIGINT NOT NULL,
    click_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_agent TEXT,
    ip_address VARCHAR(45),
    referer TEXT,
    CONSTRAINT fk_clicks_link_id FOREIGN KEY (link_id) REFERENCES links(id) ON DELETE CASCADE
);

-- Add indexes for performance
CREATE INDEX idx_link_id ON clicks(link_id);
CREATE INDEX idx_timestamp ON clicks(click_timestamp);