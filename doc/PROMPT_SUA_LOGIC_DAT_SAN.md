# Prompt: Sửa & Hoàn thiện Logic Đặt Sân + Thuê Vợt + Huỷ Booking

---

## Bối cảnh dự án

Đây là ứng dụng đặt sân cầu lông (Badminton Hub) gồm:
- **Backend**: Spring Boot (Java), JPA, VNPay, WebSocket (STOMP)
- **Frontend**: Angular 17+ (standalone components, signals)

### Cấu trúc BE quan trọng
```
service/
  BookingService.java         — preparePendingBooking(), confirmPendingBooking()
  RentalToolService.java      — handleSubmitRental(), handleOnSiteRental()
  NotificationService.java    — hiện rỗng, chưa implement
  PaymentService.java         — createVnPayPayment(), verifyVnpayCallback()
  PendingBookingCache.java    — in-memory cache cho booking chờ thanh toán
controller/
  PaymentController.java      — handleVnpayCallback() xử lý cả BOOKING + RENTAL_TOOL
  client/BookingClientController.java — /hold, /place, /available-times
domain/
  Booking.java                — có rentalToolCode = "KHONG_THUE", thiếu refund fields
  BookingStatus enum          — CHO_THANH_TOAN, DA_DAT, DA_DAT_COC, DA_THANH_TOAN, DA_HUY
  RentalTool.java             — bookingId là String (không phải FK)
  PendingBookingData.java     — cache object, CHƯA có List<RentalSlot> rackets
  PlaceBookingRequest.java    — CHƯA có List<RentalItem> rackets
```

### Cấu trúc FE quan trọng
```
features/booking/
  booking.ts                  — component đặt sân, signals, form reactive
  booking.html                — 3-step UI (chọn slot → thông tin → thanh toán)
core/services/
  booking.service.ts          — HTTP calls, CHƯA có cancelBooking(), placeBooking() không gửi rackets
```

---

## Vấn đề cần sửa — được ưu tiên từ cao xuống thấp

### NHÓM 1: Bundled Rental trong Booking (REFACTOR_RENTAL_TO_BOOKING.md Phase 2–6 đã thiết kế nhưng CHƯA implement)

**BE — BookingService.java**: Hiện tại `preparePendingBooking()` không nhận tham số vợt. Cần:
1. Thêm `List<RentalItem> rackets` vào `PendingBookingData` (nested class `RentalSlot` với: `racketId, quantity, unitPrice, subtotal`).
2. Thêm `List<RentalItem> rackets` vào `PlaceBookingRequest` (nullable, `@Valid`).
3. Trong `preparePendingBooking()`:
   - Nếu `bookingType == WEEKLY_RECURRING` và `rackets` không rỗng → throw `400 "Không hỗ trợ thuê vợt cho đặt sân theo tháng."`.
   - Với mỗi `RentalItem`: load `Racket` entity, kiểm tra `racket.getBookingStockQuantity() >= item.quantity`, nếu không đủ → throw `400 "Vợt [tên] không đủ số lượng. Còn lại: X."`.
   - Tính `rentalTotal = sum(racket.getRentalPricePerPlay() * item.quantity)`.
   - Gộp `rentalTotal` vào `depositPrice` và `totalPrice`.
   - Lưu `RentalSlot` list vào `PendingBookingData`.
4. Trong `confirmPendingBooking()`:
   - Với mỗi `RentalSlot` trong `data.getRentalSlots()`:
     - Tạo `RentalTool` mới: `type=ON_SITE`, `bookingId=String.valueOf(savedBooking.getId())`, `status=PAID`, `quantityDay=1`, `userId=data.getUser().getId()`, `rentalPrice=slot.subtotal`, `quantity=slot.quantity`, `racketId=slot.racketId`.
     - Trừ `racket.bookingStockQuantity -= slot.quantity`, save racket.
   - Nếu có rental, set `booking.setRentalToolCode("BUNDLED")`.

