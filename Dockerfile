# STEP 1: Biểu diễn máy build
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
# Sửa lại mvnw nếu bundle sẵn, hoặc dùng maven image. Ở Windows thường không có chmod +x mvnw hiệu quả trong Docker build nếu không cẩn thận.
# Lưu ý: Nếu user không có mvnw trong project, bước này sẽ lỗi.
RUN ./mvnw clean package -DskipTests

# STEP 2: Môi trường chạy thực (tiết kiệm không gian)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar run.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/run.jar"]
