# Báo cáo đối chiếu logic, dữ liệu và API giữa Frontend - Backend

Ngày kiểm tra: 2026-07-27

## 1. Phạm vi và nguyên tắc kiểm tra

Báo cáo này được lập từ mã nguồn hiện tại của hai project:

- Backend: `BACKEND_ROOT/src/main`
- Frontend: `FRONTEND_ROOT/src/app`
- Migration dữ liệu: `BACKEND_ROOT/src/main/resources/db/migration`

Quy ước đường dẫn:

- `BACKEND_ROOT` là thư mục gốc backend, được nhận diện bởi file `pom.xml`.
- `FRONTEND_ROOT` là thư mục gốc frontend, được nhận diện bởi file `angular.json` và `package.json`.
- Hai thư mục có thể mang bất kỳ tên nào và có thể nằm ở các vị trí khác nhau trên từng máy.
- Các đường dẫn trong báo cáo bắt đầu từ một trong hai thư mục gốc trên, không phải đường dẫn tuyệt đối và không phụ thuộc tên thư mục project.

Nguyên tắc:

- Chỉ ghi nhận lỗi khi có căn cứ từ mã nguồn.
- Phân biệt rõ dữ liệu nghiệp vụ hợp lệ, dữ liệu demo, dữ liệu giao diện tự suy đoán và dữ liệu test.
- Một API được đánh dấu "chưa được FE sử dụng" khi không có component hoặc service đang được component gọi tới endpoint đó.
- Không kết luận dữ liệu nào đang tồn tại trong database thực tế vì trong lúc kiểm tra không kết nối được database.
- VNPay sandbox không được xem là mock hoặc dữ liệu giả vì project đang chủ động sử dụng môi trường sandbox.

## 2. Tóm tắt mức độ ưu tiên

| Nhóm | Số vấn đề xác nhận | Mức ưu tiên cao nhất |
|---|---:|---|
| Lỗi logic và contract FE-BE | 10 | Nghiêm trọng |
| Dữ liệu bootstrap/demo được xác nhận giữ lại | 2 | Không phải lỗi dữ liệu |
| Giá trị fallback hoặc tự suy đoán cần xử lý | 6 | Cao |
| FE gọi API không tồn tại trong BE | 8 endpoint | Cao |
| API BE chưa được màn hình FE sử dụng | 14 endpoint | Trung bình |

## 3. Lỗi logic và sai contract FE-BE

### LOGIC-01 - Form thiết bị gửi thiếu trường và backend ghi đè dữ liệu hiện tại

**Mức độ: Nghiêm trọng**

Frontend chỉ gửi các trường:

- `name`
- `factory`
- `price`
- `status`
- `rentalPricePerPlay`

Bằng chứng:

- `FRONTEND_ROOT/src/app/features/admin/equipments/equipments.ts:34-41`
- `FRONTEND_ROOT/src/app/features/admin/equipments/equipments.ts:94-111`

Trong khi entity backend còn có:

- `available`
- `rentalPricePerDay`
- `bookingStockQuantity`
- `quantity`
- `product`

Bằng chứng:

- `BACKEND_ROOT/src/main/java/com/pitchbooking/app/domain/Equipment.java:22-42`

Khi cập nhật, backend lấy toàn bộ các trường trên từ request rồi ghi đè vào entity hiện tại:

- `BACKEND_ROOT/src/main/java/com/pitchbooking/app/controller/admin/EquipmentController.java:76-85`

Các trường không có trong JSON sẽ giữ giá trị khởi tạo của object request: các trường số là `0`, `product` là `null`, còn `available` được entity khởi tạo là `true`. Việc sửa tên hoặc giá thiết bị có thể làm mất số lượng tồn, giá thuê theo ngày, liên kết sân và có thể đổi một thiết bị đang không khả dụng thành khả dụng.

Khi tạo mới, cùng form thiếu trường cũng có thể tạo thiết bị với `quantity=0`, `bookingStockQuantity=0`, `rentalPricePerDay=0` và `product=null`.

**Đề xuất:** dùng DTO riêng cho create/update; frontend phải gửi đủ trường bắt buộc; update chỉ thay đổi trường được phép và không ghi đè trường không có trong request.

### LOGIC-02 - Cập nhật sân bằng entity mới có thể làm mất quan hệ hiện có

