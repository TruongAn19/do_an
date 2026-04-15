# Giai đoạn F2: Ánh Xạ & Xây Dựng UI Cho Client (Khung Angular)

Chuyển đổi thư mục `src/main/webapp/WEB-INF/view/client/*` sang nền tảng Angular.

## 1. Master Layout (Ứng với `client/layout/`)
Ở JSP, bạn phải Include (gắn) header và footer vào mọi file bằng `<jsp:include>`. Còn Angular dùng cấu trúc cha con (Outlet).
Tạo Component lệnh: `ng g c layouts/client-layout`

**`client-layout.component.html`**:
```html
<div class="d-flex flex-column min-vh-100 position-relative">
  <!-- Shared Navbar Component -->
  <app-navbar></app-navbar>

  <main class="flex-grow-1 container px-4 py-5">
    <!-- Nơi các trang con (home, booking) sẽ xuất hiện -->
    <router-outlet></router-outlet> 
  </main>

  <app-chat-widget></app-chat-widget>
  <app-footer></app-footer>
</div>
```

## 2. Hệ Thống Các Trang Client Chính (Tính module hóa)
Dùng lệnh CLI để tạo các Standalone Component ứng với từng JSP. Ngôn ngữ template chuyển sang cú pháp xịn (Dùng `@if`, `@for` của Angular 17+ thay cho `<c:if>`, `<c:forEach>`).

* **Thư mục `auth/` -> `features/auth/`**
  * `ng g c features/auth/pages/login`: Sử dụng form Angular (`ReactiveFormsModule`). Quản lý Input và Validator dễ dàng, nếu Login thành công sẽ lưu JWT vào `localStorage.setItem('ACCESS_TOKEN', token)`.

* **Thư mục `homepage/` -> `features/home/`**
  * `ng g c features/home/pages/home-page`: Giao diện bao gồm Banner và List Slider sân bằng class thiết kế Bootstrap (Row, Col, Card).

* **Thư mục `booking/` -> `features/booking/`**
  * `booking_form.jsp` -> `features/booking/pages/booking-scheduler/`: Đặc biệt chú ý form đặt sân. Trong Angular, dùng vòng lặp `@for (time of availableTimes(); track time.id)` để vẽ các ô lưới cho khách chọn giờ. Dùng Angular Signals (`signal()`) để lưu danh sách State khung giờ vừa chọn (Hold).

* **Thư mục `match-post/` -> `features/social/`**
  * `match_feed.jsp` -> `features/social/match-feed/`: Danh sách bản tin. Truyền DTO qua component con là `@Input()` thẻ Card chứ không code HTML 1 cục hỗn độn như file JSP.

* **Thư mục `racket/` & `product/` -> `features/shop/`**
  * `features/shop/shop-page/`: Danh sách thiết bị và trang phục cầu lông. Áp dụng Angular RxJS pipeable operators (như `debounceTime(300)`) vào thanh Search Bar để User gõ chữ từ từ sau 0.3 giây mới gọi API tìm Vợt, giảm tải Backend.

* **Thư mục `cart/` & `payment/` -> `features/checkout/`**
  * `vnpay_return.jsp` -> `payment-result/payment-result.component.ts`: Dùng module Router lấy params VNPay ra (qua `ActivatedRoute`), gửi lên Backend kiểm tra chuẩn xác, sau đó bật NgIf báo kết quả cho User đặt được sân cầu hay chưa.
