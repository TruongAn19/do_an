# Giai đoạn B2: Quản lý Phiên bản Cơ sở dữ liệu (Flyway)

Tắt tính năng Auto Update Hibernate gây nguy hiểm trên môi trường thật và bắt đầu quản lý phiên bản database một cách chặt chẽ.

## Cài đặt thư viện
Trong `pom.xml` thêm:
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

## Cấu hình Properties
Vào `application.properties`:
```properties
# Tắt chế độ tự sửa DB nguy hiểm
spring.jpa.hibernate.ddl-auto=validate 

# Bật Flyway
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
```

## Tạo cấu trúc lưu trữ lệnh SQL
Tạo mới Folder: `src/main/resources/db/migration`

Tạo file đầu tiên chứa lệnh Generate Schema cấu trúc Database cũ để thiết lập nền móng: `V1__init_schema.sql`. (Sử dụng công cụ Export SQL của phần mềm DBeaver/MySQL Workbench để nhúng cấu trúc bảng cũ vào đây).

Từ sau này, muốn thêm cột hay tạo bảng mới thì đặt tên theo quy tắc tịnh tiến:
- `V2__add_rating_into_product.sql`
- `V3__create_new_payment_table.sql`
Flyway sẽ tự động chạy lệnh này lên mysql db của bạn và ngăn ngừa lỗi xung đột code trong nhóm.
