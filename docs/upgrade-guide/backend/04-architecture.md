# Giai đoạn B4: Kiến Trúc Clean code phân lớp và Mapstruct

Vì hệ thống không còn Server Side Render, việc bạn trả về một Object `User` hay `Booking` trực tiếp cho HTTP Endpoint sẽ dễ dãi trả luôn các trường nhạy cảm như Mật khẩu `password`, Role ID, hoặc sinh ra vòng lặp vô hạn `Booking -> User -> Bookings ...`. Việc ánh xạ Object DTO là tối quan trọng nhằm che dấu thiết kế CSDL.

## Áp Dụng Mapstruct Tự Động Hóa Code
Bạn không cần code `userDto.setName(user.getName())`, Mapstruct sẽ biên dịch và viết ra nó lúc build java.

1. Bật `mapstruct` và thêm `mapstruct-processor` ở Maven Compiler Plugin. 
2. Luân chuyển như sau:
   - Request JSON -> `Controller` nắm lấy định dạng In-DTO
   - Truyền In-DTO vào `Service`
   - `Service` gọi Mapstruct (với `target=Entity`) biến In-DTO thành `Entity` để Repository save vào DB.
   - Khi load dữ liệu: `Service` lấy `Entity` từ Repository -> Dùng Mapstruct parse về `Out-DTO` và đi ngược qua `Controller`.

```java
@Mapper(componentModel = "spring")
public interface BookingMapper {
    @Mapping(target = "userEmail", source = "user.email")
    BookingResponseDTO toDto(Booking entity);
}
```

Nhờ quy trình này, vòng lặp vô hạn bị phá bỏ, Backend trả về Response tốc độ siêu cao vì JSON rất nhẹ, không thừa trường rác. Trang trí các logic nghiệp vụ bằng Annotation Validtation ngay ở Class DTO.
