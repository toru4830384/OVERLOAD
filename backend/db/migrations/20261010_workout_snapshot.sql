-- Apply once to the development database before starting this backend.
-- Existing records retain NULL metadata; historical calories cannot be reconstructed.
ALTER TABLE workout_sessions
    ADD COLUMN body_weight_kg DECIMAL(5,2) NULL,
    ADD COLUMN total_calories DECIMAL(12,2) NULL,
    ADD COLUMN client_request_id VARCHAR(36) CHARACTER SET ascii COLLATE ascii_bin NULL,
    ADD COLUMN request_hash VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL,
    ADD UNIQUE KEY uq_workout_request (user_id, client_request_id);