**Mức độ: Nghiêm trọng**

Backend không tải entity `Product` hiện tại để sửa mà tạo một đối tượng mới, gán lại `id`, sau đó lưu:

- `BACKEND_ROOT/src/main/java/com/pitchbooking/app/controller/admin/ProductController.java:82-117`

Entity `Product` còn có các quan hệ `user`, `availableTimes` và `equipments`:

- `BACKEND_ROOT/src/main/java/com/pitchbooking/app/domain/Product.java:45-59`

Đối tượng mới không được sao chép `user` và `availableTimes`. Khi merge qua JPA, `user_id` có thể bị đưa về `null`; dữ liệu quan hệ khung giờ cũng có nguy cơ không còn đúng với bản ghi trước khi sửa.

**Đề xuất:** gọi `productService.getRawProductById(productId)`, sửa trực tiếp entity đang được quản lý và chỉ cập nhật các trường từ DTO.

### LOGIC-03 - Khung giờ trả về là toàn hệ thống, không phải khung giờ của sân

**Mức độ: Cao**

Ba luồng đang dùng `timeRepository.findAll()` hoặc `productService.getAllTime()`:

- Chi tiết sản phẩm: `BACKEND_ROOT/src/main/java/com/pitchbooking/app/controller/client/ItemController.java:65-69`
- Thông tin đặt sân: `BACKEND_ROOT/src/main/java/com/pitchbooking/app/controller/client/BookingClientController.java:93-108`
- Khung giờ còn trống theo sân con: `BACKEND_ROOT/src/main/java/com/pitchbooking/app/controller/client/BookingClientController.java:114-150`
- Hàm nguồn: `BACKEND_ROOT/src/main/java/com/pitchbooking/app/service/ProductService.java:223-225`

Frontend lại hiển thị danh sách này là "Khung giờ được cấu hình":

- `FRONTEND_ROOT/src/app/features/products/detail/detail.html:58-73`

Do đó API có thể trả khung giờ không thuộc `product.availableTimes` hoặc không được cấu hình cho `SubPitch` đang chọn.

**Đề xuất:** lấy giờ từ quan hệ của product/sub-pitch, sau đó mới trừ các giờ đã đặt hoặc đang giữ.

### LOGIC-04 - Frontend tự tính tiền hoàn trước khi API xác nhận

**Mức độ: Cao**

Lịch sử đặt sân dùng toàn bộ `depositPrice` làm tiền hoàn dự kiến:

- `FRONTEND_ROOT/src/app/features/booking-history/booking-history.ts:66-82`

Chi tiết đặt sân tự gán `bookingType` thiếu thành `ONE_TIME`, và với lịch định kỳ cũng dùng toàn bộ tiền cọc:

- `FRONTEND_ROOT/src/app/features/booking-detail/booking-detail.ts:63-107`

Backend mới là nguồn tính tiền hoàn chính xác, bao gồm:

- Chặn hủy đặt một lần trong vòng hai giờ.
- Tính số buổi đã dùng/chưa dùng của lịch định kỳ.
- Trả `refundAmount`, `usedSessions`, `totalSessions` trong `CancelBookingResponse`.

Bằng chứng response:

- `BACKEND_ROOT/src/main/java/com/pitchbooking/app/domain/dto/CancelBookingResponse.java`
- `BACKEND_ROOT/src/main/java/com/pitchbooking/app/controller/client/BookingClientController.java:358-415`

**Đề xuất:** không hiển thị số tiền dự đoán nếu backend chưa có API preview. Chỉ hiển thị số tiền chính thức từ response hủy, hoặc bổ sung endpoint preview dùng chung logic với `cancelByUser`.

### LOGIC-05 - FE gửi sai contract của API Ntfy

**Mức độ: Cao**

Backend yêu cầu request parameters:

- `topic`
- `message`
- `title` không bắt buộc

Bằng chứng:

- `BACKEND_ROOT/src/main/java/com/pitchbooking/app/controller/admin/NtfyController.java:38-47`

Frontend chỉ gửi một chuỗi message trong body:

- `FRONTEND_ROOT/src/app/features/admin/ntfy/ntfy.ts:68-76`

Request hiện tại không cung cấp hai `@RequestParam` bắt buộc, nên endpoint có thể trả HTTP 400.

