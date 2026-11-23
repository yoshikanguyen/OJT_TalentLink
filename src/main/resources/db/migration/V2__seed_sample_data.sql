-- 1. Bảng roles (3 vai trò)
INSERT INTO roles (id, code, name) VALUES
                                       (1, 'ADMIN', 'Quản trị viên'),
                                       (2, 'PARTNER', 'Đối tác doanh nghiệp'),
                                       (3, 'STUDENT', 'Sinh viên');

-- 2. Bảng users (1 admin + 3 đối tác + 15 sinh viên = 19 tài khoản)
-- Mật khẩu mẫu (chỉ dùng cho môi trường dev/test, ghi rõ trong README.md)
INSERT INTO users (id, email, password_hash, full_name, phone, role_id, is_active, created_at) VALUES
-- 1 Admin
(1,  'abby.admin@talentlink.local',    '{noop}Admin@123',   'Abby Vance',    '0901000001', 1, TRUE, '2026-09-01 08:00:00'),
-- 3 Đối tác
(2,  'bobby.partner@talentlink.local', '{noop}Partner@123', 'Bobby Mercer',  '0901000002', 2, TRUE, '2026-09-01 08:10:00'),
(3,  'cody.partner@talentlink.local',  '{noop}Partner@123', 'Cody Sterling', '0901000003', 2, TRUE, '2026-09-01 08:20:00'),
(4,  'daisy.partner@talentlink.local', '{noop}Partner@123', 'Daisy Brooks',  '0901000004', 2, TRUE, '2026-09-01 08:30:00'),
-- 15 Sinh viên (Ethan -> Sam)
(5,  'ethan.student@talentlink.local', '{noop}Student@123', 'Ethan Carter',  '0902000005', 3, TRUE, '2026-09-02 09:00:00'),
(6,  'fiona.student@talentlink.local', '{noop}Student@123', 'Fiona Gallagher','0902000006', 3, TRUE, '2026-09-02 09:05:00'),
(7,  'george.student@talentlink.local','{noop}Student@123', 'George Miller', '0902000007', 3, TRUE, '2026-09-02 09:10:00'),
(8,  'hannah.student@talentlink.local','{noop}Student@123', 'Hannah Abbott', '0902000008', 3, TRUE, '2026-09-02 09:15:00'),
(9,  'isaac.student@talentlink.local', '{noop}Student@123', 'Isaac Bennett', '0902000009', 3, TRUE, '2026-09-02 09:20:00'),
(10, 'julia.student@talentlink.local', '{noop}Student@123', 'Julia Foster',  '0902000010', 3, TRUE, '2026-09-02 09:25:00'),
(11, 'kevin.student@talentlink.local', '{noop}Student@123', 'Kevin Hayes',   '0902000011', 3, TRUE, '2026-09-02 09:30:00'),
(12, 'lily.student@talentlink.local',  '{noop}Student@123', 'Lily Morgan',   '0902000012', 3, TRUE, '2026-09-02 09:35:00'),
(13, 'mason.student@talentlink.local', '{noop}Student@123', 'Mason Reed',    '0902000013', 3, TRUE, '2026-09-02 09:40:00'),
(14, 'nora.student@talentlink.local',  '{noop}Student@123', 'Nora Sullivan', '0902000014', 3, TRUE, '2026-09-02 09:45:00'),
(15, 'owen.student@talentlink.local',  '{noop}Student@123', 'Owen Turner',   '0902000015', 3, TRUE, '2026-09-02 09:50:00'),
(16, 'piper.student@talentlink.local', '{noop}Student@123', 'Piper Walsh',   '0902000016', 3, TRUE, '2026-09-02 09:55:00'),
(17, 'quinn.student@talentlink.local', '{noop}Student@123', 'Quinn Bell',    '0902000017', 3, TRUE, '2026-09-02 10:00:00'),
(18, 'ruby.student@talentlink.local',  '{noop}Student@123', 'Ruby Cooper',   '0902000018', 3, TRUE, '2026-09-02 10:05:00'),
(19, 'sam.student@talentlink.local',   '{noop}Student@123', 'Sam Dawson',    '0902000019', 3, TRUE, '2026-09-02 10:10:00');

