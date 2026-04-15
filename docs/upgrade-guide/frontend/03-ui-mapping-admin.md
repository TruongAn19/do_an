# Giai đoạn F3: Ánh Xạ & Xây Dựng UI Cho Admin (Khung Angular)

Để chuyển bộ quản trị (admin) cũ sang SPA, bạn nên sử dụng hệ sinh thái thư viện giao diện mạnh mẽ nhất cho Angular: **NG-ZORRO (NgZorro)** vì nó có cùng gốc Ant Design mà Backend đang cực kì phù hợp.

## 1. Cài Đặt NgZorro
```bash
ng add ng-zorro-antd
```
Tích chọn cấu hình Sidebar mặc định để nó khởi tạo ra Khung kiến trúc cực xịn.

## 2. Admin Layout (`layouts/admin-layout/`)
Thanh điều hướng Sidebar sẽ nằm cứng bên trái, không giật lag chuyển màu chớp tắt như JSP cũ.
**`admin-layout.component.html`**
```html
<nz-layout class="h-screen app-layout">
  <nz-sider [nzCollapsible]="true" nzTheme="dark">
    <div class="logo font-bold text-white text-center py-4 text-xl">QUẢN LÝ SÂN</div>
    <ul nz-menu nzTheme="dark" nzMode="inline">
      <li nz-menu-item nzMatchRouter>
        <a routerLink="/admin/dashboard"><i nz-icon nzType="dashboard"></i> Dashboard</a>
      </li>
      <li nz-menu-item nzMatchRouter>
        <a routerLink="/admin/booking"><i nz-icon nzType="calendar"></i> Lịch Đặt Sân</a>
      </li>
      <li nz-menu-item nzMatchRouter>
        <a routerLink="/admin/store"><i nz-icon nzType="shopping"></i> Kho Sản Phẩm</a>
      </li>
    </ul>
  </nz-sider>
  <nz-layout>
    <nz-header class="bg-white shadow"></nz-header>
    <nz-content class="m-6 p-6 bg-white rounded-md">
      <router-outlet></router-outlet>
    </nz-content>
  </nz-layout>
</nz-layout>
```

## 3. Bản Đồ Modules Cho Quản Trị
Xóa hoàn toàn kiểu thiết kế "Một hàm chỉnh sửa sinh ra 1 File View". Admin hiện tại chỉ cần xử lý trong 1 page.

* **Thư mục `dashboard/`, `chart/` -> `features/admin/dashboard/`**
  * Nhúng thư viện `ngx-echarts` vào, truyền data lấy từ API (vd: `bookingApi.getStats()`) thẳng vào View Component. Backend trả mảng JSON bao nhiêu số liệu, Chart sẽ vẽ ra thành dạng Cột tự động chứ không cần thư viện Js hỗn tạp như cũ.

* **Thư mục `booking/` -> `features/admin/booking/`**
  * `booking_list.jsp` dùng vòng lặp, nay ta thay bằng Khối `<nz-table [nzData]="bookings">`. Nút cập nhật Trạng Thái (Thanh toán/Hủy) gọi lên Modals (`NzModalService`), mở Form chỉnh sửa mà không load trang mạng.

* **Thư mục `product/`, `racket/`, `rental_manager/` -> `features/admin/store/`**
  * Khối lập Form tải ảnh upload. Chuyển Form lên Backend Spring Boot bằng định dạng `FormData` (chứa các File và nội dung Json).

Với Angular NgZorro Component, bạn tiết kiệm được hàng vạn dòng HTML JS CSS lộn xộn trong JSP cũ. Trang Web quản lý sẽ nhẹ như lông hồng vì trình duyệt load 1 lần, các thao tác quản lý chỉ là gửi ngầm JSON thông qua Ajax (HttpClient).