**Đề xuất:** gửi `HttpParams` chứa `topic`, `message`, `title`, hoặc đổi backend sang nhận một DTO JSON thống nhất.

### LOGIC-06 - Frontend chứa cả tính năng ghép kèo nhưng backend không có API

**Mức độ: Cao**

`MatchService` gọi các endpoint `/match-posts` và `/chat/history`:

- `FRONTEND_ROOT/src/app/core/services/match.service.ts:15-50`

Không có controller backend nào khai báo các endpoint này. Đồng thời `app.routes.ts` không đăng ký route cho các màn:

- `match-posts`
- `match-post-detail`
- `match-post-create`

Bằng chứng danh sách route hiện tại:

- `FRONTEND_ROOT/src/app/app.routes.ts:6-140`

Tuy nhiên trang đăng nhập vẫn quảng bá "ghép kèo":

- `FRONTEND_ROOT/src/app/features/auth/login/login.html:14`

**Đề xuất:** xóa toàn bộ service/màn hình/claim ghép kèo nếu không thuộc phạm vi project, hoặc triển khai backend và đăng ký route trước khi hiển thị.

### LOGIC-07 - Bộ lọc thuê ghi tìm theo tên nhưng backend chỉ tìm theo mã đơn

**Mức độ: Trung bình**

Frontend ghi "Tên người thuê/Mã đơn":

- `FRONTEND_ROOT/src/app/features/admin/rentals/rentals.html:12`

Backend chỉ gọi `findByRentalToolCodeContaining`:

- `BACKEND_ROOT/src/main/java/com/pitchbooking/app/service/RentalToolService.java:61-63`
- `BACKEND_ROOT/src/main/java/com/pitchbooking/app/repository/RentalToolRepository.java:70`

**Đề xuất:** sửa nhãn thành "Mã đơn", hoặc mở rộng repository để tìm cả `fullName` và `rentalToolCode`.

### LOGIC-08 - Frontend tự giới hạn giá sân tối đa 2.000.000 đồng

**Mức độ: Trung bình**

Giá trị `2_000_000` được gắn mặc định và gửi lên API ngay khi tải trang:

- `FRONTEND_ROOT/src/app/features/products/products.ts:18-23`
- `FRONTEND_ROOT/src/app/features/products/products.ts:83-89`
- `FRONTEND_ROOT/src/app/features/products/products.html:47`

Backend không có quy tắc nghiệp vụ giới hạn giá sân ở mức này. Vì `price` luôn có giá trị, sản phẩm đắt hơn 2.000.000 đồng sẽ bị loại khỏi kết quả mặc định.

**Đề xuất:** mặc định `price=null`, chỉ gửi `price` khi người dùng chủ động chọn; giới hạn slider phải lấy từ cấu hình hoặc API thống kê nếu cần.

### LOGIC-09 - Quy tắc độ dài mật khẩu không đồng nhất

**Mức độ: Trung bình**

Frontend yêu cầu tối thiểu 6 ký tự:

- `FRONTEND_ROOT/src/app/features/auth/register/register.ts:19-26`
- `FRONTEND_ROOT/src/app/features/auth/reset-password/reset-password.ts:20-23`

Backend đăng ký chỉ yêu cầu tối thiểu 3 ký tự:

- `BACKEND_ROOT/src/main/java/com/pitchbooking/app/domain/dto/RegisterDTO.java:17-20`
- `BACKEND_ROOT/src/main/java/com/pitchbooking/app/domain/User.java:29-32`

Endpoint reset password lấy chuỗi trực tiếp từ body và không có kiểm tra độ dài:

- `BACKEND_ROOT/src/main/java/com/pitchbooking/app/controller/client/ForgotPasswordController.java:78-89`

**Đề xuất:** định nghĩa một quy tắc duy nhất ở backend và mirror chính xác ở frontend.

### LOGIC-10 - Contract model frontend không khớp hoàn toàn response backend

**Mức độ: Trung bình**

Các điểm đã xác nhận:

- FE khai báo `EquipmentDetail.description`, nhưng entity/API equipment không có trường `description`.
- FE khai báo `EquipmentStockByDate.availableCount`, nhưng backend trả `availableStock`.
- FE `ProductResponseDTO` dùng `description`, `imageUrl`, `discountPrice`; backend dùng `detailDesc`, `shortDesc`, `image`, `depositPrice`.
- Nhiều service trả `Observable<any>` và component đọc song song `res.data?.x || res.x`, làm che giấu contract sai.

