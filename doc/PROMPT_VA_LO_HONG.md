# Prompt: Vá lỗ hổng luồng đặt sân + thêm luồng huỷ đầy đủ cho thuê vợt

> Copy toàn bộ nội dung dưới đây và đưa cho coding assistant (Claude Code / Cursor / ...).
> Bối cảnh dự án và yêu cầu đã được mô tả cụ thể theo đúng tên class/method hiện có.

---

## BỐI CẢNH DỰ ÁN

Backend Spring Boot 3.4.3, Java 17, MySQL 8, Redis, Flyway, Spring Security + JWT.
Package gốc: `com.example.quanly`. Migration mới nhất là `V13`, nên file Flyway tiếp theo phải bắt đầu từ `V14`.

Quy ước đang dùng trong codebase, hãy giữ nguyên:
- DTO trả về bọc trong `ApiResponse<T>` (có `status`, `message`, `data`, `errorCode`, `path`, `timestamp`).
- Exception nghiệp vụ ném `ResourceNotFoundException`, `ForbiddenOperationException`, `BusinessConflictException`, `IllegalArgumentException`/`IllegalStateException`, đã có `GlobalExceptionHandler` xử lý.
- Service dùng `@RequiredArgsConstructor` + `@FieldDefaults(makeFinal = true, level = PRIVATE)`.
- Lấy user hiện tại qua `SecurityUtils.getCurrentUser()`.
- Comment giải thích bằng tiếng Việt.

YÊU CẦU CHUNG:
- KHÔNG đổi format response cũ, KHÔNG đổi tên endpoint cũ.
- Mỗi thay đổi schema phải là một Flyway migration mới (V14, V15, ...) — KHÔNG sửa migration đã tồn tại.
- Viết unit/integration test cho mọi thay đổi logic (dùng profile `test` + H2 như `SecurityTests` hiện có).
- Sau khi xong, in ra danh sách file đã tạo/sửa và tóm tắt từng thay đổi.

---

## PHẦN A — VÁ LỖ HỔNG LUỒNG ĐẶT SÂN

### A1. Chặn double-booking ở bước confirm (ưu tiên cao nhất)

**Vấn đề:** Unique constraint hiện chỉ có trên bảng `temporary_booking` (migration V12). Bảng `booking_detail` KHÔNG có ràng buộc chống trùng slot. Collision check cuối trong `BookingService.confirmPendingBooking(...)` chỉ là `SELECT` thường (`BookingDetailRepository.findActiveBySubCourtAndAvailableTimeAndDate`), không lock. Hai callback VNPay chạy đồng thời (vd hold A hết hạn + B chiếm slot, hoặc callback trễ) đều thấy slot trống và cùng insert → trùng lịch. Sau đó query trả `Optional` sẽ ném `NonUniqueResultException`.

**Yêu cầu sửa:**
1. Tạo migration `V14__add_unique_active_slot_to_booking_detail.sql`:
   - Trước khi thêm constraint, dọn dữ liệu trùng đang tồn tại (giữ bản ghi `id` nhỏ nhất cho mỗi `(sub_court_id, available_time_id, date)` thuộc booking active).
   - Thêm ràng buộc chống trùng slot cho booking ĐANG ACTIVE (chưa huỷ). Lưu ý MySQL không hỗ trợ partial/filtered unique index như Postgres. Hãy chọn MỘT trong hai cách và giải thích lựa chọn:
     - (a) Thêm cột generated `active_slot_key` = `NULL` khi booking đã huỷ, ngược lại = concat của `(sub_court_id, available_time_id, date)`, rồi UNIQUE trên cột đó (NULL được phép trùng trong MySQL → booking huỷ không bị chặn).
     - (b) Hoặc cách tương đương đảm bảo: hai booking ACTIVE không thể cùng `(sub_court, time, date)`, nhưng booking đã huỷ vẫn cho phép đặt lại slot.
2. Trong `confirmPendingBooking`, bọc phần insert để khi dính `DataIntegrityViolationException` (vi phạm unique vừa thêm) thì ném `IllegalStateException` với thông điệp "Sân đã bị đặt bởi người khác trong lúc thanh toán (ngày ...)." — y hệt nhánh collision check hiện tại, để `PaymentController.handlePendingBookingCallback` tiếp tục trả 409 như cũ.
3. Giữ collision check `SELECT` hiện có như lớp phòng vệ sớm, nhưng ràng buộc DB mới là nguồn đảm bảo cuối cùng.

### A2. Re-check + lock tồn kho vợt thuê kèm lúc confirm

**Vấn đề:** `preparePendingBooking` có check `racket.getBookingStockQuantity() >= quantity`, nhưng `confirmPendingBooking` trừ thẳng `racket.setBookingStockQuantity(qty - rs.getQuantity())` mà không kiểm tra lại. Giữa prepare và confirm (tới 18 phút) booking khác có thể đã trừ stock → giá trị âm / overbook.

