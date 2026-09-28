# HNHSTORE

Một ứng dụng Spring Boot 4.1.1 (Java 26) cho hai bài đăng nhập. Bản `main` đăng nhập bằng **username hoặc email**; tag `bai-1-hoan-thanh` lưu bản chỉ đăng nhập bằng email.

Bài 3 đang được phát triển trên cùng project: đăng ký/OTP, quản lý user và product. Tag `bai-2-hoan-thanh` lưu bản login trước khi thêm bài 3. Cấu hình SMTP và Cloudinary sẽ dùng biến môi trường trong `.env.example`.

## 1. Clone và chạy test bằng H2

Cài JDK 26 và Maven 3.9+, rồi chạy:

```powershell
git clone https://github.com/NgocYuh/LT_WED_BT_28_9.git
cd LT_WED_BT_28_9
java -version
mvn test
```

`mvn test` dùng H2 trong bộ nhớ, **không cần SQL Server**. Nếu Maven không ghi được vào cache mặc định, dùng `mvn '-Dmaven.repo.local=.m2/repository' test`.

## 2. Chạy ứng dụng với SQL Server

Cần SQL Server `(local)` bật TCP cổng 1433 và tài khoản Windows có quyền tạo database. Chạy các lệnh sau **một lần trên database mới** (hoặc chạy hai file SQL tương ứng trong SSMS):

```powershell
sqlcmd -S '(local)' -E -C -b -Q "IF DB_ID(N'hnhstore_login') IS NULL CREATE DATABASE hnhstore_login"
sqlcmd -S '(local)' -d hnhstore_login -E -C -b -i 'src/main/resources/db/01-assignment-one.sql'
sqlcmd -S '(local)' -d hnhstore_login -E -C -b -i 'src/main/resources/db/02-assignment-two.sql'
```

Tải [Microsoft JDBC Driver 13.6.0 (ZIP)](https://learn.microsoft.com/en-us/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server). Giải nén và chép `sqljdbc_13.6/enu/auth/x64/mssql-jdbc_auth-13.6.0.x64.dll` vào `.local/jdbc-auth/` trong repo. Thư mục này không được đưa lên Git.

Trong PowerShell tại gốc repo, đặt mật khẩu **đăng nhập website** do bạn tự chọn, rồi chạy:

```powershell
$env:Path = "$(Resolve-Path '.local\jdbc-auth');$env:Path"
$env:SEED_USER_EMAIL = 'user@example.test'
$env:SEED_USER_PASSWORD = 'THAY_BANG_MAT_KHAU_USER'
$env:SEED_ADMIN_EMAIL = 'admin@example.test'
$env:SEED_ADMIN_PASSWORD = 'THAY_BANG_MAT_KHAU_ADMIN'
mvn spring-boot:run
```

Ứng dụng dùng **Windows Authentication**, không cần SQL username/password. Nếu SQL Server ở host hoặc cổng khác, đặt `DB_URL` theo mẫu trong `.env.example`. Account demo chỉ được tạo lần đầu nếu đã đặt email và mật khẩu; đổi biến môi trường sau đó không đổi mật khẩu account đã lưu.

Mở `http://localhost:8080/login`. Đăng nhập bằng `user01` **hoặc** `user@example.test` với mật khẩu USER; dùng `huyadmin` **hoặc** `admin@example.test` với mật khẩu ADMIN. Trang `http://localhost:8080/admin` chỉ dành cho ADMIN. Nhấn `Ctrl+C` để dừng ứng dụng.

## Xem bản bài 1

`git checkout bai-1-hoan-thanh` để xem bản email login; `git checkout main` để quay lại. Nếu **chạy** tag cũ, dùng database thử riêng chỉ có script `01-assignment-one.sql` và DLL JDBC **13.4.0 x64** tương ứng với driver của tag đó.
