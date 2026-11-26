CREATE TABLE student_projects (
                                  id BIGINT NOT NULL AUTO_INCREMENT,
                                  student_id BIGINT NOT NULL,
                                  project_name VARCHAR(150) NOT NULL,
                                  role_name VARCHAR(120) NULL,
                                  technologies VARCHAR(255) NULL,
                                  github_url VARCHAR(500) NOT NULL,
                                  demo_url VARCHAR(500) NULL,
                                  description TEXT NULL,
                                  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  CONSTRAINT pk_student_projects PRIMARY KEY (id),
                                  CONSTRAINT fk_student_projects_student FOREIGN KEY (student_id) REFERENCES student_profiles (id) ON DELETE CASCADE,
                                  INDEX idx_student_projects_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;