-- 3. Bảng companies (3 công ty gắn với 3 đối tác)
INSERT INTO companies (id, name, industry, website, address, description) VALUES
                                                                              (1, 'Bobby Tech Solutions', 'Phát triển phần mềm & Cloud', 'https://bobbytech.example.local', '10 Bobby Street, District 1', 'Xây dựng nền tảng web doanh nghiệp và dịch vụ đám mây.'),
                                                                              (2, 'Cody Digital Labs',    'Ứng dụng số & Sản phẩm UI/UX', 'https://codylabs.example.local',  '20 Cody Avenue, District 3',  'Phát triển sản phẩm số, giao diện người dùng và hệ thống API.'),
                                                                              (3, 'Daisy Cloud Systems',  'Hạ tầng dữ liệu & Tự động hóa','https://daisycloud.example.local','30 Daisy Road, Thu Duc City', 'Cung cấp giải pháp dữ liệu, kiểm thử tự động và vận hành hệ thống.');

-- 4. Bảng partner_profiles (3 hồ sơ đối tác)
INSERT INTO partner_profiles (id, user_id, company_id, job_title, department, verified) VALUES
                                                                                            (1, 2, 1, 'Trưởng nhóm Tuyển dụng Kỹ thuật', 'Nhân sự', TRUE),
                                                                                            (2, 3, 2, 'Quản lý Sản phẩm & Thực tập',      'Sản phẩm', TRUE),
                                                                                            (3, 4, 3, 'Điều phối Chương trình Thực tập',   'Vận hành', TRUE);

-- 5. Bảng student_profiles (15 hồ sơ sinh viên đầy đủ thông tin)
INSERT INTO student_profiles (id, user_id, student_code, major, gpa, graduation_year, bio, portfolio_url, availability_status) VALUES
                                                                                                                                   (1,  5,  'SV005', 'Công nghệ thông tin', 3.45, 2027, 'Sinh viên năm cuối quan tâm phát triển backend Java/Spring.', 'https://portfolio.example.local/ethan',  'AVAILABLE'),
                                                                                                                                   (2,  6,  'SV006', 'Kỹ thuật phần mềm',    3.60, 2027, 'Yêu thích thiết kế giao diện web sạch và dễ dùng.',          'https://portfolio.example.local/fiona',  'AVAILABLE'),
                                                                                                                                   (3,  7,  'SV007', 'Hệ thống thông tin',  3.25, 2027, 'Tìm hiểu về cơ sở dữ liệu quan hệ và phân tích nghiệp vụ.',  'https://portfolio.example.local/george', 'AVAILABLE'),
                                                                                                                                   (4,  8,  'SV008', 'Khoa học máy tính',   3.72, 2027, 'Quan tâm cấu trúc dữ liệu, thuật toán và kiểm thử chất lượng.','https://portfolio.example.local/hannah','AVAILABLE'),
                                                                                                                                   (5,  9,  'SV009', 'Công nghệ thông tin', 3.30, 2027, 'Thích làm việc với REST API, Docker và quy trình Git.',      'https://portfolio.example.local/isaac',  'AVAILABLE'),
                                                                                                                                   (6,  10, 'SV010', 'Thiết kế số & UI/UX', 3.55, 2027, 'Tập trung vào wireframe, design system và trải nghiệm người dùng.','https://portfolio.example.local/julia','AVAILABLE'),
                                                                                                                                   (7,  11, 'SV011', 'Kỹ thuật phần mềm',    3.18, 2027, 'Hứng thú với phát triển ứng dụng web fullstack cơ bản.',      'https://portfolio.example.local/kevin',  'AVAILABLE'),
                                                                                                                                   (8,  12, 'SV012', 'Ngôn ngữ & CNTT',     3.50, 2027, 'Có khả năng đọc tài liệu kỹ thuật tiếng Anh và tiếng Nhật.',  'https://portfolio.example.local/lily',   'AVAILABLE'),
                                                                                                                                   (9,  13, 'SV013', 'Công nghệ thông tin', 3.40, 2027, 'Quan tâm tối ưu truy vấn SQL và quản trị cơ sở dữ liệu.',     'https://portfolio.example.local/mason',  'AVAILABLE'),
                                                                                                                                   (10, 14, 'SV014', 'Hệ thống thông tin',  3.62, 2027, 'Thích viết tài liệu quy trình, đặc tả và phối hợp nhóm.',    'https://portfolio.example.local/nora',   'AVAILABLE'),
                                                                                                                                   (11, 15, 'SV015', 'Khoa học máy tính',   3.28, 2027, 'Đang rèn luyện kỹ năng lập trình hướng đối tượng và test.',  'https://portfolio.example.local/owen',   'AVAILABLE'),
                                                                                                                                   (12, 16, 'SV016', 'Kỹ thuật phần mềm',    3.48, 2027, 'Quan tâm kiến trúc MVC, bảo mật ứng dụng và triển khai.',    'https://portfolio.example.local/piper',  'AVAILABLE'),
                                                                                                                                   (13, 17, 'SV017', 'Thiết kế đa phương tiện',3.35,2027, 'Có nền tảng đồ họa 2D/3D và trình bày trực quan sản phẩm.',  'https://portfolio.example.local/quinn',  'AVAILABLE'),
                                                                                                                                   (14, 18, 'SV018', 'Công nghệ thông tin', 3.58, 2027, 'Thích xây dựng tính năng web tương tác với JS và Thymeleaf.', 'https://portfolio.example.local/ruby',   'AVAILABLE'),
                                                                                                                                   (15, 19, 'SV019', 'Hệ thống thông tin',  3.22, 2027, 'Chủ động học hỏi, làm việc nhóm tốt và sẵn sàng thực tập.',   'https://portfolio.example.local/sam',    'AVAILABLE');

