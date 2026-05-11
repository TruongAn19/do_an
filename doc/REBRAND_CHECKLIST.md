# CHECKLIST REBRAND: CẦU LÔNG → PICKLEBALL

> Dựa trên kết quả scan toàn bộ codebase ngày 2026-05-11.
> Project là **backend REST API thuần** (Spring Boot 3.4.3 + MySQL + Flyway).
> Không có view layer (JSP/Thymeleaf) — phần "UI" do frontend tách riêng đảm nhận.
>
> **Chiến lược khuyến nghị: Option A — Rebrand nội dung, GIỮ schema/code identifier**
> - Không đổi tên bảng `racket` / `racket_stock_by_date` / class `Racket` / endpoint `/api/v1/rackets`.
> - Lý do: trong tiếng Việt, người chơi pickleball vẫn gọi "vợt pickleball" → từ `racket` về mặt domain không sai. Đổi schema sẽ kéo theo refactor ~27 file Java, breaking REST API, tốn 2–3 ngày mà không tăng giá trị.
> - Chỉ đổi: **data hiển thị, string trong AI prompt, copy email/notification, docs, tên DB/container**.
>
> File hướng dẫn chi tiết: [REBRAND_GUIDE.md](REBRAND_GUIDE.md)

---

## ✅ TIẾN ĐỘ TỔNG QUAN

| Hạng mục | Số file | Mức ưu tiên | Trạng thái |
|---|---|---|---|
| 1. SQL seed data | 2 | 🔴 MUST | ✅ Đã xong (V4, V5) |
| 2. Tên DB + container | 3 | 🔴 MUST | ☐ |
| 3. Java strings (AI, email, notif) | 4 | 🔴 MUST | ☐ |
| 4. **Xoá feature Match-Post (tìm đối + chat)** | **21** | 🔴 **MUST** | ☐ |
| 5. Comment Javadoc tiếng Việt | 3 | 🟡 SHOULD | ☐ |
| 6. Docs tiếng Việt/Anh | 3 | 🟡 SHOULD | ☐ |
| 7. Log file (cleanup) | 1 | 🟢 OPTIONAL | ☐ |
| 8. JWT secret default fallback | 1 | 🟢 OPTIONAL | ☐ |

---

## 🔴 PHẦN BẮT BUỘC (MUST)

### 1. SQL seed data ✅ ĐÃ XONG
- [x] [`V4__seed_data.sql`](../src/main/resources/db/migration/V4__seed_data.sql) — tên sân, mô tả, paddle pickleball
- [x] [`V5__seed_racket_stock.sql`](../src/main/resources/db/migration/V5__seed_racket_stock.sql) — comment header + tên paddle

> ⚠ Lưu ý: nếu DB hiện đã chạy V4/V5, Flyway sẽ **không re-run**. Cần xoá row trong `flyway_schema_history` rồi truncate + chạy lại, hoặc tạo database mới hoàn toàn (xem mục 2).

### 2. Đổi tên database + container Docker
- [ ] [`docker-compose.yml:4`](../docker-compose.yml) `container_name: badminton-db` → `pickleball-db`
- [ ] [`docker-compose.yml:7`](../docker-compose.yml) `MYSQL_DATABASE: sancaulong1` → `sanpickleball1`
- [ ] [`docker-compose.yml:15`](../docker-compose.yml) `container_name: badminton-redis` → `pickleball-redis`
- [ ] [`docker-compose.yml:21`](../docker-compose.yml) `container_name: badminton-api` → `pickleball-api`
- [ ] [`docker-compose.yml:25`](../docker-compose.yml) JDBC URL `sancaulong1` → `sanpickleball1`
- [ ] [`application.properties:8`](../src/main/resources/application.properties) comment AWS URL `sancaulong1` → `sanpickleball1`
- [ ] [`application.properties:11`](../src/main/resources/application.properties) `localhost:3307/sancaulong1` → `localhost:3307/sanpickleball1`
- [ ] Tạo database mới: `CREATE DATABASE sanpickleball1` (xem REBRAND_GUIDE phần 2)