**BE — `GET /api/v1/client/bookings/products/{productId}/rackets`**: Thêm endpoint mới trong `BookingClientController` trả về `List<Racket>` của product (chỉ vợt có `bookingStockQuantity > 0`), dùng `RacketService.getAvailableRacketsByCourt()` hoặc tương tự.

**FE — booking.ts + booking.html**:
- Thêm section "Thuê thêm vợt" trong bước 2, chỉ hiện khi `bookingForm.get('bookingType')?.value === 'ONE_TIME'`.
- Toggle checkbox "🏸 Thuê vợt của sân này" (default off). Khi bật: gọi `GET /api/v1/client/bookings/products/{productId}/rackets` lazy-load.
- UI: list vợt với tên, giá/buổi, nút `+/-`, giới hạn theo `bookingStockQuantity`.
- Computed: `rentalTotal = sum(price * qty)`, hiển thị trong summary "Tiền vợt: X VNĐ / Tổng cọc: Y VNĐ".
- `placeBooking` payload thêm `rackets: [{racketId, quantity}]` khi toggle bật.
- **booking.service.ts**: `placeBooking(payload)` giữ nguyên nhưng payload type nên include `rackets?: {racketId: number, quantity: number}[]`.

---

### NHÓM 2: Cancel Booking + Refund (CANCEL_BOOKING_FEATURE.md Phase 2–4 chưa implement)

**BE — Booking entity**: Thêm các fields sau vào `Booking.java`:
```java
@Enumerated(EnumType.STRING)
private RefundStatus refundStatus = RefundStatus.NONE;
private Double refundAmount;
private LocalDateTime cancelledAt;
private Integer usedSessionsAtCancel;
private Integer totalSessionsAtCancel;
private String cancelReason;
```

**BE — 2 enums mới**:
```java
// RefundStatus.java
public enum RefundStatus { NONE, PENDING_REFUND, REFUNDED, NOT_APPLICABLE }

// NotificationType.java  
public enum NotificationType { REFUND_REQUEST, REFUND_DONE, BOOKING_CANCELLED, SYSTEM }
```

**BE — Notification entity + repository** (migration `V11__create_notification_table.sql`):
```java
@Entity @Table(name = "notification")
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long recipientUserId;
    @Enumerated(EnumType.STRING) private NotificationType type;
    private String refType; // "BOOKING"
    private Long refId;
    private String title;
    @Column(columnDefinition = "TEXT") private String message;
    private Boolean isRead = false;
    private LocalDateTime createdAt;
}
```
Repository cần: `findByRecipientUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable)`, `countByRecipientUserIdAndIsReadFalse(Long userId)`, `findStaffAndAdminUserIds()` (query tất cả userId có role ADMIN hoặc STAFF).

**BE — Migration `V10__add_cancel_refund_fields_to_booking.sql`**:
```sql
ALTER TABLE booking ADD COLUMN refund_status VARCHAR(32) DEFAULT 'NONE';
ALTER TABLE booking ADD COLUMN refund_amount DOUBLE;
ALTER TABLE booking ADD COLUMN cancelled_at DATETIME;
ALTER TABLE booking ADD COLUMN used_sessions_at_cancel INT;
ALTER TABLE booking ADD COLUMN total_sessions_at_cancel INT;
ALTER TABLE booking ADD COLUMN cancel_reason VARCHAR(255);
```

**BE — BookingService.java** thêm 2 methods:

