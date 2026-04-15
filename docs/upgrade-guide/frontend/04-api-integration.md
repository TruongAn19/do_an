# Giai đoạn F4: Kết Nối API Giữa Angular Và Spring Boot (Trùng Khớp 100%)

Với Angular, thay vì dùng Axios, chúng ta sử dụng `HttpClient` được tích hợp sẵn của framework. Đây là cách cấu hình để nối ống nước (Pipeline) tự lấy JWT cho Backend đã dựng ở bước `01b-api-service-mapping.md`.

## 1. Thiết Lập Http Interceptor Bơm Token (Thay cho Axios Interceptor)
Tạo tệp: `ng g interceptor core/interceptors/auth`
Bộ phận này chạy tự động mỗi khi Angular gọi tới API Spring Boot. Nó chọc vào vùng nhớ, rút cái Token kẹp vô Header để qua ải tường bảo mật do JwtFilter (của Backend) canh giữ.

**`auth.interceptor.ts`**
```typescript
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const token = localStorage.getItem('ACCESS_TOKEN');

  // Gắn token
  const authReq = token 
      ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) 
      : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // Bị Backend trả mã 401 thì lôi cổ về trang bắt đăng nhập lại
      if (error.status === 401) {
        localStorage.removeItem('ACCESS_TOKEN');
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
```
Khai báo nó tại `app.config.ts`:
```typescript
export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])) // Bổ sung cỗ máy này
  ]
};
```

## 2. Bản Đồ Cây API Service Của Angular (Map sát với 01b)
Bóc tách các Data Service ứng với Bảng Map "01b-api-service-mapping".

Tạo Service API: `ng g s core/services/booking-api`
**`booking-api.service.ts` (Lõi quan trọng nhất)**
```typescript
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class BookingApiService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api/v1';

  // Lấy danh sách sân trống dựa vào lớp ApiResponse<T>
  getAvailableTimes(date: string, courtId: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/client/bookings/available-times`, { 
        params: { date, court: courtId } 
    }).pipe(map(response => response.data)); // Trích xuất mảng data khỏi Wrapper
  }

  holdCourt(payload: any) {
    return this.http.post(`${this.baseUrl}/client/bookings/hold`, payload);
  }

  placeBooking(payload: any) {
    return this.http.post(`${this.baseUrl}/client/bookings/place`, payload);
  }

  getAdminBookings() {
    return this.http.get<any>(`${this.baseUrl}/admin/bookings`)
             .pipe(map(res => res.data));
  }
}
```

Tạo Service API: `ng g s core/services/auth-api`
**`auth-api.service.ts`**
```typescript
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private http = inject(HttpClient);
  
  login(payload: any) {
    return this.http.post<any>('http://localhost:8080/api/v1/auth/login', payload)
       .pipe(map(res => res.data)); // Lấy trực tiếp cục chúa Chuỗi Token { jwt: ""...}
  }
}
```

Bằng cách dùng công nghệ `Observable/RxJS` đặc hữu của Angular dỡ bỏ lớp Vỏ kén (Wrapper `ApiResponse<T>`) ở giữa, lớp View của Giao diện chỉ cần Data tinh khiết. Mọi thứ trở nên mượt mà, cấu trúc cứng ngắc không ai phá vỡ được. Màn lột xác vĩ đại kết thúc thành công.
