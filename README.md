# HNHSTORE

Ứng dụng Spring Boot 4.1.1 / Java 26 cho hai bài đăng nhập, dùng SQL Server. Xem `.env.example` để biết biến môi trường cần thiết. Tạo database `hnhstore_login` trong SQL Server local trước khi chạy. Không đưa mật khẩu vào Git.

Ứng dụng dùng Spring Security 7 do Spring Boot quản lý, Thymeleaf và MapStruct 1.6.3. Chỉ giữ các dependency phục vụ login; H2 chỉ dùng trong test. Không dùng SMTP, OTP, Cloudinary hoặc CRUD của demo PDF.

## Bài 1: đăng nhập email

Mốc hoàn thành: tag `bai-1-hoan-thanh`. Bản này dùng Thymeleaf fragment (`th:replace`), không có Layout Dialect. Email được dùng làm tên đăng nhập. Trang chủ dành cho khách và tài khoản đã đăng nhập; `/admin` chỉ dành cho ADMIN. Form đăng nhập và đăng xuất đều dùng CSRF của Spring Security.

### Chạy trên Windows PowerShell

1. Dùng JDK 26.0.2 và Maven 3.9.x. Tạo database `hnhstore_login` trên SQL Server local. Trong SSMS, chọn database này và chạy `src/main/resources/db/01-assignment-one.sql` đúng một lần.
2. Đặt biến môi trường trong phiên PowerShell hiện tại; thay giá trị placeholder bằng thông tin trên máy của bạn:

```powershell
$env:JAVA_HOME = 'D:\NNLT\jdk-26.0.2'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$env:DB_URL = 'jdbc:sqlserver://localhost:1433;databaseName=hnhstore_login;encrypt=true;trustServerCertificate=true'
$env:DB_USERNAME = 'YOUR_SQL_SERVER_USER'
$env:DB_PASSWORD = 'YOUR_SQL_SERVER_PASSWORD'
$env:SEED_USER_EMAIL = 'user@example.test'
$env:SEED_USER_PASSWORD = 'CHOOSE_A_LOCAL_TEST_PASSWORD'
$env:SEED_ADMIN_EMAIL = 'admin@example.test'
$env:SEED_ADMIN_PASSWORD = 'CHOOSE_A_DIFFERENT_LOCAL_TEST_PASSWORD'
mvn spring-boot:run
```

3. Mở `http://localhost:8080/` và `http://localhost:8080/login`. Thử email của USER và ADMIN với mật khẩu đã tự đặt. Mở `http://localhost:8080/admin` để kiểm tra phân quyền. Các tài khoản demo chỉ được tạo nếu đã đặt cả email và mật khẩu tương ứng; chạy lại không tạo bản ghi trùng. Đổi mật khẩu bằng cách sửa dữ liệu trong DB nếu account đã tồn tại.

Kiểm thử tự động: `mvn test`. Test dùng H2 trong bộ nhớ, tách khỏi SQL Server thật; Hibernate tạo và xóa schema test. Chưa kiểm tra kết nối SQL Server của máy người dùng nếu máy chưa có database/cấu hình. Trong runtime, Hibernate dùng `validate`, không tự xóa hoặc sửa schema.

`trustServerCertificate=true` chỉ dành cho SQL Server local với chứng chỉ tự ký. Với server triển khai thật, cấu hình chứng chỉ tin cậy và đổi URL tương ứng. Không lưu mật khẩu DB hay mật khẩu seed vào `.env.example`, README hoặc Git.

Khác biệt có chủ đích với PDF: đổi `layout.htnl` thành `.html`; bỏ đăng ký, OTP, SMTP, Cloudinary, H2 console và CRUD vì không thuộc hai bài login. Tài liệu Spring Security 6 chỉ được dùng cho lý thuyết xác thực, session và phân quyền; API thực thi theo Security 7. `thymeleaf-extras-springsecurity6` chỉ cung cấp thuộc tính `sec:*`, không phải Layout Dialect.
