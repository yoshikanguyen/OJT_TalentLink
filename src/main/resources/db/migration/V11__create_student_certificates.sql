CREATE TABLE student_certificates (
                                      id BIGINT NOT NULL AUTO_INCREMENT,
                                      student_profile_id BIGINT NOT NULL,
                                      certificate_name VARCHAR(150) NOT NULL,
                                      issuing_organization VARCHAR(150) NOT NULL,
                                      issue_date DATE NOT NULL,
                                      stored_file_name VARCHAR(255) NOT NULL,
                                      original_file_name VARCHAR(255) NOT NULL,
                                      content_type VARCHAR(100) NOT NULL,
                                      file_size_bytes BIGINT NOT NULL,
                                      created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      CONSTRAINT pk_student_certificates PRIMARY KEY (id),
                                      CONSTRAINT fk_student_certificates_student_profile
                                          FOREIGN KEY (student_profile_id) REFERENCES student_profiles (id) ON DELETE CASCADE,
                                      INDEX idx_student_certificates_profile_id (student_profile_id),
                                      INDEX idx_student_certificates_issue_date (issue_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;