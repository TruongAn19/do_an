<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8">
    <title>${racket.name} - Sân cầu lông</title>
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

    <!-- Custom CSS for this page -->
    <style>
        .product-image {
            transition: transform 0.3s ease;
            max-height: 400px;
            object-fit: contain;
        }

        .product-image:hover {
            transform: scale(1.05);
        }

        .product-details {
            background-color: #f8f9fa;
            border-radius: 10px;
            padding: 25px;
            box-shadow: 0 0 15px rgba(0,0,0,0.05);
        }

        .price-tag {
            background-color: #28a745;
            color: white;
            padding: 5px 10px;
            border-radius: 5px;
            font-weight: bold;
            display: inline-block;
            margin-bottom: 10px;
        }

        .rental-btn {
            background-color: #007bff;
            color: white;
            border: none;
            padding: 12px 25px;
            border-radius: 50px;
            font-weight: 600;
            transition: all 0.3s;
            margin-top: 20px;
        }

        .rental-btn:hover {
            background-color: #0056b3;
            transform: translateY(-3px);
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
        }

        .product-title {
            color: #343a40;
            font-weight: 700;
            border-bottom: 2px solid #007bff;
            padding-bottom: 10px;
            margin-bottom: 20px;
        }

        .factory-badge {
            background-color: #f8f9fa;
            border: 1px solid #dee2e6;
            padding: 5px 15px;
            border-radius: 20px;
            font-size: 14px;
            color: #6c757d;
            display: inline-block;
            margin-bottom: 15px;
        }

        .product-category {
            margin-top: 5px;
            margin-bottom: 15px;
        }

        .product-category .badge {
            font-size: 14px;
            padding: 6px 12px;
            background-color: #0d6efd;
        }

        .price-container {
            background-color: #f1f8ff;
            border-radius: 8px;
            padding: 15px;
            margin-bottom: 20px;
        }

        .price-label {
            color: #6c757d;
            font-weight: 600;
            margin-bottom: 5px;
        }

        .price-value {
            color: #28a745;
            font-weight: 700;
            font-size: 18px;
        }

        .warning-text {
            background-color: #fff3cd;
            border-left: 4px solid #ffc107;
            padding: 10px 15px;
            color: #856404;
            margin: 20px 0;
            border-radius: 0 5px 5px 0;
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

        .specs-container {
            background-color: #f8f9fa;
            border-radius: 8px;
            padding: 15px;
        }

        .spec-label {
            font-weight: 600;
            color: #6c757d;
            margin-right: 5px;
        }

        .spec-value {
            font-weight: 500;
        }
    </style>
</head>

<body>

<!-- Spinner Start -->
<div id="spinner" class="show w-100 vh-100 bg-white position-fixed translate-middle top-50 start-50 d-flex align-items-center justify-content-center">
    <div class="spinner-grow text-primary" role="status"></div>
</div>
<!-- Spinner End -->

<jsp:include page="../layout/header.jsp"/>

<!-- Single Product Start -->
<div class="container-fluid py-5 mt-5">
    <div class="container py-5">
        <div class="row g-4 mb-5">
            <div>
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><a href="/HomePage"><i class="fas fa-home me-1"></i>Home</a></li>
                        <li class="breadcrumb-item"><a href="/rackets">Badminton Rackets</a></li>
                        <li class="breadcrumb-item active" aria-current="page">${racket.name}</li>
                    </ol>
                </nav>
            </div>
            <div class="col-lg-8 col-xl-9">
                <div class="row g-4">
                    <div class="col-lg-6">
                        <div class="border rounded p-3 bg-white text-center">
                            <a href="/images/racket/${racket.image}" data-lightbox="product">
                                <img src="/images/racket/${racket.image}" class="img-fluid rounded product-image" alt="${racket.name}">
                            </a>
                        </div>
                    </div>
                    <div class="col-lg-6 product-details">
                        <h3 class="product-title">${racket.name}</h3>
                        <div class="product-category mb-3">
                            <span class="badge bg-primary rounded-pill">
                                <i class="fas fa-tag me-1"></i> ${racket.product.name}
                            </span>
                        </div>
                        <div class="factory-badge">
                            <i class="fas fa-industry me-1"></i> ${racket.factory}
                        </div>

                        <div class="price-container">
                            <h5 class="mb-3 text-primary"><i class="fas fa-money-bill-wave me-2"></i>Bảng Giá</h5>
                            <div class="row mb-2">
                                <div class="col-7">
                                    <div class="price-label">Giá thuê theo sân:</div>
                                </div>
                                <div class="col-5">
                                    <div class="price-value">
                                        <fmt:formatNumber type="number" value="${racket.rentalPricePerPlay}" /> VNĐ
                                    </div>
                                </div>
                            </div>

                            <div class="row mb-2">
                                <div class="col-7">
                                    <div class="price-label">Giá thuê theo ngày:</div>
                                </div>
                                <div class="col-5">
                                    <div class="price-value">
                                        <fmt:formatNumber type="number" value="${racket.rentalPricePerDay}" /> VNĐ
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-7">
                                    <div class="price-label">Giá vợt:</div>
                                </div>
                                <div class="col-5">
                                    <div class="price-value">
                                        <fmt:formatNumber type="number" value="${racket.price}" /> VNĐ
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="warning-text">
                            <i class="fas fa-exclamation-triangle me-2"></i>
                            Lưu ý: Nếu quý khách làm hỏng vợt hay gây thiệt hại cho vợt sẽ phải đền bù số tiền bằng giá của cái vợt
                        </div>

                        <!-- Add to Cart Button -->
                        <form action="/user/rental-page/${racket.id}" method="get" class="mt-auto text-center">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                            <input type="hidden" name="type" value="DAILY" />
                            <button class="rental-btn">
                                <i class="fa fa-shopping-bag me-2"></i> Thuê Vợt Ngay <i class="fas fa-arrow-right ms-1"></i>
                            </button>
                        </form>
                    </div>
                </div>
            </div>

            <div class="col-lg-4 col-xl-3 d-none d-lg-block">
                <div class="bg-light rounded p-4">
                    <h4 class="mb-4">Thông Tin Thêm</h4>
                    <div class="d-flex align-items-center mb-3">
                        <i class="fas fa-check-circle text-success me-3"></i>
                        <span>Vợt chính hãng</span>
                    </div>
<%--                    <div class="d-flex align-items-center mb-3">--%>
<%--                        <i class="fas fa-check-circle text-success me-3"></i>--%>
<%--                        <span>Bảo hành chất lượng</span>--%>
<%--                    </div>--%>
                    <div class="d-flex align-items-center mb-3">
                        <i class="fas fa-check-circle text-success me-3"></i>
                        <span>Dịch vụ thuê nhanh chóng</span>
                    </div>
                    <div class="d-flex align-items-center mb-3">
                        <i class="fas fa-check-circle text-success me-3"></i>
                        <span>Hỗ trợ 24/7</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<!-- Single Product End -->

<jsp:include page="../layout/footer.jsp"/>

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
</body>

</html>
