ALTER TABLE users
    ADD COLUMN date_of_birth DATE NULL AFTER phone,
    ADD COLUMN bio TEXT NULL AFTER date_of_birth,
    ADD COLUMN desired_position VARCHAR(150) NULL AFTER bio,
    ADD COLUMN desired_location VARCHAR(150) NULL AFTER desired_position;

UPDATE users u
    JOIN student_profiles sp ON sp.user_id = u.id
SET u.bio = sp.bio
WHERE u.bio IS NULL
  AND sp.bio IS NOT NULL;