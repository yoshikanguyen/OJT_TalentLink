# TalentLink — User Stories & Acceptance Criteria

## Tổng quan vai trò
1. **Ứng viên (Candidate)**: Người tìm việc tạo tài khoản, cập nhật hồ sơ cá nhân/kỹ năng/kinh nghiệm và phản hồi yêu cầu phỏng vấn.
2. **Nhà tuyển dụng / HR (Recruiter)**: Người tìm kiếm hồ sơ ứng viên, xem chi tiết ứng viên và gửi/quản lý yêu cầu phỏng vấn.
3. **Quản trị viên (Admin)**: Người quản lý tài khoản người dùng/phân quyền và kiểm duyệt các mục trong hàng chờ duyệt.

---

## Vai trò 1: Ứng viên (Candidate)

### US-01: Đăng ký tài khoản ứng viên
- **User Story**: Là **ứng viên**, tôi muốn **đăng ký tài khoản mới bằng email và mật khẩu**, để **tham gia hệ thống và tạo hồ sơ tìm việc**.
- **Acceptance Criteria**:
    - Người dùng nhập họ tên, email, mật khẩu và xác nhận mật khẩu hợp lệ.
    - Nếu email đã tồn tại trong hệ thống hoặc mật khẩu không khớp/không đủ mạnh, hiển thị thông báo lỗi rõ ràng và giữ nguyên các trường hợp lệ.
    - Khi đăng ký thành công, tài khoản được tạo với vai trò ứng viên và chuyển hướng tới trang đăng nhập hoặc hồ sơ cá nhân.

### US-02: Đăng nhập hệ thống
- **User Story**: Là **ứng viên**, tôi muốn **đăng nhập bằng email và mật khẩu**, để **truy cập khu vực cá nhân và theo dõi lịch phỏng vấn**.
- **Acceptance Criteria**:
    - Nhập đúng email và mật khẩu hợp lệ → đăng nhập thành công và chuyển tới trang hồ sơ hoặc trang chủ.
    - Nhập sai thông tin → hiển thị thông báo lỗi chung và không cho phép truy cập các trang yêu cầu xác thực.

### US-03: Quản lý "Hồ sơ của tôi"
- **User Story**: Là **ứng viên**, tôi muốn **xem và cập nhật thông tin trên màn hình "Hồ sơ của tôi"** (thông tin liên hệ, giới thiệu, kỹ năng, kinh nghiệm, học vấn, CV), để **nhà tuyển dụng đánh giá chính xác năng lực của tôi**.
- **Acceptance Criteria**:
    - Hiển thị đầy đủ thông tin hiện tại của hồ sơ ứng viên.
    - Cho phép chỉnh sửa các trường thông tin và thêm/xóa kỹ năng hoặc kinh nghiệm.
    - Khi lưu thành công, dữ liệu được cập nhật và vẫn hiển thị đúng sau khi tải lại trang; nếu thiếu thông tin bắt buộc thì báo lỗi validation.

### US-04: Bật/tắt trạng thái hiển thị hồ sơ tìm việc
- **User Story**: Là **ứng viên**, tôi muốn **bật hoặc tắt trạng thái sẵn sàng tìm việc**, để **kiểm soát việc hồ sơ của tôi có xuất hiện khi nhà tuyển dụng tìm kiếm hay không**[cite: 11].
- **Acceptance Criteria**:
    - Ứng viên có thể chuyển trạng thái (ví dụ: Đang tìm việc / Tạm ẩn hồ sơ) ngay trên hồ sơ.
    - Chỉ các ứng viên bật trạng thái sẵn sàng tìm việc (và đạt điều kiện hiển thị) mới xuất hiện trong kết quả tìm kiếm của nhà tuyển dụng.

