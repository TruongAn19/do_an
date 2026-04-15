# Giai đoạn B1b: Bản Đồ Ánh Xạ Chi Tiết API và Các Web Services

Khái niệm "Không bỏ sót" có nghĩa là 16 file Controller và 19 file Service hiện hành đều phải được tái cấu trúc theo chuẩn API. 
Nguyên tắc chung cho toàn bộ các layer (tầng):
- **Tại Controller**: `@Controller` -> `@RestController`. Lấy arguments bằng `@RequestBody` (cho POST/PUT) hoặc `@RequestParam` / `@PathVariable` (cho GET/DELETE). Trả kết quả bằng `ResponseEntity.ok(new ApiResponse(...))`.
- **Tại Service**: Trả về `OutDTO` thay vì `Entity`. Ném Exception thẳng ra ngoài nếu có lỗi (để `GlobalExceptionHandler` bắt), tuyệt đối không return `null` hay thông báo chuỗi rác kiểu `return "Lỗi rỗng"`.

Dưới đây là BẢNG THIẾT KẾ (BLUEPRINT) chi tiết cho 100% các hệ thống bên trong.

## 1. Hệ thống User & Auth
* **Controller**: `UserController` (Admin) + `ForgotPasswordController` (Client) + Mới: `AuthController`
* **Service**: `UserService`, `CustomUserDetailsService`, `AuthenticationFacade`
* **API Endpoints (Mới)**:
  * `POST /api/v1/auth/login`: Lấy Token Authentication.
  * `POST /api/v1/auth/register`: Hàm đăng ký.
  * `POST /api/v1/auth/forgot-password`: Móc vào `ForgotPasswordController` để gọi `EmailService` gửi mã token reset.
  * `GET /api/v1/admin/users`: Lấy danh sách toàn bộ Users (dành cho Table của màn hình Admin gốc).
  * `PUT /api/v1/admin/users/{id}/role`: Phân quyền.
* **Yêu cầu Nâng cấp**: Service xử lý Login thay vì trả session, sẽ đưa việc sinh JWT vào. DTO trả ra tuyêt đối không lộ password hash.

## 2. Hệ thống Booking (Lõi Core Đặt Sân)
* **Controller**: `BookingClientController` (Client) + `BookingController` (Admin)
* **Service**: `BookingService`, `BookingStatsService`, `TemporaryBookingCleaner`
* **API Endpoints (Mới)**:
  * `GET /api/v1/client/bookings/available-times?date=X&court=Y`: API gọi xem giờ trống, render lưới lịch.
  * `POST /api/v1/client/bookings/hold`: Cực kỳ quan trọng. Nhận lệnh tạm giữ sân (tương ứng hàm holdCourt cũ). 
  * `POST /api/v1/client/bookings/place`: Gửi đơn tạo Booking chính thức.
  * `GET /api/v1/admin/bookings`: Load danh sách đặt sân cho Admin kèm phân trang.
  * `PUT /api/v1/admin/bookings/{id}/status`: Đổi trạng thái lịch duyệt sân.
* **Yêu cầu Nâng cấp**: `TemporaryBookingCleaner` là Scheduler dọn rác sân bị Hold không thanh toán (Chạy ngầm @Scheduled - Giữ nguyên logic cũ nhưng phải ghi Log chi tiết). Cả `BookingService` phải dùng MapStruct để mapping toàn bộ `SubCourt` và `RentalTool`.

## 3. Hệ thống Product, Item, Equipment & Rental
* **Controller**: `ProductController`, `RacketController`, `RentalToolController` (Admin) + `ItemController`, `RentalController` (Client)
* **Service**: `ProductService`, `RacketService`, `RentalToolService`, `RacketStockByDateService`
* **API Endpoints (Mới)**:
  * `GET /api/v1/products`: (Cho Client) Có search từ khóa, giá, bộ lọc. API phân trang pageable.
  * `GET /api/v1/rackets/rentals`: Query xem số lượng vợt cho thuê ngày X. API này nối vào thư viện lịch của Frontend.
  * `POST /api/v1/admin/products`, `PUT /api/v1/admin/products`: Lưu dữ liệu dạng JSON. Đối với chức năng tải ảnh lên, dùng `multipart/form-data` và gọi `UploadService`.
  * `POST /api/v1/client/rentals/checkout`: Đẩy xe Giỏ hàng (Cart) thiết bị muốn thuê sang Backend chốt.
* **Yêu cầu Nâng cấp**: Bảng `RacketStockByDate` cần được kiểm tra cẩn thận trong Service để tránh lỗi hết hàng ảo khi Multi-thread. Chốt phương pháp khóa Pessimistic Logic ở `RacketStockByDateService` nếu cần.

## 4. Hệ thống Match / Social (Cộng Đồng Kết Nối)
* **Controller**: `MatchPostController`
* **Service**: `MatchPostService`, `MatchParticipantService`
* **API Endpoints (Mới)**:
  * `GET /api/v1/match-posts`: Feed tin tức tìm cạ.
  * `POST /api/v1/match-posts`: Tạo bài viết mới tìm người.
  * `POST /api/v1/match-posts/{postId}/join`: API để User đệ đơn tham gia vào kèo đấu (`MatchParticipantService`).
* **Yêu cầu Nâng cấp**: Hàm check logic Limit Member tham gia trong Service. Trả về Exception `"Đã đủ người"` chứ không in HTML lỗi.

## 5. Hệ thống Chat Thời Gian Thực
* **Controller**: `ChatSocketController`
* **Service**: `ChatService`
* **API Config**:
  * Các Endpoints API thông qua giao thức WS (Websocket) endpoint `/ws`. Đổi sang cấu trúc thư viện STOMP của React. 
  * `GET /api/v1/chat/history/{chatRoomId}`: REST API Tải lịch sử chat để fill màn hình đầu tiên khi vào.
* **Yêu cầu Nâng cấp**: Bắt buộc JWT phải len lỏi được vào lớp kiểm tra Authenticate của thư viện WebSocket.

## 6. Hệ thống Thông Báo (Notifications), Mail & VNPAY
* **Controller**: `NtfyController`
* **Service**: `NotificationService`, `NtfyService`, `EmailService`, `PaymentService`
* **API Endpoints (Mới)**:
  * `POST /api/v1/payments/create-url`: Khi Client bấm nút [Thanh toán], React gọi API này gửi số tiền. `PaymentService` sẽ lấy TmnCode cấu hình thành URL `sandbox.vnpayment.vn` trả lại bằng chuỗi JSON URL -> Frontend Redirect trình duyệt qua URL kia.
  * `GET /api/v1/payments/vnpay-callback`: Call Back VNPAY trả về, Spring Boot gọi `PaymentService` xác thực Chữ ký, nếu chuẩn -> gọi `BookingService` chốt đơn -> Sinh ra URL trả về trang `/payment/success` trên đường dẫn React.
* **Yêu cầu Nâng cấp**: `NtfyService` bắn thông báo đẩy thẳng vào Điện thoại / Web Browser (Web push - vapid keys). Đảm bảo Service bắt exception không làm chết mạch thanh toán của user.

*TUYỆT ĐỐI KHÔNG BỎ SÓT FILE NÀO:* Bất cứ file *.java nào trong `controller` chưa được nêu ở trên thì đồng nghĩa biến nó thành `@RestController` có Prefix là `/api/v1/tên-module`. Bất cứ `Service.java` nào cũ đều được bọc DTO ra ngoài.
