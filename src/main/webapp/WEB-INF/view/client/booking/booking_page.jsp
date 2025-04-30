<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@page import="java.time.LocalDate" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8">
    <title> Giỏ hàng - Sân cầu lông</title>
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
</head>

<body>
<!-- Thông báo lỗi -->
<div id="errorAlert"
     class="alert alert-danger alert-dismissible fade show position-fixed top-0 end-0 m-3"
     style="z-index: 1100; display: none;">
    <div class="d-flex align-items-center">
        <i class="fas fa-exclamation-circle me-2"></i>
        <span id="errorMessage"></span>
    </div>
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
</div>
<!-- Spinner Start -->
<div id="spinner"
     class="show w-100 vh-100 bg-white position-fixed translate-middle top-50 start-50  d-flex align-items-center justify-content-center">
    <div class="spinner-grow text-primary" role="status"></div>
</div>
<!-- Spinner End -->

<jsp:include page="../layout/header.jsp"/>

<!-- Cart Page Start -->
<div class="container-fluid py-5">
    <div class="container py-5">
        <div class="mb-3">
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb">
                    <li class="breadcrumb-item"><a href="/">Home</a></li>
                    <li class="breadcrumb-item active" aria-current="page">Chi Tiết sân đấu</li>
                </ol>
            </nav>
        </div>
        <div class="table-responsive">
            <table class="table">
                <thead>
                <tr>
                    <th scope="col">Sản phẩm</th>
                    <th scope="col">Tên</th>
                    <th scope="col">Giá cả</th>
                    <th scope="col">Giảm giá</th>

                    <th scope="col">Sân</th>
                    <th scope="col">Thời gian</th>
                    <!-- <th scope="col">Ngày đặt sân</th> -->
                    <th scope="col">Thành tiền</th>
                </tr>
                </thead>
                <tbody>
                <c:if test="${ empty product}">
                    <tr>
                        <td colspan="6">
                            Không có sản phẩm trong giỏ hàng
                        </td>
                    </tr>
                </c:if>
                <tr>
                    <th scope="row">
                        <div class="d-flex align-items-center">
                            <img src="/images/product/${product.image}"
                                 class="img-fluid me-5 rounded-circle"
                                 style="width: 80px; height: 80px;" alt="">
                        </div>
                    </th>
                    <td>
                        <p class="mb-0 mt-4">
                            <a href="/mainProduct/${product.id}" target="_blank">
                                ${product.name}
                            </a>
                        </p>
                    </td>
                    <td>
                        <p class="mb-0 mt-4">
                            <fmt:formatNumber type="number" value="${product.price}"/> đ
                        </p>
                    </td>
                    <td>
                        <p class="mb-0 mt-4">
                            <fmt:formatNumber type="number" value="${product.sale}"/>%
                        </p>
                    </td>

                    <td>
                        <!-- Chọn sân -->
                        <div class="input-group mb-0 mt-4" style="width: 150px;">
                            <select name="courtId" class="form-select court-select"
                                    data-product-id="${product.id}"
                                    data-cart-detail-price="${product.price}"
                                    data-cart-detail-sale="${product.sale}">
                                <option selected disabled>-- Chọn sân --</option>
                                <c:forEach var="court" items="${courts}">
                                    <option value="${court.id}">${court.name}</option>
                                </c:forEach>
                            </select>
                        </div>

                    </td>


                    <td>
                        <!-- Chọn giờ -->
                        <div class="input-group quantity mb-5 mb-0 mt-4"
                             style="width: 150px;">
                            <select name="availableTimeId" id="timeSelect"
                                    class="form-select">
                                <c:forEach var="availableTime" items="${availableTime}">
                                    <option value="${availableTime.id}">
                                            ${availableTime.time}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </td>
                    <td>
                        <p class="mb-0 mt-4" data-cart-total-price="${totalPrice}">
                            <fmt:formatNumber type="number" value="${totalPrice}"/> đ
                        </p>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>

        <!-- Form đặt sân -->
        <c:if test="${not empty product}">
            <form:form action="/place-booking" method="post">
                <!-- CSRF Token -->
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

                <!-- Thông tin sản phẩm cần thiết để gửi -->
                <input type="hidden" name="productId" value="${product.id}"/>
                <input type="hidden" name="price" value="${product.price}"/>
                <input type="hidden" name="sale" value="${product.sale}"/>

                <div class="col-12 form-group mb-3">
                    <label>Ngày đặt sân</label>
                    <input class="form-control" type="date" name="bookingDate"
                           value="${not empty selectedBookingDate ? selectedBookingDate : ''}"
                           min="<%= LocalDate.now() %>" required/>
                </div>

                <!-- Chọn vợt đi cùng -->
                <div class="col-md-6 mt-4">
                    <label class="form-label fw-bold">Chọn vợt đi cùng (nếu cần)</label>
                    <div class="dropdown">
                        <button class="btn btn-outline-secondary dropdown-toggle w-100" type="button"
                                id="racketDropdown"
                                data-bs-toggle="dropdown" aria-expanded="false">
                            -- Chọn vợt --
                        </button>
                        <ul class="dropdown-menu w-100" aria-labelledby="racketDropdown"
                            style="max-height: 300px; overflow-y: auto;">
                            <c:forEach var="racket" items="${rackets}">
                                <li>
                                    <div class="dropdown-item d-flex align-items-center justify-content-between racket-option"
                                         data-id="${racket.id}"
                                         data-name="${racket.name}"
                                         data-price="${racket.price}">
                                        <img src="/images/racket/${racket.image}" alt="${racket.name}" class="me-2"
                                             style="width: 50px; height: 50px; object-fit: cover;">
                                        <div class="flex-grow-1">
                                            <div>${racket.name}</div>
                                            <div class="text-muted"><fmt:formatNumber value="${racket.price}"
                                                                                      type="number"/> đ
                                            </div>
                                        </div>
                                        <div>
                                            <input type="number" class="form-control form-control-sm racket-quantity"
                                                   min="0" max="10" value="0" style="width: 60px;">
                                        </div>
                                    </div>
                                </li>
                            </c:forEach>
                        </ul>
                    </div>
                </div>

                <!-- Hidden input để submit dữ liệu vợt đã chọn -->
                <input type="hidden" name="selectedRackets" id="selectedRackets"/>
                <!-- Thời gian được chọn -->
                <input type="hidden" name="availableTimeId" id="hiddenAvailableTimeId"/>
                <!-- Sân được chọn -->
                <input type="hidden" name="courtId" id="hiddenCourtId"/>
                <!-- Số lượng -->
                <input type="hidden" name="quantity" id="hiddenQuantity" value=""/>

                <div class="mt-5 row g-4 justify-content-start">
                    <!-- Thông tin người nhận -->
                    <div class="col-12 col-md-6">
                        <div class="p-4">
                            <h5>Thông Tin Người Đặt</h5>
                            <div class="row">
                                <div class="col-12 form-group mb-3">
                                    <label>Tên người đặt</label>
                                    <input class="form-control" name="receiverName" required/>
                                </div>
                                <div class="col-12 form-group mb-3">
                                    <label>Địa chỉ người đặt</label>
                                    <input class="form-control" name="receiverAddress" required/>
                                </div>
                                <div class="col-12 form-group mb-3">
                                    <label>Số điện thoại</label>
                                    <input class="form-control" name="receiverPhone" required/>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Thông tin thanh toán -->
                    <div class="col-12 col-md-6">
                        <div class="bg-light rounded">
                            <div class="p-4">
                                <h1 class="display-6 mb-4">Thông Tin <span class="fw-normal">Đặt
                                                            sân</span></h1>
                                <div class="mt-3 d-flex justify-content-between">
                                    <h5 class="mb-0 me-4">Hình thức</h5>
                                    <p class="mb-0">Thanh toán tại sân (COD)</p>
                                </div>
                            </div>
                            <div
                                    class="py-4 mb-4 border-top border-bottom d-flex justify-content-between">
                                <h5 class="mb-0 ps-4 me-4">Tổng số tiền</h5>
                                <p class="mb-0 pe-4">
                                    <fmt:formatNumber type="number" value="${totalPrice}"/> đ
                                </p>
                            </div>
                            <div
                                    class="py-4 mb-4 border-top border-bottom d-flex justify-content-between">
                                <h5 class="mb-0 ps-4 me-4">Số tiền phải đặt cọc</h5>
                                <p class="mb-0 pe-4">
                                    <fmt:formatNumber type="number" value="${product.depositPrice}"/> đ
                                </p>
                            </div>
                            <button
                                    class="btn border-secondary rounded-pill px-4 py-3 text-primary text-uppercase mb-4 ms-4">
                                Xác nhận đặt sân
                            </button>
                        </div>
                    </div>
                </div>
            </form:form>
        </c:if>

        <!-- JavaScript -->
        <script>
            $(document).ready(function () {
                // Tự động ẩn thông báo sau 5 giây
                setTimeout(function () {
                    $('.alert').alert('close');
                }, 5000);

                // Validate form trước khi submit
                $('#bookingForm').on('submit', function (e) {
                    const bookingDate = $('input[name="bookingDate"]').val();
                    const courtId = $('select[name="courtId"]').val();
                    const timeId = $('select[name="availableTimeId"]').val();
                    const receiverName = $('input[name="receiverName"]').val().trim();
                    const receiverPhone = $('input[name="receiverPhone"]').val().trim();

                    if (!bookingDate) {
                        showError('Vui lòng chọn ngày đặt sân');
                        return false;
                    }

                    if (!courtId) {
                        showError('Vui lòng chọn sân');
                        return false;
                    }

                    if (!receiverName || !receiverPhone) {
                        showError('Vui lòng điền đầy đủ thông tin người đặt');
                        return false;
                    }

                    // Kiểm tra số điện thoại hợp lệ
                    const phoneRegex = /(84|0[3|5|7|8|9])+([0-9]{8})\b/;
                    if (!phoneRegex.test(receiverPhone)) {
                        showError('Số điện thoại không hợp lệ');
                        return false;
                    }

                    return true;
                });

                // Hàm hiển thị thông báo lỗi
                function showError(message) {
                    const alertHtml = `
                    <div class="alert alert-danger alert-dismissible fade show alert-notification">
                        <i class="fas fa-exclamation-circle me-2"></i>
                        ${message}
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                `;
                    $('.alert-notification').remove();
                    $('body').append(alertHtml);

                    setTimeout(function () {
                        $('.alert-notification').alert('close');
                    }, 5000);
                }
            });
        </script>

    </div>