### US-05: Xem và phản hồi "Yêu cầu phỏng vấn"
- **User Story**: Là **ứng viên**, tôi muốn **xem danh sách "Yêu cầu phỏng vấn" và phản hồi chấp nhận hoặc từ chối/đổi lịch**, để **chủ động sắp xếp thời gian tham gia phỏng vấn**[cite: 11].
- **Acceptance Criteria**:
    - Hiển thị danh sách yêu cầu phỏng vấn gồm tên nhà tuyển dụng/công ty, thời gian dự kiến, hình thức/địa điểm, ghi chú và trạng thái hiện tại.
    - Khi ứng viên bấm chấp nhận hoặc từ chối (kèm lý do/ghi chú nếu cần), trạng thái yêu cầu cập nhật ngay trên hệ thống.

---

## Vai trò 2: Nhà tuyển dụng / HR (Recruiter)

### US-06: Đăng nhập tài khoản nhà tuyển dụng
- **User Story**: Là **nhà tuyển dụng**, tôi muốn **đăng nhập vào hệ thống**, để **sử dụng các chức năng tìm ứng viên và gửi yêu cầu phỏng vấn**[cite: 11].
- **Acceptance Criteria**:
    - Tài khoản có quyền nhà tuyển dụng đăng nhập thành công và truy cập được màn hình tìm ứng viên, chi tiết ứng viên và yêu cầu phỏng vấn.
    - Tài khoản không có quyền bị chặn truy cập các chức năng dành riêng cho nhà tuyển dụng hoặc quản trị viên.

### US-07: Tìm kiếm và lọc ứng viên
- **User Story**: Là **nhà tuyển dụng**, tôi muốn **tìm kiếm và lọc ứng viên theo từ khóa, kỹ năng, kinh nghiệm hoặc vị trí trên màn hình "Tìm ứng viên"**, để **nhanh chóng tìm được ứng viên phù hợp với nhu cầu tuyển dụng**[cite: 11].
- **Acceptance Criteria**:
    - Cho phép nhập từ khóa và chọn các tiêu chí lọc rồi áp dụng tìm kiếm.
    - Danh sách kết quả chỉ hiển thị ứng viên khớp với toàn bộ điều kiện lọc đang chọn.
    - Nếu không có kết quả phù hợp, hiển thị trạng thái trống kèm gợi ý xóa bớt bộ lọc.

### US-08: Xem "Chi tiết ứng viên"
- **User Story**: Là **nhà tuyển dụng**, tôi muốn **xem đầy đủ thông tin trên màn hình "Chi tiết ứng viên"**, để **đánh giá kỹ năng, kinh nghiệm và mức độ phù hợp trước khi mời phỏng vấn**[cite: 11].
- **Acceptance Criteria**:
    - Hiển thị rõ thông tin tổng quan, kỹ năng, kinh nghiệm làm việc, học vấn, trạng thái sẵn sàng và link/tệp CV (nếu có).
    - Có nút thao tác rõ ràng để mở form gửi yêu cầu phỏng vấn cho ứng viên đang xem hoặc quay lại danh sách tìm kiếm.

### US-09: Tạo và gửi "Yêu cầu phỏng vấn"
- **User Story**: Là **nhà tuyển dụng**, tôi muốn **tạo và gửi "Yêu cầu phỏng vấn" cho ứng viên**, để **đặt lịch phỏng vấn và trao đổi thông tin buổi hẹn**[cite: 11].
- **Acceptance Criteria**:
    - Cho phép chọn ứng viên, ngày giờ phỏng vấn, hình thức/địa điểm hoặc liên kết họp và ghi chú.
    - Kiểm tra dữ liệu bắt buộc (ví dụ: không để trống ngày giờ hoặc chọn ngày giờ quá khứ không hợp lệ).
    - Sau khi gửi thành công, yêu cầu xuất hiện trong danh sách với trạng thái chờ phản hồi.

