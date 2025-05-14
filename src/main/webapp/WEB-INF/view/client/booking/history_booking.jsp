<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page import="java.time.LocalDate" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8">
    <title>Lịch sử đặt sân - Sân cầu lông</title>
    <meta content="width=device-width, initial-scale=1.0" name="viewport">
    <meta content="" name="keywords">
    <meta content="" name="description">

    <!-- Google Web Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@400;600&family=Raleway:wght@600;800&display=swap" rel="stylesheet">

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

    <style>
        .booking-history-container {
            background-color: #f8f9fa;
            padding: 40px 0;
        }

        .page-title {
            font-weight: 700;
            color: #3CB815;
            margin-bottom: 30px;
            position: relative;
            display: inline-block;
        }

        .page-title:after {
            content: '';
            position: absolute;
            width: 50%;
            height: 3px;
            background: #3CB815;
            left: 0;
            bottom: -10px;
        }

        .booking-card {
            background-color: #fff;
            border-radius: 15px;
            overflow: hidden;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
            margin-bottom: 30px;
            transition: transform 0.3s ease, box-shadow 0.3s ease;
        }

        .booking-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
        }

        .booking-table {
            margin-bottom: 0;
        }

        .booking-table th {
            background-color: #f8f9fa;
            color: #495057;
            font-weight: 600;
            border-top: none;
            padding: 15px;
            white-space: nowrap;
        }

        .booking-table td {
            vertical-align: middle;
            padding: 15px;
        }

        .product-img {
            width: 80px;
            height: 80px;
            object-fit: cover;
            border-radius: 10px;
            box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
            transition: transform 0.3s ease;
        }

        .product-img:hover {
            transform: scale(1.05);
        }

        .product-name {
            font-weight: 600;
            color: #3CB815;
            text-decoration: none;
            transition: color 0.3s ease;
        }

        .product-name:hover {
            color: #2d8a10;
            text-decoration: underline;
        }

        .price {
            font-weight: 600;
            color: #212529;
        }

        .discount {
            color: #dc3545;
            font-weight: 600;
            background-color: rgba(220, 53, 69, 0.1);
            padding: 3px 8px;
            border-radius: 20px;
            display: inline-block;
        }

        .date-time {
            color: #6c757d;
            font-weight: 500;
        }

        .court-name {
            font-weight: 500;
            color: #495057;
            background-color: #e9ecef;
            padding: 5px 10px;
            border-radius: 20px;
            display: inline-block;
        }

        .total-price {
            font-weight: 700;
            color: #3CB815;
            font-size: 1.1rem;
        }

        .status {
            padding: 5px 12px;
            border-radius: 20px;
            font-weight: 600;
            font-size: 0.85rem;
            text-transform: uppercase;
            display: inline-block;
        }

        .status-pending {
            background-color: #fff3cd;
            color: #856404;
        }

        .status-confirmed {
            background-color: #d4edda;
            color: #155724;
        }

        .status-cancelled {
            background-color: #f8d7da;
            color: #721c24;
        }

        .status-completed {
            background-color: #cce5ff;
            color: #004085;
        }

        .empty-state {
            text-align: center;
            padding: 50px 20px;
            background-color: #fff;
            border-radius: 15px;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
        }

        .empty-state-icon {
            font-size: 60px;
            color: #6c757d;
            margin-bottom: 20px;
        }

        .empty-state-text {
            font-size: 18px;
            color: #6c757d;
            margin-bottom: 30px;
        }

        .btn-browse {
            background-color: #3CB815;
            color: white;
            border: none;
            padding: 10px 25px;
            border-radius: 50px;
            font-weight: 600;
            transition: all 0.3s ease;
        }

        .btn-browse:hover {
            background-color: #2d8a10;
            transform: translateY(-3px);
            box-shadow: 0 5px 15px rgba(60, 184, 21, 0.3);
        }

        .breadcrumb {
            background-color: transparent;
            padding: 0;
            margin-bottom: 30px;
        }

        .breadcrumb-item a {
            color: #3CB815;
            text-decoration: none;
            transition: color 0.3s ease;
        }

        .breadcrumb-item a:hover {
            color: #2d8a10;
        }

        .breadcrumb-item.active {
            color: #6c757d;
        }

        .breadcrumb-item + .breadcrumb-item::before {
            content: ">";
            color: #6c757d;
        }

        @media (max-width: 767.98px) {
            .booking-table {
                min-width: 800px;
            }

            .table-responsive {
                border-radius: 15px;
                box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
            }
        }
    </style>
</head>

