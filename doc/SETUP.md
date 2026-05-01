# Hướng dẫn cài đặt dự án Badminton Booking System

Tài liệu này hướng dẫn cách cài đặt và chạy dự án Badminton Booking System (Hệ thống đặt sân cầu lông) trên máy tính cá nhân.

## 1. Yêu cầu hệ thống
Trước khi bắt đầu, hãy đảm bảo máy tính của bạn đã cài đặt các công cụ sau:
- **Java JDK 17** (hoặc mới hơn)
- **Node.js** (Phiên bản LTS) & **npm**
- **MySQL 8.0**
- **Redis Server**
- **Maven** (Tùy chọn, có thể dùng `./mvnw` đi kèm trong thư mục backend)

---

## 2. Cấu trúc dự án
- `refactor_do_an/`: Chứa mã nguồn **Backend** (Spring Boot).
- `fontend_do_an/`: Chứa mã nguồn **Frontend** (Angular).

---

## 3. Cài đặt Cơ sở dữ liệu (MySQL)
Dự án sử dụng MySQL để lưu trữ dữ liệu.
1. Khởi chạy MySQL Server.
2. Tạo một database mới có tên là `sancaulong1`.
3. Kiểm tra cấu hình kết nối trong file `refactor_do_an/src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3307/sancaulong1?createDatabaseIfNotExist=true
   spring.datasource.username=root
   spring.datasource.password=root
   ```
   *Lưu ý: Thay đổi cổng (mặc định thường là 3306, trong file đang để 3307), username và password cho phù hợp với máy của bạn.*

Dự án sử dụng **Flyway**, vì vậy cấu trúc bảng và dữ liệu mẫu sẽ được tự động tạo khi bạn chạy Backend lần đầu tiên.

---

## 4. Cài đặt và chạy Backend (Spring Boot)
1. Mở terminal và di chuyển vào thư mục `refactor_do_an`.
2. Tải các dependencies và build project:
   ```bash
   mvn clean install
   ```
3. Chạy ứng dụng:
   ```bash
   mvn spring-boot:run
   ```
   Hoặc chạy file `QuanlyApplication.java` từ IDE (IntelliJ IDEA, Eclipse, VS Code).
4. Backend sẽ chạy tại: `http://localhost:8080`

---

## 5. Cài đặt và chạy Frontend (Angular)
1. Mở một terminal mới và di chuyển vào thư mục `fontend_do_an`.
2. Cài đặt các thư viện cần thiết:
   ```bash
   npm install
   ```
3. Khởi chạy ứng dụng:
   ```bash
   npm start
   ```
4. Frontend sẽ chạy tại: `http://localhost:4200`

---

## 6. Tài khoản dùng thử (Dữ liệu mẫu)
Sau khi Flyway khởi tạo dữ liệu, bạn có thể đăng nhập bằng các tài khoản sau:
- **Admin:** `admin@gmail.com` / `123456`
- **Người dùng:** `alice@gmail.com` / `123456` hoặc `bob@gmail.com` / `123456`

---

## 7. Các lưu ý quan trọng
- **Redis:** Đảm bảo Redis server đang chạy (mặc định port 6379) để hệ thống có thể quản lý việc giữ chỗ sân (Hold slot). Nếu không có Redis, chức năng đặt sân sẽ báo lỗi.
- **CORS:** Nếu bạn đổi port chạy frontend, hãy cập nhật `cors.allowed-origins` trong `application.properties` của backend.
- **Thanh toán:** Hiện tại dự án đang bật chế độ `payment.mock.enabled=true`, cho phép giả lập thanh toán VNPay để test local dễ dàng mà không cần cấu hình API Key thật.

---
*Chúc bạn cài đặt thành công!*
