CREATE TABLE educations (
                            id BIGINT NOT NULL AUTO_INCREMENT,
                            user_id BIGINT NOT NULL,
                            school_name VARCHAR(150) NOT NULL,
                            major VARCHAR(150) NOT NULL,
                            start_date DATE NOT NULL,
                            end_date DATE NULL,
                            gpa DECIMAL(3,2) NULL,
                            created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT pk_educations PRIMARY KEY (id),
                            CONSTRAINT fk_educations_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
                            INDEX idx_educations_user_id (user_id),
                            INDEX idx_educations_start_date (start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;