**Yêu cầu sửa:**
1. Thêm vào `RacketRepository` một query lấy `Racket` theo id có `@Lock(LockModeType.PESSIMISTIC_WRITE)` (vd `findByIdForUpdate`).
2. Trong `confirmPendingBooking`, với mỗi `RentalSlot`: load lại racket bằng query có lock, kiểm tra `bookingStockQuantity >= quantity`; nếu không đủ ném `IllegalStateException("Vợt \"...\" đã hết hàng trong lúc thanh toán.")` (→ controller trả 409, đồng thời rollback toàn bộ transaction confirm, KHÔNG được tạo booking nửa vời).
3. Đảm bảo việc trừ stock và insert booking nằm trong cùng một transaction `confirmPendingBooking` (đã có `@Transactional`).

### A3. Làm callback VNPay idempotent dưới điều kiện đồng thời

**Vấn đề:** `PendingBookingCache.get(pendingId)` rồi `remove(pendingId)` không atomic. VNPay có thể gọi callback nhiều lần (IPN + redirect). Hai callback cùng `get` trước khi bên nào `remove` → một lần trả tiền tạo hai booking.

**Yêu cầu sửa:**
1. Thêm method `getAndRemove(long pendingId)` vào `PendingBookingCache` dùng thao tác atomic của Redis (vd `GETDEL`, hoặc Lua script, hoặc `opsForValue().getAndDelete(...)`). Chỉ callback "giành" được snapshot mới xử lý tiếp; callback còn lại nhận empty và trả về thông điệp "đơn đã được xử lý".
2. Trong `handlePendingBookingCallback`, thay luồng `get(...)` + `remove(...)` bằng `getAndRemove(...)`. Lưu ý: với nhánh thanh toán THẤT BẠI vẫn cần giải phóng hold (`bookingService.cancelPendingBooking(data)`); với nhánh THÀNH CÔNG nếu `confirmPendingBooking` ném lỗi (vd A1/A2) thì KHÔNG được mất tiền âm thầm — log rõ ràng và cân nhắc cơ chế đánh dấu cần hoàn tiền thủ công (tái dùng pattern notification admin như `BookingService.cancelByUser`).

### A4. Đối chiếu số tiền VNPay trả về

**Vấn đề:** Callback chỉ xét `vnp_ResponseCode == "00"`, không so `vnp_Amount` với số tiền mong đợi.

**Yêu cầu sửa:**
1. Lưu `depositPrice` mong đợi trong snapshot (đã có trong `PendingBookingData`/`PendingBookingSnapshot`).
2. Trong callback, trước khi confirm, đọc `vnp_Amount` và so với `expectedDeposit * 100` (VNPay nhân 100). Nếu lệch → coi như thất bại: log cảnh báo, giải phóng hold, trả 400 "Số tiền thanh toán không khớp". Áp dụng tương tự cho nhánh `RENTAL_TOOL` (so với `rentalTool.getPrice() * 100`).

### A5. Giới hạn số hold active mỗi user

**Vấn đề:** Endpoint `/api/v1/client/bookings/hold` không giới hạn số slot một user giữ đồng thời → có thể "ôm chỗ" cản người khác.

**Yêu cầu sửa:**
1. Thêm cấu hình `booking.hold.max-active-per-user` (mặc định 5) vào `application.properties`.
2. Trong `holdCourt`, trước khi tạo hold mới, đếm số hold chưa hết hạn của user (thêm query `countActiveHoldsByUser`); nếu vượt ngưỡng → trả 429/409 với thông điệp rõ ràng. Không tính lại slot mà user đang gia hạn chính nó.

### A6. Tập trung hằng số "3 phút" giữ chỗ

**Vấn đề:** Hằng số hold 3 phút bị lặp ở `TemporaryBooking.isExpired()`, `TemporaryBookingCleaner.HOLD_EXPIRY_MINUTES`, `BookingService.HOLD_EXPIRY_GRACE` và query `deleteExpiredHolds`. Sửa một chỗ quên chỗ khác là vỡ logic.

**Yêu cầu sửa:**
1. Đưa về một nguồn duy nhất (vd hằng số `HOLD_DURATION` trong một lớp config/constants, hoặc property `booking.hold.duration-minutes`).
2. `isExpired()` nhận giá trị này thay vì hardcode (truyền vào hoặc đọc từ một holder tĩnh được set lúc khởi động). Cleaner, grace và query dùng chung nguồn đó.
3. Thêm một test khẳng định: hold sống đúng `PAYMENT_HOLD_WINDOW` sau khi `preparePendingBooking` dời `holdStartTime`, và bị `isExpired()` đúng vào ngưỡng.

---

## PHẦN B — THÊM LUỒNG HUỶ ĐẦY ĐỦ CHO THUÊ VỢT

Hiện trạng: `RentalController` chỉ có `createRental` và `payForRental`, KHÔNG có huỷ. `RentalToolService.changeStatus` khi set `CANCELLED` chỉ `setStatus` mà KHÔNG hoàn tồn kho `RacketStockByDate` (rò rỉ stock). Không có cơ chế dọn đơn `PENDING` treo. Trong khi đó luồng booking đã có `BookingService.cancelByUser` rất đầy đủ — hãy dùng nó làm khuôn mẫu.

