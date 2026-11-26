UPDATE student_skills
SET proficiency_level = CASE UPPER(TRIM(proficiency_level))
                            WHEN 'BEGINNER' THEN '1'
                            WHEN 'INTERMEDIATE' THEN '3'
                            WHEN 'ADVANCED' THEN '5'
                            ELSE proficiency_level
    END
WHERE proficiency_level NOT REGEXP '^[0-9]+$';

ALTER TABLE student_skills
    MODIFY COLUMN proficiency_level INT NOT NULL DEFAULT 1,
    ADD CONSTRAINT chk_student_skills_level CHECK (proficiency_level BETWEEN 1 AND 5);