# Giai đoạn B1: Chuyển đổi thành RESTful API Chuyên Sâu

Để biến ứng dụng hiện tại thành API tiêu chuẩn, làm theo từng bước chi tiết sau:

## 1. Chuẩn hóa Định dạng Payload Trả Về (API Response)
Để Frontend xử lý lỗi và dữ liệu dễ dàng, bạn cần mọi API trả về đúng 1 cấu trúc chuẩn, thay vì lúc trả kiểu này, lúc trả kiểu khác.
Tạo class `ApiResponse.java` trong `com.example.quanly.domain.dto`:

```java
package com.example.quanly.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private int status;       // HTTP Status: 200, 400, 404, 500
    private String message;   // Thông điệp: "Thành công", "Không tìm thấy"
    private T data;           // Nội dung kết quả trả về
}
```

## 2. Viết Global Exception Handler
Hiện tại khi có lỗi, Spring nhảy tới trang `error.jsp`. Cần đổi nó thành JSON Exception handler chặn toàn bộ lỗi.
Tạo `GlobalExceptionHandler.java` trong `com.example.quanly.controller`:

```java
import com.example.quanly.domain.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleAllExceptions(Exception ex) {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Lỗi hệ thống: " + ex.getMessage())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    // Bổ sung các lỗi tuỳ chỉnh như ResourceNotFoundException tại đây
}
```

## 3. Quy trình Refactor 1 Controller Điển hình
Xóa bỏ thư mục `src/main/webapp/WEB-INF/view`. Xóa toàn bộ biến `Model model`, `@ModelAttribute` cũ.
Ví dụ đối với `BookingController`:

**Bước 3.1:** Ký hiệu dòng đầu Controller.
```java
@RestController
@RequestMapping("/api/v1/booking")
// @RequiredArgsConstructor // Dùng lombok thay cho việc gõ @Autowired tay
public class BookingApiController {
    private final BookingService bookingService;
```

**Bước 3.2:** Fix các Request xử lý tạo.
```java
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<BookingDTO>> createBooking(@RequestBody BookingRequest request) {
        BookingDTO newBooking = bookingService.createBooking(request); // Service tự xử
        
        return ResponseEntity.ok(ApiResponse.<BookingDTO>builder()
            .status(200)
            .message("Tạo Booking thành công")
            .data(newBooking)
            .build());
    }
```

## 4. Xóa Dependencies Dư Thừa Mảng Frontend Cũ
Mở lệnh `pom.xml`, xóa đi toàn bộ các thư viện JSP sau khi refactor API xong:
- `tomcat-embed-jasper`
- `jakarta.servlet.jsp.jstl-api`
- `jakarta.servlet.jsp.jstl`
- `jstl`

Mở file `application.properties`, xóa các config rác liên quan tới MVC view suffix (bão đảm nó đã được xóa).
