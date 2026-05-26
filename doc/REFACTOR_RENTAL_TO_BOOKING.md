# Refactor: Gộp thuê vợt vào flow đặt sân

## Bối cảnh

### Hiện trạng

- `RentalType` có 2 giá trị: `DAILY` (thuê theo ngày, độc lập), `ON_SITE` (thuê tại sân, gắn với `bookingCode`).
- Trang `/rackets/detail/:id`: user thuê vợt độc lập, chọn ngày + số lượng, thanh toán riêng (DAILY flow).
- Flow ON_SITE cũ: user nhập bookingCode → thêm vợt → thanh toán riêng.
- Booking và Rental là 2 luồng thanh toán tách biệt.

### Vấn đề

1. **Sân đã hết hạn vẫn dùng bookingCode để thuê vợt được** → logic không nhất quán.
2. **Vợt thuê theo ngày trong khi sân chỉ đặt 1 buổi** → tính tiền sai, user thiệt.
3. **Hai luồng thanh toán tách biệt** → UX phức tạp, dễ thanh toán quên một vế.

---

## Logic mới

### A. Workflow gộp booking + rental

```
[1] User chọn sân + ngày + khung giờ
[2] Hold slot (giữ chỗ tạm thời 3 phút)
[3] Điền thông tin người đặt
[4] Bấm "Tiến hành thanh toán" → mở section/modal:
[5] "Bạn có muốn thuê vợt của sân này không?" (Có/Không)
    ├─ Không → (8)
    └─ Có → (6)(7)
[6] Hiển thị danh sách vợt của sân (rackets được link tới Product)
[7] User chọn vợt + số lượng từng loại
[8] FE tính & hiển thị tổng deposit = depositSân + sum(depositVợt)
[9] FE gọi placeBooking với payload bao gồm `rackets: [{racketId, quantity}]`
[10] BE preparePendingBooking:
     - Validate stock của từng racket
     - Tính rentalDeposit (theo giá per-play, nhân với số buổi nếu WEEKLY_RECURRING)
     - Cache pending data
     - Redirect VNPay với tổng deposit
[11] VNPay success → BE confirmPendingBooking:
     - Tạo Booking (status DA_DAT)
     - Tạo n RentalTool cho từng racket, type=ON_SITE, bookingId=savedBooking.id, status=PAID
     - Giảm Racket.bookingStockQuantity tương ứng
[12] User vào /booking-history → click vào booking → xem chi tiết (booking + vợt thuê)
```

### B. Pricing vợt theo booking (chỉ ONE_TIME)

- Đơn giá: `Racket.rentalPricePerPlay` (đã có sẵn trong DB).
- `vợtPrice = rentalPricePerPlay × quantity` (1 buổi/booking).
- **KHÔNG dùng** `rentalPricePerDay` cho flow này.
- BE reject 400 nếu `rackets` không rỗng mà `bookingType=WEEKLY_RECURRING`.

### C. Validation cho rental ON_SITE cũ (qua bookingCode)

Khi user vào `/rackets/...` thuê vợt theo sân bằng cách nhập bookingCode, BE phải:

1. Booking tồn tại + thuộc user đang đăng nhập (nếu cần).
2. Booking status thuộc {`DA_DAT`, `DA_THANH_TOAN`} — không phải `DA_HUY` hay `CHO_THANH_TOAN`.
3. **Khung giờ chơi chưa hết**: tính `endTime = bookingDate + availableTime.time + 1h` (giả định 1 buổi = 1 giờ); nếu `LocalDateTime.now() > endTime` → reject với message "Booking đã hết giờ chơi, không thể thuê thêm vợt".
4. (Optional) Booking là ONE_TIME hoặc lấy buổi gần nhất trong WEEKLY_RECURRING.

### D. Giữ flow rental cũ (DAILY)

- Page `/rackets/detail/:id` giữ nguyên nút "Thuê vợt" cho DAILY rental.
- Không xóa endpoint, không xóa enum.
- Chỉ thêm flow bundled bên trong booking + validation ON_SITE.

### E. Booking detail view

