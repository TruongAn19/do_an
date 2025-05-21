<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8">
    <title>${product.name} - Sân cầu lông</title>
    <meta content="width=device-width, initial-scale=1.0" name="viewport">
    <meta content="" name="keywords">
    <meta content="" name="description">

    <!-- Google Web Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@400;600&family=Raleway:wght@600;800&display=swap" rel="stylesheet">

    <!-- Icon Font Stylesheet -->
    <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.15.4/css/all.css" />
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.4.1/font/bootstrap-icons.css" rel="stylesheet">

    <!-- Libraries Stylesheet -->
    <link href="/client/lib/lightbox/css/lightbox.min.css" rel="stylesheet">
    <link href="/client/lib/owlcarousel/assets/owl.carousel.min.css" rel="stylesheet">

    <!-- Customized Bootstrap Stylesheet -->
    <link href="/client/css/bootstrap.min.css" rel="stylesheet">

    <!-- Template Stylesheet -->
    <link href="/client/css/style.css" rel="stylesheet">

    <!-- Custom CSS for this page -->
    <style>
        .court-image {
            transition: transform 0.3s ease;
            border-radius: 12px;
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
            max-height: 400px;
            object-fit: cover;
            width: 100%;
        }

        .court-image:hover {
            transform: scale(1.02);
        }

        .product-details {
            background-color: #f8f9fa;
            border-radius: 12px;
            padding: 25px;
            box-shadow: 0 5px 15px rgba(0,0,0,0.05);
        }

        .product-title {
            color: #343a40;
            font-weight: 700;
            border-bottom: 2px solid #007bff;
            padding-bottom: 10px;
            margin-bottom: 20px;
        }

        .short-desc {
            color: #6c757d;
            font-size: 1.1rem;
            margin-bottom: 20px;
            line-height: 1.6;
        }

        .price-container {
            background-color: #f1f8ff;
            border-radius: 8px;
            padding: 15px;
            margin-bottom: 20px;
        }

        .original-price {
            font-size: 1.2rem;
            color: #6c757d;
            text-decoration: line-through;
        }

        .discount-price {
            font-size: 1.5rem;
            color: #28a745;
            font-weight: 700;
        }

        .discount-badge {
            background-color: #dc3545;
            color: white;
            padding: 5px 10px;
            border-radius: 20px;
            font-size: 0.9rem;
            margin-left: 10px;
        }

        .address-container {
            display: flex;
            align-items: center;
            margin-bottom: 20px;
            padding: 10px 15px;
            background-color: #f8f9fa;
            border-radius: 8px;
            border-left: 4px solid #007bff;
        }

        .address-icon {
            color: #007bff;
            font-size: 1.5rem;
            margin-right: 15px;
        }

        .time-slot {
            transition: all 0.3s ease;
            border: 2px solid #dee2e6;
        }

        .time-slot:hover {
            transform: translateY(-3px);
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
            border-color: #007bff;
        }

        .time-slot.active {
            background-color: #007bff;
            border-color: #007bff;
        }

        .time-slot.active .fw-bold {
            color: white !important;
        }

        .quantity-badge {
            background-color: #17a2b8;
            color: white;
            padding: 8px 15px;
            border-radius: 50px;
            display: inline-block;
            margin-bottom: 20px;
        }

        .booking-btn {
            background-color: #007bff;
            color: white !important;
            border: none !important;
            padding: 12px 25px !important;
            border-radius: 50px !important;
            font-weight: 600;
            transition: all 0.3s;
            font-size: 1.1rem;
            display: inline-flex;
            align-items: center;
        }

        .booking-btn:hover {
            background-color: #0056b3;
            transform: translateY(-3px);
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
        }

        .description-container {
            background-color: white;
            border-radius: 12px;
            padding: 25px;
            box-shadow: 0 5px 15px rgba(0,0,0,0.05);
            margin-top: 30px;
        }

        .nav-tabs {
            border-bottom: 2px solid #dee2e6;
        }

        .nav-tabs .nav-link {
            border: none;
            color: #6c757d;
            font-weight: 600;
            padding: 12px 20px;
            margin-right: 10px;
            border-radius: 0;
        }

        .nav-tabs .nav-link.active {
            color: #007bff;
            border-bottom: 3px solid #007bff;
        }

        .tab-content {
            padding: 20px 0;
        }

        .detail-desc {
            line-height: 1.8;
            color: #495057;
        }

        .time-slots-title {
            margin-bottom: 15px;
            color: #343a40;
            font-weight: 600;
            display: flex;
            align-items: center;
        }

        .time-slots-title i {
            margin-right: 10px;
            color: #007bff;
        }

        .breadcrumb {
            background-color: transparent;
            padding: 0;
            margin-bottom: 30px;
        }

        .breadcrumb-item a {
            color: #007bff;
            text-decoration: none;
        }

        .breadcrumb-item.active {
            color: #6c757d;
        }
    </style>
