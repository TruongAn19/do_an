<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8">
    <title>Sản Phẩm - Sân cầu lông</title>
    <meta content="width=device-width, initial-scale=1.0" name="viewport">
    <meta content="" name="keywords">
    <meta content="" name="description">

    <!-- Google Web Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
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
    <script src="https://use.fontawesome.com/releases/v6.3.0/js/all.js" crossorigin="anonymous"></script>

    <style>
        .rental-form .form-label {
            font-weight: 600;
            color: #333;
            margin-bottom: 0.5rem;
        }

        .rental-form .form-control {
            padding: 0.6rem 0.75rem;
            border-radius: 0.375rem;
            border: 1px solid #ced4da;
            transition: border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
        }

        .rental-form .form-control:focus {
            border-color: #86b7fe;
            box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25);
        }

        .rental-form .form-control[readonly] {
            background-color: #f8f9fa;
        }

        .rental-form .input-group-text {
            background-color: #f8f9fa;
        }

        .rental-form .btn-primary {
            font-weight: 600;
            letter-spacing: 0.5px;
            transition: all 0.3s ease;
        }

        .rental-form .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
        }

        .card {
            border-radius: 0.75rem;
            overflow: hidden;
            transition: all 0.3s ease;
        }

        .card:hover {
            box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
        }

        .card-header {
            border-bottom: none;
        }

        @media (max-width: 767.98px) {
            .container.rental-container {
                padding-left: 15px;
                padding-right: 15px;
            }
        }

        .price-badge {
            position: absolute;
            top: 10px;
            right: 10px;
            background-color: #28a745;
            color: white;
            padding: 5px 10px;
            border-radius: 20px;
            font-weight: bold;
        }
    </style>
</head>

<body>

<!-- Spinner Start -->
<div id="spinner"
     class="show w-100 vh-100 bg-white position-fixed translate-middle top-50 start-50 d-flex align-items-center justify-content-center">
    <div class="spinner-grow text-primary" role="status"></div>
</div>
<!-- Spinner End -->

<jsp:include page="../layout/header.jsp"/>

<!-- Single Product Start -->
<div class="container rental-container mt-5 py-4">
    <div class="row justify-content-center" style="margin-top: 100px">
        <div class="col-lg-8">
            <div class="card shadow-sm border-0">
                <div class="card-header bg-primary text-white text-center py-3">
                    <h3 class="mb-0">Thông Tin Thuê Vợt</h3>
                </div>
                <div class="card-body p-4">
                    <form action="/submit-rental" method="post" class="rental-form">
                        <!-- CSRF token -->
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                        <input type="hidden" id="type" name="type" value="${typeOrder}"/>

                        <div class="row mb-4">
                            <div class="col-md-6 mb-3 mb-md-0">
                                <label for="fullName" class="form-label">Họ và tên</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-user"></i></span>
                                    <input type="text" class="form-control" id="fullName" name="fullName" required
                                           placeholder="Nhập họ tên">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label for="email" class="form-label">Email</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-envelope"></i></span>
                                    <input type="email" class="form-control" id="email" name="email" required
                                           placeholder="Nhập email">
                                </div>
                            </div>
                        </div>

                        <div class="row mb-4">
                            <div class="col-md-6 mb-3 mb-md-0">
                                <label for="phone" class="form-label">Số điện thoại</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-phone"></i></span>
                                    <input type="text" class="form-control" id="phone" name="phone" required
                                           placeholder="Nhập số điện thoại">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label for="racketName" class="form-label">Tên vợt</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-table-tennis"></i></span>
                                    <input type="text" class="form-control" id="racketName" name="racketName"
                                           value="${racket.name}" readonly>
                                </div>
                                <input id="racketId" type="hidden" name="racketId" value="${racket.id}">
                            </div>
                        </div>

                        <!-- Trường hiển thị khi thuê theo ngày -->
                        <div class="row mb-4">
                            <div class="col-md-6 mb-3 mb-md-0" id="rentalDateDiv" style="display: none;">
                                <label for="rentalDate" class="form-label">Ngày Thuê</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-calendar"></i></span>
                                    <input type="date" class="form-control" id="rentalDate" name="rentalDate">
                                </div>
                            </div>

                            <div class="col-md-6 mb-3 mb-md-0" id="rentalDaysDiv" style="display: none;">
                                <label for="numberDate" class="form-label">Số ngày thuê</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-calendar-day"></i></span>
                                    <input type="number" class="form-control" id="numberDate" name="quantityDay" min="1"
                                           value="1" placeholder="Nhập số ngày thuê" onchange="calculateRentalPrice()">
                                </div>
                            </div>
                        </div>

                        <div class="row mb-4">
                            <div class="col-md-6 mb-3 mb-md-0">
                                <label for="typeDisplay" class="form-label">Loại Thuê</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-tag"></i></span>
                                    <input type="text" class="form-control" id="typeDisplay"
                                           value="${typeOrder == 'DAILY' ? 'Thuê theo ngày' : 'Thuê tại sân'}" readonly>
                                </div>
                            </div>

                            <div class="col-md-6">
                                <label for="quantity"
                                       class="form-label d-flex justify-content-between align-items-center">
                                    <span>Số lượng</span>
                                    <span id="availableStockLabel" class="text-success small"></span>
                                </label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-sort-numeric-up"></i></span>
                                    <input type="number" class="form-control" id="quantity" name="quantity"
                                           onchange="calculateRentalPrice()" required min="1" value="1">
                                </div>
                            </div>
                        </div>

                        <!-- Trường hiển thị khi thuê tại sân -->
                        <div class="mb-4" id="bookingIdDiv" style="display: none;">
                            <label for="bookingId" class="form-label">Mã Booking</label>
                            <div class="input-group">
                                <span class="input-group-text"><i class="fas fa-bookmark"></i></span>
                                <input type="text" class="form-control" id="bookingId" name="bookingId"
                                       value="${bookingCode}">
                            </div>
                        </div>

                        <div class="row mb-4">
                            <c:if test="${typeOrder == 'DAILY'}">
                                <div class="col-md-6 mb-3 mb-md-0">
                                    <label for="price" class="form-label">Tiền Cọc</label>
                                    <div class="input-group">
                                        <span class="input-group-text"><i class="fas fa-money-bill"></i></span>
                                        <input type="text" class="form-control" id="price" name="price"
                                               value="${racket.price}" readonly>
                                        <span class="input-group-text">VND</span>
                                    </div>
                                </div>
                            </c:if>
                            <div class="col-md-6">
                                <label for="rentalPrice" class="form-label">Tiền Thuê</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-coins"></i></span>
                                    <input type="text" class="form-control" id="rentalPrice" name="rentalPrice"
                                           value="0" readonly>
                                    <span class="input-group-text">VND</span>
                                </div>
                            </div>
                        </div>

                        <div class="d-grid gap-2 mt-4">
                            <button type="submit" class="btn btn-primary py-3">
                                <i class="fas fa-check-circle me-2"></i>Xác Nhận Thuê Vợt
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>
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

