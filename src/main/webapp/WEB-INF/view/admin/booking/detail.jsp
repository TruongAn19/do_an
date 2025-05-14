<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no" />
    <meta name="description" content="Hỏi Dân IT - Dự án laptopshop" />
    <meta name="author" content="Hỏi Dân IT" />
    <title>Chi tiết đơn hàng</title>
    <link href="/css/styles.css" rel="stylesheet" />
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://use.fontawesome.com/releases/v6.3.0/js/all.js" crossorigin="anonymous"></script>
    <style>
        :root {
            --primary-color: #4e73df;
            --secondary-color: #f8f9fc;
            --accent-color: #e74a3b;
        }

        body {
            background-color: #f8f9fc;
            font-family: 'Nunito', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
        }

        .card {
            border-radius: 0.75rem;
            box-shadow: 0 0.15rem 1.75rem 0 rgba(58, 59, 69, 0.15);
            margin-bottom: 1.5rem;
        }

        .card-header {
            background-color: white;
            border-bottom: 1px solid #e3e6f0;
            padding: 1rem 1.25rem;
            border-top-left-radius: 0.75rem !important;
            border-top-right-radius: 0.75rem !important;
        }

        .breadcrumb {
            background-color: transparent;
            padding: 0.75rem 1rem;
            margin-bottom: 1rem;
        }

        .breadcrumb-item a {
            color: var(--primary-color);
            text-decoration: none;
        }

        .breadcrumb-item.active {
            color: #5a5c69;
        }

        .table-container {
            background-color: white;
            border-radius: 0.75rem;
            box-shadow: 0 0.15rem 1.75rem 0 rgba(58, 59, 69, 0.15);
            padding: 1.5rem;
        }

        .table {
            margin-bottom: 0;
        }

        .table thead th {
            border-top: none;
            background-color: #f8f9fc;
            color: #5a5c69;
            font-weight: 700;
            text-transform: uppercase;
            font-size: 0.85rem;
            letter-spacing: 0.05em;
        }

        .product-img {
            width: 80px;
            height: 80px;
            object-fit: cover;
            border-radius: 0.5rem;
            border: 1px solid #e3e6f0;
        }

        .product-link {
            color: var(--primary-color);
            font-weight: 600;
            text-decoration: none;
        }

        .product-link:hover {
            text-decoration: underline;
        }

        .btn-back {
            background-color: var(--primary-color);
            border-color: var(--primary-color);
            color: white;
            font-weight: 600;
            padding: 0.5rem 1.5rem;
            border-radius: 0.5rem;
            transition: all 0.2s;
        }

        .btn-back:hover {
            background-color: #2e59d9;
            border-color: #2e59d9;
            transform: translateY(-2px);
        }

        .page-header {
            color: #5a5c69;
            font-weight: 700;
            margin-bottom: 1.5rem;
        }

        .booking-id {
            background-color: #e74a3b;
            color: white;
            padding: 0.25rem 0.75rem;
            border-radius: 0.5rem;
            font-size: 0.9rem;
            font-weight: 600;
        }

        .rental-table th {
            background-color: #4e73df !important;
            color: white !important;
        }

        .rental-badge {
            display: inline-block;
            padding: 0.25rem 0.75rem;
            border-radius: 0.5rem;
            font-weight: 600;
        }

        .badge-onsite {
            background-color: #1cc88a;
            color: white;
        }

        .badge-daily {
            background-color: #f6c23e;
            color: white;
        }

        .badge-other {
            background-color: #858796;
            color: white;
        }

        .price {
            font-weight: 700;
            color: #e74a3b;
        }
    </style>
</head>

