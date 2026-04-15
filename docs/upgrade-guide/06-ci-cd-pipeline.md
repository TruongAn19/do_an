# Giai đoạn 6: Triển Khai CI/CD Pipeline (Tích Hợp Liên Tục & Giao Hàng Liên Tục)

Khi dự án đã được chia nhỏ thành Backend (Spring Boot) và Frontend (Angular) sử dụng chung một kho lưu trữ (Monorepo), việc thiết lập một quy trình tự động hóa (CI/CD) sẽ giúp bạn chấm dứt cảnh phải tự tay gõ lệnh build file JAR, build Angular và đẩy lên Server thủ công.

Chúng ta sẽ sử dụng **GitHub Actions** - Công cụ CI/CD miễn phí và xịn nhất hiện hành dành cho các project lưu ở GitHub.

## 1. Cơ Chế Hoạt Động Của Pipeline
Mỗi khi có ai đó đẩy Code (Push) hoặc hợp nhất (Merge) vào nhánh `main`:
1. **Runner** của Github tự bật lên.
2. Cài đặt Java 17 và NodeJS 20.
3. Chạy lệnh Build Backend (`./mvnw clean package`) để kiểm tra xem source Java có bị lỗi compile không.
4. Chạy lệnh Build Frontend (`ng build`) để gom CSS/JS/HTML.
5. (Bước CD Option) Tự động Build thành Docker Image và ném vào Server AWS/VPS của bạn để chạy website thực tế.

## 2. Xây Dựng File Template GitHub Actions
Quy định hệ thống: Mọi workflow của Github bắt buộc nằm trong đường dẫn tĩnh: `.github/workflows/`. Thay vì nhúng tay, bạn sẽ tạo tệp `ci-cd.yml` với kịch bản sau:

```yaml
name: QuanLySancaulong CI/CD

# Chỉ kích hoạt tự động chạy khi có code đẩy lên nhánh main
on:
  push:
    branches: [ "main" ]
  pull_request:
    branches: [ "main" ]

jobs:
  # ------ JOB 1: BUILD BACKEND ------
  build-backend:
    runs-on: ubuntu-latest
    steps:
    - name: Checkout Source Code
      uses: actions/checkout@v4
      
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: maven
        
    - name: Cấp quyền thực thi cho Maven wrapper
      run: chmod +x mvnw
      
    - name: Build Spring Boot Backend với Maven
      run: ./mvnw clean package -DskipTests
      
    - name: Upload Tạm File JAR nều cần sang CD (Tùy chọn)
      uses: actions/upload-artifact@v4
      with:
        name: backend-jar
        path: target/*.jar

  # ------ JOB 2: BUILD FRONTEND ------
  build-frontend:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: ./quanly-frontend # Nhảy vô thư mục chứa source Angular
    steps:
    - name: Checkout Source Code
      uses: actions/checkout@v4
      
    - name: Set up Node.js 20
      uses: actions/setup-node@v4
      with:
        node-version: '20'
        cache: 'npm'
        cache-dependency-path: './quanly-frontend/package-lock.json'
        
    - name: Cài đặt các thư viện (NPM Install)
      run: npm ci
      
    - name: Compile Angular Code (Build)
      run: npm run build --configuration production
      
    - name: Upload File tĩnh Angular 
      uses: actions/upload-artifact@v4
      with:
        name: frontend-dist
        path: quanly-frontend/dist/

  # ------ JOB 3: DEPLOY DOCKER (Tùy Chọn Mở Rộng Sau Này) ------
  # deploy:
  #   needs: [build-backend, build-frontend]
  #   runs-on: ubuntu-latest
  #   steps:
  #    - name: Thực thi ssh login vô VPS, pull ảnh docker và compose up...
```

## 3. Quản Trị Bí Mật (Secrets)
Nếu tương lai bạn đưa khâu Deploy tự động lên Server (VPS/AWS RDS), đừng bao giờ viết mật khẩu của cơ sở dữ liệu `TruongAn1910` cứng vào YAML. 
- Vào Github > Settings > Secrets and variables > Actions.
- Tạo một biến `SPRING_DATASOURCE_PASSWORD`.
- Trong file `application.properties` chỉnh thành `${SPRING_DATASOURCE_PASSWORD}`, Pipeline sẽ nhét khóa vào cực kì bảo mật.

Bằng việc cấu hình file này, trong sơ yếu lý lịch (CV) của bạn sẽ vô cùng lộng lẫy điểm sáng rực rỡ bởi cụm từ: *"Có thiết kế và vận hành CI/CD Automation bằng GitHub Actions cho ứng dụng Fullstack Monorepo"*.
