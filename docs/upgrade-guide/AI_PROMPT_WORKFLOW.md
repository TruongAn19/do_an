# Kịch Bản Auto-Upgrade Dành Cho Trợ Lý AI (AI Prompts Workflow)

Tài liệu này không dành cho người đọc. Đây là tập hợp các **Câu lệnh mồi (Prompts)** được tiêu chuẩn hóa cao độ để các công cụ AI đọc, lấy dữ liệu và tự động code dựa trên các tài liệu Blueprint (Thiết kế) đã soạn sẵn. Dưới đây là 9 Phase bám sát tuyệt đối vào danh mục tài liệu Upgrade mới nhất.

> **Trợ lý AI tuân thủ Quy định nghiêm ngặt:** Tuyệt đối không xóa logic kinh doanh cốt lõi bên trong các hàm Service. Chỉ thay đổi phần giao tiếp tại lớp Security, Controller, DTO, Config và chuyển đổi UI sang React dựa trên tài liệu.

---

## 💻 SYSTEM_CONTEXT (Cung cấp góc nhìn toàn cảnh cho AI)
**AI Prompt:**
```text
Bạn là một Senior Java Architect & Fullstack Frontend Engineer. Hãy đọc file `UPGRADE_PLAN.md` ở thư mục gốc để nắm rõ lộ trình chia tách Monolithic JSP thành Backend REST API và Frontend React riêng biệt.

Mục tiêu chính hiện tại của bạn là đóng vai trò là một Execution Engine để biến tất cả các kịch bản tôi vạch ra trong thư mục `docs/upgrade-guide/` trở thành mã code thực thi và đưa vào project.
```

---

## =============================
## MẢNG 1: BACKEND (SPRING BOOT)
## =============================

## 🚀 PHASE B1: Chuyển Đổi Thành REST APIs Chuyên Sâu
**AI Prompt:**
```text
Thực thi Phase B1:
1. Hãy MỞ và ĐỌC KỸ tài liệu `docs/upgrade-guide/backend/01-rest-api.md` VÀ tài liệu `docs/upgrade-guide/backend/01b-api-service-mapping.md`.
2. Theo tài liệu, áp dụng chuẩn thiết kế `ApiResponse<T>` vào hệ thống. Xây dựng `GlobalExceptionHandler` theo chuẩn RestControllerAdvice.
3. Không bỏ sót bất kỳ một Service và Controller nào theo bảng kiểm tra (Checklist) ở `01b-api-service-mapping.md`. Viết lại toàn bộ theo chuẩn RESTful API. Thay thế tham số truyền vào bằng `@RequestBody`.
4. Gỡ bỏ mọi thư viện thừa liên quan tới jstl, jasper render UI trong `pom.xml`.
```

---

## 🚀 PHASE B2: Quản Lý Phiên Bản Database 
**AI Prompt:**
```text
Thực thi Phase B2: 
1. Hãy MỞ và ĐỌC KỸ tài liệu `docs/upgrade-guide/backend/02-database.md`.
2. Sửa `application.properties`: Tắt chế độ `ddl-auto=update`, cấp phép và khai báo bật Flyway.
3. Bổ sung dependency flyway vào pom.xml.
4. Tạo thư mục `src/main/resources/db/migration` và sinh ra tệp khung rỗng `V1__init_schema.sql` để tôi (user) copy schema db thật vào sau.
```

---

## 🚀 PHASE B3: Stateless JWT Security Của Spring 6
**AI Prompt:**
```text
Thực thi Phase B3:
1. Hãy MỞ và ĐỌC KỸ code cấu hình bảo mật ở `docs/upgrade-guide/backend/03-security.md`.
2. Xóa tất cả plugin `spring-session-jdbc` trên toàn hệ thống. Thêm jjwt bản 0.11.5.
3. Tạo ra cấu trúc JwtTokenProvider và JwtAuthenticationFilter.
4. Config SecurityFilterChain bản cho Spring boot 3.x (ko tự ý dùng code cũ). Disable CSRF, set STATELESS session, và gỡ rào login cho các tuyến đường Auth & Swagger. Tạo `AuthController` để cung cấp Endpoint lấy Token bằng email.
```

---

## 🚀 PHASE B4: Áp Dụng Clean Architecture và MapStruct
**AI Prompt:**
```text
Thực thi Phase B4:
1. Hãy MỞ và ĐỌC KỸ tài liệu `docs/upgrade-guide/backend/04-architecture.md`.
2. Bổ sung `mapstruct` đè lên `lombok` trong Maven compiler configuration của file pom.xml.
3. Thiết kế sẵn một DTO, ví dụ `BookingResponseDTO` và tạo `BookingMapper` (ánh xạ Entity sang DTO). Tái cấu trúc hàm xử lý tạo Request ở Service tránh lộ Entity ra ngoài Endpoint.
```

---

## 🚀 PHASE B5: Xây Dựng Docker 
**AI Prompt:**
```text
Thực thi Phase B5:
1. Hãy MỞ và ĐỌC KỸ tài liệu `docs/upgrade-guide/backend/05-docker.md`.
2. Sinh ra tệp `Dockerfile` thiết kế build đa giai đoạn và `docker-compose.yml` định nghĩa service `backend-app` như tài liệu yêu cầu. Đặt chúng tải thư mục rễ do_an.
```

