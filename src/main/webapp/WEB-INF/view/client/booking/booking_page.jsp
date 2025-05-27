<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@page import="java.time.LocalDate" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8">
    <title>Booking - Sân cầu lông</title>
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

    <!-- Additional Custom Styles -->
    <style>
        .booking-container {
            background-color: #f8f9fa;
            border-radius: 10px;
            box-shadow: 0 0 15px rgba(0,0,0,0.05);
            padding: 30px;
            margin-bottom: 30px;
        }

        .product-image {
            width: 100px;
            height: 100px;
            object-fit: cover;
            border-radius: 8px;
            border: 2px solid #f0f0f0;
            transition: transform 0.3s;
        }

        .product-image:hover {
            transform: scale(1.05);
        }

        .form-label {
            font-weight: 600;
            margin-bottom: 8px;
            color: #333;
        }

        .form-control, .form-select {
            border-radius: 8px;
            padding: 10px 15px;
            border: 1px solid #ddd;
            transition: all 0.3s;
        }

        .form-control:focus, .form-select:focus {
            border-color: #4CAF50;
            box-shadow: 0 0 0 0.25rem rgba(76, 175, 80, 0.25);
        }

        .booking-summary {
            background-color: #fff;
            border-radius: 10px;
            box-shadow: 0 0 10px rgba(0,0,0,0.05);
            padding: 25px;
        }

        .booking-summary h1 {
            color: #2E7D32;
            font-size: 1.8rem;
            margin-bottom: 20px;
            border-bottom: 2px solid #E8F5E9;
            padding-bottom: 10px;
        }

        .summary-item {
            padding: 15px 0;
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-bottom: 1px solid #f0f0f0;
        }

        .summary-item:last-child {
            border-bottom: none;
        }

        .summary-label {
            font-weight: 600;
            color: #333;
        }

        .summary-value {
            font-weight: 500;
            color: #2E7D32;
        }

        .btn-booking {
            background-color: #2E7D32;
            color: white;
            border: none;
            border-radius: 30px;
            padding: 12px 30px;
            font-weight: 600;
            letter-spacing: 0.5px;
            transition: all 0.3s;
            width: 100%;
            margin-top: 20px;
        }

        .btn-booking:hover {
            background-color: #1B5E20;
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
        }

        .hold-notification {
            background-color: #FFF8E1;
            border-left: 4px solid #FFC107;
            padding: 15px;
            border-radius: 8px;
            margin: 15px 0;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .hold-timer {
            font-weight: bold;
            color: #F57C00;
        }

        .btn-cancel-hold {
            background-color: #FFEBEE;
            color: #D32F2F;
            border: 1px solid #FFCDD2;
            border-radius: 20px;
            padding: 5px 15px;
            font-size: 0.85rem;
            transition: all 0.2s;
        }

        .btn-cancel-hold:hover {
            background-color: #D32F2F;
            color: white;
        }

        .error-alert {
            background-color: #FFEBEE;
            border-left: 4px solid #D32F2F;
            color: #D32F2F;
            padding: 15px;
            border-radius: 8px;
            display: flex;
            align-items: center;
            margin-bottom: 20px;
        }

        .error-alert i {
            margin-right: 10px;
            font-size: 1.2rem;
        }

        .breadcrumb {
            background-color: transparent;
            padding: 0;
            margin-bottom: 20px;
        }

        .breadcrumb-item a {
            color: #2E7D32;
            text-decoration: none;
        }

        .breadcrumb-item.active {
            color: #555;
        }

        .table {
            border-radius: 10px;
            overflow: hidden;
            box-shadow: 0 0 10px rgba(0,0,0,0.03);
        }

        .table thead {
            background-color: #E8F5E9;
        }

        .table thead th {
            font-weight: 600;
            color: #2E7D32;
            border-bottom: none;
            padding: 15px;
        }

        .table tbody td {
            padding: 15px;
            vertical-align: middle;
        }

        .user-info-section {
            background-color: white;
            border-radius: 10px;
            padding: 25px;
            box-shadow: 0 0 10px rgba(0,0,0,0.05);
        }

        .user-info-section h5 {
            color: #2E7D32;
            margin-bottom: 20px;
            padding-bottom: 10px;
            border-bottom: 2px solid #E8F5E9;
        }
    </style>
</head>

<body>
<!-- Thông báo lỗi -->
<div id="errorAlert" class="alert alert-danger alert-dismissible fade show position-fixed top-0 end-0 m-3" style="z-index: 1100; display: none;">
    <div class="d-flex align-items-center">
        <i class="fas fa-exclamation-circle me-2"></i>
        <span id="errorMessage"></span>
    </div>
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
</div>

<!-- Spinner Start -->
<div id="spinner" class="show w-100 vh-100 bg-white position-fixed translate-middle top-50 start-50 d-flex align-items-center justify-content-center">
    <div class="spinner-grow text-success" role="status"></div>
</div>
<!-- Spinner End -->

<jsp:include page="../layout/header.jsp"/>

<!-- Booking Page Start -->
<div class="container-fluid py-5 bg-light">
    <div class="container py-5">
        <div class="mb-4">
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb">
                    <li class="breadcrumb-item"><a href="/"><i class="fas fa-home me-1"></i>Home</a></li>
                    <li class="breadcrumb-item active" aria-current="page">Chi tiết sân đấu</li>
                </ol>
            </nav>
            <h2 class="mb-4 text-center">Đặt sân cầu lông</h2>
        </div>

        <div class="booking-container">
            <div class="table-responsive mb-4">
                <table class="table">
                    <thead>
                    <tr>
                        <th scope="col">Sản phẩm</th>
                        <th scope="col">Tên</th>
                        <th scope="col">Giá cả</th>
                        <th scope="col">Giảm giá</th>
                        <th scope="col">Thành tiền</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:if test="${empty product}">
                        <tr>
                            <td colspan="6" class="text-center py-4">
                                <i class="fas fa-exclamation-circle me-2 text-muted"></i>
                                Không có sản phẩm trong giỏ hàng
                            </td>
                        </tr>
                    </c:if>
                    <tr>
                        <th scope="row">
                            <div class="d-flex align-items-center">
                                <img src="/images/product/${product.image}" class="product-image me-3" alt="${product.name}">
                            </div>
                        </th>
                        <td>
                            <p class="mb-0 fw-bold">
                                <a href="/mainProduct/${product.id}" target="_blank" class="text-decoration-none text-dark">
                                    ${product.name}
                                </a>
                            </p>
                        </td>
                        <td>
                            <p class="mb-0">
                                <fmt:formatNumber type="number" value="${product.price}"/> đ
                            </p>
                        </td>
                        <td>
                            <p class="mb-0">
                                <span class="badge bg-success"><fmt:formatNumber type="number" value="${product.sale}"/>%</span>
                            </p>
                        </td>
                        <td>
                            <p class="mb-0 fw-bold text-success" data-cart-total-price="${totalPrice}">
                                <fmt:formatNumber type="number" value="${totalPrice}"/> đ
                            </p>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>

            <!-- Form đặt sân -->
            <c:if test="${not empty product}">
                <form:form id="bookingForm" action="/place-booking" method="post" class="mt-4">
                    <!-- CSRF Token -->
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

                    <!-- Thông tin sản phẩm cần thiết để gửi -->
                    <input type="hidden" name="productId" value="${product.id}"/>
                    <input type="hidden" name="price" value="${product.price}"/>
                    <input type="hidden" name="sale" value="${product.sale}"/>
                    <input type="hidden" id="hiddenCourtId" name="courtId" value=""/>
                    <input type="hidden" id="hiddenAvailableTimeId" name="availableTimeId" value=""/>

                    <div class="row g-4">
                        <div class="col-12">
                            <div class="card border-0 shadow-sm">
                                <div class="card-body p-4">
                                    <h5 class="card-title mb-4 text-success"><i class="far fa-calendar-alt me-2"></i>Thông tin đặt sân</h5>

                                    <div class="row g-3">
                                        <div class="col-12">
                                            <label for="bookingDate" class="form-label">Ngày đặt sân</label>
                                            <input class="form-control" type="date" id="bookingDate" name="bookingDate"
                                                   value="${not empty selectedBookingDate ? selectedBookingDate : ''}"
                                                   min="<%= LocalDate.now() %>" required/>
                                        </div>

                                        <div class="col-md-6">
                                            <label for="courtId" class="form-label">Sân</label>
                                            <select name="courtId" id="courtId" class="form-select court-select" required>
                                                <option value="">-- Chọn sân --</option>
                                                <c:forEach var="court" items="${courts}">
                                                    <option value="${court.id}">${court.name}</option>
                                                </c:forEach>
                                            </select>
                                        </div>

                                        <div class="col-md-6">
                                            <label for="availableTime" class="form-label">Giờ</label>
                                            <select name="availableTimeId" id="availableTime" class="form-select" required>
                                                <option value="">-- Chọn giờ --</option>
                                            </select>
                                            <div id="timeStatus" class="mt-2 small text-danger"></div>
                                        </div>
                                    </div>

                                    <!-- Thêm thông báo giữ chỗ -->
                                    <div id="holdNotification" class="hold-notification mt-4" style="display: none;">
                                        <div>
                                            <i class="fas fa-clock me-2"></i>
                                            <span>Bạn đang giữ chỗ sân này. Thời gian còn lại: <span id="holdTimer" class="hold-timer">03:00</span></span>
                                        </div>
                                        <button type="button" class="btn-cancel-hold" id="cancelHold">
                                            <i class="fas fa-times me-1"></i>Hủy bỏ
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="col-12 col-lg-6">
                            <div class="user-info-section h-100">
                                <h5><i class="fas fa-user me-2"></i>Thông Tin Người Đặt</h5>
                                <div class="row g-3">
                                    <div class="col-12">
                                        <label for="receiverName" class="form-label">Tên người đặt</label>
                                        <input id="receiverName" class="form-control" name="receiverName" placeholder="Nhập họ tên" required/>
                                    </div>
                                    <div class="col-12">
                                        <label for="receiverAddress" class="form-label">Địa chỉ người đặt</label>
                                        <input id="receiverAddress" class="form-control" name="receiverAddress" placeholder="Nhập địa chỉ" required/>
                                    </div>
                                    <div class="col-12">
                                        <label for="receiverPhone" class="form-label">Số điện thoại</label>
                                        <input id="receiverPhone" class="form-control" name="receiverPhone" placeholder="Nhập số điện thoại" required/>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="col-12 col-lg-6">
                            <div class="booking-summary h-100">
                                <h1 class="fw-bold"><i class="fas fa-receipt me-2"></i>Thông Tin Đặt sân</h1>

                                <div class="summary-item">
                                    <span class="summary-label">Hình thức thanh toán</span>
                                    <span class="summary-value">
                                            <i class="fas fa-money-bill-wave me-1"></i>
                                            Thanh toán tại sân (COD)
                                        </span>
                                </div>

                                <div class="summary-item">
                                    <span class="summary-label">Tổng số tiền</span>
                                    <span class="summary-value">
                                            <fmt:formatNumber type="number" value="${totalPrice}"/> đ
                                        </span>
                                </div>

                                <div class="summary-item">
                                    <span class="summary-label">Số tiền phải đặt cọc</span>
                                    <span class="summary-value fw-bold">
                                            <fmt:formatNumber type="number" value="${product.depositPrice}"/> đ
                                        </span>
                                </div>

                                <button type="submit" class="btn-booking">
                                    <i class="fas fa-check-circle me-2"></i>Xác nhận đặt sân
                                </button>
                            </div>
                        </div>
                    </div>
                </form:form>
            </c:if>
        </div>
    </div>
</div>
<!-- Booking Page End -->

<jsp:include page="../layout/footer.jsp"/>

<!-- Back to Top -->
<a href="#" class="btn btn-success border-3 border-success rounded-circle back-to-top">
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
<script>
    $(document).ready(function () {
        // Tự động ẩn thông báo sau 5 giây
        setTimeout(function () {
            $('.alert').alert('close');
        }, 5000);

        // Load giờ trống khi chọn sân hoặc ngày
        $("#bookingDate, #courtId").on("change", function() {
            const date = $("#bookingDate").val();
            const courtId = $("#courtId").val();

            console.log("Date:", date, "Court ID:", courtId);

            if (!date || !courtId) {
                $("#availableTime").html('<option selected disabled value="">-- Chọn ngày và sân trước --</option>');
                return;
            }

            $.ajax({
                url: "/api/available-time",
                type: "GET",
                data: {
                    date: date,
                    courtId: courtId
                },
                success: function(data) {
                    console.log("Available times data:", data);
                    const timeSelect = $("#availableTime");
                    timeSelect.empty();

                    if (!data || data.length === 0) {
                        timeSelect.append('<option disabled selected>Không còn giờ nào</option>');
                    } else {
                        timeSelect.append('<option disabled selected value="">-- Chọn giờ --</option>');
                        $.each(data, function(index, time) {
                            timeSelect.append($('<option></option>').attr('value', time.id).text(time.time));
                        });
                    }
                },
                error: function(xhr, status, error) {
                    console.error("Error fetching available times:", error);
                    $("#availableTime").html('<option disabled selected>Lỗi khi tải giờ</option>');
                }
            });
        });

        // Gán dữ liệu vào hidden input trước khi submit
        $('#bookingForm').on('submit', function(e) {
            const courtId = $("#courtId").val();
            const timeId = $("#availableTime").val();
            const bookingDate = $("#bookingDate").val();

            if (!courtId) {
                e.preventDefault();
                showError('Vui lòng chọn sân');
                return false;
            }

            if (!timeId) {
                e.preventDefault();
                showError('Vui lòng chọn giờ');
                return false;
            }

            if (!bookingDate) {
                e.preventDefault();
                showError('Vui lòng chọn ngày đặt sân');
                return false;
            }

            $("#hiddenCourtId").val(courtId);
            $("#hiddenAvailableTimeId").val(timeId);

            const receiverName = $('input[name="receiverName"]').val().trim();
            const receiverPhone = $('input[name="receiverPhone"]').val().trim();

            if (!receiverName || !receiverPhone) {
                e.preventDefault();
                showError('Vui lòng điền đầy đủ thông tin người đặt');
                return false;
            }

            const phoneRegex = /(84|0[3|5|7|8|9])+([0-9]{8})\b/;
            if (!phoneRegex.test(receiverPhone)) {
                e.preventDefault();
                showError('Số điện thoại không hợp lệ');
                return false;
            }

            return true;
        });

        function showError(message) {
            $('#errorMessage').text(message);
            $('#errorAlert').show();
            setTimeout(() => $('#errorAlert').fadeOut(), 5000);
        }

        let countdownInterval = null;

        function showHoldNotification(seconds) {
            $('#holdNotification').show();
            updateTimer(seconds);

            if (countdownInterval) clearInterval(countdownInterval);
            let remaining = seconds;

            countdownInterval = setInterval(() => {
                remaining--;
                if (remaining <= 0) {
                    clearInterval(countdownInterval);
                    hideHoldNotification();
                    $('#timeStatus').text('Giữ chỗ đã hết hạn. Vui lòng chọn lại giờ.');
                } else {
                    updateTimer(remaining);
                }
            }, 1000);
        }

        function updateTimer(seconds) {
            const min = String(Math.floor(seconds / 60)).padStart(2, '0');
            const sec = String(seconds % 60).padStart(2, '0');
            $('#holdTimer').text(`${min}:${sec}`);
        }

        function hideHoldNotification() {
            $('#holdNotification').hide();
            if (countdownInterval) clearInterval(countdownInterval);
            $('#holdTimer').text('03:00');
        }

        // Khi chọn giờ, gọi API giữ chỗ tạm thời
        $('#availableTime').on('change', function () {
            const courtId = $('#courtId').val();
            const timeId = $(this).val();
            const bookingDate = $('#bookingDate').val();

            $('#timeStatus').text('');
            hideHoldNotification();

            if (!courtId || !timeId || !bookingDate) {
                return;
            }

            $.ajax({
                url: '/api/temp-booking',
                method: 'POST',
                data: {
                    subCourtId: courtId,
                    availableTimeId: timeId,
                    bookingDate: bookingDate
                },
                success: function (response) {
                    console.log("Response from /api/temp-booking:", response);
                    if (response.status === 'success') {
                        $('#holdMessage').html('Bạn đang giữ chỗ sân này. Thời gian còn lại: <span id="holdTimer">' + formatSeconds(response.remainingTime || 180) + '</span>');
                        $('#cancelHold').show(); // Chỉ bạn mới được hủy
                        $('#holdNotification').show();
                        $('#timeStatus').text('');
                    } else {
                        $('#timeStatus').text(response.message || 'Lỗi giữ chỗ.');
                    }
                },
                error: function (xhr) {
                    // Chuyển console.log() sang dùng xhr để tránh lỗi undefined
                    console.log("Error response from /api/temp-booking:", xhr);
                    // Ẩn thông báo giữ chỗ của chính bạn nếu có lỗi

                    if (xhr.status === 409) { // Có người khác giữ rồi
                        const res = xhr.responseJSON || {};
                        $('#holdMessage').html('Khung giờ này đang được người khác giữ chỗ. Thời gian còn lại: <span id="holdTimer">' + formatSeconds(res.remainingTime || 0) + '</span>');
                        $('#cancelHold').hide(); // Không cho hủy nếu không phải bạn
                        $('#holdNotification').hide();
                        $('#timeStatus').text(res.message || 'Khung giờ này đang được giữ.');
                    } else {
                        $('#timeStatus').text('Lỗi khi giữ chỗ. Vui lòng thử lại.');
                    }
                }
            });
        });

        // Nút hủy giữ chỗ
        $('#cancelHold').on('click', function () {
            hideHoldNotification();
            $('#timeStatus').text('Bạn đã hủy giữ chỗ.');
            $('#availableTime').val('');
        });

        // Ẩn thông báo giữ chỗ
        function hideHoldNotification() {
            $('#holdNotification').hide();
            $('#holdMessage').text('');
            $('#holdTimer').text('');
        }

        // Format giây thành mm:ss
        function formatSeconds(seconds) {
            if (!seconds || seconds < 0) return '00:00';
            const min = String(Math.floor(seconds / 60)).padStart(2, '0');
            const sec = String(seconds % 60).padStart(2, '0');
            return `${min}:${sec}`;
        }
    });
</script>
</body>
</html>