- Route mới FE: `/booking-detail/:id`
- BE endpoint `GET /api/v1/client/booking-history/{id}` đã có sẵn, chỉ cần đảm bảo return cả `rentalTools`.
- FE page hiển thị:

  - Thông tin booking: mã, sân, ngày bắt đầu, khung giờ, loại đặt, status, deposit, total.
  - Nếu WEEKLY_RECURRING: liệt kê danh sách các ngày (BookingDetail).
  - Danh sách vợt thuê (nếu có): tên vợt, số lượng, đơn giá, thành tiền.
  - Tổng kết: tiền sân + tiền vợt = tổng.

### F. Rental detail view

- Route mới FE: `/rental-detail/:id`
- Hiển thị rental info + (nếu type=ON_SITE và có bookingId) thông tin booking cha.
- Trang `/rental-history` thêm nút "Xem chi tiết" mỗi row.

---

## Checklist tracking

### Phase 0: Decisions (đã chốt)

- [x] **D0.1**: WEEKLY_RECURRING → **KHÔNG cho thuê vợt**. Section chọn vợt chỉ hiển thị khi `bookingType=ONE_TIME`. BE reject nếu rackets gửi kèm WEEKLY.
- [x] **D0.2**: **Giữ cả 2 flow rental cũ** (DAILY standalone + ON_SITE qua bookingCode) song song với flow mới (bundled trong booking). **Thêm validation** ở ON_SITE: kiểm tra booking chưa hết giờ chơi trước khi cho thuê vợt.
- [x] **D0.3**: Chi tiết hiển thị **trong cả 2 history**:
  - Booking-history: mỗi row có nút "Xem chi tiết" → page/modal hiển thị booking + rentals liên kết.
  - Rental-history: mỗi row có nút "Xem chi tiết" → hiển thị rental info + (nếu là ON_SITE) thông tin booking cha.

### Phase 1: BE — Domain & DTO ✅

- [x] **B1.1**: Tạo `RentalItem` DTO (`racketId: Long`, `quantity: int`) trong `domain/dto/`.
- [x] **B1.2**: Thêm `List<RentalItem> rackets` (nullable, `@Valid`) vào `PlaceBookingRequest`.
- [x] **B1.3**: Thêm `List<RentalSlot> rentals` vào `PendingBookingData` (nested class chứa Racket + quantity + unitPrice + subtotal).

### Phase 2: BE — Service logic (bundled rental in booking) ✅

- [x] **B2.1**: `preparePendingBooking` nhận `rackets`, reject WEEKLY+rackets, validate stock & ownership của racket, tính `rentalTotal`, gộp vào cache pending data.
- [x] **B2.2**: `confirmPendingBooking` tạo `RentalTool` cho mỗi vợt (`type=ON_SITE`, `bookingId=savedBooking.id`, `status=PAID`, `quantityDay=1`), gán `rentalToolCode="BUNDLED"` vào Booking.
- [x] **B2.3**: `depositPrice = courtDeposit + rentalTotal`; `totalPrice = courtTotal + rentalTotal`.
- [x] **B2.4**: Trừ `Racket.bookingStockQuantity` khi confirm.
- [x] **B2.5**: Hỗ trợ `weekdays` cho WEEKLY_RECURRING — `PlaceBookingRequest` thêm field; service expand `[bookingDate, recurringEndDate]` theo từng day-of-week trong list (vd Thứ 3+Thứ 5). Fallback: nếu null/rỗng dùng day-of-week của bookingDate.

### Phase 3: BE — Validation cho rental ON_SITE cũ (fix bug) ✅

- [x] **B3.1**: Endpoint nằm tại `RentalToolService.handleOnSiteRental()`.
- [x] **B3.2**: Check `status ∈ {DA_DAT, DA_THANH_TOAN}` trong `validateBookingActiveForRental()`.
- [x] **B3.3**: Check `now < bookingDate + availableTime.time + 1h` — sau thời điểm này reject.
- [x] **B3.4**: Throw `IllegalArgumentException` với message rõ (lý do + thời điểm hết hạn).

### Phase 4: BE — Detail response ✅

