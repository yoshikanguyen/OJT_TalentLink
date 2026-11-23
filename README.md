# TalentLink

Hệ thống web kết nối cơ hội thực tập và việc làm giữa sinh viên với doanh nghiệp đối tác, xây dựng trên nền tảng **Spring Boot**, **Thymeleaf**, **Spring Security**, **Flyway** và **MySQL**.

---

## 1. Công nghệ sử dụng

- **Backend:** Java, Spring Boot, Spring Security, Spring Data JPA
- **Frontend:** Thymeleaf, HTML5, CSS3, Bootstrap
- **Cơ sở dữ liệu & Migration:** MySQL, Flyway (`src/main/resources/db/migration`)
- **Build Tool:** Gradle

---

## 2. Hướng dẫn chạy trên cơ sở dữ liệu trống (Flyway Migration)

1. Mở MySQL và tạo một cơ sở dữ liệu trống mới (chưa có bảng nào, ví dụ: `talentlink_dev`):
   ```sql
   CREATE DATABASE talentlink_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   USE talentlink_dev;
   ```
2. Cập nhật thông tin kết nối MySQL (URL, username, password) trỏ tới database trống vừa tạo (ví dụ `talentlink_dev`) trong file cấu hình ứng dụng (`src/main/resources/application.properties` hoặc `application-dev.properties`).
3. Khởi chạy ứng dụng Spring Boot (từ lớp `TalentLinkApplication` hoặc lệnh `./gradlew bootRun` / `.\gradlew.bat bootRun`).
4. Khi ứng dụng khởi động trên database trống, **Flyway** sẽ tự động thực thi:
    - `V1__init_schema.sql`: Khởi tạo cấu trúc bảng, khóa chính, khóa ngoại, ràng buộc unique và các index.
    - `V2__seed_sample_data.sql`: Nạp dữ liệu mẫu giả lập phục vụ kiểm thử.
5. Kiểm tra bảng `flyway_schema_history` để xác nhận các script đều chạy thành công (`success = 1`).

---

## 3. Tài khoản mẫu & Dữ liệu seed (Môi trường Dev/Test)

> **Lưu ý:** Toàn bộ dữ liệu và tài khoản mẫu dưới đây chỉ là dữ liệu giả lập phục vụ phát triển/kiểm thử (không sử dụng thông tin cá nhân của người thật).

### Quản trị viên (Admin)
- **Abby** — `abby.admin@talentlink.local` / Mật khẩu mẫu: `Admin@123`

### Đối tác doanh nghiệp (3 tài khoản kèm công ty)
- **Bobby** — `bobby.partner@talentlink.local` / Mật khẩu mẫu: `Partner@123` (kèm công ty mẫu)
- **Cody** — `cody.partner@talentlink.local` / Mật khẩu mẫu: `Partner@123` (kèm công ty mẫu)
- **Daisy** — `daisy.partner@talentlink.local` / Mật khẩu mẫu: `Partner@123` (kèm công ty mẫu)

### Ứng viên / Sinh viên (15 hồ sơ mẫu từ Ethan đến Sam)
- **Mật khẩu mẫu chung:** `Student@123`
- **Danh sách tên giả theo thứ tự alphabet:** Ethan, Fiona, George, Hannah, Isaac, Julia, Kevin, Lily, Mason, Nora, Owen, Piper, Quinn, Ruby, Sam (ví dụ tài khoản đầu: `ethan.student@talentlink.local`).

### Dữ liệu danh mục & nghiệp vụ mẫu đi kèm
- **30 kỹ năng** cùng các dữ liệu mẫu liên quan được nạp tự động qua `V2__seed_sample_data.sql`.