### US-10: Theo dõi danh sách và trạng thái "Yêu cầu phỏng vấn"
- **User Story**: Là **nhà tuyển dụng**, tôi muốn **xem danh sách các "Yêu cầu phỏng vấn" đã gửi và theo dõi trạng thái phản hồi của ứng viên**, để **quản lý tiến độ lịch hẹn hiệu quả**[cite: 11].
- **Acceptance Criteria**:
    - Hiển thị danh sách yêu cầu phỏng vấn kèm tên ứng viên, thời gian, trạng thái (Chờ phản hồi / Đã chấp nhận / Đã từ chối / Đã hủy).
    - Cho phép lọc theo trạng thái và hủy yêu cầu khi cần thiết.

---

## Vai trò 3: Quản trị viên (Admin)

### US-11: Đăng nhập tài khoản quản trị viên
- **User Story**: Là **quản trị viên**, tôi muốn **đăng nhập bằng tài khoản quản trị**, để **quản lý tài khoản người dùng và kiểm duyệt hệ thống**[cite: 11].
- **Acceptance Criteria**:
    - Đăng nhập thành công bằng tài khoản quản trị hợp lệ và mở được màn hình quản trị tài khoản cùng hàng chờ duyệt.
    - Từ chối truy cập nếu người dùng thường cố tình mở trực tiếp URL quản trị.

### US-12: Xem và tìm kiếm/lọc trên "Quản trị tài khoản"
- **User Story**: Là **quản trị viên**, tôi muốn **xem danh sách tài khoản, tìm kiếm theo tên/email và lọc theo vai trò hoặc trạng thái trên màn hình "Quản trị tài khoản"**, để **dễ dàng rà soát người dùng trên hệ thống**[cite: 11].
- **Acceptance Criteria**:
    - Hiển thị bảng danh sách tài khoản với các cột cơ bản: tên, email, vai trò, trạng thái hoạt động, ngày tạo và thao tác.
    - Tìm kiếm và bộ lọc vai trò/trạng thái hoạt động chính xác và cập nhật ngay danh sách.

### US-13: Thay đổi vai trò hoặc khóa/mở khóa tài khoản
- **User Story**: Là **quản trị viên**, tôi muốn **cập nhật vai trò hoặc khóa/mở khóa tài khoản người dùng**, để **đảm bảo phân quyền đúng và ngăn chặn tài khoản vi phạm hoặc không còn hợp lệ**[cite: 11].
- **Acceptance Criteria**:
    - Cho phép đổi vai trò (Ứng viên / Nhà tuyển dụng / Quản trị viên) hoặc chuyển trạng thái Hoạt động / Bị khóa.
    - Thay đổi được lưu vào cơ sở dữ liệu; tài khoản bị khóa không thể đăng nhập sử dụng hệ thống.

### US-14: Xem danh sách các mục cần xử lý trong "Hàng chờ duyệt"
- **User Story**: Là **quản trị viên**, tôi muốn **xem các mục đang chờ xử lý trong "Hàng chờ duyệt"**, để **kiểm tra nội dung trước khi cho phép áp dụng hoặc hiển thị rộng rãi**[cite: 11].
- **Acceptance Criteria**:
    - Liệt kê các bản ghi đang ở trạng thái chờ duyệt kèm loại mục, người gửi, thời gian gửi và tóm tắt nội dung.
    - Hỗ trợ xem chi tiết từng mục trước khi đưa ra quyết định.

### US-15: Duyệt hoặc từ chối mục trong "Hàng chờ duyệt"
- **User Story**: Là **quản trị viên**, tôi muốn **duyệt hoặc từ chối (kèm lý do) các mục trong "Hàng chờ duyệt"**, để **kiểm soát chất lượng dữ liệu và quy trình trên nền tảng**[cite: 11].
- **Acceptance Criteria**:
    - Khi bấm **Duyệt**: mục chuyển sang trạng thái đã duyệt và áp dụng trạng thái tương ứng trên hệ thống.
    - Khi bấm **Từ chối**: yêu cầu nhập lý do từ chối (nếu bắt buộc), lưu lý do và cập nhật mục sang trạng thái từ chối.