- [x] **B4.1**: `GET /api/v1/client/booking-history/{id}` giờ return `rentalTools` qua DTO (enriched với `racketName`).
- [x] **B4.2**: `RentalToolDTO.racketName` đã có sẵn; service mới `findRentalsByBookingId()` + `findRentalDtoById()` populate via `toEnrichedDTO()`.
- [x] **B4.3**: Thêm `GET /api/v1/client/rental-history/{id}` — return rental DTO + (nếu ON_SITE) booking gắn kèm.
- [x] **B4.4**: Thêm `GET /api/v1/client/bookings/products/{productId}/rackets` — FE dùng để load danh sách vợt khi đặt sân.

### Phase 5: FE — Booking page (chọn vợt) ✅

- [x] **F5.1**: Section "Thuê thêm vợt" trong booking page, chỉ hiển thị khi `bookingType=ONE_TIME` (sử dụng `isOneTime()`).
- [x] **F5.2**: Toggle checkbox "🏸 Thuê vợt của sân này" (default off), lazy-load rackets khi bật.
- [x] **F5.3**: Service mới `getRacketsByProduct(productId)` gọi endpoint BE `/products/{productId}/rackets`.
- [x] **F5.4**: UI list với +/- buttons, giới hạn theo `bookingStockQuantity`.
- [x] **F5.5**: Computed signals `rentalTotal`, `totalDeposit` hiển thị live trong "deposit-summary".
- [x] **F5.6**: `placeBooking` payload gửi `rackets: selectedRacketItems()` khi `rentRackets()` bật.

### Phase 6: FE — Booking detail page ✅

- [x] **F6.1**: `BookingDetailComponent` ở `features/booking-detail/` (ts + html + css).
- [x] **F6.2**: Route `/booking-detail/:id` lazy-loaded với `authGuard`.
- [x] **F6.3**: Hiển thị thông tin booking + các buổi (WEEKLY) + vợt thuê + tổng kết (tách tiền sân/tiền vợt).
- [x] **F6.4**: Thêm link "Xem chi tiết" mỗi row trong `booking-history`; migrate `*ngFor` → `@for`.

### Phase 7: FE — Rental detail page ✅

- [x] **F7.1**: `RentalDetailComponent` ở `features/rental-detail/` (ts + html + css).
- [x] **F7.2**: Route `/rental-detail/:id` lazy-loaded với `authGuard`.
- [x] **F7.3**: Hiển thị rental info + (nếu ON_SITE) section "Booking sân gắn kèm" có link tới `/booking-detail/:bookingId`.
- [x] **F7.4**: Thêm link "Xem chi tiết" mỗi row trong `rental-history`; migrate `*ngFor` → `@for`.

### Phase 8: Test

- [ ] **T8.1**: Booking ONE_TIME, không vợt → VNPay → status DA_DAT, không có RentalTool.
- [ ] **T8.2**: Booking ONE_TIME + 2 vợt → VNPay → status DA_DAT, 2 RentalTool gắn đúng bookingId.
- [ ] **T8.3**: Booking WEEKLY_RECURRING + chọn vợt → BE reject 400 (D0.1).
- [ ] **T8.4**: Vợt hết stock → BE reject với 400, FE hiển thị lỗi.
- [ ] **T8.5**: Booking detail page hiển thị đủ booking + vợt + tổng tiền.
- [ ] **T8.6**: Rental detail page hiển thị đủ rental + booking cha (nếu có).
- [ ] **T8.7**: Thuê vợt qua bookingCode CŨ + booking đã hết giờ chơi → BE reject 400 (B3.3).
- [ ] **T8.8**: Thuê vợt qua bookingCode CŨ + booking còn hiệu lực → vẫn thuê được (giữ flow cũ).
- [ ] **T8.9**: Rental DAILY độc lập vẫn hoạt động bình thường.

---

## Trạng thái

**Phase hiện tại**: Phase 8 — Test E2E (user thao tác).

✅ Phase 0–7 hoàn thành. BE 4 phases (Domain DTO, Service bundled rental, ON_SITE validation fix, Detail response endpoints) + FE 3 phases (booking page racket picker, booking-detail page, rental-detail page).
