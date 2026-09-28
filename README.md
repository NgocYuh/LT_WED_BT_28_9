# HNHSTORE

Spring Boot 4.1.1 · Java 26 · Spring Security 7 · SQL Server Windows Authentication. Một ứng dụng cho bài 1–3; các mốc nằm ở tag `bai-1-hoan-thanh`, `bai-2-hoan-thanh`, `bai-3-hoan-thanh`.

## Clone và chạy test

```powershell
git clone https://github.com/NgocYuh/LT_WED_BT_28_9.git
cd LT_WED_BT_28_9
mvn test
```

Test dùng H2 trong bộ nhớ, không cần SQL Server, SMTP hay Cloudinary. Nếu Maven không ghi được cache mặc định: `mvn '-Dmaven.repo.local=.m2/repository' test`.

## Chạy ứng dụng trên SQL Server `(local)`

1. Tạo database `hnhstore_login`. Với database **mới**, chạy lần lượt `01`, `02`, `03` trong SSMS. Với database **đã làm bài 2**, sao lưu trước rồi chỉ chạy `03-assignment-three.sql`. Script `03` có thể chạy lại và giữ nguyên user/role/password/ảnh cũ.
2. Tải [Microsoft JDBC Driver 13.6.0](https://learn.microsoft.com/en-us/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server), chép `mssql-jdbc_auth-13.6.0.x64.dll` vào `.local/jdbc-auth/` (thư mục này không lên Git).
3. Trong PowerShell tại gốc repo, đặt biến rồi chạy:

```powershell
$env:Path = "$(Resolve-Path '.local\jdbc-auth');$env:Path"
$env:SEED_USER_EMAIL = 'user@example.test'
$env:SEED_USER_PASSWORD = 'MAT_KHAU_TU_CHON'
$env:SEED_ADMIN_EMAIL = 'admin@example.test'
$env:SEED_ADMIN_PASSWORD = 'MAT_KHAU_ADMIN_TU_CHON'
mvn spring-boot:run
```

Mặc định app dùng Windows Authentication đến `localhost:1433`, database `hnhstore_login`; không cần SQL username/password. Nếu instance khác, đặt `DB_URL` theo `.env.example`. Seed chỉ tạo tài khoản chưa tồn tại; đổi biến môi trường sau này không đổi mật khẩu đã lưu.

## Thử các chức năng

- `http://localhost:8080/login`: USER `user01` hoặc email seed; ADMIN `huyadmin` hoặc email seed. Mật khẩu là giá trị bạn tự đặt ở trên.
- `/register` → nhận OTP qua email → `/verify-otp` → đăng nhập; `/forgot-password` → `/reset-password` để đổi mật khẩu.
- `/products`: thêm, tìm, sửa, xóa sản phẩm. USER chỉ sửa/xóa sản phẩm của mình; ADMIN quản lý được tất cả.
- `/admin/users`: ADMIN thêm, tìm, phân trang, sửa, xóa user; dashboard ở `/admin`.

Để thử OTP thật, đặt `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`. Để thử upload ảnh thật, đặt `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`. Xem tên biến trong `.env.example`; giữ giá trị thật ở môi trường máy, không commit `.env`. Nếu chưa có cấu hình dịch vụ, `mvn test` vẫn kiểm thử luồng bằng mock.

Ghi chú tích hợp: bài 3 tiếp tục đăng nhập bằng username **hoặc** email từ bài 2, dùng chung `User`/`Role` và một `SecurityFilterChain`; schema nâng cấp bằng `03` thay cho tự tạo bảng bằng Hibernate.
