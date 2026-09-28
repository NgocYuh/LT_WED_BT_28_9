# HNHSTORE

Ứng dụng Spring Boot 4.1.1 / Java 26 cho hai bài đăng nhập, dùng SQL Server. Xem `.env.example` để biết biến môi trường cần thiết. Tạo database `hnhstore_login` trong SQL Server local trước khi chạy. Không đưa mật khẩu vào Git.

Ứng dụng dùng Spring Security 7 do Spring Boot quản lý, Thymeleaf và MapStruct 1.6.3. Chỉ giữ các dependency phục vụ login; không dùng SMTP, OTP, Cloudinary, H2 hoặc CRUD của demo PDF.