-- 6. Bảng skills (30 kỹ năng)
INSERT INTO skills (id, name, category) VALUES
                                            (1,  'Java',                    'BACKEND'),
                                            (2,  'Spring Boot',             'BACKEND'),
                                            (3,  'Spring Security',         'BACKEND'),
                                            (4,  'Thymeleaf',               'FRONTEND'),
                                            (5,  'HTML5',                   'FRONTEND'),
                                            (6,  'CSS3',                    'FRONTEND'),
                                            (7,  'Bootstrap',               'FRONTEND'),
                                            (8,  'JavaScript',              'FRONTEND'),
                                            (9,  'TypeScript',              'FRONTEND'),
                                            (10, 'React',                   'FRONTEND'),
                                            (11, 'SQL',                     'DATABASE'),
                                            (12, 'MySQL',                   'DATABASE'),
                                            (13, 'PostgreSQL',              'DATABASE'),
                                            (14, 'Flyway',                  'DATABASE'),
                                            (15, 'Git',                     'DEVOPS_TOOLS'),
                                            (16, 'Docker',                  'DEVOPS_TOOLS'),
                                            (17, 'Gradle',                  'DEVOPS_TOOLS'),
                                            (18, 'Maven',                   'DEVOPS_TOOLS'),
                                            (19, 'Postman',                 'DEVOPS_TOOLS'),
                                            (20, 'JUnit',                   'TESTING'),
                                            (21, 'Figma',                   'DESIGN'),
                                            (22, 'Photoshop',               'DESIGN'),
                                            (23, '3D Modeling',             'DESIGN'),
                                            (24, 'UI/UX Design',            'DESIGN'),
                                            (25, 'REST API',                'BACKEND'),
                                            (26, 'Linux',                   'DEVOPS_TOOLS'),
                                            (27, 'Agile/Scrum',             'SOFT_SKILL'),
                                            (28, 'Technical Documentation', 'SOFT_SKILL'),
                                            (29, 'English Reading',         'LANGUAGE'),
                                            (30, 'Japanese N2',             'LANGUAGE');

