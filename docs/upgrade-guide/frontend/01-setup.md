# Giai đoạn F1: Khởi tạo Dàn Giáo Frontend (Angular)

Vì Backend đã được định tuyến lại bằng các JSON Endpoints nên bạn cần xây một giao diện ứng dụng phía Client theo chuẩn doanh nghiệp. Sử dụng **Angular** sẽ mang lại sự đồng bộ và cấu trúc module hóa cực độ.

## 1. Khởi tạo Project Angular
Đứng bên ngoài thư mục `do_an` của Backend. Mở Terminal lên gõ:
```bash
# Cài đặt công cụ CLI của Angular nếu chưa có
npm install -g @angular/cli

# Khởi tạo App dùng kiến trúc Standalone mới nhất của Angular, cấu hình SCSS và Routing
ng new quanly-frontend --standalone --routing --style=scss
cd quanly-frontend
```

## 2. Cài Đặt Bootstrap & NgBootstrap
Để thay thế hệ thống form & view tĩnh cũ của BootStrap JSP, chúng ta sử dụng `ng-bootstrap`. Đây là phiên bản Bootstrap chuyên dụng và sạch nhất dành cho Angular (Loại bỏ hoàn toàn jQuery nặng nề).
```bash
ng add @ng-bootstrap/ng-bootstrap
```
Sau đó, thư viện sẽ tự động cấu hình file `angular.json` để nhúng sẵn Bootstrap CSS. Bạn có thể chèn các class như `d-flex`, `container`, `row`, `col-12` thẳng vào mã giao diện như truyền thống.

## 3. Kiến Trúc Thư Mục Tiêu Chuẩn Trong Angular
Cấu trúc dưới `src/app/` nên tuân thủ Clean Architecture của Angular:

```
src/app/
├── core/            # Chứa Singleton Services, Interceptors (AuthInterceptor), Guards
│   ├── interceptors/
│   ├── guards/      # (VD: AuthGuard để bảo vệ tuyến đường Admin)
│   └── services/    # HttpClient call tới Spring Boot API
├── shared/          # Components xài chung, UI tĩnh, Pipes, Directives (Navbar, Footer, Button Bootstrap)
├── features/        # Phân chia theo nghiệp vụ (Booking, Auth, Shop)
│   ├── auth/        
│   │   ├── pages/login/login.component.ts
│   │   └── services/auth.service.ts
│   ├── booking/     
│   └── admin/       
├── layouts/         # Các khung xương bọc component con
│   ├── client-layout/
│   └── admin-layout/
└── app.routes.ts    # File điều hướng chính
```

## 4. Bảo Vệ Tuyến Đường (Auth Guard)
Tạo tệp `auth.guard.ts` bằng lệnh `ng g g core/guards/auth` để chặn User chưa đăng nhập.
```typescript
import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const token = localStorage.getItem('ACCESS_TOKEN');
  
  if (token) return true;
  
  router.navigate(['/login']);
  return false;
};
```
Trong `app.routes.ts`:
```typescript
import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { ClientLayoutComponent } from './layouts/client-layout/client-layout.component';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./features/auth/pages/login/login.component').then(m => m.LoginComponent) },
  { 
    path: '', 
    component: ClientLayoutComponent, 
    children: [
      { path: 'book-court', loadComponent: () => import('./features/booking/pages/booking.component').then(m => m.BookingComponent), canActivate: [authGuard] }
    ] 
  }
];
```
