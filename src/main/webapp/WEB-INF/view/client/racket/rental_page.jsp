<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8">
    <title> Sản Phẩm - Sân cầu lông</title>
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
    <script src="https://use.fontawesome.com/releases/v6.3.0/js/all.js"
            crossorigin="anonymous"></script>

</head>

<body>

<!-- Spinner Start -->
<div id="spinner"
     class="show w-100 vh-100 bg-white position-fixed translate-middle top-50 start-50  d-flex align-items-center justify-content-center">
    <div class="spinner-grow text-primary" role="status"></div>
</div>
<!-- Spinner End -->

<jsp:include page="../layout/header.jsp"/>

<!-- Single Product Start -->
<div class="container mt-5">
    <h2 class="text-center">Thông Tin Thuê Vợt</h2>
    <form action="/submit-rental" method="post" class="rental-form">
        <!-- CSRF token -->
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

        <div class="row mb-3">
            <div class="col-md-6">
                <label for="fullName" class="form-label">Họ và tên</label>
                <input type="text" class="form-control" id="fullName" name="fullName" required
                       placeholder="Nhập họ tên">
            </div>
            <div class="col-md-6">
                <label for="email" class="form-label">Email</label>
                <input type="email" class="form-control" id="email" name="email" required placeholder="Nhập email">
            </div>
        </div>

        <div class="row mb-3">
            <div class="col-md-6">
                <label for="phone" class="form-label">Số điện thoại</label>
                <input type="text" class="form-control" id="phone" name="phone" required
                       placeholder="Nhập số điện thoại">
            </div>
            <div class="col-md-6">
                <label for="racketName" class="form-label">Tên vợt</label>
                <input type="text" class="form-control" id="racketName" name="racketName" value="${racket.name}" readonly>
                <input type="hidden" name="racketId" value="${racket.id}">

            </div>
        </div>

        <div class="row mb-3">
            <div class="col-md-6">
                <label for="type" class="form-label">Loại Thuê</label>
                <select class="form-select" id="type" name="type" required onchange="calculateRentalPrice()">
                    <option value="ON_SITE">Thuê tại sân</option>
                    <option value="DAILY">Thuê theo ngày</option>
                </select>
            </div>
            <div class="col-md-6">
                <label for="quantity" class="form-label">Số lượng</label>
                <input type="number" class="form-control" id="quantity" name="quantity" onchange="calculateRentalPrice()" required>
            </div>
        </div>
        <!-- Thông tin Mã Booking (hiển thị khi chọn "Thuê tại sân") -->
        <div class="mb-3" id="bookingIdDiv" style="display: block;">
            <label for="bookingId" class="form-label">Mã Booking</label>
            <input type="text" class="form-control" id="bookingId" name="bookingId"
                   placeholder="Nhập mã booking"   onchange="calculateRentalPrice()">
        </div>

        <!-- Thông tin Số ngày thuê (hiển thị khi chọn "Thuê theo ngày") -->
        <div class="mb-3" id="rentalDaysDiv" style="display: none;">
            <label for="numberDate" class="form-label">Số ngày thuê</label>
            <input type="number" class="form-control" id="numberDate" name="quantityDay" min= 1
                   placeholder="Nhập số ngày thuê" onchange="calculateRentalPrice()">
        </div>

        <div class="mb-3" id="rentalDateDiv" style="display: none;">
            <label for="rentalDate" class="form-label">Ngày Thuê</label>
            <input type="date" class="form-control" id="rentalDate" name="rentalDate" >
        </div>


        <div class="row mb-3">
            <div class="col-md-6">
                <label for="price" class="form-label">Tiền Cọc</label>
                <input type="text" class="form-control" id="price" name="price" value="${racket.price}" readonly>
            </div>
            <div class="col-md-6">
                <label for="rentalPrice" class="form-label">Tiền Thuê</label>
                <input type="text" class="form-control" id="rentalPrice" name="rentalPrice" value="0" readonly>
            </div>
        </div>

        <button type="submit" class="btn btn-primary w-100">Xác Nhận Thuê Vợt</button>
    </form>
</div>

<script>
    // JavaScript để hiển thị/ẩn trường Mã Booking và Số ngày thuê khi chọn loại thuê
    document.getElementById('type').addEventListener('change', function () {
        var type = this.value;
        var bookingIdDiv = document.getElementById('bookingIdDiv');
        var rentalDaysDiv = document.getElementById('rentalDaysDiv');
        var rentalDateDiv = document.getElementById('rentalDateDiv');

        // Ẩn hoặc hiện trường Mã Booking và Số ngày thuê dựa trên loại thuê
        if (type === 'ON_SITE') {
            bookingIdDiv.style.display = 'block';
            rentalDaysDiv.style.display = 'none';
            rentalDateDiv.style.display = 'none';
            quantityDayInput.disabled = true;
        } else if (type === 'DAILY') {
            rentalDaysDiv.style.display = 'block';
            bookingIdDiv.style.display = 'none';
            rentalDateDiv.style.display = 'block';
        }


    });


    function calculateRentalPrice() {
        const type = document.getElementById('type').value;
        const rentalPricePerDay = ${racket.rentalPricePerDay}; // Giá thuê theo ngày
        const rentalPricePerPlay = ${racket.rentalPricePerPlay}; // Giá thuê tại sân
        const rentalDaysInput = document.getElementById('numberDate'); // Trường số ngày thuê
        const rentalMoneyInput = document.getElementById('rentalPrice');
        const quantity = document.getElementById('quantity');
        const quantitys = parseInt(quantity.value) || 0;
        if (type === 'DAILY') {
            const days = parseInt(rentalDaysInput.value) || 0; // Lấy số ngày thuê, mặc định 0 nếu không nhập
            rentalMoneyInput.value = days * rentalPricePerDay*quantitys ; // Tính tiền thuê thqeo số ngày
        } else {
            rentalDaysInput.value = 0;
            rentalMoneyInput.value = rentalPricePerPlay*quantitys ; // Giá thuê tại sân
        }
    }

    // Gọi hàm để tính tiền ngay khi trang được load
    calculateRentalPrice();
</script>

<style>
    .rental-form .row {
        margin-bottom: 15px;
    }

    .rental-form .form-label {
        font-weight: 600;
    }

    .rental-form .form-control {
        padding: 10px;
    }

    .rental-form .btn {
        padding: 10px;
    }
</style>

<!-- Single Product End -->

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
