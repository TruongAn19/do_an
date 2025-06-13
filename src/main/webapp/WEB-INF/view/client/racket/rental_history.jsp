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
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">

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
        body {
            font-family: 'Inter', sans-serif;
            background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
            color: #343a40;
        }

        .container-fluid {
            max-width: 1600px;
            margin: 0 auto;
            padding: 0 30px;
        }

        .page-header {
            background: linear-gradient(135deg, #4e54c8 0%, #8f94fb 100%);
            color: white;
            padding: 80px 0;
            margin-bottom: 50px;
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
            background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grid" width="20" height="20" patternUnits="userSpaceOnUse"><path d="M 20 0 L 0 0 0 20" fill="none" stroke="rgba(255,255,255,0.1)" stroke-width="1"/></pattern></defs><rect width="100" height="100" fill="url(%23grid)"/></svg>');
            opacity: 0.4;
        }

        .page-header::after {
            content: '';
            position: absolute;
            bottom: -2px;
            left: 0;
            right: 0;
            height: 30px;
            background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
            clip-path: polygon(0 100%, 100% 0, 100% 100%);
        }

        .page-header h1 {
            position: relative;
            z-index: 2;
            font-weight: 800;
            margin: 0;
            text-shadow: 0 4px 8px rgba(0,0,0,0.2);
            font-size: 3rem;
        }

        .page-header .subtitle {
            position: relative;
            z-index: 2;
            opacity: 0.9;
            margin-top: 15px;
            font-size: 1.2rem;
            font-weight: 400;
        }

        .stats-card {
            background: white;
            border-radius: 16px;
            padding: 30px 25px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.08);
            text-align: center;
            margin-bottom: 40px;
            position: relative;
            overflow: hidden;
        }

        .stats-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 4px;
            background: linear-gradient(90deg, #4e54c8 0%, #8f94fb 100%);
        }

        /* Removed hover transform effect */
        .stats-number {
            font-size: 2.5rem;
            font-weight: 800;
            background: linear-gradient(135deg, #4e54c8 0%, #8f94fb 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin-bottom: 10px;
            line-height: 1;
        }

        .stats-label {
            color: #6c757d;
            font-size: 0.95rem;
            text-transform: uppercase;
            letter-spacing: 1px;
            font-weight: 600;
        }

        .stats-icon {
            position: absolute;
            top: 20px;
            right: 20px;
            font-size: 2rem;
            color: rgba(78, 84, 200, 0.1);
        }

        .history-container {
            background: white;
            border-radius: 20px;
            box-shadow: 0 15px 35px rgba(0,0,0,0.08);
            padding: 0;
            margin-bottom: 50px;
            overflow: hidden;
            width: 100%;
        }

        .container-header {
            background: linear-gradient(135deg, #4e54c8 0%, #8f94fb 100%);
            color: white;
            padding: 25px 30px;
            margin: 0;
        }

        .container-header h3 {
            margin: 0;
            font-weight: 700;
            font-size: 1.5rem;
        }

        .table-container {
            padding: 0;
            background: white;
            width: 100%;
        }

        .table {
            margin-bottom: 0;
            border-collapse: separate;
            border-spacing: 0;
            width: 100%;
        }

        .table thead th {
            background: #f8f9fa;
            color: #495057;
            font-weight: 700;
            padding: 20px 15px;
            border: none;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            font-size: 0.85rem;
            border-bottom: 2px solid #e9ecef;
            text-align: center;
            vertical-align: middle;
        }

        .table tbody td {
            padding: 18px 15px;
            vertical-align: middle;
            border-bottom: 1px solid #f0f0f0;
            text-align: center;
        }

        .table tbody tr {
            /* Removed hover transform and background change */
            transition: background-color 0.2s ease;
        }

        .table tbody tr:hover {
            background-color: #f8f9fa;
            /* Removed transform scale effect */
        }

        .table tbody tr:last-child td {
            border-bottom: none;
        }

        .table th:nth-child(1), .table td:nth-child(1) { width: 3%; }
        .table th:nth-child(2), .table td:nth-child(2) { width: 15%; }
        .table th:nth-child(3), .table td:nth-child(3) { width: 10%; }
        .table th:nth-child(4), .table td:nth-child(4) { width: 10%; }
        .table th:nth-child(5), .table td:nth-child(5) { width: 15%; }
        .table th:nth-child(6), .table td:nth-child(6) { width: 7%; }
        .table th:nth-child(7), .table td:nth-child(7) { width: 10%; }
        .table th:nth-child(8), .table td:nth-child(8) { width: 10%; }
        .table th:nth-child(9), .table td:nth-child(9) { width: 15%; }

        .status-badge {
            padding: 10px 18px;
            border-radius: 25px;
            font-size: 0.8rem;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            border: none;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
            white-space: nowrap;
        }

        .status-pending {
            background: linear-gradient(135deg, #ffc107 0%, #ffb300 100%);
            color: white;
        }

        .status-completed {
            background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
            color: white;
        }

        .status-cancelled {
            background: linear-gradient(135deg, #dc3545 0%, #c82333 100%);
            color: white;
        }

        .status-paid {
            background: linear-gradient(135deg, #007bff 0%, #0056b3 100%);
            color: white;
        }

        .rental-code {
            font-family: 'Courier New', monospace;
            background: linear-gradient(135deg, #4e54c8 0%, #8f94fb 100%);
            color: white;
            padding: 8px 12px;
            border-radius: 8px;
            font-weight: 700;
            font-size: 0.9rem;
            letter-spacing: 1px;
            white-space: nowrap;
        }

        .rental-type {
            padding: 8px 16px;
            border-radius: 20px;
            font-size: 0.85rem;
            font-weight: 600;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            white-space: nowrap;
        }

        .type-onsite {
            background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
            color: #1565c0;
            border: 1px solid #90caf9;
        }

        .type-daily {
            background: linear-gradient(135deg, #f3e5f5 0%, #e1bee7 100%);
            color: #7b1fa2;
            border: 1px solid #ce93d8;
        }

        .price-display {
            font-weight: 700;
            background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            font-size: 1.1rem;
            white-space: nowrap;
        }

        .quantity-badge {
            background: linear-gradient(135deg, #6c757d 0%, #495057 100%);
            color: white;
            padding: 6px 12px;
            border-radius: 15px;
            font-weight: 600;
            font-size: 0.9rem;
        }

        .empty-state {
            text-align: center;
            padding: 80px 20px;
            color: #6c757d;
        }

        .empty-state i {
            font-size: 5rem;
            color: #dee2e6;
            margin-bottom: 25px;
        }

        .empty-state h5 {
            font-weight: 700;
            margin-bottom: 15px;
            color: #495057;
        }

        .pagination-wrapper {
            margin-top: 0;
            padding: 1.5rem 0;
            border-top: 1px solid #e9ecef;
            background: white;
            border-radius: 0 0 20px 20px;
            display: flex;
            justify-content: center;
            align-items: center;
            flex-wrap: wrap;
            gap: 1rem;
        }

        .pagination {
            margin: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            flex-wrap: nowrap;
            gap: 0.5rem;
        }

        .page-item {
            margin: 0;
        }

        .page-item.active .page-link {
            background: linear-gradient(135deg, #4e54c8 0%, #8f94fb 100%);
            border-color: #4e54c8;
            color: white;
            box-shadow: 0 4px 12px rgba(78, 84, 200, 0.3);
        }

        .page-link {
            color: #4e54c8;
            border: 1px solid #dee2e6;
            border-radius: 8px;
            padding: 0.6rem 0.9rem;
            font-weight: 500;
            transition: color 0.3s ease, background-color 0.3s ease;
            text-decoration: none;
            display: flex;
            align-items: center;
            justify-content: center;
            min-width: 40px;
            height: 40px;
            white-space: nowrap;
        }

        .page-link:hover {
            background-color: #f8f9fa;
            color: #4e54c8;
            border-color: #4e54c8;
            /* Removed transform translateY effect */
        }

        .page-item.disabled .page-link {
            color: #6c757d;
            background-color: #f8f9fa;
            border-color: #dee2e6;
            cursor: not-allowed;
        }

        .pagination-info {
            text-align: center;
            color: #6c757d;
            font-size: 0.9rem;
            padding: 0.5rem 1rem;
            background: #f8f9fa;
            border-radius: 8px;
            display: inline-block;
            white-space: nowrap;
        }

        .user-info {
            display: flex;
            align-items: center;
            gap: 10px;
            justify-content: center;
        }

        .user-avatar {
            width: 35px;
            height: 35px;
            background: linear-gradient(135deg, #4e54c8 0%, #8f94fb 100%);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-weight: 700;
            font-size: 0.9rem;
            flex-shrink: 0;
        }

        .user-name {
            font-weight: 600;
            overflow: hidden;
            text-overflow: ellipsis;
            max-width: 150px;
        }

        .phone-info {
            font-weight: 500;
            white-space: nowrap;
        }

        .date-info {
            font-weight: 500;
            white-space: nowrap;
        }

        @media (max-width: 1400px) {
            .container-fluid {
                max-width: 1320px;
                padding: 0 15px;
            }
        }

        @media (max-width: 1200px) {
            .container-fluid {
                max-width: 1140px;
            }

            .user-name {
                max-width: 120px;
            }
        }

        @media (max-width: 992px) {
            .container-fluid {
                max-width: 960px;
            }

            .user-name {
                max-width: 100px;
            }
        }

        @media (max-width: 768px) {
            .container-fluid {
                max-width: 720px;
                padding: 0 10px;
            }

            .page-header h1 {
                font-size: 2.2rem;
            }

            .stats-card {
                padding: 20px 15px;
                margin-bottom: 20px;
            }

            .stats-number {
                font-size: 2rem;
            }

            .table-responsive {
                overflow-x: auto;
            }

            .table {
                min-width: 800px;
            }

            .table thead th {
                padding: 15px 8px;
                font-size: 0.75rem;
            }

            .table tbody td {
                padding: 15px 8px;
                font-size: 0.85rem;
            }

            .history-container {
                margin: 0 10px 30px;
                border-radius: 15px;
            }

            .container-header {
                padding: 20px;
            }

            .rental-code {
                font-size: 0.8rem;
                padding: 6px 10px;
            }

            .status-badge {
                padding: 8px 12px;
                font-size: 0.75rem;
            }

            .pagination-wrapper {
                flex-direction: column;
                gap: 1rem;
            }

            .pagination {
                gap: 0.25rem;
                overflow-x: auto;
                padding: 0 10px;
            }

            .page-link {
                padding: 0.5rem 0.7rem;
                min-width: 35px;
                height: 35px;
                font-size: 0.9rem;
            }

            .user-name {
                max-width: 80px;
            }
        }

        @media (max-width: 576px) {
            .container-fluid {
                max-width: 100%;
                padding: 0 10px;
            }

            .page-header {
                padding: 60px 0;
            }

            .page-header h1 {
                font-size: 1.8rem;
            }

            .stats-card {
                padding: 15px;
            }

            .table {
                min-width: 700px;
            }

            .pagination {
                gap: 0.2rem;
                justify-content: flex-start;
            }

            .page-link {
                padding: 0.4rem 0.6rem;
                min-width: 32px;
                height: 32px;
                font-size: 0.85rem;
            }

            .rental-type {
                padding: 6px 10px;
                font-size: 0.75rem;
            }

            .user-name {
                max-width: 60px;
            }
        }
    </style>
</head>

<body>
<jsp:include page="../layout/header.jsp"/>

<!-- Page Header -->
<div class="page-header">
    <div class="container-fluid">
        <div class="text-center">
            <h1><i class="fas fa-history me-3"></i>Lịch Sử Thuê Vợt</h1>
            <p class="subtitle">Quản lý và theo dõi lịch sử thuê vợt cầu lông một cách hiệu quả</p>
        </div>
    </div>
</div>

<div class="container-fluid">
    <!-- Statistics Cards -->
    <div class="row mb-5">
        <div class="col-md-3 col-6">
            <div class="stats-card">
                <i class="fas fa-clipboard-list stats-icon"></i>
                <div class="stats-number">${rentalHistories.size()}</div>
                <div class="stats-label">Tổng đơn thuê</div>
            </div>
        </div>
        <div class="col-md-3 col-6">
            <div class="stats-card">
                <i class="fas fa-clock stats-icon"></i>
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
                <i class="fas fa-check-circle stats-icon"></i>
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
                <i class="fas fa-money-bill-wave stats-icon"></i>
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

    <!-- Bảng dữ liệu đơn giản -->
    <div class="history-container">
        <div class="container-header">
            <h3><i class="fas fa-table me-2"></i>Danh Sách Lịch Sử Thuê Vợt</h3>
        </div>
        <div class="table-container">
            <table class="table">
                <thead>
                <tr>
                    <th><i class="fas fa-hashtag me-2"></i>#</th>
                    <th><i class="fas fa-user me-2"></i>Khách hàng</th>
                    <th><i class="fas fa-phone me-2"></i>Liên hệ</th>
                    <th><i class="fas fa-barcode me-2"></i>Mã thuê</th>
                    <th><i class="fas fa-tag me-2"></i>Loại thuê</th>
                    <th><i class="fas fa-sort-numeric-up me-2"></i>Số lượng</th>
                    <th><i class="fas fa-calendar me-2"></i>Ngày thuê</th>
                    <th><i class="fas fa-money-bill-wave me-2"></i>Giá thuê</th>
                    <th><i class="fas fa-info-circle me-2"></i>Trạng thái</th>
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
                                    <p class="text-muted">Hiện tại chưa có đơn thuê vợt nào được ghi nhận trong hệ thống.</p>
                                </div>
                            </td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach items="${rentalHistories}" var="rental" varStatus="status">
                            <tr>
                                <td><strong>${status.index + 1}</strong></td>
                                <td>
                                    <div class="user-info">
                                        <div class="user-avatar">
                                                ${rental.fullName.substring(0,1).toUpperCase()}
                                        </div>
                                        <div class="user-name" title="${rental.fullName}">
                                                ${rental.fullName}
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <div class="phone-info">
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
                                                <i class="fas fa-building"></i>Thuê tại sân
                                            </span>
                                        </c:when>
                                        <c:when test="${rental.type == 'DAILY'}">
                                            <span class="rental-type type-daily">
                                                <i class="fas fa-calendar-day"></i>Thuê theo ngày
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="rental-type">
                                                <i class="fas fa-question"></i>Khác
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <span class="quantity-badge">${rental.quantity}</span>
                                </td>
                                <td>
                                    <div class="date-info">
                                        <i class="fas fa-calendar-alt text-muted me-2"></i>
                                            ${rental.rentalDate}
                                    </div>
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
                                                Đã hủy
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

        <!-- Pagination đơn giản -->
        <c:if test="${totalPages > 1}">
            <div class="pagination-wrapper">
                <nav>
                    <ul class="pagination">
                        <c:if test="${currentPage > 0}">
                            <li class="page-item">
                                <a class="page-link" href="?page=${currentPage - 1}&size=5">
                                    <i class="fas fa-chevron-left"></i>
                                </a>
                            </li>
                        </c:if>
                        <c:forEach begin="0" end="${totalPages - 1}" var="i">
                            <li class="page-item ${i == currentPage ? 'active' : ''}">
                                <a class="page-link" href="?page=${i}&size=5">${i + 1}</a>
                            </li>
                        </c:forEach>
                        <c:if test="${currentPage < totalPages - 1}">
                            <li class="page-item">
                                <a class="page-link" href="?page=${currentPage + 1}&size=5">
                                    <i class="fas fa-chevron-right"></i>
                                </a>
                            </li>
                        </c:if>
                    </ul>
                </nav>

                <div class="pagination-info">
                    Hiển thị ${currentPage * 5 + 1} - ${currentPage * 5 + rentalHistories.size()}
                    trong tổng số ${totalPages * 5} bài đăng
                </div>
            </div>
        </c:if>
    </div>
</div>

<jsp:include page="../layout/footer.jsp"/>

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