```java
// Method 1: cancelByUser
@Transactional
public CancelBookingResponse cancelByUser(Long bookingId, Long userId, String reason) {
    // 1. Load + verify ownership → 403 nếu sai
    // 2. Check status ∈ {CHO_THANH_TOAN, DA_DAT, DA_DAT_COC} → reject nếu DA_HUY hoặc DA_THANH_TOAN
    // 3. Tính refund:
    //    - ONE_TIME: check now < bookingDate.atTime(availableTime.time).minusHours(2) → nếu < 2h: throw 400 kèm message còn bao nhiêu phút
    //                refund = booking.getDepositPrice()
    //    - WEEKLY_RECURRING: totalSessions = bookingDetails.size()
    //                        usedSessions = count(detail.date < today)
    //                        remaining = total - used
    //                        refund = depositPrice * remaining / total
    // 4. Update booking fields
    // 5. Cascade rental: rentalToolRepository.findRentalToolsByBookingId(bookingId+"")
    //    → set CANCELLED, trả stock
    // 6. Tạo notifications (gọi NotificationService)
    // 7. Return CancelBookingResponse
}

// Method 2: confirmRefund  
@Transactional
public void confirmRefund(Long bookingId) {
    // Check status DA_HUY + refundStatus PENDING_REFUND → idempotent nếu đã REFUNDED
    // Set refundStatus = REFUNDED
    // Notify user
}
```

**BE — DTOs mới**:
- `CancelBookingRequest`: `String reason` (optional).
- `CancelBookingResponse`: `refundAmount, refundStatus, usedSessions, totalSessions, hotline, email`.

**BE — NotificationService.java** (hiện rỗng, cần implement):
```java
@Service
public class NotificationService {
    // inject: NotificationRepository, UserRepository, SimpMessagingTemplate
    
    Notification create(Long userId, NotificationType type, String refType, Long refId, String title, String message);
    Page<NotificationDTO> listByUser(Long userId, Pageable pageable);
    int countUnread(Long userId);
    void markRead(Long notificationId, Long userId);
    void markAllRead(Long userId);
    void pushToUser(String email, NotificationDTO dto);      // /user/{email}/queue/notifications
    void pushToStaff(NotificationDTO dto);                    // /topic/staff-notifications
}
```

**BE — Endpoints mới**:
```
Client:
  POST /api/v1/client/bookings/{id}/cancel          → cancelByUser
  GET  /api/v1/client/notifications?page=0&size=20  → listByUser
  GET  /api/v1/client/notifications/unread-count    → countUnread
  PUT  /api/v1/client/notifications/{id}/read       → markRead
  PUT  /api/v1/client/notifications/read-all        → markAllRead

Admin:
  PUT  /api/v1/admin/bookings/{id}/refund            → confirmRefund
  GET  /api/v1/admin/notifications?page=0&size=20   → list for staff
  GET  /api/v1/admin/refund-requests?status=PENDING_REFUND&page=0 → bookings chờ hoàn
```

**BE — BookingClientController** thêm:
```java
@PostMapping("/{id}/cancel")
public ResponseEntity<ApiResponse<CancelBookingResponse>> cancelBooking(
    @PathVariable Long id,
    @RequestBody(required = false) CancelBookingRequest req) {
    // getCurrentUser(), gọi bookingService.cancelByUser()
}
```

**FE — booking.service.ts** thêm:
```typescript
cancelBooking(id: number, reason?: string): Observable<ApiResponse<any>> {
  return this.http.post<ApiResponse<any>>(`${this.API_URL}/${id}/cancel`, { reason });
}

getBookingDetail(id: number): Observable<ApiResponse<any>> {
  return this.http.get<ApiResponse<any>>(`${environment.apiBaseUrl}/client/booking-history/${id}`);
}
```

---

### NHÓM 3: Booking Detail Page (FE — chưa có)