</head>

<body>

<!-- Spinner Start -->
<div id="spinner" class="show w-100 vh-100 bg-white position-fixed translate-middle top-50 start-50 d-flex align-items-center justify-content-center">
    <div class="spinner-grow text-primary" role="status"></div>
</div>
<!-- Spinner End -->

<jsp:include page="../layout/header.jsp" />

<!-- Single Product Start -->
<div class="container-fluid py-5 mt-5">
    <div class="container py-5">
        <div class="row g-4 mb-5">
            <div>
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><a href="/HomePage"><i class="fas fa-home me-1"></i>Home</a></li>
                        <li class="breadcrumb-item"><a href="/courts">Badminton Courts</a></li>
                        <li class="breadcrumb-item active" aria-current="page">${product.name}</li>
                    </ol>
                </nav>
            </div>
            <div class="col-lg-8 col-xl-9">
                <div class="row g-4">
                    <div class="col-lg-6">
                        <div class="text-center">
                            <a href="/images/product/${product.image}" data-lightbox="product">
                                <img src="/images/product/${product.image}" class="court-image" alt="${product.name}">
                            </a>
                        </div>
                    </div>
                    <div class="col-lg-6 product-details">
                        <h3 class="product-title">${product.name}</h3>
                        <p class="short-desc">${product.shortDesc}</p>

                        <div class="price-container">
                            <div class="d-flex align-items-center">
                                <c:choose>
                                    <c:when test="${product.sale > 0}">
                                        <div class="original-price me-3">
                                            <fmt:formatNumber type="number" value="${product.price}" /> đ
                                        </div>
                                        <div class="discount-price">
                                            <fmt:formatNumber type="number" value="${discountPrice}" /> đ
                                        </div>
                                        <span class="discount-badge">
                                            <i class="fas fa-tags me-1"></i>
                                            <fmt:formatNumber type="number" value="${product.sale}" />% OFF
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="discount-price">
                                            <fmt:formatNumber type="number" value="${product.price}" /> đ
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <div class="address-container">
                            <i class="fas fa-map-marker-alt address-icon"></i>
                            <div>${product.address}</div>
                        </div>

                        <h5 class="time-slots-title">
                            <i class="far fa-clock"></i> Available Time Slots
                        </h5>
                        <div class="mb-4">
                            <div class="row row-cols-2 row-cols-sm-3 row-cols-md-4 row-cols-lg-6 g-3">
                                <c:forEach var="availableTime" items="${availableTime}">
                                    <div class="col">
                                        <div class="card text-center h-100 shadow-sm time-slot" onclick="selectTimeSlot(this)">
                                            <div class="card-body d-flex align-items-center justify-content-center p-2">
                                                <span class="fw-bold text-primary">${availableTime.time}</span>
                                            </div>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>

                        <div class="quantity-badge">
                            <i class="fas fa-table-tennis me-2"></i> Số lượng sân: ${product.quantity}
                        </div>

                        <div class="text-center text-lg-start">
                            <a href="/booking/${product.id}" class="booking-btn">
                                <i class="fa fa-calendar-check me-2"></i> Đặt Sân Ngay
                                <i class="fas fa-arrow-right ms-2"></i>
                            </a>
                        </div>
                    </div>

                    <div class="col-lg-12 description-container">
                        <nav>
                            <div class="nav nav-tabs mb-3">
                                <button class="nav-link active" type="button" role="tab" id="nav-about-tab"
                                        data-bs-toggle="tab" data-bs-target="#nav-about"
                                        aria-controls="nav-about" aria-selected="true">
                                    <i class="fas fa-info-circle me-2"></i> Chi Tiết Sân
                                </button>
                                <button class="nav-link" type="button" role="tab" id="nav-facilities-tab"
                                        data-bs-toggle="tab" data-bs-target="#nav-facilities"
                                        aria-controls="nav-facilities" aria-selected="false">
                                    <i class="fas fa-list-ul me-2"></i> Tiện Ích
                                </button>
                                <button class="nav-link" type="button" role="tab" id="nav-rules-tab"
                                        data-bs-toggle="tab" data-bs-target="#nav-rules"
                                        aria-controls="nav-rules" aria-selected="false">
                                    <i class="fas fa-exclamation-circle me-2"></i> Quy Định
                                </button>
                            </div>
                        </nav>
                        <div class="tab-content">
                            <div class="tab-pane fade show active detail-desc" id="nav-about" role="tabpanel" aria-labelledby="nav-about-tab">
                                ${product.detailDesc}
                            </div>
                            <div class="tab-pane fade detail-desc" id="nav-facilities" role="tabpanel" aria-labelledby="nav-facilities-tab">
                                <ul class="list-group list-group-flush">
                                    <li class="list-group-item bg-transparent"><i class="fas fa-check-circle text-success me-2"></i> Phòng thay đồ</li>
                                    <li class="list-group-item bg-transparent"><i class="fas fa-check-circle text-success me-2"></i> Nhà vệ sinh</li>
                                    <li class="list-group-item bg-transparent"><i class="fas fa-check-circle text-success me-2"></i> Nước uống miễn phí</li>
                                    <li class="list-group-item bg-transparent"><i class="fas fa-check-circle text-success me-2"></i> Bãi đỗ xe</li>
                                    <li class="list-group-item bg-transparent"><i class="fas fa-check-circle text-success me-2"></i> Wifi miễn phí</li>
                                </ul>
                            </div>
                            <div class="tab-pane fade detail-desc" id="nav-rules" role="tabpanel" aria-labelledby="nav-rules-tab">
                                <ul class="list-group list-group-flush">
                                    <li class="list-group-item bg-transparent"><i class="fas fa-info-circle text-primary me-2"></i> Vui lòng mang giày phù hợp khi chơi</li>
                                    <li class="list-group-item bg-transparent"><i class="fas fa-info-circle text-primary me-2"></i> Không hút thuốc trong khu vực sân</li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-lg-4 col-xl-3 d-none d-lg-block">
                <div class="bg-light rounded p-4 mb-4">
                    <h4 class="mb-4">Thông Tin Liên Hệ</h4>
                    <div class="d-flex align-items-center mb-3">
                        <i class="fas fa-phone-alt text-primary me-3"></i>
                        <span>+84 123 456 789</span>
                    </div>
                    <div class="d-flex align-items-center mb-3">
                        <i class="fas fa-envelope text-primary me-3"></i>
                        <span>info@sancaulong.com</span>
                    </div>
                    <div class="d-flex align-items-center mb-3">
                        <i class="fas fa-clock text-primary me-3"></i>
                        <span>Mở cửa: 6:00 - 22:00</span>
                    </div>
                </div>

                <div class="bg-light rounded p-4">
                    <h4 class="mb-4">Tiện Ích</h4>
                    <div class="d-flex align-items-center mb-3">
                        <i class="fas fa-check-circle text-success me-3"></i>
                        <span>Sân tiêu chuẩn quốc tế</span>
                    </div>
                    <div class="d-flex align-items-center mb-3">
                        <i class="fas fa-check-circle text-success me-3"></i>
                        <span>Ánh sáng tốt</span>
                    </div>
                    <div class="d-flex align-items-center mb-3">
                        <i class="fas fa-check-circle text-success me-3"></i>
                        <span>Dịch vụ cho thuê vợt</span>
                    </div>
                    <div class="d-flex align-items-center mb-3">
                        <i class="fas fa-check-circle text-success me-3"></i>
                        <span>Bán cầu lông</span>
                    </div>
                    <div class="d-flex align-items-center mb-3">
                        <i class="fas fa-check-circle text-success me-3"></i>
                        <span>Máy lạnh</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<!-- Single Product End -->

<jsp:include page="../layout/footer.jsp" />

<!-- Back to Top -->
<a href="#" class="btn btn-primary border-3 border-primary rounded-circle back-to-top"><i class="fa fa-arrow-up"></i></a>

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
    function selectTimeSlot(element) {
        // Remove active class from all time slots
        document.querySelectorAll('.time-slot').forEach(slot => {
            slot.classList.remove('active');
        });

        // Add active class to selected time slot
        element.classList.add('active');
    }
</script>
</body>

</html>
