ALTER TABLE message ADD COLUMN IF NOT EXISTS reply_to_id BIGINT NULL AFTER send_time;
ALTER TABLE group_message ADD COLUMN IF NOT EXISTS reply_to_id BIGINT NULL AFTER recall_time;
