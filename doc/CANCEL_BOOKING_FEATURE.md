# Feature: Huỷ đặt sân + Hoàn cọc + Notification realtime

## Bối cảnh

Hiện tại có `BookingStatus.DA_HUY` ("Đã hủy") nhưng:

- Chỉ admin đổi được, không có flow user tự huỷ.
- Không có business rule (2h trước giờ, hoàn tiền theo buổi đã dùng).
- Không có refund tracking (admin đã hoàn hay chưa).
- Không có notification cho user/staff.

Spec mới định nghĩa flow đầy đủ. Doc này thiết kế chi tiết trước khi code.

---

## Quyết định đã chốt (Phase 0) ✅

- [x] **D0.1**: Không cho huỷ booking đã ở `DA_THANH_TOAN`. Chỉ huỷ được khi status ∈ {`CHO_THANH_TOAN`, `DA_DAT`}.
- [x] **D0.2**: WEEKLY — buổi "đã dùng" = `BookingDetail.date < today`.
- [x] **D0.3**: WEEKLY refund = `depositPrice × remainingSessions / totalSessions`.
- [x] **D0.4**: Cascade — huỷ booking → rental ON_SITE đính kèm CANCELLED + trả stock vợt.
- [x] **D0.5**: Notification lưu DB + WS push realtime. Mark-as-read khi click.
- [x] **D0.6**: Notification staff/admin gửi tới ALL user role ADMIN hoặc STAFF.
- [x] **D0.7**: Manual refund — admin tự chuyển khoản + bấm "Đã hoàn cọc". KHÔNG dùng VNPay refund API.

---

## 1. Database design

### 1.1. Mở rộng `booking` table

Thêm các cột:

| Column | Type | Description |
|---|---|---|
| `refund_status` | VARCHAR(32) | NONE / PENDING_REFUND / REFUNDED / NOT_APPLICABLE |
| `refund_amount` | DOUBLE | Số tiền cần hoàn (computed lúc huỷ) |
| `cancelled_at` | DATETIME | Thời điểm user bấm huỷ |
| `used_sessions_at_cancel` | INT | Số buổi đã dùng tại thời điểm huỷ (WEEKLY); ONE_TIME = 0 |
| `total_sessions_at_cancel` | INT | Tổng số buổi (cho hiển thị "X / Y buổi") |
| `cancel_reason` | VARCHAR(255) | Optional, lưu lý do user nhập (nếu UI có) |

Migration: `V10__add_cancel_refund_fields_to_booking.sql`.

### 1.2. Table mới `notification`

| Column | Type | Description |
|---|---|---|
| `id` | BIGINT PK AUTO_INCREMENT | |
| `recipient_user_id` | BIGINT | FK → user.id |
| `type` | VARCHAR(32) | REFUND_REQUEST / REFUND_DONE / SYSTEM |
| `ref_type` | VARCHAR(32) | BOOKING / RENTAL / ... |
| `ref_id` | BIGINT | id của entity liên quan |
| `title` | VARCHAR(255) | Tiêu đề hiển thị (vd: "Yêu cầu hoàn cọc mới") |
| `message` | TEXT | Nội dung chi tiết |
| `is_read` | BOOLEAN DEFAULT FALSE | |
| `created_at` | DATETIME | |

Index: `(recipient_user_id, is_read, created_at DESC)` cho query unread.

Migration: `V11__create_notification_table.sql`.

---

## 2. Entity / Enum / DTO

### 2.1. New enum `RefundStatus`

```java
public enum RefundStatus {
    NONE("Không áp dụng"),
    PENDING_REFUND("Chờ hoàn cọc"),
    REFUNDED("Đã hoàn cọc"),
    NOT_APPLICABLE("Không hoàn cọc");
    ...
}
```

### 2.2. New enum `NotificationType`

```java
public enum NotificationType {
    REFUND_REQUEST,  // gửi cho admin/staff khi user huỷ
    REFUND_DONE,     // gửi cho user khi admin xác nhận hoàn
    BOOKING_CANCELLED, // gửi cho user khi huỷ thành công
    SYSTEM           // generic
}
```

### 2.3. Booking entity

Thêm fields tương ứng với DB.

### 2.4. New entity `Notification`

Tương ứng table `notification`.

### 2.5. DTO

- `CancelBookingRequest`: optional `reason: String`.
- `CancelBookingResponse`: refundAmount, refundStatus, usedSessions, totalSessions, contact info.
- `NotificationDTO`: id, type, title, message, isRead, createdAt, refType, refId.