Bằng chứng:

- `FRONTEND_ROOT/src/app/core/models/equipment.model.ts:11-30`
- `FRONTEND_ROOT/src/app/core/models/product.model.ts:1-13`
- `BACKEND_ROOT/src/main/java/com/pitchbooking/app/domain/Equipment.java:13-43`
- `BACKEND_ROOT/src/main/java/com/pitchbooking/app/domain/EquipmentStockByDate.java:25`
- `BACKEND_ROOT/src/main/java/com/pitchbooking/app/domain/dto/ProductResponseDTO.java`

Hiện một số component tự map thủ công nên chưa phải tất cả đều gây lỗi runtime, nhưng model không còn phản ánh đúng API.

**Đề xuất:** tạo model đúng theo DTO backend, bỏ `any`, và chuẩn hóa việc unwrap duy nhất qua `ApiResponse<T>`.

## 4. Phân loại dữ liệu bootstrap, demo và fallback

### ACCEPTED-01 - Bootstrap admin được xác nhận giữ lại

**Kết luận: Không phải lỗi dữ liệu**

`@PostConstruct` tự tạo:

- Email: `admin@gmail.com`
- Mật khẩu: `123456`
- Số điện thoại: `0963931420`
- Địa chỉ: `Hà Nội`
- Ba role mới

Bằng chứng:

- `BACKEND_ROOT/src/main/java/com/pitchbooking/app/service/UserService.java:32-51`

Việc tạo tài khoản admin đầu tiên là hợp lý: nếu database mới hoàn toàn không có admin thì không thể truy cập các trang quản trị. Cơ chế bootstrap này được xác nhận là dữ liệu khởi tạo có chủ đích và phải được giữ lại.

Lưu ý bảo mật riêng, không phải lỗi dữ liệu demo: thông tin bootstrap đang hardcode trong source, dùng mật khẩu yếu và tạo lại cả ba role khi không tìm thấy đúng email `admin@gmail.com`. Nếu role đã tồn tại nhưng email này chưa tồn tại, đoạn code có thể lưu role trùng hoặc vi phạm ràng buộc unique.

**Đề xuất nếu triển khai ra môi trường production:**

- Giữ cơ chế bootstrap nhưng kiểm tra hệ thống đã có bất kỳ user mang role `ADMIN` hay chưa, thay vì kiểm tra một email cố định.
- Đưa email, mật khẩu và thông tin admin ban đầu vào biến môi trường bắt buộc.
- Tạo role bằng migration dữ liệu tham chiếu hoặc dùng `findByName` trước khi tạo, không lưu role mới một cách mặc định.
- Bắt buộc đổi mật khẩu sau lần đăng nhập đầu tiên nếu domain hiện tại được bổ sung cờ tương ứng.
- Cho phép bật/tắt bootstrap bằng cấu hình theo môi trường; không dùng thông tin hardcode trong production.

### ACCEPTED-02 - Seed demo Flyway được xác nhận giữ lại

**Kết luận: Không phải lỗi dữ liệu**

`V4__seed_data.sql` chèn:

- User demo như `admin@antigravity.vn`, `staff@antigravity.vn`, `alice@gmail.com`, `bob@gmail.com`.
- Hai cụm sân mẫu, địa chỉ mẫu, ảnh Unsplash và mô tả phục vụ demo.
- Sân con và liên kết khung giờ mẫu.
- Thiết bị mẫu.

Bằng chứng:

- `BACKEND_ROOT/src/main/resources/db/migration/V4__seed_data.sql:17-36`
- `BACKEND_ROOT/src/main/resources/db/migration/V4__seed_data.sql:55-80`
- `BACKEND_ROOT/src/main/resources/db/migration/V4__seed_data.sql:87-146`

`V5__seed_equipment_stock.sql` tạo tồn kho bảy ngày cho các thiết bị mẫu:

- `BACKEND_ROOT/src/main/resources/db/migration/V5__seed_equipment_stock.sql:11-68`

