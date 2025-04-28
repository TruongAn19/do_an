<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8">
    <title>Thông Báo Thuê Vợt Thành Công</title>
    <meta content="width=device-width, initial-scale=1.0" name="viewport">
    <meta content="" name="keywords">
    <meta content="" name="description">

    <!-- Google Web Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link
            href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@400;600&family=Raleway:wght@600;800&display=swap"
            rel="stylesheet">

    <!-- Icon Font Stylesheet -->
    <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.15.4/css/all.css"/>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.4.1/font/bootstrap-icons.css"
          rel="stylesheet">

    <!-- Libraries Stylesheet -->
    <link href="/client/lib/lightbox/css/lightbox.min.css" rel="stylesheet">
    <link href="/client/lib/owlcarousel/assets/owl.carousel.min.css" rel="stylesheet">

    <!-- Customized Bootstrap Stylesheet -->
    <link href="/client/css/bootstrap.min.css" rel="stylesheet">

    <!-- Template Stylesheet -->
    <link href="/client/css/style.css" rel="stylesheet">

    <style>
        .btn-primary:hover {
            background-color: #0056b3;
            border-color: #004085;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
        }

        .alert {
            background-color: #f8f9fa;
            color: #333;
            border: none;
        }

        .alert .fa-check-circle {
            color: #28a745;
        }

        .alert p {
            font-size: 16px;
            line-height: 1.5;
        }

        .alert .h5 {
            font-weight: 600;
            margin: auto;
        }
    </style>
</head>

<body>

<!-- Spinner Start -->
<div id="spinner"
     class="show w-100 vh-100 bg-white position-fixed translate-middle top-50 start-50  d-flex align-items-center justify-content-center">
    <div class="spinner-grow text-primary" role="status"></div>
</div>
<!-- Spinner End -->

<jsp:include page="../layout/header.jsp"/>
<div class="container mt-5" >
    <h2 class="text-center mb-4" style="font-family: 'Raleway', sans-serif; color: #333;">Thông Báo Thuê Vợt Thành
        Công</h2>

    <!-- Thông báo thành công với cải tiến về kiểu dáng -->
    <div class="alert alert-success shadow-sm rounded p-4 "
         style="background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb;">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <strong class="h5">Đặt thuê vợt thành công!</strong>
            <i class="fa fa-check-circle text-success" style="font-size: 2rem;"></i>
        </div>
        <p><strong>Tên vợt:</strong> ${racketName}</p>
        <p><strong>Số lượng:</strong> ${rentalTool.quantity}</p>
        <p><strong>Giá cọc:</strong> ${rentalTool.price}</p>
        <p><strong>Giá thuê:</strong> ${rentalTool.rentalPrice}</p>

        <!-- Kiểm tra loại thuê (DAILY hay ON_SITE) để hiển thị thông tin phù hợp -->
        <c:if test="${rentalTool.type == 'ON_SITE'}">
            <p><strong>Mã booking:</strong> ${rentalTool.bookingId}</p>
        </c:if>

        <c:if test="${rentalTool.type == 'DAILY'}">
            <p><strong>Số ngày thuê:</strong> ${quantityDay}</p>
            <p><strong>Ngày thuê:</strong> ${rentalDate}</p>
        </c:if>
    </div>

    <!-- Nút quay lại -->
    <div class="text-center">
        <a href="/HomePage" class="btn btn-primary rounded-pill py-2 px-4"
           style="font-size: 16px; background-color: #007bff; border-color: #007bff; transition: all 0.3s ease;">
            Trở lại
        </a>
    </div>
</div>

<!-- CSS bổ sung cho hiệu ứng và nút -->


<jsp:include page="../layout/footer.jsp"/>

<!-- Back to Top -->
<a href="#" class="btn btn-primary border-3 border-primary rounded-circle back-to-top"><i
        class="fa fa-arrow-up"></i></a>

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