---

## 3. Backend flow

### 3.1. `BookingService.cancelByUser(bookingId, userId, reason)`

```
1. Load booking; verify booking.user.id == userId.
2. Check status ∈ allowed states (D0.1):
   - Reject if status in [DA_HUY, DA_THANH_TOAN].
3. Compute refund:
   - ONE_TIME:
       - now < bookingDate.atTime(time) - 2h → OK, refund = booking.depositPrice
       - else → reject "Không thể huỷ trong vòng 2 tiếng trước giờ bắt đầu"
   - WEEKLY:
       - totalSessions = count(BookingDetail) trong booking
       - usedSessions = count(BookingDetail where date < today)
       - remainingSessions = total - used
       - refund = depositPrice × remaining / total
       - (Không gate 2h vì WEEKLY có nhiều buổi)
4. Update booking:
   - status = DA_HUY
   - refundStatus = PENDING_REFUND (hoặc NOT_APPLICABLE nếu refund=0)
   - refundAmount, cancelledAt = now()
   - usedSessionsAtCancel, totalSessionsAtCancel
5. Cascade rental ON_SITE đính kèm (D0.4):
   - RentalTool.status = CANCELLED
   - Trả stock: racket.bookingStockQuantity += rt.quantity
6. Slot tự động được "free" vì query available-times exclude DA_HUY.
   (Verify: cần cập nhật query nếu đang không exclude.)
7. Tạo notifications:
   - 1 cho user: type BOOKING_CANCELLED, message có refundAmount + contact info
   - n cho mỗi user role ADMIN/STAFF: type REFUND_REQUEST
8. Publish WS realtime:
   - /user/{email}/queue/notifications → cho user
   - /topic/staff-notifications → cho admin/staff
9. Return CancelBookingResponse.
```

### 3.2. `BookingService.confirmRefund(bookingId, adminUserId)`

```
1. Load booking; status == DA_HUY, refundStatus == PENDING_REFUND.
2. refundStatus = REFUNDED.
3. Save audit info (refundedBy, refundedAt — optional, có thể thêm sau).
4. Notify user (REFUND_DONE).
5. Push WS realtime tới user.
```

### 3.3. `NotificationService`

```java
class NotificationService {
    Notification create(userId, type, refType, refId, title, message)
    List<NotificationDTO> listByUser(userId, page, size)
    int countUnread(userId)
    void markRead(notificationId, userId)
    void markAllRead(userId)
    // WS push
    void pushToUser(userId, NotificationDTO)
    void pushToStaff(NotificationDTO)
}
```

---

## 4. REST API

### Client
- `POST /api/v1/client/bookings/{id}/cancel` — Body: `{ reason?: string }` → Response: refund summary.
- `GET /api/v1/client/notifications?page=0&size=20` — list của user hiện tại.
- `GET /api/v1/client/notifications/unread-count` — int.
- `PUT /api/v1/client/notifications/{id}/read` — mark single.
- `PUT /api/v1/client/notifications/read-all` — mark tất cả.

### Admin
- `PUT /api/v1/admin/bookings/{id}/refund` — mark REFUNDED. Body optional.
- `GET /api/v1/admin/notifications?page=0&size=20` — list staff notifications.
- `GET /api/v1/admin/refund-requests?status=PENDING_REFUND&page=...` — list bookings chờ hoàn.

### WebSocket
- `/user/{userEmail}/queue/notifications` — push notification cho user cụ thể.
- `/topic/staff-notifications` — broadcast cho mọi staff/admin subscribe.

---

## 5. Frontend UI/UX

### 5.1. `/booking-detail/:id` (đã có)

Thêm:
- Nếu `status ∈ {CHO_THANH_TOAN, DA_DAT}` → button "Huỷ đặt sân" (đỏ).
- Click → modal confirm:
  ```
  Bạn có chắc chắn muốn huỷ lịch đặt sân này không?
  Số tiền cọc dự kiến hoàn: 200,000 VNĐ
  (Nếu là WEEKLY: thêm "Đã sử dụng 4/10 buổi")
  [Đóng] [Xác nhận huỷ]
  ```
- ONE_TIME nếu < 2h: chỉ hiển thị thông báo, không cho mở modal.
- Sau khi huỷ thành công:
  - Refresh trang.
  - Hiển thị block "Đã huỷ" với:
    - Status: Đã hủy.
    - Refund status: Chờ hoàn cọc / Đã hoàn cọc.
    - Số tiền hoàn.
    - "Nếu chưa nhận được hoàn tiền, vui lòng liên hệ:"
      - Hotline: 0123456789
      - Email: admin@gmail.com