</div>
<!-- Cart Page End -->
<jsp:include page="../layout/footer.jsp"/>


<!-- Back to Top -->
<a href="#" class="btn btn-primary border-3 border-primary rounded-circle back-to-top"><i
        class="fa fa-arrow-up"></i></a>


<!-- JavaScript Libraries -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.4/jquery.min.js"></script>
<script
        src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="/client/lib/easing/easing.min.js"></script>
<script src="/client/lib/waypoints/waypoints.min.js"></script>
<script src="/client/lib/lightbox/js/lightbox.min.js"></script>
<script src="/client/lib/owlcarousel/owl.carousel.min.js"></script>

<!-- Template Javascript -->
<script src="/client/js/main.js"></script>
<script>
    function loadAvailableTimes() {
        const date = $("#bookingDate").val();
        const courtId = $("#subCourt").val();

        if (!date || !courtId) return;

        $.get("/api/available-time", {date, courtId}, function (data) {
            const timeSelect = $("#availableTime");
            timeSelect.empty();

            if (data.length === 0) {
                timeSelect.append('<option disabled selected>Không còn giờ nào</option>');
            } else {
                data.forEach(t => {
                    timeSelect.append(`<option value="${t.id}">${t.time}</option>`);
                });
            }
        });
    }

    $(document).ready(function () {
        $("#bookingDate, #subCourt").on("change", loadAvailableTimes);
    });
