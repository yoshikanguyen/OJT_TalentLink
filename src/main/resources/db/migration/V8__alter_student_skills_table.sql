ALTER TABLE student_skills
    MODIFY COLUMN proficiency_level TINYINT NOT NULL DEFAULT 1,
    ADD CONSTRAINT chk_student_skills_level CHECK (proficiency_level BETWEEN 1 AND 5);