<body>
<!-- Spinner Start -->
<div id="spinner" class="show w-100 vh-100 bg-white position-fixed translate-middle top-50 start-50 d-flex align-items-center justify-content-center">
    <div class="spinner-grow text-success" role="status"></div>
</div>
<!-- Spinner End -->

<jsp:include page="../layout/header.jsp"/>

<!-- Booking History Start -->
<div class="booking-history-container">
    <div class="container py-5">
        <div class="row">
            <div class="col-12">
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><a href="/HomePage"><i class="fas fa-home me-2"></i>Trang chủ</a></li>
                        <li class="breadcrumb-item active" aria-current="page">Lịch sử đặt sân</li>
                    </ol>
                </nav>

                <h2 class="page-title">Lịch sử đặt sân</h2>

                <c:if test="${empty bookings}">
                    <div class="empty-state">
                        <div class="empty-state-icon">
                            <i class="fas fa-calendar-times"></i>
                        </div>
                        <h4>Bạn chưa có lịch sử đặt sân nào</h4>
                        <p class="empty-state-text">Hãy đặt sân ngay để trải nghiệm dịch vụ của chúng tôi</p>
                        <a href="/main-products" class="btn btn-browse">
                            <i class="fas fa-search me-2"></i>Tìm sân ngay
                        </a>
                    </div>
                </c:if>

                <c:if test="${not empty bookings}">
                    <c:forEach var="booking" items="${bookings}" varStatus="bookingStatus">
                        <div class="booking-card">
                            <div class="table-responsive">
                                <table class="table booking-table">
                                    <thead>
                                    <tr>
                                        <th>Sản phẩm</th>
                                        <th>Tên</th>
                                        <th>Giá cả</th>
                                        <th>Giảm giá</th>
                                        <th>Ngày đặt</th>
                                        <th>Giờ đặt</th>
                                        <th>Sân đặt</th>
                                        <th>Thành tiền</th>
                                        <th>Trạng thái</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach var="bookingDetail" items="${booking.bookingDetails}" varStatus="detailStatus">
                                        <tr>
                                            <td>
                                                <img src="/images/product/${bookingDetail.product.image}" class="product-img" alt="${bookingDetail.product.name}">
                                            </td>
                                            <td>
                                                <a href="/mainProduct/${bookingDetail.product.id}" class="product-name" target="_blank">
                                                        ${bookingDetail.product.name}
                                                </a>
                                            </td>
                                            <td>
                                                        <span class="price">
                                                            <fmt:formatNumber type="number" value="${bookingDetail.product.price}"/>đ
                                                        </span>
                                            </td>
                                            <td>
                                                <c:if test="${bookingDetail.sale > 0}">
                                                            <span class="discount">
                                                                <i class="fas fa-tag me-1"></i>
                                                                <fmt:formatNumber type="number" value="${bookingDetail.sale}"/>%
                                                            </span>
                                                </c:if>
                                                <c:if test="${bookingDetail.sale == 0}">
                                                    <span>-</span>
                                                </c:if>
                                            </td>
                                            <td>
                                                        <span class="date-time">
                                                            <i class="far fa-calendar-alt me-1"></i>${bookingDetail.date}
                                                        </span>
                                            </td>
                                            <td>
                                                        <span class="date-time">
                                                            <i class="far fa-clock me-1"></i>${bookingDetail.availableTime.time}
                                                        </span>
                                            </td>
                                            <td>
                                                        <span class="court-name">
                                                                ${bookingDetail.subCourt.name}
                                                        </span>
                                            </td>
                                            <td>
                                                        <span class="total-price">
                                                            <fmt:formatNumber type="number" value="${booking.totalPrice}"/>đ
                                                        </span>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${booking.status == 'PENDING'}">
                                                        <span class="status status-pending">Chờ xác nhận</span>
                                                    </c:when>
                                                    <c:when test="${booking.status == 'CONFIRMED'}">
                                                        <span class="status status-confirmed">Đã xác nhận</span>
                                                    </c:when>
                                                    <c:when test="${booking.status == 'CANCELLED'}">
                                                        <span class="status status-cancelled">Đã hủy</span>
                                                    </c:when>
                                                    <c:when test="${booking.status == 'COMPLETED'}">
                                                        <span class="status status-completed">Hoàn thành</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="status">${booking.status}</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </c:forEach>
                </c:if>
            </div>
        </div>
    </div>
</div>
<!-- Booking History End -->

<jsp:include page="../layout/footer.jsp"/>

<!-- Back to Top -->
<a href="#" class="btn btn-success border-0 rounded-circle back-to-top">
    <i class="fa fa-arrow-up"></i>
</a>

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