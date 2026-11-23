-- 1. Bang roles
CREATE TABLE roles (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       code VARCHAR(30) NOT NULL,
                       name VARCHAR(100) NOT NULL,
                       CONSTRAINT pk_roles PRIMARY KEY (id),
                       CONSTRAINT uq_roles_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Bang users
CREATE TABLE users (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       email VARCHAR(150) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       full_name VARCHAR(150) NOT NULL,
                       phone VARCHAR(20) NULL,
                       role_id BIGINT NOT NULL,
                       is_active BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       CONSTRAINT pk_users PRIMARY KEY (id),
                       CONSTRAINT uq_users_email UNIQUE (email),
                       CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id),
                       INDEX idx_users_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Bang companies
CREATE TABLE companies (
                           id BIGINT NOT NULL AUTO_INCREMENT,
                           name VARCHAR(150) NOT NULL,
                           industry VARCHAR(120) NULL,
                           website VARCHAR(255) NULL,
                           address VARCHAR(255) NULL,
                           description TEXT NULL,
                           CONSTRAINT pk_companies PRIMARY KEY (id),
                           CONSTRAINT uq_companies_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Bang partner_profiles
CREATE TABLE partner_profiles (
                                  id BIGINT NOT NULL AUTO_INCREMENT,
                                  user_id BIGINT NOT NULL,
                                  company_id BIGINT NOT NULL,
                                  job_title VARCHAR(120) NULL,
                                  department VARCHAR(120) NULL,
                                  verified BOOLEAN NOT NULL DEFAULT FALSE,
                                  CONSTRAINT pk_partner_profiles PRIMARY KEY (id),
                                  CONSTRAINT uq_partner_profiles_user UNIQUE (user_id),
                                  CONSTRAINT fk_partner_profiles_user FOREIGN KEY (user_id) REFERENCES users (id),
                                  CONSTRAINT fk_partner_profiles_company FOREIGN KEY (company_id) REFERENCES companies (id),
                                  INDEX idx_partner_profiles_company_id (company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Bang student_profiles
CREATE TABLE student_profiles (
                                  id BIGINT NOT NULL AUTO_INCREMENT,
                                  user_id BIGINT NOT NULL,
                                  student_code VARCHAR(30) NOT NULL,
                                  major VARCHAR(120) NULL,
                                  gpa DECIMAL(3,2) NULL,
                                  graduation_year INT NULL,
                                  bio TEXT NULL,
                                  portfolio_url VARCHAR(255) NULL,
                                  availability_status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
                                  CONSTRAINT pk_student_profiles PRIMARY KEY (id),
                                  CONSTRAINT uq_student_profiles_user UNIQUE (user_id),
                                  CONSTRAINT uq_student_profiles_code UNIQUE (student_code),
                                  CONSTRAINT fk_student_profiles_user FOREIGN KEY (user_id) REFERENCES users (id),
                                  INDEX idx_student_profiles_major (major)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Bang skills
CREATE TABLE skills (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        name VARCHAR(100) NOT NULL,
                        category VARCHAR(50) NULL,
                        CONSTRAINT pk_skills PRIMARY KEY (id),
                        CONSTRAINT uq_skills_name UNIQUE (name),
                        INDEX idx_skills_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Bang student_skills
CREATE TABLE student_skills (
                                student_id BIGINT NOT NULL,
                                skill_id BIGINT NOT NULL,
                                proficiency_level VARCHAR(30) NOT NULL DEFAULT 'BEGINNER',
                                CONSTRAINT pk_student_skills PRIMARY KEY (student_id, skill_id),
                                CONSTRAINT fk_student_skills_student FOREIGN KEY (student_id) REFERENCES student_profiles (id),
                                CONSTRAINT fk_student_skills_skill FOREIGN KEY (skill_id) REFERENCES skills (id),
                                INDEX idx_student_skills_skill_id (skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Bang job_posts
CREATE TABLE job_posts (
                           id BIGINT NOT NULL AUTO_INCREMENT,
                           company_id BIGINT NOT NULL,
                           created_by_user_id BIGINT NOT NULL,
                           title VARCHAR(200) NOT NULL,
                           location VARCHAR(150) NULL,
                           employment_type VARCHAR(40) NOT NULL DEFAULT 'INTERNSHIP',
                           status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
                           description TEXT NULL,
                           CONSTRAINT pk_job_posts PRIMARY KEY (id),
                           CONSTRAINT fk_job_posts_company FOREIGN KEY (company_id) REFERENCES companies (id),
                           CONSTRAINT fk_job_posts_created_by FOREIGN KEY (created_by_user_id) REFERENCES users (id),
                           INDEX idx_job_posts_company_id (company_id),
                           INDEX idx_job_posts_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Bang job_post_skills
CREATE TABLE job_post_skills (
                                 job_post_id BIGINT NOT NULL,
                                 skill_id BIGINT NOT NULL,
                                 is_required BOOLEAN NOT NULL DEFAULT TRUE,
                                 CONSTRAINT pk_job_post_skills PRIMARY KEY (job_post_id, skill_id),
                                 CONSTRAINT fk_job_post_skills_job_post FOREIGN KEY (job_post_id) REFERENCES job_posts (id),
                                 CONSTRAINT fk_job_post_skills_skill FOREIGN KEY (skill_id) REFERENCES skills (id),
                                 INDEX idx_job_post_skills_skill_id (skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Bang applications
CREATE TABLE applications (
                              id BIGINT NOT NULL AUTO_INCREMENT,
                              job_post_id BIGINT NOT NULL,
                              student_id BIGINT NOT NULL,
                              status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',
                              cover_letter TEXT NULL,
                              applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT pk_applications PRIMARY KEY (id),
                              CONSTRAINT uq_applications_job_student UNIQUE (job_post_id, student_id),
                              CONSTRAINT fk_applications_job_post FOREIGN KEY (job_post_id) REFERENCES job_posts (id),
                              CONSTRAINT fk_applications_student FOREIGN KEY (student_id) REFERENCES student_profiles (id),
                              INDEX idx_applications_student_id (student_id),
                              INDEX idx_applications_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Bang notifications
CREATE TABLE notifications (
                               id BIGINT NOT NULL AUTO_INCREMENT,
                               user_id BIGINT NOT NULL,
                               title VARCHAR(200) NOT NULL,
                               message TEXT NOT NULL,
                               is_read BOOLEAN NOT NULL DEFAULT FALSE,
                               created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT pk_notifications PRIMARY KEY (id),
                               CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id),
                               INDEX idx_notifications_user_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Bang system_settings
CREATE TABLE system_settings (
                                 id BIGINT NOT NULL AUTO_INCREMENT,
                                 setting_key VARCHAR(100) NOT NULL,
                                 setting_value VARCHAR(255) NOT NULL,
                                 description VARCHAR(255) NULL,
                                 CONSTRAINT pk_system_settings PRIMARY KEY (id),
                                 CONSTRAINT uq_system_settings_key UNIQUE (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. Bang activity_logs
CREATE TABLE activity_logs (
                               id BIGINT NOT NULL AUTO_INCREMENT,
                               user_id BIGINT NOT NULL,
                               action VARCHAR(100) NOT NULL,
                               target_type VARCHAR(60) NULL,
                               target_id BIGINT NULL,
                               created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT pk_activity_logs PRIMARY KEY (id),
                               CONSTRAINT fk_activity_logs_user FOREIGN KEY (user_id) REFERENCES users (id),
                               INDEX idx_activity_logs_user_id (user_id),
                               INDEX idx_activity_logs_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;