---

## 🚀 PHASE B6: Tích Hợp Features Doanh Nghiệp (Master Level)
**AI Prompt:**
```text
Thực thi Phase B6:
1. Hãy MỞ và ĐỌC KỸ tài liệu `docs/upgrade-guide/backend/06-advanced-features.md`.
2. Bổ sung ngay cơ chế **Strategy Pattern** cho giá sân vào PricingService.
3. Chỉnh sửa logic Database Bookings để thích hợp với Chu kỳ đặt (`BOOKING_TYPE` Enum) và check collision cho 8 tuần.
4. Cài đặt dependency Bucket4j và Redis; Gắn Interceptor giới hạn F5 tần suất spam API đối với Endpoint Hold() đặt sân. 
5. Tạo nhánh riêng `AI-Forecast` trong codebase chứa 1 file Python Flask mỏng sử dụng `Prophet` để Spring Boot giao tiếp nội bộ phục vụ Analytics Dashboard Admin. Dưới Spring boot viết service RestClient bắn data sang phân tích. 
```

---

## =============================
## MẢNG 2: FRONTEND (ANGULAR)
## =============================

## 🚀 PHASE F1: Khởi Tạo Môi Trường Chuẩn Angular
**AI Prompt:**
```text
Thực thi Phase F1:
1. Hãy MỞ và ĐỌC KỸ tài liệu `docs/upgrade-guide/frontend/01-setup.md`.
2. Chạy bash để sinh source Angular: `npx @angular/cli new quanly-frontend --standalone --routing --style=scss --interactive=false`. (Bỏ qua câu hỏi tự gen css/routing).
3. CD vào folder `quanly-frontend`, tích hợp thư viện UI bằng lệnh: `ng add @ng-bootstrap/ng-bootstrap` để sử dụng Bootstrap chuẩn Angular.
4. Xếp hệ thống thư mục src theo kiến trúc chuẩn được thiết kế sẵn (core, features, layouts, shared) của Angular. Setup `authGuard` như bản thiết kế.
```

---

## 🚀 PHASE F2: Trải Giao Diện (Layout) & Ánh Xạ Client
**AI Prompt:**
```text
Thực thi Phase F2:
1. Hãy MỞ và ĐỌC KỸ tài liệu `docs/upgrade-guide/frontend/02-ui-mapping-client.md`.
2. Xây dựng Layout tổng `client-layout.component.ts` lồng bằng `router-outlet`.
3. Ánh xạ, khởi tạo ra các Standalone Component tương đương với những file .jsp gốc (Ví dụ: `login.component`, `booking-scheduler.component` có lưới đặt sân, trang Thanh Toán `payment-result.component`) trong cấu trúc `features`. Dùng cú pháp @if, @for hiện đại.
```

---

## 🚀 PHASE F3: Xây Dựng Master SPA Cho Admin
**AI Prompt:**
```text
Thực thi Phase F3:
1. Hãy MỞ và ĐỌC KỸ tài liệu `docs/upgrade-guide/frontend/03-ui-mapping-admin.md`.
2. Cài đặt thêm thư viện `ng-zorro-antd`.
3. Tạo file Layout cha `admin-layout` với Sidebar dark theme. Khảo sát các table từ JSP cũ, biến chúng thành `<nz-table>` chạy trên nền Single Page Application.
```

---

## 🚀 PHASE F4: Kết Nối Băng Chuyền Dữ Liệu Tự Động (HttpClient & RxJS)
**AI Prompt:**
```text
Thực thi Phase F4:
1. Hãy MỞ và ĐỌC KỸ 2 tài liệu cực kỳ quan trọng: `docs/upgrade-guide/frontend/04-api-integration.md` và `docs/upgrade-guide/backend/01b-api-service-mapping.md`.
2. Nhiệm vụ của bạn là phải bảo đảm sự TRÙNG KHỚP 100% giữa Frontend và Backend. Khởi tạo `auth.interceptor.ts` cài cắm tự lấy Token từ LocalStorage nhét vào Request Header. Dùng RxJS `map` bóc tách lõi dữ liệu từ lớp `ApiResponse`.
3. Khởi tạo các Injectable Services (VD `booking-api.service.ts`, `auth-api.service.ts`) chứa các lời gọi `HttpClient` đâm tới đúng API Endpoint của Backend trong `01b`. Không được phép tự "chế" ra API nếu như Backend chưa có định nghĩa.
```

---

## =============================
## MẢNG 3: DEVOPS & TRIỂN KHAI
## =============================

## 🚀 PHASE 6: Thiết Lập Github Actions Ngầm
**AI Prompt:**
```text
Thực thi Phase 6:
1. Hãy MỞ và ĐỌC KỸ tài liệu `docs/upgrade-guide/06-ci-cd-pipeline.md`.
2. Tạo cấu trúc thư mục rễ bắt buộc là `.github/workflows/`.
3. Khởi tạo file `ci-cd.yml` đổ mã cấu hình yaml đã thiết kế trong tài liệu vào đó. Bám sát chuẩn action v4. Thiết lập tách thành 2 Jobs riêng biệt để Compile App Spring boot và build độc lập thư mục quanly-frontend của Angular. Mở đường cho DevOps.
```
