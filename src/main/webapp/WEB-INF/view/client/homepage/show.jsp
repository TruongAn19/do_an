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

<!-- Spinner Start -->
<div id="spinner"
     class="show w-100 vh-100 bg-white position-fixed translate-middle top-50 start-50  d-flex align-items-center justify-content-center">
    <div class="spinner-grow text-primary" role="status"></div>
</div>
<!-- Spinner End -->

<jsp:include page="../layout/header.jsp"/>

<jsp:include page="../layout/banner.jsp"/>

<!-- Fruits Shop Start-->
<div class="container-fluid fruite py-5">
    <div class="container py-5">
        <div class="tab-class text-center">
            <div class="row g-4">
                <div class="col-lg-4 text-start">
                    <h1>Sản phẩm chính</h1>
                </div>
                <div class="col-lg-8 text-end">
                    <ul class="nav nav-pills d-inline-flex text-center mb-5">
                        <li class="nav-item">
                            <a class="d-flex m-2 py-2 bg-light rounded-pill active" href="/main-products">
                                <span class="text-dark" style="width: 130px;">All Products</span>
                            </a>
                        </li>
                    </ul>
                </div>
            </div>
            <div class="tab-content">
                <div id="tab-1" class="tab-pane fade show p-0 active">
                    <div class="row g-4 align-items-stretch">
                        <c:forEach var="mainProduct" items="${mainProducts}">
                            <div class="col-md-6 col-lg-4 col-xl-3 d-flex">
                                <div class="rounded position-relative fruite-item h-100 d-flex flex-column w-100 border border-secondary">
                                    <div class="fruite-img">
                                        <img src="/images/product/${mainProduct.image}"
                                             class="img-fluid w-100 rounded-top"
                                             style="object-fit: cover; aspect-ratio: 1/1;" alt="Sản phẩm">
                                    </div>

                                    <div class="text-white bg-warning px-3 py-1 rounded position-absolute"
                                         style="top: 10px; left: 10px;">Sân
                                    </div>

                                    <div class="p-4 border-top-0 rounded-bottom d-flex flex-column flex-grow-1">
                                        <!-- Giữ chiều cao cố định cho tên -->
                                        <h4 class="text-success text-center" style="min-height: 40px; font-size: 20px;">
                                            <a href="/mainProduct/${mainProduct.id}"
                                               class="text-decoration-none">${mainProduct.name}</a>
                                        </h4>

                                        <!-- Giữ chiều cao cố định cho mô tả -->
                                        <p class="text-center flex-grow-1" style="min-height: 50px;">
                                                ${mainProduct.shortDesc}
                                        </p>

                                        <div class="mt-auto text-center">
                                            <h5 class="fw-bold mb-3">
                                                <c:choose>
                                                    <c:when test="${mainProduct.sale > 0}">
                                                        <del><fmt:formatNumber type="number"
                                                                               value="${mainProduct.price}"/> đ
                                                        </del>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <fmt:formatNumber type="number" value="${mainProduct.price}"/> đ
                                                    </c:otherwise>
                                                </c:choose>
                                            </h5>

                                            <c:if test="${mainProduct.sale > 0}">
                                                <h5 class="fw-bold text-danger mb-3">
                                                    <fmt:formatNumber type="number"
                                                                      value="${mainProduct.price - (mainProduct.price * mainProduct.sale /100)}"/>
                                                    đ
                                                    (<fmt:formatNumber type="number" value="${mainProduct.sale}"/> %)
                                                </h5>
                                            </c:if>

                                            <a href="/booking/${mainProduct.id}"
                                               class="btn btn-outline-success rounded-pill px-3">
                                                <i class="fa fa-shopping-bag me-2"></i> Book a course
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </div>

            </div>
        </div>
    </div>
</div>
<div class="container-fluid fruite py-5">
    <div class="container py-5">
        <div class="tab-class text-center">
            <div class="row g-4">
                <div class="col-lg-4 text-start">
                    <h1>Sản phẩm phụ kiện</h1>
                </div>
                <div class="col-lg-8 text-end">
                    <ul class="nav nav-pills d-inline-flex text-center mb-5">
                        <li class="nav-item">
                            <a class="d-flex m-2 py-2 bg-light rounded-pill active" href="/by-products">
                                <span class="text-dark" style="width: 130px;">All Products</span>
                            </a>
                        </li>
                    </ul>
                </div>
            </div>
            <div class="tab-content">
                <div id="tab-1" class="tab-pane fade show p-0 active">
                    <div class="row g-4 align-items-stretch">
                        <div class="col-lg-12">
                            <div class="row g-4">
                                <c:forEach var="racket" items="${racketList}">
                                    <div class="col-md-6 col-lg-4 col-xl-3 d-flex">
                                        <div class="rounded position-relative fruite-item h-100 d-flex flex-column w-100 border border-secondary">
                                            <div class="fruite-img">
                                                <img src="/images/racket/${racket.image}"
                                                     class="img-fluid w-100 rounded-top"
                                                     style="object-fit: cover; aspect-ratio: 1/1;"
                                                     alt="Sản phẩm">
                                            </div>

                                            <div class="text-white bg-secondary px-3 py-1 rounded position-absolute"
                                                 style="top: 10px; left: 10px;">Phụ kiện
                                            </div>

                                            <div class="p-4 border-top-0 rounded-bottom d-flex flex-column flex-grow-1">
                                                <!-- Tên sản phẩm -->
                                                <h4 class="text-center" style="font-size: 16px; min-height: 40px;">
                                                    <a href="/racket/${racket.id}" class="text-decoration-none">
                                                            ${racket.name}
                                                    </a>
                                                </h4>
                                                <p class="text-center text-decoration-none" style="font-size: 15px;">
                                                        ${racket.factory}
                                                </p>
                                                <!-- Product Price -->
                                                <p class="fw-bold text-dark text-center" style="font-size: 15px;">
                                                    <fmt:formatNumber type="number" value="${racket.price}"/> đ
                                                </p>
                                                <!-- Add to Cart Button -->
                                                <form action="/user/rental-page/${racket.id}" method="get" class="mt-auto">
                                                    <input type="hidden" name="${_csrf.parameterName}"
                                                           value="${_csrf.token}"/>
                                                    <button class="btn border border-secondary rounded-pill px-3 text-primary d-flex align-items-center mx-auto">
                                                        <i class="fa fa-shopping-bag me-2 text-primary"></i> Rental racket
                                                    </button>
                                                </form>
                                            </div>

                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </div>

                </div>

            </div>
        </div>
    </div>
</div>

<jsp:include page="../layout/feature.jsp"/>
<!-- Fruits Shop End-->
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