<script>
    function toggleRentalFields() {
        const type = document.getElementById('type').value;
        const bookingIdDiv = document.getElementById('bookingIdDiv');
        const rentalDaysDiv = document.getElementById('rentalDaysDiv');
        const rentalDateDiv = document.getElementById('rentalDateDiv');
        const quantityDayInput = document.getElementById('numberDate');

        if (type === 'ON_SITE') {
            bookingIdDiv.style.display = 'block';
            rentalDaysDiv.style.display = 'none';
            rentalDateDiv.style.display = 'none';
            quantityDayInput.disabled = true;
            quantityDayInput.removeAttribute('name');
        } else if (type === 'DAILY') {
            bookingIdDiv.style.display = 'none';
            rentalDaysDiv.style.display = 'block';
            rentalDateDiv.style.display = 'block';
            quantityDayInput.disabled = false;
            quantityDayInput.setAttribute('name', 'quantityDay');
        }
    }

    function calculateRentalPrice() {
        const type = document.getElementById('type').value;
        const rentalPricePerDay = ${racket.rentalPricePerDay};
        const rentalPricePerPlay = ${racket.rentalPricePerPlay};
        const rentalDaysInput = document.getElementById('numberDate');
        const rentalMoneyInput = document.getElementById('rentalPrice');
        const quantity = document.getElementById('quantity');
        const quantitys = parseInt(quantity.value) || 0;

        if (type === 'DAILY') {
            const days = parseInt(rentalDaysInput.value) || 0;
            rentalMoneyInput.value = days * rentalPricePerDay * quantitys;
        } else {
            rentalMoneyInput.value = rentalPricePerPlay * quantitys;
        }
    }

    // Gọi khi trang load
    window.onload = function () {
        toggleRentalFields();
        calculateRentalPrice();
    };


</script>
<script>
    document.addEventListener("DOMContentLoaded", function () {
        const rentalDateInput = document.getElementById("rentalDate");
        if (rentalDateInput) {
            rentalDateInput.addEventListener("change", function () {
                const selectedDate = this.value;
                console.log(selectedDate);
                const racketId = document.getElementById("racketId").value;

                if (!selectedDate || !racketId) {
                    console.warn("Thiếu ngày hoặc ID vợt");
                    return;
                }

                // Tạo object dữ liệu gửi lên server
                const dataToSend = {
                    date: selectedDate,
                    racketId: racketId
                };
                // Lấy input ẩn chứa token CSRF// chính là parameterName (tên header)
                fetch('/racket-stock', {
                    method: 'POST', // đổi từ GET thành POST để có thể gửi body
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(dataToSend)
                })
                    .then(response => response.json())
                    .then(data => {

                        if (data && data.availableStock !== undefined) {
                            document.getElementById("availableStockLabel").textContent = `Còn :` + data.availableStock;
                        } else {
                            document.getElementById("availableStockLabel").textContent = `Không có dữ liệu`;
                        }
                    })
                    .catch(error => {
                        console.error("Lỗi khi gọi API:", error);
                        document.getElementById("availableStockLabel").textContent = `Lỗi tải dữ liệu`;
                    });
            });
        } else {
            console.warn("Không tìm thấy phần tử #rentalDate");
        }
    });


</script>
<script src="/client/js/main.js"></script>

</body>
</html>