Các bản ghi này bám theo chức năng hiện có của project và giúp database mới có đủ dữ liệu để chạy thử luồng sân, sân con, khung giờ, thiết bị và tồn kho. Theo xác nhận nghiệp vụ, không xóa `V4__seed_data.sql` hoặc `V5__seed_equipment_stock.sql`.

Role và danh mục khung giờ là dữ liệu tham chiếu. User, sân và thiết bị trong hai migration là dữ liệu demo có chủ đích.

**Các giá trị còn cần sửa bắt đầu từ `DATA-03` vì chúng được FE tự suy đoán hoặc không có căn cứ từ API.**

### DATA-03 - Tiền hoàn và loại booking được FE tự suy đoán

**Mức độ: Cao**

- `refundAmount = depositPrice`
- Thiếu `bookingType` thì tự gán `ONE_TIME`
- Lịch định kỳ tự hiển thị `usedSessions=0`, `totalSessions=0`

Bằng chứng:

- `FRONTEND_ROOT/src/app/features/booking-history/booking-history.ts:74-80`
- `FRONTEND_ROOT/src/app/features/booking-detail/booking-detail.ts:75-88`

Các giá trị này không phải response xác nhận tiền hoàn từ API.

### DATA-04 - Trạng thái thiếu bị hiển thị thành trạng thái có thật

**Mức độ: Trung bình**

Các fallback:

- Rental thiếu status bị hiển thị thành "Chờ thanh toán".
- Equipment thiếu status bị hiển thị/điền form thành `ACTIVE`.

Bằng chứng:

- `FRONTEND_ROOT/src/app/features/admin/rentals/rentals.ts:34-37`
- `FRONTEND_ROOT/src/app/features/admin/equipments/equipments.ts:80-85`
- `FRONTEND_ROOT/src/app/features/admin/equipments/equipments.html:49`

Điều này biến dữ liệu thiếu thành một trạng thái nghiệp vụ không được API xác nhận.

### DATA-05 - Giá trị mặc định khi sửa sân có thể che dữ liệu API thiếu

**Mức độ: Trung bình**

Khi mở form sửa:

- Thiếu `quantity` thì gán `1`.
- Thiếu `pitchType` thì gán `FIVE_ASIDE`.
- Thiếu `sale` thì gán `0`.

Bằng chứng:

- `FRONTEND_ROOT/src/app/features/admin/products/products.ts:181-197`

Các giá trị mặc định hợp lý khi tạo mới, nhưng không nên dùng để thay dữ liệu thiếu trong chế độ sửa.

### DATA-06 - Topic Ntfy được tự đặt thành `admin-alerts`

**Mức độ: Thấp**

Bằng chứng:

- `FRONTEND_ROOT/src/app/features/admin/ntfy/ntfy.ts:18`

Backend không cung cấp topic mặc định này qua API hoặc cấu hình trả về cho FE.

### DATA-07 - Giao diện đăng nhập hiển thị chức năng chưa được triển khai

**Mức độ: Trung bình**

Các phần không có luồng xử lý tương ứng:

- "Ghi nhớ đăng nhập": checkbox không có binding.
- "Tiếp tục với Google": button không có click handler và backend không có OAuth endpoint.
- "Ghép kèo": backend không có API.

Bằng chứng:

- `FRONTEND_ROOT/src/app/features/auth/login/login.html:14`
- `FRONTEND_ROOT/src/app/features/auth/login/login.html:38`
- `FRONTEND_ROOT/src/app/features/auth/login/login.html:49`

### DATA-08 - Cấu hình mặc định vẫn chứa giá trị dummy

**Mức độ: Trung bình**

Các placeholder còn tồn tại:

- `dummy@gmail.com`
- `dummy_password`
- `dummy_public_key_for_vapid...`
- `dummy_private_key_for_vapid...`

Bằng chứng:

- `BACKEND_ROOT/src/main/resources/application.properties:44-50`

Đây là placeholder cấu hình, không phải dữ liệu nghiệp vụ. Tuy nhiên ứng dụng có thể khởi động với cấu hình giả thay vì báo thiếu biến môi trường.

## 5. API được FE gọi nhưng backend không tồn tại

Toàn bộ các endpoint sau nằm trong `FRONTEND_ROOT/src/app/core/services/match.service.ts` nhưng không có controller backend tương ứng:

