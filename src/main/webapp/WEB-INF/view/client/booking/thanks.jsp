<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đặt sân thành công - GolfClub</title>

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

    <style>
        .racket-card {
            border: 1px solid #dee2e6;
            border-radius: 10px;
            overflow: hidden;
            background-color: #fff;
            box-shadow: 0 2px 6px rgba(0,0,0,0.05);
            transition: transform 0.2s;
        }

        .racket-card:hover {
            transform: translateY(-4px);
        }

        .racket-img img {
            width: 100%;
            height: 150px;
            object-fit: cover;
        }

        .racket-body {
            padding: 12px;
            text-align: center;
        }

        .racket-name {
            font-size: 15px;
            font-weight: 600;
            color: #333;
            margin-bottom: 6px;
        }

        .racket-name a {
            text-decoration: none;
            color: inherit;
        }

        .racket-price {
            font-size: 14px;
            color: #555;
            margin-bottom: 12px;
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

<div class="container" style="margin-top: 100px;">
    <div class="row">
        <div class="col-12 mt-5">
            <div class="alert alert-success" role="alert">
                Cảm ơn bạn đã đặt sân, đơn đặt đã được xác nhận thành công.
                Mã đặt sân của bạn là: <strong>${bookingCode}</strong>
            </div>
        </div>

        <!-- Hỏi thuê vợt -->
        <div class="col-12 mt-3 text-center">
            <h5>Bạn có muốn thuê vợt không?</h5>
            <button class="btn btn-success m-2" data-bs-toggle="modal" data-bs-target="#racketModal">Có</button>
            <button class="btn btn-secondary m-2">Không</button>
        </div>
    </div>
</div>

<!-- Modal thuê vợt -->
<div class="modal fade" id="racketModal" tabindex="-1" aria-labelledby="racketModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="racketModalLabel">Danh sách vợt cho thuê</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button>
            </div>
            <div class="modal-body">
                <div class="row">
                    <c:forEach var="racket" items="${racketList}">
                        <div class="col-sm-6 col-md-4 col-lg-3 d-flex">
                            <div class="racket-card d-flex flex-column flex-grow-1 w-100">
                                <!-- Image -->
                                <div class="racket-img">
                                    <img src="/images/racket/${racket.image}" alt="${racket.name}">
                                </div>

                                <!-- Content -->
                                <div class="racket-body d-flex flex-column flex-grow-1">
                                    <h6 class="racket-name">
                                        <a href="/byProduct/${racket.id}">${racket.name}</a>
                                    </h6>
                                    <p class="racket-price">
                                        <fmt:formatNumber type="number" value="${racket.price}" /> đ
                                    </p>
                                    <form action="/user/rental-page/${racket.id}" method="get" class="mt-auto">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                        <input type="hidden" name="type" value="ON_SITE" />
                                        <input type="hidden" name="bookingCode" value="${bookingCode}" />
                                        <button class="btn btn-outline-primary btn-sm w-100">
                                            <i class="fa fa-shopping-bag me-1"></i> Thuê vợt
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

<jsp:include page="../layout/feature.jsp" />
<jsp:include page="../layout/footer.jsp" />

<!-- Back to Top -->
<a href="#" class="btn btn-primary border-3 border-primary rounded-circle back-to-top">
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
