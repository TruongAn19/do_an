<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Lịch sử thuê vợt - Sân cầu lông</title>
    <meta content="width=device-width, initial-scale=1.0" name="viewport">

    <!-- Google Web Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@400;600&family=Raleway:wght@600;800&display=swap"
          rel="stylesheet">

    <!-- Icon Font Stylesheet -->
    <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.15.4/css/all.css"/>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.4.1/font/bootstrap-icons.css" rel="stylesheet">

    <!-- Libraries Stylesheet -->
    <link href="/client/lib/lightbox/css/lightbox.min.css" rel="stylesheet">
    <link href="/client/lib/owlcarousel/assets/owl.carousel.min.css" rel="stylesheet">

    <!-- Customized Bootstrap Stylesheet -->
    <link href="/client/css/bootstrap.min.css" rel="stylesheet">

    <!-- Template Stylesheet -->
    <link href="/client/css/style.css" rel="stylesheet">

    <!-- Custom Styles -->
    <style>
        .page-header {
            background: linear-gradient(135deg, #2E7D32 0%, #4CAF50 100%);
            color: white;
            padding: 60px 0;
            margin-bottom: 40px;
            position: relative;
            overflow: hidden;
        }

        .page-header::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grid" width="10" height="10" patternUnits="userSpaceOnUse"><path d="M 10 0 L 0 0 0 10" fill="none" stroke="rgba(255,255,255,0.1)" stroke-width="1"/></pattern></defs><rect width="100" height="100" fill="url(%23grid)"/></svg>');
            opacity: 0.3;
        }

        .page-header h1 {
            position: relative;
            z-index: 2;
            font-weight: 700;
            margin: 0;
            text-shadow: 0 2px 4px rgba(0,0,0,0.3);
        }

        .page-header .subtitle {
            position: relative;
            z-index: 2;
            opacity: 0.9;
            margin-top: 10px;
        }

        .history-container {
            background: white;
            border-radius: 15px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.1);
            padding: 30px;
            margin-bottom: 40px;
        }

        .table-container {
            background: white;
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 4px 15px rgba(0,0,0,0.08);
        }

        .table {
            margin-bottom: 0;
            border-collapse: separate;
            border-spacing: 0;
        }

        .table thead th {
            background: linear-gradient(135deg, #2E7D32, #388E3C);
            color: white;
            font-weight: 600;
            padding: 18px 15px;
            border: none;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            font-size: 0.85rem;
            position: relative;
        }

        .table thead th:first-child {
            border-top-left-radius: 12px;
        }

        .table thead th:last-child {
            border-top-right-radius: 12px;
        }

        .table tbody td {
            padding: 16px 15px;
            vertical-align: middle;
            border-bottom: 1px solid #f0f0f0;
            transition: background-color 0.2s;
        }

        .table tbody tr:hover {
            background-color: #f8f9fa;
        }

        .table tbody tr:last-child td {
            border-bottom: none;
        }

        .status-badge {
            padding: 8px 16px;
            border-radius: 20px;
            font-size: 0.8rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            border: none;
            display: inline-flex;
            align-items: center;
            gap: 5px;
        }

        .status-pending {
            background-color: #FFF3CD;
            color: #856404;
            border: 1px solid #FFEAA7;
        }

        .status-completed {
            background-color: #D4EDDA;
            color: #155724;
            border: 1px solid #C3E6CB;
        }

        .status-cancelled {
            background-color: #F8D7DA;
            color: #721C24;
            border: 1px solid #F5C6CB;
        }

        .status-paid {
            background-color: #CCE5FF;
            color: #004085;
            border: 1px solid #B3D7FF;
        }

        .rental-code {
            font-family: 'Courier New', monospace;
            background-color: #f8f9fa;
            padding: 4px 8px;
            border-radius: 4px;
            font-weight: 600;
            color: #495057;
        }

        .rental-type {
            padding: 6px 12px;
            border-radius: 15px;
            font-size: 0.85rem;
            font-weight: 500;
        }

        .type-onsite {
            background-color: #E3F2FD;
            color: #1565C0;
        }

        .type-daily {
            background-color: #F3E5F5;
            color: #7B1FA2;
        }

        .price-display {
            font-weight: 600;
            color: #2E7D32;
            font-size: 1.05rem;
        }

        .modal-content {
            border-radius: 15px;
            border: none;
            box-shadow: 0 20px 40px rgba(0,0,0,0.15);
        }

        .modal-header {
            background: linear-gradient(135deg, #2E7D32, #388E3C);
            color: white;
            border-radius: 15px 15px 0 0;
            padding: 20px 25px;
        }

        .modal-title {
            font-weight: 600;
            margin: 0;
        }

        .btn-close {
            filter: brightness(0) invert(1);
        }

        .modal-body {
            padding: 25px;
        }

        .detail-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 12px 0;
            border-bottom: 1px solid #f0f0f0;
        }

        .detail-item:last-child {
            border-bottom: none;
        }

        .detail-label {
            font-weight: 600;
            color: #333;
        }

        .detail-value {
            color: #666;
        }

        .form-label {
            font-weight: 600;
            color: #333;
            margin-bottom: 8px;
        }

        .form-select {
            border-radius: 8px;
            padding: 10px 15px;
            border: 1px solid #ddd;
            transition: all 0.3s;
        }

        .form-select:focus {
            border-color: #4CAF50;
            box-shadow: 0 0 0 0.25rem rgba(76, 175, 80, 0.25);
        }

        .btn-update {
            background-color: #2E7D32;
            color: white;
            border: none;
            border-radius: 8px;
            padding: 10px 25px;
            font-weight: 600;
            transition: all 0.3s;
        }

        .btn-update:hover {
            background-color: #1B5E20;
            transform: translateY(-1px);
        }

        .empty-state {
            text-align: center;
            padding: 60px 20px;
            color: #666;
        }

        .empty-state i {
            font-size: 4rem;
            color: #ddd;
            margin-bottom: 20px;
        }

        .stats-card {
            background: white;
            border-radius: 12px;
            padding: 20px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.08);
            text-align: center;
            margin-bottom: 30px;
        }

        .stats-number {
            font-size: 2rem;
            font-weight: 700;
            color: #2E7D32;
            margin-bottom: 5px;
        }

        .stats-label {
            color: #666;
            font-size: 0.9rem;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        @media (max-width: 768px) {
            .table-responsive {
                border-radius: 12px;
            }

            .table thead th {
                padding: 12px 8px;
                font-size: 0.75rem;
            }

            .table tbody td {
                padding: 12px 8px;
                font-size: 0.85rem;
            }

            .history-container {
                padding: 20px;
                margin: 0 10px 30px;
            }
        }
    </style>
</head>

<body>
<jsp:include page="../layout/header.jsp"/>

<!-- Page Header -->
<div class="page-header">
    <div class="container">
        <div class="text-center">
            <h1><i class="fas fa-history me-3"></i>Lịch Sử Thuê Vợt</h1>
            <p class="subtitle">Quản lý và theo dõi lịch sử thuê vợt cầu lông</p>
        </div>
    </div>
</div>

<div class="container">
    <!-- Statistics Cards -->
    <div class="row mb-4">
        <div class="col-md-3 col-6">
            <div class="stats-card">
                <div class="stats-number">${rentalHistories.size()}</div>
                <div class="stats-label">Tổng đơn thuê</div>
            </div>
        </div>
        <div class="col-md-3 col-6">
            <div class="stats-card">
                <div class="stats-number">
                    <c:set var="pendingCount" value="0"/>
                    <c:forEach items="${rentalHistories}" var="rental">
                        <c:if test="${rental.status == 'PENDING'}">
                            <c:set var="pendingCount" value="${pendingCount + 1}"/>
                        </c:if>
                    </c:forEach>
                    ${pendingCount}
                </div>
                <div class="stats-label">Chờ xác nhận</div>
            </div>
        </div>
        <div class="col-md-3 col-6">
            <div class="stats-card">
                <div class="stats-number">
                    <c:set var="completedCount" value="0"/>
                    <c:forEach items="${rentalHistories}" var="rental">
                        <c:if test="${rental.status == 'COMPLETED'}">
                            <c:set var="completedCount" value="${completedCount + 1}"/>
                        </c:if>
                    </c:forEach>
                    ${completedCount}
                </div>
                <div class="stats-label">Đã hoàn tất</div>
            </div>
        </div>
        <div class="col-md-3 col-6">
            <div class="stats-card">
                <div class="stats-number">
                    <c:set var="totalRevenue" value="0"/>
                    <c:forEach items="${rentalHistories}" var="rental">
                        <c:if test="${rental.status == 'COMPLETED' || rental.status == 'PAID'}">
                            <c:set var="totalRevenue" value="${totalRevenue + rental.rentalPrice}"/>
                        </c:if>
                    </c:forEach>
                    <fmt:formatNumber value="${totalRevenue}" type="number"/>
                </div>
                <div class="stats-label">Doanh thu (đ)</div>
            </div>
        </div>
    </div>

    <div class="history-container">
        <div class="table-container">
            <div class="table-responsive">
                <table class="table">
                    <thead>
                    <tr>
                        <th><i class="fas fa-hashtag me-2"></i>#</th>
                        <th><i class="fas fa-user me-2"></i>Họ và Tên</th>
                        <th><i class="fas fa-phone me-2"></i>Số điện thoại</th>
                        <th><i class="fas fa-barcode me-2"></i>Mã thuê vợt</th>
                        <th><i class="fas fa-tag me-2"></i>Loại Thuê</th>
                        <th><i class="fas fa-sort-numeric-up me-2"></i>Số lượng</th>
                        <th><i class="fas fa-calendar me-2"></i>Ngày Thuê</th>
                        <th><i class="fas fa-money-bill-wave me-2"></i>Tiền Thuê</th>
                        <th><i class="fas fa-info-circle me-2"></i>Trạng Thái</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${empty rentalHistories}">
                            <tr>
                                <td colspan="9">
                                    <div class="empty-state">
                                        <i class="fas fa-inbox"></i>
                                        <h5>Chưa có lịch sử thuê vợt</h5>
                                        <p class="text-muted">Hiện tại chưa có đơn thuê vợt nào được ghi nhận.</p>
                                    </div>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach items="${rentalHistories}" var="rental" varStatus="status">
                                <tr>
                                    <td><strong>${status.index + 1}</strong></td>
                                    <td>
                                        <div class="d-flex align-items-center">
                                            <i class="fas fa-user-circle text-muted me-2"></i>
                                                ${rental.fullName}
                                        </div>
                                    </td>
                                    <td>
                                        <div class="d-flex align-items-center">
                                            <i class="fas fa-phone text-muted me-2"></i>
                                                ${rental.phone}
                                        </div>
                                    </td>
                                    <td>
                                        <span class="rental-code">${rental.rentalToolCode}</span>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${rental.type == 'ON_SITE'}">
                                                        <span class="rental-type type-onsite">
                                                            <i class="fas fa-building me-1"></i>Thuê tại sân
                                                        </span>
                                            </c:when>
                                            <c:when test="${rental.type == 'DAILY'}">
                                                        <span class="rental-type type-daily">
                                                            <i class="fas fa-calendar-day me-1"></i>Thuê theo ngày
                                                        </span>
                                            </c:when>
                                            <c:otherwise>
                                                        <span class="rental-type">
                                                            <i class="fas fa-question me-1"></i>Khác
                                                        </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <span class="badge bg-light text-dark">${rental.quantity}</span>
                                    </td>
                                    <td>
                                        <i class="fas fa-calendar-alt text-muted me-2"></i>
                                            ${rental.rentalDate}
                                    </td>
                                    <td>
                                                <span class="price-display">
                                                    <fmt:formatNumber value="${rental.rentalPrice}" type="number"/> đ
                                                </span>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${rental.status == 'PENDING'}">
                                                        <span class="status-badge status-pending">
                                                            <i class="fas fa-clock"></i>
                                                            Chờ xác nhận
                                                        </span>
                                            </c:when>
                                            <c:when test="${rental.status == 'COMPLETED'}">
                                                        <span class="status-badge status-completed">
                                                            <i class="fas fa-check-circle"></i>
                                                            Đã hoàn tất
                                                        </span>
                                            </c:when>
                                            <c:when test="${rental.status == 'CANCELLED'}">
                                                        <span class="status-badge status-cancelled">
                                                            <i class="fas fa-times-circle"></i>
                                                            Hủy
                                                        </span>
                                            </c:when>
                                            <c:when test="${rental.status == 'PAID'}">
                                                        <span class="status-badge status-paid">
                                                            <i class="fas fa-credit-card"></i>
                                                            Đã thanh toán
                                                        </span>
                                            </c:when>
                                            <c:otherwise>
                                                        <span class="status-badge bg-secondary">
                                                            <i class="fas fa-question"></i>
                                                            Không xác định
                                                        </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<!-- Modal Xem chi tiết -->
<div class="modal fade" id="viewDetailsModal" tabindex="-1" aria-labelledby="viewDetailsModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="viewDetailsModalLabel">
                    <i class="fas fa-info-circle me-2"></i>Chi Tiết Đơn Thuê
                </h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <div id="rentalDetailsContent">
                    <!-- Nội dung chi tiết đơn thuê sẽ được hiển thị ở đây -->
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Modal Cập nhật -->
<div class="modal fade" id="updateRentalModal" tabindex="-1" aria-labelledby="updateRentalModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="updateRentalModalLabel">
                    <i class="fas fa-edit me-2"></i>Cập Nhật Đơn Thuê
                </h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <form id="updateRentalForm">
                    <div class="mb-3">
                        <label for="rentalStatus" class="form-label">
                            <i class="fas fa-flag me-2"></i>Trạng thái
                        </label>
                        <select id="rentalStatus" class="form-select">
                            <option value="PENDING">Chờ xác nhận</option>
                            <option value="COMPLETED">Đã hoàn tất</option>
                        </select>
                    </div>
                    <div class="d-grid">
                        <button type="submit" class="btn-update">
                            <i class="fas fa-save me-2"></i>Cập Nhật
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../layout/footer.jsp"/>

<!-- Back to Top -->
<a href="#" class="btn btn-success border-3 border-success rounded-circle back-to-top">
    <i class="fa fa-arrow-up"></i>
</a>

<script>
    function viewDetails(rentalId) {
        // Gọi API để lấy chi tiết đơn thuê
        fetch(`/api/rentals/${rentalId}`)
            .then(response => response.json())
            .then(data => {
                // Điền chi tiết vào modal
                const detailsContent = `
                        <div class="detail-item">
                            <span class="detail-label"><i class="fas fa-user me-2"></i>Họ và Tên:</span>
                            <span class="detail-value">${data.fullName}</span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label"><i class="fas fa-envelope me-2"></i>Email:</span>
                            <span class="detail-value">${data.email}</span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label"><i class="fas fa-calendar me-2"></i>Ngày Thuê:</span>
                            <span class="detail-value">${data.rentalDate}</span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label"><i class="fas fa-flag me-2"></i>Trạng Thái:</span>
                            <span class="detail-value">${data.status}</span>
                        </div>
                    `;
                document.getElementById('rentalDetailsContent').innerHTML = detailsContent;
            });
    }

    function loadRentalForUpdate(rentalId) {
        // Gọi API để lấy chi tiết đơn thuê
        fetch(`/api/rentals/${rentalId}`)
            .then(response => response.json())
            .then(data => {
                // Điền thông tin vào form cập nhật
                document.getElementById('rentalStatus').value = data.status;
                // Điền các trường khác nếu cần
            });
    }
</script>

<!-- JavaScript Libraries -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.4/jquery.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="/client/lib/easing/easing.min.js"></script>
<script src="/client/lib/waypoints/waypoints.min.js"></script>
<script src="/client/lib/lightbox/js/lightbox.min.js"></script>
<script src="/client/lib/owlcarousel/owl.carousel.min.js"></script>

<!-- Template Javascript -->
<script src="/client/js/main.js"></script>
</body>
</html>