-- 7. Bảng student_skills (gắn kỹ năng cho 15 sinh viên)
INSERT INTO student_skills (student_id, skill_id, proficiency_level) VALUES
                                                                         (1, 1, 'INTERMEDIATE'), (1, 2, 'INTERMEDIATE'), (1, 12, 'INTERMEDIATE'), (1, 15, 'INTERMEDIATE'),
                                                                         (2, 5, 'ADVANCED'),     (2, 6, 'ADVANCED'),     (2, 7, 'INTERMEDIATE'),  (2, 8, 'INTERMEDIATE'),
                                                                         (3, 11, 'INTERMEDIATE'),(3, 12, 'INTERMEDIATE'),(3, 28, 'INTERMEDIATE'), (3, 27, 'BEGINNER'),
                                                                         (4, 1, 'INTERMEDIATE'), (4, 20, 'INTERMEDIATE'),(4, 15, 'INTERMEDIATE'), (4, 29, 'INTERMEDIATE'),
                                                                         (5, 2, 'INTERMEDIATE'), (5, 25, 'INTERMEDIATE'),(5, 16, 'BEGINNER'),     (5, 19, 'INTERMEDIATE'),
                                                                         (6, 21, 'INTERMEDIATE'),(6, 24, 'INTERMEDIATE'),(6, 5, 'INTERMEDIATE'),  (6, 6, 'INTERMEDIATE'),
                                                                         (7, 1, 'BEGINNER'),     (7, 4, 'BEGINNER'),     (7, 12, 'BEGINNER'),     (7, 8, 'BEGINNER'),
                                                                         (8, 29, 'ADVANCED'),     (8, 30, 'INTERMEDIATE'),(8, 28, 'INTERMEDIATE'), (8, 15, 'BEGINNER'),
                                                                         (9, 11, 'ADVANCED'),     (9, 12, 'INTERMEDIATE'),(9, 14, 'INTERMEDIATE'), (9, 13, 'BEGINNER'),
                                                                         (10, 27, 'INTERMEDIATE'),(10, 28, 'ADVANCED'),  (10, 21, 'BEGINNER'),    (10, 29, 'INTERMEDIATE'),
                                                                         (11, 1, 'INTERMEDIATE'),(11, 17, 'BEGINNER'),   (11, 20, 'BEGINNER'),    (11, 26, 'BEGINNER'),
                                                                         (12, 2, 'INTERMEDIATE'),(12, 3, 'BEGINNER'),    (12, 4, 'INTERMEDIATE'), (12, 18, 'BEGINNER'),
                                                                         (13, 22, 'INTERMEDIATE'),(13, 23, 'INTERMEDIATE'),(13, 24, 'BEGINNER'),    (13, 21, 'BEGINNER'),
                                                                         (14, 8, 'INTERMEDIATE'),(14, 4, 'INTERMEDIATE'),(14, 7, 'INTERMEDIATE'),  (14, 9, 'BEGINNER'),
                                                                         (15, 27, 'INTERMEDIATE'),(15, 15, 'BEGINNER'),  (15, 19, 'BEGINNER'),    (15, 29, 'BEGINNER');

-- 8. Bảng job_posts (3 tin tuyển thực tập từ 3 công ty đối tác)
INSERT INTO job_posts (id, company_id, created_by_user_id, title, location, employment_type, status, description) VALUES
                                                                                                                      (1, 1, 2, 'Thực tập sinh Backend Java & Spring Boot', 'TP. Hồ Chí Minh', 'INTERNSHIP', 'OPEN', 'Tham gia xây dựng REST API, làm việc với MySQL và quy trình Git.'),
                                                                                                                      (2, 2, 3, 'Thực tập sinh Frontend & UI/UX',          'TP. Hồ Chí Minh', 'INTERNSHIP', 'OPEN', 'Phát triển giao diện web responsive với HTML/CSS/Bootstrap và Thymeleaf.'),
                                                                                                                      (3, 3, 4, 'Thực tập sinh Vận hành & Kiểm thử hệ thống','TP. Hồ Chí Minh', 'INTERNSHIP', 'OPEN', 'Hỗ trợ kiểm thử, tài liệu hóa hệ thống và theo dõi chất lượng bản dựng.');

