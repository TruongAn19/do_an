# HƯỚNG DẪN REBRAND CHI TIẾT: CẦU LÔNG → PICKLEBALL

> Tài liệu đồng hành với [REBRAND_CHECKLIST.md](REBRAND_CHECKLIST.md). Mỗi mục tương ứng 1 đầu việc trong checklist.
>
> **Chiến lược**: Option A (rebrand nội dung, giữ schema). Không refactor class/table/endpoint.
>
> **Ngày scan**: 2026-05-11
> **Project**: Spring Boot 3.4.3 backend REST API (không có view layer)

---

## 📋 MỤC LỤC

1. [SQL seed data (đã xong)](#1-sql-seed-data-đã-xong)
2. [Đổi tên database + container Docker](#2-đổi-tên-database--container-docker)
3. [String Java có ảnh hưởng người dùng / AI](#3-string-java-có-ảnh-hưởng-người-dùng--ai)
4. [Xoá feature Match-Post (tìm đối + chat)](#4-xoá-feature-match-post-tìm-đối--chat)
5. [Comment Javadoc tiếng Việt (optional)](#5-comment-javadoc-tiếng-việt-optional)
6. [Tài liệu trong `doc/`](#6-tài-liệu-trong-doc)
7. [Cleanup file log](#7-cleanup-file-log)
8. [JWT secret default fallback](#8-jwt-secret-default-fallback)
9. [Verify sau khi đổi](#9-verify-sau-khi-đổi)

---

## 1. SQL seed data (đã xong)

### Bối cảnh
File `V4__seed_data.sql` và `V5__seed_racket_stock.sql` đã được cập nhật từ data cầu lông → pickleball.

Các thay đổi:
- **Cụm sân**: `Sân Cầu Lông Antigravity` → `Sân Pickleball Antigravity`, `Elite Arena Badminton` → `Elite Arena Pickleball`, `Pro Center Badminton` → `Pro Center Pickleball`
- **Mô tả**: BWF (badminton federation) → USAPA/IFP/PPA (pickleball federation), "thảm PVC/sàn Taraflex" → "mặt sân acrylic/acrylic cushion/DecoTurf Pro"
- **Giá thuê sân**: 50k/65k/80k → 120k/150k/180k VNĐ/giờ (phù hợp giá pickleball thị trường)
- **Paddle**: 8 paddle thật của các hãng pickleball: Selkirk, Joola, Franklin, Paddletek, ONIX, Engage, Gamma

### Lưu ý quan trọng — Flyway
Nếu DB hiện tại đã chạy V4/V5 trước đó, **Flyway sẽ không re-run** vì version đã có trong `flyway_schema_history`. Có 3 cách xử lý:

**Cách 1 — Tạo database mới (khuyến nghị, sạch nhất):**
```sql
DROP DATABASE IF EXISTS sancaulong1;
CREATE DATABASE sanpickleball1;
```
Sau đó kết hợp với Mục 2 (đổi tên DB) → Flyway tự chạy lại V1→V8.

**Cách 2 — Reset Flyway, giữ database name cũ:**
```sql
USE sancaulong1;
DELETE FROM flyway_schema_history WHERE version IN ('4', '5');
-- Xoá data cũ:
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE racket_stock_by_date;
TRUNCATE TABLE racket;
TRUNCATE TABLE subcourt_available_time;
TRUNCATE TABLE sub_courts;
TRUNCATE TABLE court_time;
TRUNCATE TABLE products;
TRUNCATE TABLE user;
TRUNCATE TABLE roles;
SET FOREIGN_KEY_CHECKS = 1;
```
Restart app → Flyway re-run V4/V5.

**Cách 3 — Manual update bằng SQL UPDATE statement** (không khuyến nghị vì dễ sót).

---

## 2. Đổi tên database + container Docker

### 2.1. `docker-compose.yml`

**File**: [`docker-compose.yml`](../docker-compose.yml)

Mở file và đổi 5 chỗ:

```yaml
services:
  mysql:
    image: mysql:8.0
    container_name: badminton-db          # → pickleball-db
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: sancaulong1          # → sanpickleball1
    ...

  redis:
    image: redis:alpine
    container_name: badminton-redis        # → pickleball-redis
    ...

  backend:
    container_name: badminton-api          # → pickleball-api
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/sancaulong1?...
                                                              ^^^^^^^^^^^
                                                              → sanpickleball1
```

### 2.2. `application.properties`

**File**: [`src/main/resources/application.properties`](../src/main/resources/application.properties)

```properties
# Dòng 8 (comment AWS URL — đổi để consistency):
# spring.datasource.url=jdbc:mysql://database-1.cbqwmi4e6bb9.ap-southeast-2.rds.amazonaws.com/sancaulong1?...
                                                                                              ^^^^^^^^^^^
                                                                                              → sanpickleball1

# Dòng 11 (active URL — BẮT BUỘC đổi):
spring.datasource.url=jdbc:mysql://localhost:3307/sancaulong1?createDatabaseIfNotExist=true
                                                  ^^^^^^^^^^^
                                                  → sanpickleball1
```

> ✅ Tham số `createDatabaseIfNotExist=true` → MySQL tự tạo DB mới nếu chưa có.

### 2.3. Migrate data sang DB mới

Có 2 hướng:

**Hướng 1 — Bắt đầu sạch (recommend nếu là môi trường dev):**
```bash
# 1. Stop app
# 2. Start MySQL nếu chưa chạy
# 3. App tự tạo sanpickleball1 + chạy Flyway V1→V8
mvn spring-boot:run
```

**Hướng 2 — Dump + restore (giữ data cũ):**
```bash
mysqldump -u root -p sancaulong1 > backup.sql
mysql -u root -p -e "CREATE DATABASE sanpickleball1"
mysql -u root -p sanpickleball1 < backup.sql
```
Sau đó vẫn cần làm Cách 2 ở Mục 1 để re-run V4/V5 với data mới.

---

## 3. String Java có ảnh hưởng người dùng / AI

### 3.1. AI Chat System Prompt

**File**: [`src/main/java/com/example/quanly/service/ai/AIChatService.java`](../src/main/java/com/example/quanly/service/ai/AIChatService.java#L24-L37)

**Trước (dòng 24–37):**
```java
String systemPrompt = """
        Bạn là trợ lý ảo thông minh của Hệ thống Đặt Sân Cầu Lông.
        Nhiệm vụ của bạn:
        1. Hỗ trợ khách hàng đặt sân bằng cách sử dụng công cụ 'listAllCourts'...
        2. Trả lời các câu hỏi về địa chỉ, giá cả, và dịch vụ...
        3. Tư vấn kỹ thuật cầu lông: Cách cầm vợt, di chuyển, các loại vợt phù hợp.
        4. Báo cáo doanh thu cho Admin...
        """;
```

**Sau:**
```java
String systemPrompt = """
        Bạn là trợ lý ảo thông minh của Hệ thống Đặt Sân Pickleball.
        Nhiệm vụ của bạn:
        1. Hỗ trợ khách hàng đặt sân bằng cách sử dụng công cụ 'listAllCourts'...
        2. Trả lời các câu hỏi về địa chỉ, giá cả, và dịch vụ...
        3. Tư vấn kỹ thuật pickleball: Cách cầm paddle, di chuyển trên sân, các loại paddle phù hợp (control / power / all-court).
        4. Báo cáo doanh thu cho Admin...
        """;
```

> ⚠ **Đây là mục quan trọng nhất**: system prompt định hình toàn bộ câu trả lời của AI chatbot. Nếu không đổi, user hỏi pickleball nhưng AI trả lời theo context cầu lông.

### 3.2. AI Tool Descriptions

**File**: [`src/main/java/com/example/quanly/service/ai/AIToolsConfig.java`](../src/main/java/com/example/quanly/service/ai/AIToolsConfig.java)

**Dòng 46:**
```java
// Trước:
@Tool(description = "Liệt kê danh sách tất cả các sân cầu lông, bao gồm tên sân, khu vực (Hà Nội, HCM...) và địa chỉ chi tiết.")
// Sau:
@Tool(description = "Liệt kê danh sách tất cả các sân pickleball, bao gồm tên sân, khu vực (Hà Nội, HCM...) và địa chỉ chi tiết.")
```

**Dòng 64:**
```java
// Trước:
@Tool(description = "Kiểm tra lịch trống của các sân cầu lông theo ngày. Tham số date phải có định dạng YYYY-MM-DD.")
// Sau:
@Tool(description = "Kiểm tra lịch trống của các sân pickleball theo ngày. Tham số date phải có định dạng YYYY-MM-DD.")
```

> 💡 `@Tool description` được gửi đến LLM dưới dạng tool schema → ảnh hưởng cách LLM hiểu/chọn tool. Đổi cũng giúp tool chọn đúng khi user dùng từ "pickleball".

### 3.3. Push notification (ntfy.sh)

**File**: [`src/main/java/com/example/quanly/service/NtfyService.java`](../src/main/java/com/example/quanly/service/NtfyService.java#L67)

**Dòng 67:**
```java
// Trước:
String message = "You have a badminton match coming up.";
// Sau:
String message = "You have a pickleball match coming up.";
```

> Đây là nội dung push notification gửi tới điện thoại user 1 giờ trước trận đấu.

---

## 4. Xoá feature Match-Post (tìm đối + chat)

### 4.1. Bối cảnh & scope

**Feature đang có**:
- User đăng bài tìm đối thủ chơi (entity `MatchPost`): `play_date`, `area`, `time_slot`, `skill_level`, `max_participants`...
- User khác tham gia (entity `MatchParticipant`)
- Chat real-time giữa người tham gia qua WebSocket/STOMP (entity `ChatMessage`)
- Scheduler tự động đóng bài đăng quá ngày (`MatchPostScheduler` chạy 23:00 hằng ngày)
- Notification qua `/queue/notifications` khi có người join/leave/kick

**Quyết định**: xoá toàn bộ feature → giải phóng ~21 file Java + 3 table DB.

**Side effect quan trọng**:
- WebSocket chỉ được dùng cho chat match-post → xoá luôn `WebSocketConfig` và endpoint `/ws/**`
- `NotificationService` chỉ phục vụ match-post → xoá luôn
- AI chat (`AIChatService`, `AIChatController`) dùng `Flux<String>` (reactive SSE), **KHÔNG** dùng WebSocket → an toàn, không bị ảnh hưởng
- Push notification cho booking (`NtfyService`) dùng HTTP đến ntfy.sh → **KHÔNG** dùng WebSocket → an toàn

### 4.2. Danh sách file cần xoá hoàn toàn (19 file)

```
src/main/java/com/example/quanly/
├── config/
│   └── WebSocketConfig.java                    ❌ XOÁ
├── controller/client/
│   ├── ChatSocketController.java               ❌ XOÁ
│   └── MatchPostController.java                ❌ XOÁ
├── domain/
│   ├── ChatMessage.java                        ❌ XOÁ
│   ├── MatchParticipant.java                   ❌ XOÁ
│   ├── MatchPost.java                          ❌ XOÁ
│   └── dto/
│       ├── ChatMessageDto.java                 ❌ XOÁ
│       ├── MatchPostDTO.java                   ❌ XOÁ
│       └── MatchPostResponseDTO.java           ❌ XOÁ
├── mapper/
│   ├── ChatMessageMapper.java                  ❌ XOÁ
│   └── MatchPostMapper.java                    ❌ XOÁ
├── repository/
│   ├── ChatMessageRepository.java              ❌ XOÁ
│   ├── MatchParticipantRepository.java         ❌ XOÁ
│   └── MatchPostRepository.java                ❌ XOÁ
├── scheduler/
│   └── MatchPostScheduler.java                 ❌ XOÁ
└── service/
    ├── ChatService.java                        ❌ XOÁ
    ├── MatchParticipantService.java            ❌ XOÁ
    ├── MatchPostService.java                   ❌ XOÁ
    └── NotificationService.java                ❌ XOÁ (chỉ dùng cho match-post)
```

**Lệnh xoá hàng loạt (Git Bash):**
```bash
cd src/main/java/com/example/quanly
rm config/WebSocketConfig.java \
   controller/client/ChatSocketController.java \
   controller/client/MatchPostController.java \
   domain/ChatMessage.java \
   domain/MatchParticipant.java \
   domain/MatchPost.java \
   domain/dto/ChatMessageDto.java \
   domain/dto/MatchPostDTO.java \
   domain/dto/MatchPostResponseDTO.java \
   mapper/ChatMessageMapper.java \
   mapper/MatchPostMapper.java \
   repository/ChatMessageRepository.java \
   repository/MatchParticipantRepository.java \
   repository/MatchPostRepository.java \
   scheduler/MatchPostScheduler.java \
   service/ChatService.java \
   service/MatchParticipantService.java \
   service/MatchPostService.java \
   service/NotificationService.java
```

### 4.3. File cần SỬA (2 file)

#### 4.3.1. `User.java` — xoá 3 quan hệ OneToMany

**File**: [`src/main/java/com/example/quanly/domain/User.java`](../src/main/java/com/example/quanly/domain/User.java#L53-L63)

**Trước (dòng 53–63):**
```java
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
@JsonIgnoreProperties("user")
private List<MatchPost> matchPosts = new ArrayList<>();

@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
@JsonIgnoreProperties({ "user", "matchPost" })
private List<MatchParticipant> participations = new ArrayList<>();

@OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
@JsonIgnoreProperties({ "sender", "matchPost" })
private List<ChatMessage> messages = new ArrayList<>();
```

**Sau:** xoá toàn bộ 11 dòng trên.

Đồng thời rà các import không còn dùng (IDE sẽ highlight):
```java
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;  // có thể còn dùng cho field products
import com.example.quanly.domain.MatchPost;           // xoá
import com.example.quanly.domain.MatchParticipant;    // xoá
import com.example.quanly.domain.ChatMessage;         // xoá
```

> ⚠ Field `products` cũng dùng `@JsonIgnoreProperties("user")` (dòng 66) — vậy import `JsonIgnoreProperties` vẫn cần giữ. Còn `ArrayList`/`List` cũng còn vì `products` dùng `List<Product>`. **Không xoá nhầm.**

#### 4.3.2. `SecurityConfiguration.java` — xoá 2 route

**File**: [`src/main/java/com/example/quanly/config/SecurityConfiguration.java`](../src/main/java/com/example/quanly/config/SecurityConfiguration.java#L95-L118)

**Trước (dòng ~95–109):**
```java
.requestMatchers(
        "/api/v1/auth/**",
        "/api/v1/products/**",
        "/api/v1/rackets/**",
        "/api/v1/client/home",
        "/api/v1/racket-stock/**",
        "/api/v1/ntfy-sse/**",
        "/api/v1/payments/vnpay-callback",
        "/api/v1/mock-payment/**",
        "/api/v1/rentals/*/rackets",
        "/api/v1/ai/**",
        "/ws/**")                                          // ← XOÁ dòng này
.permitAll()
// Match-post cần đăng nhập                                // ← XOÁ comment + dòng dưới
.requestMatchers("/api/v1/match-posts/**").authenticated()
```

**Sau:**
```java
.requestMatchers(
        "/api/v1/auth/**",
        "/api/v1/products/**",
        "/api/v1/rackets/**",
        "/api/v1/client/home",
        "/api/v1/racket-stock/**",
        "/api/v1/ntfy-sse/**",
        "/api/v1/payments/vnpay-callback",
        "/api/v1/mock-payment/**",
        "/api/v1/rentals/*/rackets",
        "/api/v1/ai/**")
.permitAll()
```

### 4.4. Migration SQL — DROP TABLE

**Tạo file mới**: [`src/main/resources/db/migration/V9__drop_match_post_tables.sql`](../src/main/resources/db/migration/V9__drop_match_post_tables.sql)

```sql
-- ============================================================
-- V9__drop_match_post_tables.sql
-- Xoá feature Match-Post (tìm đối thủ + chat real-time).
-- Thứ tự DROP: child (FK) → parent.
-- ============================================================

DROP TABLE IF EXISTS chat_messages;
DROP TABLE IF EXISTS match_participants;
DROP TABLE IF EXISTS match_posts;
```

> ⚠ **Lý do thứ tự**:
> - `chat_messages` có FK `match_post_id` → drop trước
> - `match_participants` có FK `match_post_id` → drop tiếp
> - `match_posts` drop cuối
>
> Dùng `IF EXISTS` để idempotent (chạy nhiều lần không lỗi).

### 4.5. Tài liệu (optional, làm sau)

- [`doc/TAI_LIEU_DU_AN.md`](TAI_LIEU_DU_AN.md): tìm các section nói về match-post, chat, WebSocket → xoá hoặc đánh dấu deprecated. Có thể search trong file:
  ```bash
  grep -n "match.post\|chat.message\|websocket\|tìm đối\|tin nhắn" doc/TAI_LIEU_DU_AN.md
  ```
- [`doc/ANALYSIS_DESIGN.md`](ANALYSIS_DESIGN.md): kiểm tra sequence diagram match-post nếu có

### 4.6. Verify sau khi xoá

```bash
# 1. Build phải pass
mvn clean compile

# Nếu có lỗi unresolved import → có chỗ còn reference, xem tiếp ở 4.7

# 2. Test pass (4 test class hiện tại không reference match-post nên OK)
mvn test

# 3. Khởi động app
mvn spring-boot:run

# 4. Verify DB: 3 table đã bị drop
mysql -u root -proot -e "USE sanpickleball1; SHOW TABLES;" | grep -E "match_posts|match_participants|chat_messages"
# → Output rỗng = OK

# 5. Test endpoint cũ trả 404
curl -i http://localhost:8080/api/v1/match-posts
# → HTTP/1.1 404
```

### 4.7. Troubleshooting

**Lỗi**: `cannot find symbol: class MatchPost` khi build.
- **Nguyên nhân**: còn file Java reference tới `MatchPost`/`ChatMessage`/`MatchParticipant`.
- **Fix**: chạy `grep -rn "MatchPost\|ChatMessage\|MatchParticipant" src/main/java/ | grep -v "Match"` để tìm tất cả chỗ còn reference. Xoá hoặc gỡ import.

**Lỗi**: `BeanCreationException: NotificationService` khi start app.
- **Nguyên nhân**: file nào đó còn `@Autowired NotificationService`.
- **Fix**: `grep -rn "NotificationService" src/main/java/` → gỡ ref.

**Lỗi**: Flyway báo `Validate failed` về V9.
- **Nguyên nhân**: DB hiện không có sẵn 3 table (vd: đã chạy DROP manual).
- **Fix**: vì dùng `DROP TABLE IF EXISTS` → idempotent, không xảy ra lỗi này. Nếu vẫn lỗi → set `spring.flyway.validate-on-migrate=false` trong `application.properties` (đã có sẵn).

**Lỗi**: Frontend gọi `/ws/**` báo 401/connection refused.
- **Nguyên nhân**: client cũ vẫn cố kết nối WebSocket.
- **Fix**: frontend cần xoá luôn UI match-post + chat (out of scope tài liệu này — backend đã loại).

### 4.8. Rollback feature match-post

Nếu sau khi xoá user muốn khôi phục:

```bash
# Code
git revert <commit_xoa_match_post>

# Data
# 1. Restore từ mysqldump backup
mysql -u root -proot sanpickleball1 < backup_before_drop.sql
# 2. Xoá row V9 trong flyway_schema_history
mysql -u root -proot -e "USE sanpickleball1; DELETE FROM flyway_schema_history WHERE version = '9';"
```

---

## 5. Comment Javadoc tiếng Việt (optional)

Các comment này không ảnh hưởng runtime nhưng làm code dễ hiểu hơn về domain. Tuỳ chọn đổi.

### 4.1. `Racket.java`
**File**: [`src/main/java/com/example/quanly/domain/Racket.java`](../src/main/java/com/example/quanly/domain/Racket.java)

```java
// Dòng 17:  private Long id;     // Mã ID của cây vợt
// Dòng 20:  private String name; // Tên của cây vợt
// Dòng 22:  private double price; // Giá của cây vợt (giá mua)
// Dòng 34:  private int bookingStockQuantity; // Số lượng vợt cho thuê theo booking
// Dòng 36:  private int quantity; // số vợt cho thuê
```

→ Đổi "vợt" thành "vợt/paddle pickleball" (chỉ comment, **giữ tên field/class**).

### 4.2. `RacketStockByDate.java`
**File**: [`src/main/java/com/example/quanly/domain/RacketStockByDate.java`](../src/main/java/com/example/quanly/domain/RacketStockByDate.java) — dòng 19, 23, 26

### 4.3. `RacketStatisticsService.java`
**File**: [`src/main/java/com/example/quanly/service/RacketStatisticsService.java`](../src/main/java/com/example/quanly/service/RacketStatisticsService.java) — dòng 31, 39, 51, 58, 65, 72 (Javadoc trên các method thống kê)

> ⚠ **Không đổi** các string trong throw exception như `"Không tìm thấy vợt id="`, `"Thuê vợt thành công"`, `"Tạo vợt thành công"`... Vì "vợt" tiếng Việt vẫn dùng được. Nếu muốn chuẩn hoá hoàn toàn → tìm-thay `"vợt"` → `"paddle"` trong 8 chỗ:
> - [`RacketController.java`](../src/main/java/com/example/quanly/controller/admin/RacketController.java) dòng 46, 64, 74, 92
> - [`ItemController.java:108`](../src/main/java/com/example/quanly/controller/client/ItemController.java#L108)
> - [`RentalController.java`](../src/main/java/com/example/quanly/controller/client/RentalController.java) dòng 51, 98
> - [`RentalToolController.java:47`](../src/main/java/com/example/quanly/controller/admin/RentalToolController.java#L47)
> - [`RentalToolService.java`](../src/main/java/com/example/quanly/service/RentalToolService.java) dòng 79, 87, 280
> - [`EmailService.java:36`](../src/main/java/com/example/quanly/service/EmailService.java#L36)
> - [`PaymentController.java:111`](../src/main/java/com/example/quanly/controller/PaymentController.java#L111)
> - [`CreateRentalRequest.java:28`](../src/main/java/com/example/quanly/domain/dto/CreateRentalRequest.java#L28) (validation message)
> - [`ValidRentalContext.java:18`](../src/main/java/com/example/quanly/domain/dto/validation/ValidRentalContext.java#L18) (validation message)

---

## 6. Tài liệu trong `doc/`

### 5.1. `SETUP.md`

**File**: [`doc/SETUP.md`](SETUP.md)

| Dòng | Trước | Sau |
|---|---|---|
| 1 | `# Hướng dẫn cài đặt dự án Badminton Booking System` | `# Hướng dẫn cài đặt dự án Pickleball Booking System` |
| 3 | `...dự án Badminton Booking System (Hệ thống đặt sân cầu lông)...` | `...dự án Pickleball Booking System (Hệ thống đặt sân pickleball)...` |
| 24 | `Tạo một database mới có tên là \`sancaulong1\`.` | `... \`sanpickleball1\`.` |
| 27 | `spring.datasource.url=jdbc:mysql://localhost:3307/sancaulong1?...` | `... /sanpickleball1?...` |

### 5.2. `ANALYSIS_DESIGN.md`

**File**: [`doc/ANALYSIS_DESIGN.md`](ANALYSIS_DESIGN.md)

| Dòng | Trước | Sau |
|---|---|---|
| 1 | `# Tài liệu Phân tích Thiết kế Hệ thống Badminton Booking` | `# Tài liệu Phân tích Thiết kế Hệ thống Pickleball Booking` |
| 3 | `...dự án Badminton Booking System...` | `...dự án Pickleball Booking System...` |
| 20 | `**Product:** Thông tin sân cầu lông chính (ví dụ: Sân cầu lông Quận 1).` | `**Product:** Thông tin sân pickleball chính (ví dụ: Sân pickleball Quận 1).` |

### 5.3. `TAI_LIEU_DU_AN.md`

**File**: [`doc/TAI_LIEU_DU_AN.md`](TAI_LIEU_DU_AN.md)

7 chỗ — dùng tìm-thay toàn file:
- `San Cau Long` → `San Pickleball`
- `san cau long` → `san pickleball`
- `sancaulong1` → `sanpickleball1`

Cụ thể các dòng: 1, 7, 214, 288, 667, 759, 844.

> 💡 Có thể dùng `sed` (Git Bash) cho gọn:
> ```bash
> sed -i 's/San Cau Long/San Pickleball/g; s/san cau long/san pickleball/g; s/sancaulong1/sanpickleball1/g' doc/TAI_LIEU_DU_AN.md
> ```

---

## 7. Cleanup file log

**File**: [`run.log`](../run.log) — dòng 1505 chứa URL `sancaulong1`.

Đây chỉ là log lịch sử khi app chạy. Có 2 lựa chọn:
- Xoá file (`rm run.log`) → app sẽ tạo lại khi chạy.
- Add `run.log` vào `.gitignore` (nếu chưa có) — log không nên commit.

---

## 8. JWT secret default fallback

**File**: [`src/main/java/com/example/quanly/config/JwtTokenProvider.java`](../src/main/java/com/example/quanly/config/JwtTokenProvider.java#L17)

**Dòng 17:**
```java
@Value("${jwt.secret:quanlysancaulong_super_secret_key_256bit_minimum_length_required_here}")
private String jwtSecret;
```

Đây là **default value** dùng khi env var `JWT_SECRET` không được set. Nếu production luôn có `JWT_SECRET` env (xem [`application.properties:59`](../src/main/resources/application.properties)) thì không ảnh hưởng.

**Đề xuất đổi (cosmetic):**
```java
@Value("${jwt.secret:quanlypickleball_super_secret_key_256bit_minimum_length_required_here}")
```

> ⚠ **Lưu ý security**: cả 2 giá trị trên đều **không nên dùng trong production**. Production phải có `JWT_SECRET` riêng đủ mạnh.

---

## 9. Verify sau khi đổi

### Checklist verify
- [ ] `mvn clean compile` không lỗi
- [ ] `mvn flyway:info` xác nhận V1→V8 đã chạy trên `sanpickleball1`
- [ ] Connect DB `sanpickleball1`, query `SELECT name FROM products` thấy `Sân Pickleball Antigravity`, `Elite Arena Pickleball`, `Pro Center Pickleball`
- [ ] Query `SELECT name, factory FROM racket` thấy Selkirk, Joola, Franklin, Paddletek, ONIX, Engage, Gamma
- [ ] Khởi động app: `mvn spring-boot:run` — log không có lỗi connection
- [ ] Test API `GET /api/v1/admin/products` (cần auth ADMIN) → thấy 3 sân pickleball
- [ ] Test API `GET /api/v1/rackets` → thấy 8 paddle
- [ ] Test AI: `POST /api/v1/ai/chat` với body `{"chatId":"test","message":"Liệt kê các sân pickleball ở TP.HCM"}` → AI gọi tool `listAllCourts` và trả lời theo context pickleball
- [ ] Test push notification: tạo booking sắp tới trong 1 giờ → kiểm tra topic ntfy nhận message `"You have a pickleball match coming up."`

### Lệnh test nhanh
```bash
# 1. Build & compile
mvn clean compile

# 2. Connect MySQL kiểm tra data
mysql -u root -proot -e "USE sanpickleball1; SELECT id, name FROM products;"

# Output mong đợi:
# 1  Sân Pickleball Antigravity
# 2  Elite Arena Pickleball
# 3  Pro Center Pickleball

mysql -u root -proot -e "USE sanpickleball1; SELECT id, name, factory FROM racket;"

# Output mong đợi (8 dòng): Selkirk, Joola, Franklin, Paddletek, ONIX, Selkirk, Engage, Gamma

# 3. Khởi động app
mvn spring-boot:run
```

---

## 🔁 ROLLBACK PLAN

Nếu cần rollback (vd: AI hiểu sai context, customer feedback xấu):

1. **Code**: `git revert <commit_rebrand_hash>` — đảo ngược tất cả thay đổi
2. **Data**: Restore từ backup `mysqldump` đã làm ở Mục 2.3
3. **Docker**: `docker-compose down && docker-compose up -d` (container names sẽ reset)

> 💡 Đề xuất chia thành **3 commit riêng**:
> - Commit 1: "chore(rebrand): SQL seed data sang pickleball" (V4, V5 — đã xong)
> - Commit 2: "chore(rebrand): strings + config + DB name sang pickleball" (mục 2, 3, 5, 7, 8)
> - Commit 3: "feat: remove match-post feature" (mục 4 — destructive, tách riêng)
> - Commit 4 (optional): "docs: update for pickleball rebrand" (mục 6)
>
> → Dễ rollback từng phần. Đặc biệt commit 3 (xoá match-post) cần tách riêng vì có DROP TABLE.

---

## 📊 PHỤ LỤC: KẾT QUẢ SCAN ĐẦY ĐỦ

### Files chứa từ khoá "cầu lông / badminton / sancaulong / shuttle"

| File | Số chỗ | Loại |
|---|---|---|
| [`docker-compose.yml`](../docker-compose.yml) | 5 | config |
| [`src/main/resources/application.properties`](../src/main/resources/application.properties) | 2 | config |
| [`src/main/java/.../service/ai/AIChatService.java`](../src/main/java/com/example/quanly/service/ai/AIChatService.java) | 2 | code (AI prompt) |
| [`src/main/java/.../service/ai/AIToolsConfig.java`](../src/main/java/com/example/quanly/service/ai/AIToolsConfig.java) | 2 | code (tool desc) |
| [`src/main/java/.../service/NtfyService.java`](../src/main/java/com/example/quanly/service/NtfyService.java) | 1 | code (notif) |
| [`src/main/java/.../config/JwtTokenProvider.java`](../src/main/java/com/example/quanly/config/JwtTokenProvider.java) | 1 | code (JWT default) |
| [`doc/SETUP.md`](SETUP.md) | 4 | docs |
| [`doc/ANALYSIS_DESIGN.md`](ANALYSIS_DESIGN.md) | 3 | docs |
| [`doc/TAI_LIEU_DU_AN.md`](TAI_LIEU_DU_AN.md) | 7 | docs |
| [`run.log`](../run.log) | 1 | log (xoá) |

### Files chứa từ "vợt" (comment Javadoc + validation message — optional)

| File | Số chỗ |
|---|---|
| `RacketStatisticsService.java` | 6 (Javadoc) |
| `Racket.java` | 5 (comment field) |
| `RacketStockByDate.java` | 3 (comment field) |
| `RentalToolService.java` | 3 (string error) |
| `RacketController.java` | 4 (string response) |
| `RentalController.java` | 2 (string response) |
| `ItemController.java` | 1 |
| `RentalToolController.java` | 1 |
| `EmailService.java` | 1 |
| `PaymentController.java` | 1 |
| `CreateRentalRequest.java` | 1 (validation) |
| `ValidRentalContext.java` | 1 (validation) |

**Tổng (must + should + optional): 56 chỗ trong 22 file.**

Tuy nhiên **bắt buộc** chỉ có 13 chỗ trong 7 file. Phần còn lại là tuỳ chọn vì từ "vợt" tiếng Việt vẫn dùng được cho pickleball paddle.

### Files liên quan feature match-post (xoá hoàn toàn ở Mục 4)

| Loại | File | Hành động |
|---|---|---|
| Entity | `MatchPost.java`, `MatchParticipant.java`, `ChatMessage.java` | XOÁ |
| DTO | `MatchPostDTO.java`, `MatchPostResponseDTO.java`, `ChatMessageDto.java` | XOÁ |
| Repository | `MatchPostRepository.java`, `MatchParticipantRepository.java`, `ChatMessageRepository.java` | XOÁ |
| Mapper | `MatchPostMapper.java`, `ChatMessageMapper.java` | XOÁ |
| Service | `MatchPostService.java`, `MatchParticipantService.java`, `ChatService.java`, `NotificationService.java` | XOÁ |
| Controller | `MatchPostController.java`, `ChatSocketController.java` | XOÁ |
| Scheduler | `MatchPostScheduler.java` | XOÁ |
| Config | `WebSocketConfig.java` | XOÁ |
| Liên quan (SỬA) | `User.java` (xoá 3 OneToMany), `SecurityConfiguration.java` (xoá 2 route) | SỬA |
| Migration mới | `V9__drop_match_post_tables.sql` | TẠO |

**Tổng**: 19 file xoá + 2 file sửa + 1 file tạo mới.
