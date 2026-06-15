-- Migration: Add reply_to_id for message quote functionality
-- Date: 2026-06-15

-- Private chat messages: add reply_to_id column after send_time
ALTER TABLE message ADD COLUMN IF NOT EXISTS reply_to_id BIGINT NULL AFTER send_time;

-- Group chat messages: add reply_to_id column after recall_time
ALTER TABLE group_message ADD COLUMN IF NOT EXISTS reply_to_id BIGINT NULL AFTER recall_time;
