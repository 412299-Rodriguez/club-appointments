-- Migration script to update notification_logs table
-- Allows NULL user_id for session-level notifications (SESSION_CANCELLED, SESSION_MODIFIED)

USE turnero_db;

-- Drop the existing table if it exists (safe because we're using init scripts)
DROP TABLE IF EXISTS notification_logs;

-- Recreate notification_logs table with correct schema
CREATE TABLE IF NOT EXISTS notification_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NULL COMMENT 'NULL for session-level notifications',
    event_type ENUM('BOOKING_CONFIRMED', 'BOOKING_CANCELLED', 'SESSION_MODIFIED', 'SESSION_CANCELLED', 'REMINDER_24H') NOT NULL,
    training_session_id BIGINT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('PENDING', 'SENT', 'FAILED') DEFAULT 'PENDING',
    error_message TEXT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (training_session_id) REFERENCES training_sessions(id) ON DELETE SET NULL,
    INDEX idx_user (user_id),
    INDEX idx_event_type (event_type),
    INDEX idx_status (status),
    INDEX idx_sent_at (sent_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
