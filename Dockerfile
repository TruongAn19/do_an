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