| Method | Endpoint |
|---|---|
| GET | `/api/v1/match-posts` |
| GET | `/api/v1/match-posts/{id}` |
| POST | `/api/v1/match-posts` |
| POST | `/api/v1/match-posts/{id}/cancel` |
| POST | `/api/v1/match-posts/{id}/join` |
| POST | `/api/v1/match-posts/{id}/leave` |
| POST | `/api/v1/match-posts/{postId}/kick/{userId}` |
| GET | `/api/v1/chat/history/{chatRoomId}` |

Các feature `match-posts`, `match-post-detail`, `match-post-create` cũng không có route trong `FRONTEND_ROOT/src/app/app.routes.ts`.

## 6. API backend chưa được màn hình frontend sử dụng

### 6.1 API hoàn toàn chưa có lời gọi FE

| Method | Endpoint backend | Vị trí backend | Ghi chú |
|---|---|---|---|
| GET | `/api/v1/admin/users/{userId}` | `BACKEND_ROOT/src/main/java/com/pitchbooking/app/controller/admin/UserController.java:49` | FE chỉ tải toàn bộ danh sách |
| GET | `/api/v1/admin/products/{productId}` | `BACKEND_ROOT/src/main/java/com/pitchbooking/app/controller/admin/ProductController.java:75` | Form sửa dùng object từ danh sách |
| GET | `/api/v1/admin/equipments/{equipmentId}` | `BACKEND_ROOT/src/main/java/com/pitchbooking/app/controller/admin/EquipmentController.java:43` | Không nhầm với public `/equipments/{id}` đang được dùng |
| GET | `/api/v1/admin/bookings/{id}` | `BACKEND_ROOT/src/main/java/com/pitchbooking/app/controller/admin/BookingController.java:71` | Chưa có màn chi tiết booking admin |
| DELETE | `/api/v1/admin/bookings/{id}` | `BACKEND_ROOT/src/main/java/com/pitchbooking/app/controller/admin/BookingController.java:88` | Chưa có action FE |
| GET | `/api/v1/admin/rentals/{id}` | `BACKEND_ROOT/src/main/java/com/pitchbooking/app/controller/admin/RentalToolController.java:52` | Chưa có màn chi tiết rental admin |
| GET | `/api/v1/client/booking-history/{id}` | `BACKEND_ROOT/src/main/java/com/pitchbooking/app/controller/client/HomePageController.java:120` | FE dùng `/client/bookings/detail/{id}` thay thế |
| GET | `/api/v1/client/bookings/{bookingCode}/{courtId}/equipments` | `BACKEND_ROOT/src/main/java/com/pitchbooking/app/controller/client/BookingClientController.java:339` | Không có wrapper/call FE |
| DELETE | `/api/v1/client/bookings/{bookingId}` | `BACKEND_ROOT/src/main/java/com/pitchbooking/app/controller/client/BookingClientController.java:417` | Endpoint legacy; FE dùng POST `/cancel` |

### 6.2 Có wrapper trong service FE nhưng không component nào gọi

| Method | Endpoint backend | Wrapper FE | Ghi chú |
|---|---|---|---|
| POST | `/api/v1/admin/users` | `FRONTEND_ROOT/src/app/core/services/admin.service.ts:32` (`createUser`) | Màn users chỉ đổi role và xóa |
| PUT | `/api/v1/admin/users/{userId}` | `FRONTEND_ROOT/src/app/core/services/admin.service.ts:36` (`updateUser`) | Màn users chưa có form sửa thông tin |
| GET | `/api/v1/client/bookings/recommend/{productId}` | `FRONTEND_ROOT/src/app/core/services/booking.service.ts:29` (`getRecommendSlots`) | Màn booking không gọi gợi ý |

### 6.3 API mock payment không được FE sử dụng

| Method | Endpoint backend | Vị trí |
|---|---|---|
| GET | `/api/v1/mock-payment` | `BACKEND_ROOT/src/main/java/com/pitchbooking/app/controller/MockPaymentController.java:29` |
| GET | `/api/v1/mock-payment/confirm` | `BACKEND_ROOT/src/main/java/com/pitchbooking/app/controller/MockPaymentController.java:78` |

Controller này chỉ được bật khi `payment.mock.enabled=true`:

- `BACKEND_ROOT/src/main/java/com/pitchbooking/app/controller/MockPaymentController.java:24`

