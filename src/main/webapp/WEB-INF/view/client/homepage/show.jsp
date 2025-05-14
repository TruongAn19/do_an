<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HomePage</title>

    <!-- Google Web Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@400;500;600&family=Raleway:wght@600;800&display=swap" rel="stylesheet">

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
        :root {
            --primary: #3CB815;
            --secondary: #F65005;
            --light: #F7F8FC;
            --dark: #111111;
        }

        body {
            font-family: 'Open Sans', sans-serif;
            background-color: #f8f9fa;
        }

        .section-title {
            position: relative;
            display: inline-block;
            letter-spacing: 1px;
            font-weight: bold;
            color: var(--dark);
        }

        .section-title::after {
            content: '';
            position: absolute;
            width: 50%;
            height: 3px;
            background: var(--primary);
            left: 0;
            bottom: -5px;
        }

        .product-section {
            background-color: #fff;
            border-radius: 15px;
            box-shadow: 0 0 20px rgba(0, 0, 0, 0.05);
            padding: 40px 0;
            margin-bottom: 30px;
        }

        .nav-pills .nav-item .active {
            background-color: var(--primary) !important;
            color: white !important;
        }

        .nav-pills .nav-item a:hover {
            background-color: #e9f7e5;
        }

        .product-card {
            transition: all 0.3s;
            height: 100%;
            border: none !important;
            box-shadow: 0 0 15px rgba(0, 0, 0, 0.05);
            background-color: white;
            border-radius: 10px;
            overflow: hidden;
        }

        .product-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
        }

        .product-img {
            overflow: hidden;
            position: relative;
        }

        .product-img img {
            transition: transform 0.5s;
        }

        .product-card:hover .product-img img {
            transform: scale(1.05);
        }

        .product-badge {
            position: absolute;
            top: 10px;
            left: 10px;
            padding: 5px 10px;
            border-radius: 5px;
            font-size: 12px;
            font-weight: 600;
            z-index: 1;
        }

        .badge-primary {
            background-color: var(--primary);
            color: white;
        }

        .badge-secondary {
            background-color: var(--secondary);
            color: white;
        }

        .product-title {
            font-weight: 600;
            margin: 15px 0 10px;
            overflow: hidden;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
        }

        .product-title a {
            color: var(--dark);
            text-decoration: none;
            transition: color 0.3s;
        }

        .product-title a:hover {
            color: var(--primary);
        }

        .product-desc {
            color: #6c757d;
            font-size: 14px;
            margin-bottom: 15px;
            overflow: hidden;
            display: -webkit-box;
            -webkit-line-clamp: 3;
            -webkit-box-orient: vertical;
        }

        .product-price {
            font-size: 18px;
            font-weight: 700;
            color: var(--dark);
            margin-bottom: 5px;
        }

        .product-price del {
            color: #6c757d;
            font-weight: 400;
            font-size: 16px;
        }

        .product-sale-price {
            font-size: 18px;
            font-weight: 700;
            color: #dc3545;
            margin-bottom: 15px;
        }

        .btn-book {
            background-color: white;
            color: var(--primary);
            border: 1px solid var(--primary);
            transition: all 0.3s;
            font-weight: 500;
            border-radius: 30px;
            padding: 8px 20px;
        }

        .btn-book:hover {
            background-color: var(--primary);
            color: white;
        }

        .btn-rent {
            background-color: white;
            color: var(--secondary);
            border: 1px solid var(--secondary);
            transition: all 0.3s;
            font-weight: 500;
            border-radius: 30px;
            padding: 8px 20px;
        }

        .btn-rent:hover {
            background-color: var(--secondary);
            color: white;
        }

        .view-all {
            display: inline-block;
            padding: 8px 25px;
            background-color: var(--light);
            color: var(--dark);
            font-weight: 500;
            border-radius: 30px;
            transition: all 0.3s;
            text-decoration: none;
        }

        .view-all:hover {
            background-color: var(--primary);
            color: white;
        }

        .spinner-grow {
            color: var(--primary) !important;
        }

        .back-to-top {
            position: fixed;
            right: 30px;
            bottom: 30px;
            z-index: 99;
            background-color: var(--primary);
            color: white;
            border: none;
            width: 40px;
            height: 40px;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: all 0.3s;
        }

        .back-to-top:hover {
            background-color: var(--dark);
        }

        @media (max-width: 768px) {
            .section-title::after {
                width: 30%;
            }
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

<jsp:include page="../layout/banner.jsp"/>

<!-- Main Products Section Start -->
<div class="container-fluid py-5">
    <div class="container py-5 product-section">
        <div class="row g-4 mb-4">
            <div class="col-lg-6">
                <h2 class="section-title">Sản phẩm chính</h2>
            </div>
            <div class="col-lg-6 text-lg-end">
                <a href="/main-products" class="view-all">
                    <i class="fas fa-th-large me-2"></i>Xem tất cả
                </a>
            </div>
        </div>

        <div class="row g-4">
            <c:forEach var="mainProduct" items="${mainProducts}">
                <div class="col-md-6 col-lg-4 col-xl-3">
                    <div class="product-card h-100 d-flex flex-column">
                        <div class="product-img">
                            <img src="/images/product/${mainProduct.image}" class="img-fluid w-100"
                                 style="aspect-ratio: 1/1; object-fit: cover;" alt="${mainProduct.name}">
                            <div class="product-badge badge-primary">Sân</div>
                        </div>
                        <div class="p-4 d-flex flex-column flex-grow-1">
                            <h5 class="product-title" style="min-height: 40px;">
                                <a href="/mainProduct/${mainProduct.id}">${mainProduct.name}</a>
                            </h5>
                            <p class="product-desc" style="min-height: 50px;">${mainProduct.shortDesc}</p>
                            <div class="mt-auto">
                                <c:choose>
                                    <c:when test="${mainProduct.sale > 0}">
                                        <p class="product-price mb-1">
                                            <del><fmt:formatNumber type="number" value="${mainProduct.price}"/> đ</del>
                                        </p>
                                        <p class="product-sale-price">
                                            <fmt:formatNumber type="number" value="${mainProduct.price - (mainProduct.price * mainProduct.sale /100)}"/> đ
                                            <span class="badge bg-danger ms-2">-<fmt:formatNumber type="number" value="${mainProduct.sale}"/>%</span>
                                        </p>
                                    </c:when>
                                    <c:otherwise>
                                        <p class="product-price mb-3">
                                            <fmt:formatNumber type="number" value="${mainProduct.price}"/> đ
                                        </p>
                                    </c:otherwise>
                                </c:choose>
                                <div class="text-center">
                                    <a href="/booking/${mainProduct.id}" class="btn btn-book w-100">
                                        <i class="fa fa-calendar-check me-2"></i>Book a course
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>
</div>
<!-- Main Products Section End -->

<!-- Accessories Section Start -->
<div class="container-fluid py-5">
    <div class="container py-5 product-section">
        <div class="row g-4 mb-4">
            <div class="col-lg-6">
                <h2 class="section-title">Sản phẩm phụ kiện</h2>
            </div>
            <div class="col-lg-6 text-lg-end">
                <a href="/by-products" class="view-all">
                    <i class="fas fa-th-large me-2"></i>Xem tất cả
                </a>
            </div>
        </div>

        <div class="row g-4">
            <c:forEach var="racket" items="${racketList}">
                <div class="col-md-6 col-lg-4 col-xl-3">
                    <div class="product-card h-100 d-flex flex-column">
                        <div class="product-img">
                            <img src="/images/racket/${racket.image}" class="img-fluid w-100"
                                 style="aspect-ratio: 1/1; object-fit: cover;" alt="${racket.name}">
                            <div class="product-badge badge-secondary">Phụ kiện</div>
                        </div>
                        <div class="p-4 d-flex flex-column flex-grow-1">
                            <h5 class="product-title" style="min-height: 40px;">
                                <a href="/racket/${racket.id}">${racket.name}</a>
                            </h5>
                            <p class="text-center mb-2" style="font-size: 14px; color: #6c757d;">
                                    ${racket.factory}
                            </p>
                            <div class="mt-auto">
                                <p class="product-price text-center mb-3">
                                    <fmt:formatNumber type="number" value="${racket.price}"/> đ
                                </p>
                                <form action="/user/rental-page/${racket.id}" method="get" class="text-center">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                    <input type="hidden" name="type" value="DAILY" />
                                    <button class="btn btn-rent w-100">
                                        <i class="fa fa-shopping-bag me-2"></i>Rental racket
                                    </button>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>
</div>
<!-- Accessories Section End -->

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