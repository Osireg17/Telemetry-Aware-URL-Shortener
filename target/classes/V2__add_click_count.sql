-- Add click_count column to links table
ALTER TABLE links ADD COLUMN click_count INT NOT NULL DEFAULT 0;