### 5.2. Header — Notification bell

- Icon chuông góc phải header, badge số unread.
- Click → dropdown 10 notifications gần nhất:
  - Title + message + relative time ("5 phút trước").
  - Click 1 notification → mark read + navigate đến detail (booking hoặc rental).
- "Xem tất cả" → `/notifications` page (optional Phase 7).

### 5.3. Admin notification

- Bell icon trong admin layout, badge unread cho admin/staff.
- Click notification REFUND_REQUEST → navigate tới `/admin/bookings/{id}` hoặc page mới `/admin/refund-requests/:id` có:
  - Thông tin booking.
  - Số tiền cần hoàn.
  - SĐT + email user.
  - Button "Đã hoàn cọc" → confirm → mark REFUNDED.

### 5.4. Booking history list

- Booking status `Đã hủy` hiển thị thêm badge phụ "🟡 Chờ hoàn cọc" hoặc "🟢 Đã hoàn cọc" để dễ phân biệt.

---

## 6. Edge cases

| # | Case | Xử lý |
|---|---|---|
| E1 | Double cancel (user click 2 lần) | DB-level check status != DA_HUY trước khi update. Trả 409 lần sau. |
| E2 | Booking đã COMPLETED (DA_THANH_TOAN) | Reject với "Đơn đã hoàn thành, không thể huỷ". |
| E3 | ONE_TIME nhưng < 2h | Reject với message rõ + còn bao nhiêu phút. |
| E4 | WEEKLY tất cả buổi đã qua | refund = 0, refundStatus = NOT_APPLICABLE. Vẫn cho huỷ để tránh stuck data. |
| E5 | Admin confirm refund 2 lần | Check refundStatus == PENDING_REFUND, idempotent. |
| E6 | User huỷ booking của người khác | Verify ownership; trả 403. |
| E7 | Booking có rental DAILY (standalone, không gắn) | Không cascade — DAILY không liên kết booking. |
| E8 | Race: 2 user xem slot cùng lúc, 1 huỷ → slot kia phải thấy slot trống trở lại | Reuse logic SlotEventPublisher đã có — publish SLOT_RELEASED. |
| E9 | Notification table query chậm khi nhiều | Index `(recipient_user_id, is_read, created_at DESC)` |
| E10 | Booking đã huỷ rồi user xem detail | Vẫn show refund info + contact. Không hiện nút huỷ. |
| E11 | Refund=0 (WEEKLY hết buổi) | Hiển thị "Không có cọc hoàn", refundStatus=NOT_APPLICABLE, không tạo notification cho admin (không có gì làm). |

---

## 7. Checklist phases

### Phase 0: Decisions ✋ (chờ user confirm D0.1–D0.7)

### Phase 1: BE Domain & DTO ✅
- [x] **C1.1**: `RefundStatus` enum (NONE / PENDING_REFUND / REFUNDED / NOT_APPLICABLE).
- [x] **C1.2**: `NotificationType` enum (REFUND_REQUEST / REFUND_DONE / BOOKING_CANCELLED / SYSTEM).
- [x] **C1.3**: Booking thêm 6 fields (refundStatus, refundAmount, cancelledAt, usedSessionsAtCancel, totalSessionsAtCancel, cancelReason) + migration `V10__add_cancel_refund_fields_to_booking.sql`.
- [x] **C1.4**: `Notification` entity + `NotificationRepository` (có `findStaffAndAdminUserIds`) + migration `V11__create_notification_table.sql`.
- [x] **C1.5**: DTOs `CancelBookingRequest`, `CancelBookingResponse`, `NotificationDTO`.

### Phase 2: BE Service — Cancel logic
- [ ] **C2.1**: `BookingService.cancelByUser()` — validate, compute refund, update, cascade rental.
- [ ] **C2.2**: `BookingService.confirmRefund()` — admin mark REFUNDED.
- [ ] **C2.3**: Verify query available-times exclude `status=DA_HUY` (nếu chưa).

### Phase 3: BE Notification
- [ ] **C3.1**: `NotificationService` với create/list/markRead/countUnread.
- [ ] **C3.2**: Integrate WebSocket push (reuse SimpMessagingTemplate đã setup).
- [ ] **C3.3**: Tạo helper publish `pushToUser(email)` + `pushToStaff()` (broadcast tới topic).
- [ ] **C3.4**: `NotificationController` với client + admin endpoints.