Tạo `features/booking-detail/` với `BookingDetailComponent`:
- Route `/booking-detail/:id` lazy-loaded với `authGuard`.
- Load từ `GET /api/v1/client/booking-history/{id}`.
- Hiển thị:
  - Thông tin booking: mã, sân, ngày, khung giờ, loại (ONE_TIME/WEEKLY), status badge.
  - Nếu WEEKLY: danh sách `bookingDetails[]` (ngày, sân phụ, giờ, giá).
  - Nếu có vợt thuê (rentalTools[]): tên vợt, số lượng, đơn giá, thành tiền.
  - Tổng kết: tiền sân + tiền vợt = tổng cọc.
  - **Nút "Huỷ đặt sân"** (đỏ): chỉ hiển thị khi `status ∈ {CHO_THANH_TOAN, DA_DAT, DA_DAT_COC}`.
  - ONE_TIME: nếu < 2 tiếng trước giờ đặt → disable button, hiện tooltip.
  - Khi click → modal confirm hiển thị: số tiền hoàn dự kiến, (WEEKLY: X/Y buổi đã dùng).
  - Sau cancel thành công: reload, hiển thị block "Đã huỷ" với refundStatus badge + số tiền + contact (hotline/email lấy từ response).
- `BookingDetailComponent` thêm vào `booking-history.html`: mỗi row thêm nút "Xem chi tiết" → routerLink `/booking-detail/:id`.

---

### NHÓM 4: Notification Bell (FE — chưa có)

Tạo `shared/components/notification-bell/`:
- **notification.service.ts** (new):
```typescript
getNotifications(page=0, size=10): Observable<ApiResponse<any>>
getUnreadCount(): Observable<ApiResponse<number>>
markRead(id: number): Observable<any>
markAllRead(): Observable<any>
watchUserNotifications(): Observable<any>   // RxStomp /user/queue/notifications
watchStaffNotifications(): Observable<any>  // RxStomp /topic/staff-notifications
```
- **NotificationBellComponent** (`[forAdmin]="isAdmin()"`):
  - Icon 🔔 + badge unread count.
  - Click → dropdown 10 notification gần nhất: title + message + relativeTime.
  - Click notification → markRead + navigate (REFUND_REQUEST → `/admin/refund-requests`, BOOKING_CANCELLED → `/booking-detail/{refId}`).
  - "Xem tất cả" link (optional).
  - Khi `forAdmin=true`: subscribe thêm `/topic/staff-notifications`.
- Tích hợp vào `app.html` trong `.nav-actions` khi logged-in.

---

### NHÓM 5: Admin Refund Management (FE — chưa có)

Tạo `features/admin/refund-requests/`:
- **3 tabs**: "Chờ hoàn cọc" / "Đã hoàn cọc" / "Tất cả".
- Mỗi row: bookingCode, tên user, SĐT, số tiền hoàn, ngày huỷ.
- Click → modal chi tiết: full booking info + nút "✅ Đã hoàn cọc" → gọi `PUT /admin/bookings/{id}/refund`.
- Thêm link "Hoàn cọc" vào admin navbar.
- Admin service thêm:
```typescript
getRefundRequests(status?: string, page=0, size=10): Observable<any>
confirmRefund(bookingId: number): Observable<any>
```

---

### NHÓM 6: Fix/Verify các edge cases

**BE — available-times query**: Kiểm tra `BookingDetailRepository.findBySubCourtAndDate()` có **exclude** booking với `status = DA_HUY` chưa. Nếu chưa, sửa query:
```java
// Thêm điều kiện: booking.status != 'DA_HUY'
@Query("SELECT bd FROM BookingDetail bd WHERE bd.subCourt = :subCourt AND bd.date = :date AND bd.booking.status <> 'DA_HUY'")
```

**BE — Double cancel protection**: Trong `cancelByUser`, check `status == DA_HUY` trước update → throw `409 "Booking đã được huỷ."`.

**BE — RentalTool cascade khi cancel**: Khi `cancelByUser()`, với rental ON_SITE đính kèm:
- Set `rentalTool.status = CANCELLED`.
- `racket.bookingStockQuantity += rentalTool.quantity` (hoàn lại stock).
- Save cả 2.

**BE — BookingStatus trong callback**: Hiện `confirmPendingBooking()` set `status = DA_DAT_COC` nhưng doc spec dùng `DA_DAT`. Xác nhận và đồng bộ với FE (hiện FE check `status ∈ {CHO_THANH_TOAN, DA_DAT}` để hiện nút huỷ — cần include `DA_DAT_COC`).