Project hiện dùng VNPay sandbox nên controller mock không cần cho luồng chính. Không được xóa VNPay sandbox hoặc thay bằng mock.

## 7. API đã được FE sử dụng đúng hoặc có căn cứ backend

Các nhóm sau đã có lời gọi FE và endpoint backend tương ứng:

- Đăng nhập, đăng xuất, đăng ký, quên và đặt lại mật khẩu.
- Danh sách/chi tiết sân và thiết bị.
- Kiểm tra tồn kho thiết bị.
- Giữ chỗ, ước tính giá, tạo booking và callback VNPay.
- Tạo đơn thuê, thanh toán VNPay/CASH, hủy và lịch sử thuê.
- Lịch sử booking, chi tiết booking và hủy booking.
- Profile và đổi mật khẩu.
- Dashboard, thống kê doanh thu và thống kê thiết bị.
- Quản lý sân, sân con, thiết bị, booking, rental và hoàn cọc.
- Notification client/admin qua HTTP và STOMP.
- AI chat qua `/api/v1/ai/chat`.
- Ntfy SSE có endpoint tương ứng; riêng POST gửi thông báo đang sai contract như `LOGIC-05`.

Các giá trị sau có căn cứ backend và không được xem là tự dựng:

- Tiền cọc 50%.
- Giảm giá booking định kỳ.
- Phụ thu giờ cao điểm/cuối tuần.
- Thời hạn giữ sân.
- Điều kiện hủy trước hai giờ.
- Các enum trạng thái booking/rental/refund.
- Bộ cấu hình VNPay sandbox.

## 8. Vấn đề mã hóa tiếng Việt

Nhiều chuỗi trong cả FE và BE đang ở dạng mojibake như `KhÃ´ng`, `ÄÃ£`, `Chá»`. Ví dụ:

- `BACKEND_ROOT/src/main/java/com/pitchbooking/app/service/UserService.java:39`
- `BACKEND_ROOT/src/main/java/com/pitchbooking/app/controller/client/BookingClientController.java`
- `FRONTEND_ROOT/src/app/features/booking-detail/booking-detail.ts`
- `FRONTEND_ROOT/src/app/features/admin/rentals/rentals.html`

Đây không phải dữ liệu tự dựng nhưng là lỗi hiển thị và có thể làm hỏng so sánh label tiếng Việt trong enum.

**Đề xuất:** chuẩn hóa toàn bộ source về UTF-8, sau đó ưu tiên trao đổi enum name (`DA_DAT`, `PENDING`) giữa FE-BE thay vì so sánh label tiếng Việt.

## 9. Thứ tự sửa đề xuất

1. Sửa `LOGIC-01` để ngăn mất dữ liệu thiết bị khi create/update.
2. Sửa `LOGIC-02` để không làm mất owner và quan hệ của sân.
3. Không xóa bootstrap admin hoặc dữ liệu demo Flyway; chỉ chuyển thông tin admin hardcode sang cấu hình an toàn nếu triển khai production.
4. Sửa `LOGIC-03` để khung giờ bám theo product/sub-pitch.
5. Bỏ tiền hoàn tự tính ở FE hoặc bổ sung API preview chính thức.
6. Sửa request Ntfy.
7. Quyết định xóa hoàn toàn tính năng ghép kèo hoặc triển khai backend đầy đủ.
8. Đồng bộ model DTO và bỏ dần `any`.
9. Xử lý các API backend chưa được dùng: tích hợp nếu cần nghiệp vụ, hoặc xóa endpoint legacy/dead code sau khi xác nhận.
10. Chuẩn hóa mã hóa tiếng Việt.

## 10. Giới hạn xác minh

- Không kiểm tra được dữ liệu đang có trong MySQL vì Docker daemon không hoạt động và môi trường không có MySQL CLI.
- Không gọi dịch vụ Ntfy hoặc VNPay trong lần audit tài liệu này.
- Danh sách API dựa trên static analysis mã nguồn; không bao gồm lời gọi có thể phát sinh từ client bên ngoài project FE.
- Không đề xuất xóa dữ liệu demo trong `V4`/`V5`; đây là dữ liệu khởi tạo đã được xác nhận cần giữ để project có đủ dữ liệu chạy các chức năng.
