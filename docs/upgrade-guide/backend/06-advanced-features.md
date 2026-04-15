# Giai đoạn B6: Tích Hợp Các Tính Năng Nâng Cao (Enterprise Level)

Đây là những chức năng đẳng cấp "cán đích" nhằm giúp đồ án của bạn rũ bỏ cái mác "Dự án sinh viên" và vươn tầm kiến trúc mức Doanh Nghiệp (Enterprise/Senior Level).

## 1. Giá Biến Động (Dynamic Pricing) - Áp dụng Strategy Pattern
Thay vì gán cứng giá 100k/giờ, hệ thống sẽ linh hoạt tính giá phụ thuộc vào Tình trạng sân, Giờ Vàng, hay Level của Member.

**Kiến trúc Strategy Pattern:**
Tạo package mới `com.example.quanly.service.pricing` gồm:
1. `public interface PricingStrategy { double calculatePrice(double basePrice, BookingContext context); }`
2. `public class PeakHourStrategy implements PricingStrategy { ... }` (Giờ cao điểm cộng thêm 30%)
3. `public class VipDiscountStrategy implements PricingStrategy { ... }` (Hội viên Vàng giảm 20%)

Trong `BookingService`, khi tạo đơn giá: Hệ thống nhận diện Khung thời gian và User, gán `PricingStrategy` tương ứng vào hàm `calculatePrice` tại thời điểm code chạy (Runtime). Không còn vô vàn vòng lặp `if-else` lộn xộn nữa.

## 2. Quản Lý Đặt Sân Cố Định (Subscription / Recurring Slot)
Chuyển đổi hệ thống Booking hiện tại "Đặt 1 lần" (One-time) thành "Chu kỳ" (Recurring).

- **Cấu trúc DB:** Bảng `Bookings` thêm Enum `BOOKING_TYPE` (`ONE_TIME`, `WEEKLY_RECURRING`). Bổ sung cột `recurring_end_date`.
- **Logic Service:** Người dùng chọn mua Cố định lịch đánh "Thứ 4 hàng tuần lúc 7h tối trong vòng 2 tháng tới". Backend dùng vòng lặp `LocalDate` trong `BookingService` để kiểm tra quét xem trong 8 tuần tới có ngày Thứ 4 nào bị ai đặt trùng không (Collision Check). Nếu hoàn toàn trống -> Tạo Insert Bulk Database cho 8 record cùng lúc.

## 3. Rate Limiting Định Tuyến (Chống Spam Bằng Redis)
Vì API là mở, Kẻ gian/Đối thủ có thể dùng lệnh Bot F5 liên tục trang Đặt Sân (`HoldCourt`), khiến toàn bộ Sân của bạn bị khóa tạm (Hold) và mất khách thực sự. Giải pháp: Rate Limiting bằng thư viện **Bucket4j** + **Redis**.

- Cài đặt `spring-boot-starter-data-redis` và `bucket4j-core` trong Maven.
- Thiết lập một biến Bucket đại diện cho Mức cho phép: vd `5 tokens / 1 phút`.
- Cài Filter trước Endpoint POST `/hold`: Lấy User ID làm Khóa Redis. Trước khi chạy hàm Hold, tiêu hao 1 Token `bucket.tryConsume(1)`. Nếu User F5 quá 5 lần, Bucket rỗng -> Trả về lỗi `Http 429 Too Many Requests`. Hoàn toàn ngăn chặn được Hacker dDOS khóa sân ảo.

## 4. Hệ Thống Gợi Ý Thông Minh (Smart Recommendation)
Nếu một người dùng tuần nào cũng đặt đá lúc 19:00 Thứ Tư, khi họ vào Web vào ngày Thứ Bảy, hệ thống sẽ chừa sẵn 1 bảng Alert (Gợi ý).
- Truy vấn DB: Chạy truy vấn đếm (`GROUP BY time_slot`) để ra được Giờ mà User hay đặt nhất từ trước tới nay.
- Kết hợp với truy vấn Sân đang còn Trống ngay hôm nay (Today_available).
- Trả API về Client mảng Data `recommended_slots`. Nếu user thấy thích, bấm 1 nút là Đặt Sân tự động vào ô đó ngay lập tức. Cực kỳ tối ưu User Experience.

## 5. Dự Báo Doanh Thu Tháng Kế Tiếp Bằng ML (Machine Learning Python)
Bạn có thể kết hợp nhẹ mô hình Trí Tuệ Nhân Tạo để show lên màn hình Dashboard Admin. Sử dụng thư viện **Prophet** (Meta).

- **Thiết kế Microservice Nhỏ:** 
  Cạnh folder thư mục đồ án, tạo một thư mục Flask cực mỏng (khoảng 1 file Python `app.py`). Trong file này có thư viện `pandas` và `prophet`. Nó đọc dữ liệu doanh thu của bạn và đưa ra biểu đồ đường thẳng đi lên/đi xuống tiên đoán cho tương lai 30 ngày.
- **Tiếp cận Gọi API nội bộ:**
  Spring Boot khi Admin vào trang `Dashboard`, sẽ tự gọi RestTemplate (hoặc WebClient) sang cái cổng API của con Python ML (Localhost 5000). Nó nhận chuỗi JSON kết quả từ bên python và đổ lên React Biểu đồ `Recharts` cho ông chủ sân cầu lông coi. Chức năng siêu đẳng cấp cho điểm 10 thực tập kĩ sư.