---

## Yêu cầu khi implement

1. **Không xoá code cũ** (DAILY rental flow, ON_SITE qua bookingCode vẫn giữ nguyên).
2. **Transactions**: mỗi method service phải có `@Transactional`, đặc biệt `cancelByUser` (nhiều table update).
3. **Error handling**: dùng đúng exception class đã có (`ResourceNotFoundException`, `ForbiddenOperationException`, `BusinessConflictException`). Không throw raw `RuntimeException`.
4. **Migration files**: tạo đúng naming convention `V10__...sql`, `V11__...sql` trong `src/main/resources/db/migration/`.
5. **Angular**: dùng signals (`signal()`, `computed()`) nhất quán với codebase hiện tại. Không dùng `ngModel`, chỉ reactive forms. Lazy-load route mới.
6. **Không hardcode** contact hotline/email trong FE — đọc từ `environment.ts` hoặc từ `CancelBookingResponse`.

---

## Thứ tự implement gợi ý

```
1. BE: RefundStatus enum + NotificationType enum
2. BE: V10 migration + Booking entity fields
3. BE: Notification entity + V11 migration + NotificationRepository
4. BE: NotificationService (full impl)
5. BE: BookingService.cancelByUser() + confirmRefund()
6. BE: CancelBookingRequest/Response DTOs + endpoints
7. BE: PlaceBookingRequest + PendingBookingData thêm rackets
8. BE: BookingService.preparePendingBooking() + confirmPendingBooking() — bundled racket
9. BE: GET /products/{productId}/rackets endpoint
10. FE: booking.ts — section thuê vợt + payload rackets
11. FE: booking.service.ts — cancelBooking(), getBookingDetail()
12. FE: BookingDetailComponent — full UI + cancel modal
13. FE: notification.service.ts + NotificationBellComponent
14. FE: Admin refund-requests page
15. FE: booking-history.html — thêm link "Xem chi tiết"
```

---

## Files cần tạo mới

**BE:**
- `domain/RefundStatus.java`
- `domain/NotificationType.java`
- `domain/Notification.java`
- `repository/NotificationRepository.java`
- `domain/dto/CancelBookingRequest.java`
- `domain/dto/CancelBookingResponse.java`
- `domain/dto/NotificationDTO.java`
- `domain/dto/RentalItem.java`
- `resources/db/migration/V10__add_cancel_refund_fields_to_booking.sql`
- `resources/db/migration/V11__create_notification_table.sql`

**FE:**
- `features/booking-detail/booking-detail.ts`
- `features/booking-detail/booking-detail.html`
- `features/booking-detail/booking-detail.css`
- `features/admin/refund-requests/refund-requests.ts`
- `features/admin/refund-requests/refund-requests.html`
- `shared/components/notification-bell/notification-bell.ts`
- `shared/components/notification-bell/notification-bell.html`
- `core/services/notification.service.ts`

**Files cần sửa:**
- `domain/Booking.java` — thêm refund fields
- `domain/dto/PlaceBookingRequest.java` — thêm rackets
- `domain/dto/PendingBookingData.java` — thêm RentalSlot list
- `service/BookingService.java` — preparePendingBooking, confirmPendingBooking, cancelByUser, confirmRefund
- `service/NotificationService.java` — implement từ đầu
- `controller/client/BookingClientController.java` — thêm cancel endpoint + rackets endpoint
- `controller/admin/BookingController.java` — thêm refund endpoint
- `core/services/booking.service.ts` — thêm cancelBooking, getBookingDetail
- `core/services/admin.service.ts` — thêm getRefundRequests, confirmRefund
- `app.routes.ts` — thêm lazy routes mới
- `app.html` — tích hợp notification bell
- `features/booking/booking.ts` — thêm rental section
- `features/booking/booking.html` — thêm rental UI
- `features/booking-history/booking-history.html` — thêm link chi tiết
