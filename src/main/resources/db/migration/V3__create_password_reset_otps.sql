CREATE TABLE password_reset_otps (
                                     id BIGINT NOT NULL AUTO_INCREMENT,
                                     user_id BIGINT NOT NULL,
                                     email VARCHAR(255) NOT NULL,
                                     otp_code VARCHAR(6) NOT NULL,
                                     expires_at DATETIME NOT NULL,
                                     failed_attempts INT NOT NULL DEFAULT 0,
                                     is_used BOOLEAN NOT NULL DEFAULT FALSE,
                                     is_cancelled BOOLEAN NOT NULL DEFAULT FALSE,
                                     created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                     CONSTRAINT pk_password_reset_otps PRIMARY KEY (id),
                                     CONSTRAINT fk_password_reset_otps_user FOREIGN KEY (user_id)
                                         REFERENCES users(id) ON DELETE CASCADE,
                                     INDEX idx_otp_email_active (email, is_used, is_cancelled, expires_at),
                                     INDEX idx_otp_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;