Lưu ý phạm vi: chỉ xử lý rental **standalone** loại `DAILY`/off-site (dùng `RacketStockByDate`). Vợt `ON_SITE` thuê kèm booking đã được cascade đúng trong `BookingService.cancelByUser`/`updateBooking` — KHÔNG đụng vào.

### B1. Tách logic huỷ + hoàn tồn kho trong RentalToolService

1. Thêm method `@Transactional void cancelRental(Long rentalToolId)` (hoặc nhận thêm `userId`, `reason`):
   - Load rental, nếu không thấy ném `ResourceNotFoundException`.
   - Chỉ cho huỷ khi status ∈ {`PENDING`, `IN_USE`}. Nếu `COMPLETED`/`CANCELLED` → ném `IllegalStateException`/`BusinessConflictException` với thông điệp phù hợp (idempotent: nếu đã `CANCELLED` thì trả về luôn, không lỗi).
   - Nếu status là `IN_USE` (đã trừ stock qua `handleDailyRental`): hoàn tồn kho `RacketStockByDate` cho từng ngày trong `[rentalDate, rentalDate + quantityDay)` — đảo ngược đúng phép trừ của `handleDailyRental` (`availableStock += quantity`, `reservedStock -= quantity`; nếu ngày đó đã chuyển sang `rentalStock` thì hoàn từ `rentalStock`). Hãy đối chiếu kỹ với `completeRental` để hoàn cho đúng bucket stock.
   - Nếu status là `PENDING` (chưa trừ stock): chỉ đổi trạng thái, không đụng stock.
   - Set `status = CANCELLED`, cập nhật `updateAt`, lưu.
   - Bắn notification cho user (tái dùng `NotificationService` như `cancelByUser`).
2. Sửa `changeStatus`: khi `status == CANCELLED` phải gọi `cancelRental(...)` thay vì `setStatus` trực tiếp (giống cách `COMPLETED` gọi `completeRental`). Đảm bảo admin huỷ qua `PUT /api/v1/admin/rentals/{id}/status` cũng đi qua đường hoàn stock.

### B2. Endpoint huỷ cho người dùng

1. Thêm vào `RentalController`: `POST /api/v1/rentals/{id}/cancel`.
   - Lấy user hiện tại qua `SecurityUtils`.
   - Kiểm tra `rentalTool.getUserId().equals(currentUser.getId())`, nếu không → `ForbiddenOperationException`.
   - Gọi `rentalToolService.cancelRental(...)`.
   - Trả `ApiResponse` chứa trạng thái mới + thông điệp.
2. Cập nhật `SecurityConfiguration` nếu cần để endpoint này yêu cầu đăng nhập (mặc định `anyRequest().authenticated()` đã phủ — kiểm tra lại không lọt vào nhóm permitAll `/api/v1/rentals/*/rackets`).

### B3. Scheduler dọn đơn PENDING quá hạn thanh toán

1. Thêm property `rental.pending.expiry-minutes` (mặc định vd 30).
2. Thêm `@Scheduled` job (tái dùng style của `TemporaryBookingCleaner`/`updateRentalStockForToday`): tìm các rental `PENDING` có `createAt` cũ hơn ngưỡng và tự động `CANCELLED` (vì `PENDING` chưa trừ stock nên chỉ cần đổi trạng thái + có thể notify user là đơn đã hết hạn).
3. Thêm query repository tương ứng (vd `findByStatusAndCreateAtBefore`).

---

## YÊU CẦU TEST (bắt buộc)

- **A1:** test hai luồng confirm cùng slot/ngày → đúng một booking thành công, cái còn lại nhận 409; verify chỉ có 1 `booking_detail` active.
- **A2:** test confirm khi `bookingStockQuantity` không đủ → rollback, không tạo booking, stock không âm.
- **A3:** test gọi callback thành công hai lần cho cùng `pendingId` → chỉ tạo 1 booking.
- **A4:** test callback với `vnp_Amount` sai → bị từ chối, hold được giải phóng.
- **A5:** test user vượt ngưỡng hold → bị chặn.
- **B1/B2:** test user huỷ rental `IN_USE` → stock được hoàn đúng từng ngày; huỷ rental không thuộc user → 403; huỷ đơn `COMPLETED` → bị chặn; admin set CANCELLED cũng hoàn stock.
- **B3:** test job dọn → rental `PENDING` quá hạn chuyển `CANCELLED`.

## TIÊU CHÍ HOÀN THÀNH

- `./mvnw test` xanh.
- Không double-booking sân và không overbook vợt dưới truy cập đồng thời.
- Một lần thanh toán không bao giờ tạo quá một booking.
- User và admin đều huỷ được đơn thuê vợt, tồn kho luôn được hoàn đúng, không còn đơn `PENDING` treo vĩnh viễn.
- Liệt kê đầy đủ file đã thêm/sửa kèm tóm tắt.