<body class="sb-nav-fixed">
<jsp:include page="../layout/header.jsp" />
<div id="layoutSidenav">
    <jsp:include page="../layout/sidebar.jsp" />
    <div id="layoutSidenav_content">
        <main>
            <div class="container-fluid px-4">
                <h1 class="mt-4 page-header">Chi tiết đơn hàng</h1>
                <ol class="breadcrumb mb-4">
                    <li class="breadcrumb-item"><a href="/admin"><i class="fas fa-tachometer-alt me-1"></i> Dashboard</a></li>
                    <li class="breadcrumb-item"><a href="/admin/booking"><i class="fas fa-shopping-cart me-1"></i> Đơn hàng</a></li>
                    <li class="breadcrumb-item active">Chi tiết đơn hàng</li>
                </ol>

                <div class="card mb-4">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <div>
                            <h3 class="mb-0"><i class="fas fa-info-circle me-2"></i> Chi tiết đơn hàng</h3>
                            <p class="text-muted mb-0">Mã đơn hàng: <span class="booking-id">${id}</span></p>
                        </div>
                        <a href="/admin/booking" class="btn btn-back">
                            <i class="fas fa-arrow-left me-1"></i> Quay lại
                        </a>
                    </div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-hover">
                                <thead>
                                <tr>
                                    <th scope="col" width="15%">Sản phẩm</th>
                                    <th scope="col" width="40%">Tên</th>
                                    <th scope="col" width="20%">Giá cả</th>
                                    <th scope="col" width="25%">Thành tiền</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:if test="${empty bookingDetails}">
                                    <tr>
                                        <td colspan="6" class="text-center py-4">
                                            <i class="fas fa-shopping-cart fa-2x mb-3 text-muted"></i>
                                            <p class="mb-0">Không có sản phẩm trong giỏ hàng</p>
                                        </td>
                                    </tr>
                                </c:if>
                                <c:forEach var="bookingDetail" items="${bookingDetails}">
                                    <tr>
                                        <th scope="row">
                                            <div class="d-flex align-items-center">
                                                <img src="/images/product/${bookingDetail.product.image}"
                                                     class="product-img" alt="${bookingDetail.product.name}">
                                            </div>
                                        </th>
                                        <td>
                                            <p class="mb-0 mt-4">
                                                <a href="/mainProduct/${bookingDetail.product.id}"
                                                   class="product-link" target="_blank">
                                                        ${bookingDetail.product.name}
                                                </a>
                                            </p>
                                        </td>
                                        <td>
                                            <p class="mb-0 mt-4 price">
                                                <fmt:formatNumber type="number" value="${bookingDetail.product.price}" /> đ
                                            </p>
                                        </td>
                                        <td>
                                            <p class="mb-0 mt-4 price">
                                                <fmt:formatNumber type="number" value="${bookingDetail.price}" /> đ
                                            </p>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <div class="card">
                    <div class="card-header">
                        <h3 class="mb-0"><i class="fas fa-tools me-2"></i> Thông tin thuê thiết bị</h3>
                    </div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-striped table-bordered rental-table">
                                <thead class="text-center">
                                <tr>
                                    <th>#</th>
                                    <th>Mã thuê</th>
                                    <th>Loại Thuê</th>
                                    <th>Số lượng</th>
                                    <th>Ngày Thuê</th>
                                    <th>Tiền Thuê</th>
                                </tr>
                                </thead>
                                <tbody class="text-center">
                                <c:forEach items="${rentalTool}" var="rental" varStatus="status">
                                    <tr>
                                        <td>${rental.racketId}</td>
                                        <td>${rental.rentalToolCode}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${rental.type == 'ON_SITE'}">
                                                            <span class="rental-badge badge-onsite">
                                                                <i class="fas fa-map-marker-alt me-1"></i> Thuê tại sân
                                                            </span>
                                                </c:when>
                                                <c:when test="${rental.type == 'DAILY'}">
                                                            <span class="rental-badge badge-daily">
                                                                <i class="fas fa-calendar-day me-1"></i> Thuê theo ngày
                                                            </span>
                                                </c:when>
                                                <c:otherwise>
                                                            <span class="rental-badge badge-other">
                                                                <i class="fas fa-question-circle me-1"></i> Khác
                                                            </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td><span class="fw-bold">${rental.quantity}</span></td>
                                        <td>${rental.rentalDate}</td>
                                        <td class="price"><fmt:formatNumber value="${rental.rentalPrice}" type="number" /> đ</td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </main>
        <jsp:include page="../layout/footer.jsp" />
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js" crossorigin="anonymous"></script>
<script src="/js/scripts.js"></script>
</body>

</html>