-- 9. Bảng job_post_skills (kỹ năng yêu cầu cho từng tin tuyển)
INSERT INTO job_post_skills (job_post_id, skill_id, is_required) VALUES
                                                                     (1, 1, TRUE), (1, 2, TRUE), (1, 12, TRUE), (1, 15, FALSE),
                                                                     (2, 5, TRUE), (2, 6, TRUE), (2, 7, TRUE),  (2, 8, FALSE),
                                                                     (3, 15, TRUE),(3, 20, FALSE),(3, 28, TRUE),(3, 19, FALSE);

-- 10. Bảng applications (hồ sơ ứng tuyển mẫu của sinh viên)
INSERT INTO applications (id, job_post_id, student_id, status, cover_letter, applied_at) VALUES
                                                                                             (1, 1, 1, 'SUBMITTED',    'Ethan mong muốn thực tập vị trí Backend Java tại Bobby Tech Solutions.', '2026-10-01 10:00:00'),
                                                                                             (2, 1, 5, 'REVIEWING',    'Isaac quan tâm phát triển dịch vụ backend và làm việc với API.',         '2026-10-02 11:00:00'),
                                                                                             (3, 2, 2, 'SHORTLISTED',  'Fiona muốn đóng góp vào giao diện người dùng tại Cody Digital Labs.',   '2026-10-03 14:00:00'),
                                                                                             (4, 2, 6, 'SUBMITTED',    'Julia gửi hồ sơ với kinh nghiệm wireframe và thiết kế giao diện.',     '2026-10-04 15:00:00'),
                                                                                             (5, 3, 4, 'SUBMITTED',    'Hannah muốn tham gia kiểm thử và đảm bảo chất lượng hệ thống.',       '2026-10-05 16:00:00');

-- 11. Bảng notifications (thông báo mẫu cho người dùng)
INSERT INTO notifications (id, user_id, title, message, is_read, created_at) VALUES
                                                                                 (1, 1, 'Chào mừng quản trị viên', 'Tài khoản Abby Vance đã sẵn sàng quản lý hệ thống TalentLink.', TRUE,  '2026-09-01 08:05:00'),
                                                                                 (2, 2, 'Tin tuyển dụng đã mở',     'Tin thực tập Backend Java tại Bobby Tech Solutions đã được đăng.', FALSE, '2026-10-01 09:00:00'),
                                                                                 (3, 5, 'Hồ sơ đang được xem xét',  'Đơn ứng tuyển của Ethan cho vị trí Backend Java đang được xem xét.',FALSE, '2026-10-02 12:00:00');

-- 12. Bảng system_settings (cấu hình hệ thống mẫu)
INSERT INTO system_settings (id, setting_key, setting_value, description) VALUES
                                                                              (1, 'SITE_NAME',        'TalentLink', 'Tên hiển thị của hệ thống'),
                                                                              (2, 'MAX_APPLICATIONS', '5',          'Số lượng hồ sơ ứng tuyển tối đa mỗi sinh viên');

-- 13. Bảng activity_logs (nhật ký hoạt động mẫu)
INSERT INTO activity_logs (id, user_id, action, target_type, target_id, created_at) VALUES
                                                                                        (1, 1, 'SEED_INITIAL_DATA', 'DATABASE', 1, '2026-10-01 08:00:00'),
                                                                                        (2, 2, 'CREATE_JOB_POST',   'JOB_POST', 1, '2026-10-01 09:00:00'),
                                                                                        (3, 5, 'SUBMIT_APPLICATION','APPLICATION',1,'2026-10-01 10:00:00');