### Phase 4: BE Wire cancel → notifications
- [ ] **C4.1**: Trong `cancelByUser`: tạo notification user + admin/staff.
- [ ] **C4.2**: Trong `confirmRefund`: tạo notification user.
- [ ] **C4.3**: Endpoint client + admin cancel/refund.

### Phase 5: FE Cancel UI ✅
- [x] **C5.1**: `BookingDetailComponent` thêm nút "Huỷ đặt sân" (chỉ hiện khi status ∈ {CHO_THANH_TOAN, DA_DAT}) + modal confirm.
- [x] **C5.2**: `BookingService.cancelBooking(id, reason?)` gọi `POST /client/bookings/{id}/cancel`.
- [x] **C5.3**: Section refund hiển thị khi DA_HUY: badge refund status (PENDING_REFUND/REFUNDED), số tiền hoàn, contact hotline+email từ response. Refund_done có message ✅.
- [x] **C5.4**: `cancelPreview` computed signal — FE tính trước ở client (ONE_TIME: check 2h gate + refund=deposit; WEEKLY: count usedSessions từ bookingDetails đã có).

### Phase 6: FE Notification bell ✅
- [x] **C6.1**: `notification.service.ts` — HTTP (list/unread-count/markRead/markAllRead) + WS (watchUserNotifications, watchStaffNotifications dùng RxStomp + SockJS).
- [x] **C6.2**: Subscribe `/user/queue/notifications` (RxStomp tự xử lý prefix `/user/`).
- [x] **C6.3**: `NotificationBellComponent` (shared) — icon 🔔 + badge unread + dropdown 10 notif gần nhất + mark-all-read + relativeTime.
- [x] **C6.4**: Bell tích hợp vào `app.html` `.nav-actions` khi logged-in. `[forAdmin]="isAdmin()"` đẩy admin/staff subscribe thêm `/topic/staff-notifications`.

### Phase 7: FE Admin refund management ✅
- [x] **C7.1**: `forAdmin=true` ở bell → subscribe `/topic/staff-notifications`.
- [x] **C7.2**: Click notification REFUND_REQUEST trong bell → navigate `/admin/refund-requests`.
- [x] **C7.3**: Modal chi tiết trong `/admin/refund-requests`: hiển thị full info booking + nút "✅ Đã hoàn cọc" gọi `PUT /admin/bookings/{id}/refund`.
- [x] **C7.4**: Page `/admin/refund-requests` với 3 tabs filter (Chờ hoàn cọc / Đã hoàn cọc / Tất cả) + nav link "Hoàn cọc" thêm vào admin navbar.

### Phase 8: Test E2E
- [ ] **T8.1**: User huỷ ONE_TIME > 2h → DA_HUY, PENDING_REFUND, refund=deposit, slot release.
- [ ] **T8.2**: User huỷ ONE_TIME < 2h → reject 400.
- [ ] **T8.3**: User huỷ WEEKLY giữa chu kỳ → refund đúng tỉ lệ.
- [ ] **T8.4**: User huỷ ONE_TIME + bundled rackets → rental CANCELLED + trả stock.
- [ ] **T8.5**: Admin nhận notification REFUND_REQUEST realtime → click vào → hoàn → REFUNDED → user nhận notification.
- [ ] **T8.6**: Double cancel / double refund → idempotent.

---

## 8. Configuration cần thêm

Trong `application.properties`:
```
# Contact info cho user liên hệ khi cần hoàn cọc
app.contact.hotline=${CONTACT_HOTLINE:0123456789}
app.contact.email=${CONTACT_EMAIL:admin@badmintonhub.vn}
```

FE `environment.ts`:
```ts
contact: { hotline: '0123456789', email: 'admin@badmintonhub.vn' }
```

Hoặc fetch từ BE qua `/api/v1/config/contact`.

---

## Trạng thái

**Phase hiện tại**: Phase 8 — chờ user test E2E. Phase 0–7 đã xong + compile cả BE và FE pass.

### Tổng quan thay đổi
- **BE**: 2 enum + 1 entity + 2 migrations + DTOs + service cancel/refund + notification service mở rộng + 6 endpoints (5 client + 3 admin + 1 confirm refund).
- **FE**: cancel UI (button + modal preview), refund info display, notification bell shared component, admin refund management page.
