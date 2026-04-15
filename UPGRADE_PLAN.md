# Kế Hoạch Nâng Cấp Tách Rời Hệ Thống

Tài liệu này hướng dẫn chiến lược nâng cấp và tái cấu trúc hệ thống.

## 1. Nâng Cấp Backend (Spring Boot)
Thư mục chứa tài liệu: `docs/upgrade-guide/backend/`

* [Giai đoạn B1: Chuyển đổi thành RESTful API](docs/upgrade-guide/backend/01-rest-api.md)
* [**Giai đoạn B1b: Bản Đồ Ánh Xạ Mọi API & Services Chi Tiết**](docs/upgrade-guide/backend/01b-api-service-mapping.md) *(Quan Trọng Bậc Nhất)*
* [Giai đoạn B2: Quản lý Phiên bản Database (Flyway)](docs/upgrade-guide/backend/02-database.md)
* [Giai đoạn B3: Áp dụng Stateless JWT Authentication](docs/upgrade-guide/backend/03-security.md)
* [Giai đoạn B4: Kiến Trúc Sạch (Clean Architecture) & MapStruct](docs/upgrade-guide/backend/04-architecture.md)
* [Giai đoạn B5: Đóng Gói Với Docker](docs/upgrade-guide/backend/05-docker.md)
* [**Giai đoạn B6: Tính Năng Nâng Cao (Strategy, Rate Limit, ML Forecasting)**](docs/upgrade-guide/backend/06-advanced-features.md)

## 2. Xây Dựng Frontend (Angular & Bootstrap)
Thư mục chứa tài liệu: `docs/upgrade-guide/frontend/`

* [Giai đoạn F1: Đóng Gói Và Cài Đặt Base Tầng Giao Diện](docs/upgrade-guide/frontend/01-setup.md)
* [Giai đoạn F2: Ánh Xạ UI Client Tự Động Từ JSP](docs/upgrade-guide/frontend/02-ui-mapping-client.md)
* [Giai đoạn F3: Ánh Xạ UI Admin Từ JSP](docs/upgrade-guide/frontend/03-ui-mapping-admin.md)
* [Giai đoạn F4: Nhúng API HttpClient và JWT](docs/upgrade-guide/frontend/04-api-integration.md)

## 3. Vận Hành Tự Động Hóa (DevOps)
Công nghệ đưa code mới lên Server và đảm bảo hệ thống không bị lỗi nhảm.

* [Giai đoạn 6: Triển Khai CI/CD Pipeline (GitHub Actions)](docs/upgrade-guide/06-ci-cd-pipeline.md)