### 3. String Java có ảnh hưởng người dùng / AI
- [ ] [`AIChatService.java:25`](../src/main/java/com/example/quanly/service/ai/AIChatService.java#L25) — system prompt "Hệ thống Đặt Sân Cầu Lông" → "Hệ thống Đặt Sân Pickleball"
- [ ] [`AIChatService.java:29`](../src/main/java/com/example/quanly/service/ai/AIChatService.java#L29) — "Tư vấn kỹ thuật cầu lông: Cách cầm vợt..." → "Tư vấn kỹ thuật pickleball: Cách cầm paddle..."
- [ ] [`AIToolsConfig.java:46`](../src/main/java/com/example/quanly/service/ai/AIToolsConfig.java#L46) — `@Tool(description = "...sân cầu lông...")` → "sân pickleball"
- [ ] [`AIToolsConfig.java:64`](../src/main/java/com/example/quanly/service/ai/AIToolsConfig.java#L64) — `@Tool(description = "...sân cầu lông...")` → "sân pickleball"
- [ ] [`NtfyService.java:67`](../src/main/java/com/example/quanly/service/NtfyService.java#L67) — `"You have a badminton match coming up."` → `"You have a pickleball match coming up."`

### 4. Xoá feature Match-Post (tìm đối + chat real-time)

> **Lý do**: project rebrand sang pickleball, không cần giữ chức năng tìm đối thủ qua bài đăng + chat.
> Đây là **destructive change** — cần backup DB trước. Chia 1 commit riêng để dễ rollback.

#### 4.1. Xoá file Java (19 file)

**Domain (3 entity + 3 DTO):**
- [ ] Xoá [`domain/MatchPost.java`](../src/main/java/com/example/quanly/domain/MatchPost.java)
- [ ] Xoá [`domain/MatchParticipant.java`](../src/main/java/com/example/quanly/domain/MatchParticipant.java)
- [ ] Xoá [`domain/ChatMessage.java`](../src/main/java/com/example/quanly/domain/ChatMessage.java)
- [ ] Xoá [`domain/dto/MatchPostDTO.java`](../src/main/java/com/example/quanly/domain/dto/MatchPostDTO.java)
- [ ] Xoá [`domain/dto/MatchPostResponseDTO.java`](../src/main/java/com/example/quanly/domain/dto/MatchPostResponseDTO.java)
- [ ] Xoá [`domain/dto/ChatMessageDto.java`](../src/main/java/com/example/quanly/domain/dto/ChatMessageDto.java)

**Repository (3 file):**
- [ ] Xoá [`repository/MatchPostRepository.java`](../src/main/java/com/example/quanly/repository/MatchPostRepository.java)
- [ ] Xoá [`repository/MatchParticipantRepository.java`](../src/main/java/com/example/quanly/repository/MatchParticipantRepository.java)
- [ ] Xoá [`repository/ChatMessageRepository.java`](../src/main/java/com/example/quanly/repository/ChatMessageRepository.java)

**Mapper (2 file):**
- [ ] Xoá [`mapper/MatchPostMapper.java`](../src/main/java/com/example/quanly/mapper/MatchPostMapper.java)
- [ ] Xoá [`mapper/ChatMessageMapper.java`](../src/main/java/com/example/quanly/mapper/ChatMessageMapper.java)

**Service (4 file):**
- [ ] Xoá [`service/MatchPostService.java`](../src/main/java/com/example/quanly/service/MatchPostService.java)
- [ ] Xoá [`service/MatchParticipantService.java`](../src/main/java/com/example/quanly/service/MatchParticipantService.java)
- [ ] Xoá [`service/ChatService.java`](../src/main/java/com/example/quanly/service/ChatService.java)
- [ ] Xoá [`service/NotificationService.java`](../src/main/java/com/example/quanly/service/NotificationService.java) ← chỉ phục vụ match-post

**Controller (2 file):**
- [ ] Xoá [`controller/client/MatchPostController.java`](../src/main/java/com/example/quanly/controller/client/MatchPostController.java)
- [ ] Xoá [`controller/client/ChatSocketController.java`](../src/main/java/com/example/quanly/controller/client/ChatSocketController.java)

**Scheduler (1 file):**
- [ ] Xoá [`scheduler/MatchPostScheduler.java`](../src/main/java/com/example/quanly/scheduler/MatchPostScheduler.java)

**Config (1 file):**
- [ ] Xoá [`config/WebSocketConfig.java`](../src/main/java/com/example/quanly/config/WebSocketConfig.java) ← chỉ dùng cho chat match-post (AI dùng Flux/SSE, không liên quan)

#### 4.2. Sửa file Java (2 file)

- [ ] [`domain/User.java`](../src/main/java/com/example/quanly/domain/User.java#L53-L63) — xoá 3 field quan hệ:
  - dòng 53–55: `private List<MatchPost> matchPosts;`
  - dòng 57–59: `private List<MatchParticipant> participations;`
  - dòng 61–63: `private List<ChatMessage> messages;`
  - Xoá luôn 2 import `JsonIgnoreProperties` / `ArrayList` / `List` nếu không còn dùng

- [ ] [`config/SecurityConfiguration.java`](../src/main/java/com/example/quanly/config/SecurityConfiguration.java) — xoá 2 route:
  - dòng 105: `"/ws/**"` trong permit list
  - dòng 108–109: comment `// Match-post cần đăng nhập` + `.requestMatchers("/api/v1/match-posts/**").authenticated()`

#### 4.3. Migration DROP TABLE (1 file mới)

- [ ] Tạo [`src/main/resources/db/migration/V9__drop_match_post_tables.sql`](../src/main/resources/db/migration/V9__drop_match_post_tables.sql) — chứa:
  ```sql
  DROP TABLE IF EXISTS chat_messages;
  DROP TABLE IF EXISTS match_participants;
  DROP TABLE IF EXISTS match_posts;
  ```
  (xoá theo đúng thứ tự FK: child → parent)

#### 4.4. Sửa tài liệu

- [ ] [`doc/TAI_LIEU_DU_AN.md`](TAI_LIEU_DU_AN.md) — xoá hoặc đánh dấu deprecated các section nói về match-post, chat real-time, WebSocket
- [ ] [`doc/ANALYSIS_DESIGN.md`](ANALYSIS_DESIGN.md) — kiểm tra sequence diagram match-post nếu có

#### 4.5. Verify sau khi xoá

- [ ] `mvn clean compile` không lỗi unresolved import
- [ ] `mvn test` chạy được (4 test class hiện không reference match-post)
- [ ] Khởi động app — không có lỗi `BeanCreationException`
- [ ] Query DB: `SHOW TABLES;` không còn `match_posts`, `match_participants`, `chat_messages`
- [ ] Test API `GET /api/v1/match-posts` → 404 (đã xoá controller)

---

## 🟡 PHẦN NÊN LÀM (SHOULD)

### 4. Comment tiếng Việt trong Java (cosmetic, không ảnh hưởng runtime)
- [ ] [`Racket.java:17,20,22,34,36`](../src/main/java/com/example/quanly/domain/Racket.java) — comment "cây vợt" → "vợt/paddle pickleball"
- [ ] [`RacketStockByDate.java:19,23,26`](../src/main/java/com/example/quanly/domain/RacketStockByDate.java) — comment "vợt" có thể giữ hoặc đổi tuỳ ý
- [ ] [`RacketStatisticsService.java:31,39,51,58,65,72`](../src/main/java/com/example/quanly/service/RacketStatisticsService.java) — Javadoc nói về "vợt"

> Không bắt buộc đổi vì "vợt" trong tiếng Việt vẫn dùng được cho paddle pickleball.

### 5. Tài liệu trong `doc/`
- [ ] [`doc/SETUP.md:1,3,24,27`](SETUP.md) — tiêu đề + DB name
- [ ] [`doc/ANALYSIS_DESIGN.md:1,3,20`](ANALYSIS_DESIGN.md) — "Badminton Booking" → "Pickleball Booking"
- [ ] [`doc/TAI_LIEU_DU_AN.md:1,7,214,288,667,759,844`](TAI_LIEU_DU_AN.md) — 7 chỗ "san cau long" → "san pickleball"

---

## 🟢 PHẦN TUỲ CHỌN (OPTIONAL)

### 6. Cleanup
- [ ] [`run.log:1505`](../run.log) — log file cũ chứa URL `sancaulong1`, có thể xoá (sẽ tự sinh lại khi chạy app)

### 7. JWT secret default fallback (không ảnh hưởng nếu set env var `JWT_SECRET`)
- [ ] [`JwtTokenProvider.java:17`](../src/main/java/com/example/quanly/config/JwtTokenProvider.java#L17) — default fallback `quanlysancaulong_super_secret_key_...` chứa từ "sancaulong". Production luôn dùng env var nên không lộ — đổi tuỳ ý.

---

## ❌ KHÔNG CẦN ĐỔI (giữ nguyên)

| Đối tượng | Lý do |
|---|---|
| Table `racket`, `racket_stock_by_date` (V1 schema) | "Vợt/racket" vẫn dùng được cho paddle pickleball |
| Class `Racket`, `RacketController`, `RacketRepository`, `RacketService`, `RacketStatisticsService`, `RacketStockByDateService` | Tránh refactor ~27 file Java |
| REST endpoint `/api/v1/rackets`, `/api/v1/admin/rackets`, `/api/v1/admin/racket-statistics`, `/api/v1/racket-stock` | Breaking change cho frontend/mobile |
| Folder `quanly` (package name) | Tên generic, không dính cầu lông |
| `pom.xml` `<artifactId>quanly</artifactId>` | Tên generic |
| `spring.application.name=quanly` | Tên generic |
| Các chuỗi "Không tìm thấy vợt", "Thuê vợt thành công" trong controller error/success message | "Vợt" tiếng Việt vẫn dùng được; nếu muốn chuẩn hoá toàn bộ → đổi thêm 8 chỗ (xem GUIDE) |
| Dependency `spring-boot-starter-websocket` trong `pom.xml` | Có thể giữ để dùng sau, hoặc xoá luôn (cosmetic) — sau khi xoá `WebSocketConfig` thì dependency này thành unused |
| `target/` folder | Build output, sẽ tự sinh lại |
| `ml_service/` | Không chứa từ khoá cầu lông |

---

## 📊 TÓM TẮT SCAN

**Tổng số tác vụ (must + should): rebrand 28 chỗ + xoá feature match-post (21 file Java + 1 migration)**

- 🔴 MUST rebrand: 13 lần / 7 file
- 🔴 MUST xoá feature: 19 file xoá + 2 file sửa + 1 migration mới
- 🟡 SHOULD: 14 lần / 5 file (chủ yếu comment Javadoc + docs)
- 🟢 OPTIONAL: 2 lần / 2 file

**Thời gian ước tính:**
- Option A (rebrand + xoá match-post): **1–2 giờ** + verify build/test
- Option B (đổi cả schema racket + class): 2–3 ngày + migration

---

## 🚦 ORDER OF EXECUTION (đề xuất)

1. **Backup DB hiện tại** (mysqldump) ← BẮT BUỘC vì có DROP TABLE
2. Đổi `docker-compose.yml` + `application.properties` (DB name)
3. Tạo DB mới `sanpickleball1`, để Flyway chạy V1→V8 từ đầu
4. Verify V4/V5 seed data đúng pickleball
5. Đổi 5 string Java (AI + Ntfy) → **commit 1**: `chore(rebrand): pickleball strings + DB name`
6. Xoá feature match-post:
   - Xoá 19 file Java liệt kê ở mục 4.1
   - Sửa `User.java` + `SecurityConfiguration.java` (mục 4.2)
   - Tạo migration `V9__drop_match_post_tables.sql` (mục 4.3)
7. Build + test: `mvn clean compile && mvn test`
8. Khởi động app, verify mục 4.5 → **commit 2**: `feat: remove match-post feature`
9. Đổi docs (SETUP, ANALYSIS_DESIGN, TAI_LIEU_DU_AN) → **commit 3**: `docs: update for pickleball rebrand`
10. Đổi comment Javadoc (optional)

---

## 🔗 LIÊN KẾT

- 📘 Hướng dẫn chi tiết từng bước: [REBRAND_GUIDE.md](REBRAND_GUIDE.md)
- 📂 V4 seed data đã rebrand: [V4__seed_data.sql](../src/main/resources/db/migration/V4__seed_data.sql)
- 📂 V5 racket stock đã rebrand: [V5__seed_racket_stock.sql](../src/main/resources/db/migration/V5__seed_racket_stock.sql)