</script>

<script>
    document.addEventListener('DOMContentLoaded', function () {
        const courtSelects = document.querySelectorAll('.court-select');
        const hiddenQuantity = document.getElementById('hiddenQuantity');

        courtSelects.forEach(function (select) {
            select.addEventListener('change', function () {
                // Khi chọn sân, set quantity = 1
                hiddenQuantity.value = 1;

                // Đồng thời cũng cập nhật sân được chọn
                document.getElementById('hiddenCourtId').value = this.value;
            });
        });

        // Khi chọn thời gian
        const timeSelect = document.getElementById('timeSelect');
        timeSelect.addEventListener('change', function () {
            document.getElementById('hiddenAvailableTimeId').value = this.value;
        });
    });
</script>
<!-- Script xử lý hiển thị lỗi -->
<script>
    document.addEventListener('DOMContentLoaded', function() {
        <%-- Nếu có errorMessage từ server --%>
        <c:if test="${not empty errorMessage}">
        var errorAlert = document.getElementById('errorAlert');
        var errorMessageSpan = document.getElementById('errorMessage');
        errorMessageSpan.textContent = '${errorMessage}';
        errorAlert.style.display = 'block';

        // Auto ẩn sau 5 giây
        setTimeout(function() {
            errorAlert.style.display = 'none';
        }, 5000);
        </c:if>
    });
</script>
</body>

</html>