# Giai đoạn B5: Đóng gói dự án

Chuyển hệ thống lên Docker chuẩn bị cho CD chạy trên Server thực như AWS, Azure hay Digital Ocean.

## Tạo Dockerfile 
Tại thư mục gốc của Backend tạo file `Dockerfile`.
Đây là kỹ thuật Multi-stage build (vừa build jar sạch sẽ vừa có image nhẹ tối thiểu hóa).

```dockerfile
# STEP 1: Biểu diễn máy build
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# STEP 2: Môi trường chạy thực (tiết kiệm không gian)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar run.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/run.jar"]
```

## Vận Hành System Backend Ngầm
Chạy Command `docker build -t sancaulong-api .`
Chạy tiếp con Container `docker run -d -p 8080:8080 --name backend-api sancaulong-api`

Bây giờ server Backend đã trở thành một nền tảng độc lập, độc quyền mở cổng 8080 và cung cấp Data. Mọi nâng cấp sửa chữa cho backend sẽ không làm chập chờn mã nguồn của Frontend.
