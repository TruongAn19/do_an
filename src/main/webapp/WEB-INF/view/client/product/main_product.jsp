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
    <link href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@400;500;600&family=Raleway:wght@600;800&display=swap" rel="stylesheet">

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

        .breadcrumb-container {
            background-color: #f8f9fa;
            padding: 15px 0;
            margin-bottom: 20px;
            border-radius: 8px;
        }

        .breadcrumb-item a {
            color: var(--primary);
            text-decoration: none;
            font-weight: 500;
        }

        .search-form {
            margin-bottom: 25px;
        }

        .search-form .form-control {
            border-radius: 30px 0 0 30px;
            padding: 12px 20px;
            box-shadow: none;
            border: 1px solid #ddd;
        }

        .search-form .btn-primary {
            border-radius: 0 30px 30px 0;
            padding: 12px 25px;
        }

        .search-form .btn-outline-secondary {
            border-radius: 30px;
            padding: 12px 20px;
            margin-left: 10px;
        }

        .filter-card {
            background-color: white;
            border-radius: 10px;
            box-shadow: 0 0 15px rgba(0, 0, 0, 0.05);
            padding: 20px;
            margin-bottom: 20px;
        }

        .filter-title {
            font-size: 18px;
            font-weight: 600;
            margin-bottom: 15px;
            color: var(--dark);
            border-bottom: 1px solid #eee;
            padding-bottom: 10px;
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
            background-color: var(--secondary);
            color: white;
            padding: 5px 10px;
            border-radius: 5px;
            font-size: 12px;
            font-weight: 600;
            z-index: 1;
        }

        .product-content {
            padding: 20px;
        }

        .product-title {
            font-weight: 600;
            margin-bottom: 10px;
            font-size: 16px;
            height: 40px;
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
            height: 60px;
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

        .book-btn {
            background-color: white;
            color: var(--primary) !important;
            border: 1px solid var(--primary);
            transition: all 0.3s;
            font-weight: 500;
            border-radius: 30px;
            padding: 8px 20px;
        }

        .book-btn:hover {
            background-color: var(--primary);
            color: white !important;
        }

        .pagination {
            margin-top: 30px;
            display: flex;
            justify-content: center;
            align-items: center;
        }

        .pagination .page-item {
            display: inline-block;
            margin: 0 3px;
        }

        .pagination .page-item .page-link {
            color: var(--primary);
            border-radius: 5px;
            padding: 8px 15px;
            display: flex;
            align-items: center;
            justify-content: center;
            min-width: 40px;
            height: 40px;
        }

        .pagination .page-item .active {
            background-color: var(--primary);
            border-color: var(--primary);
            color: white;
        }

        .pagination .page-item .disabled {
            color: #6c757d;
            pointer-events: none;
        }

        .form-check-input:checked {
            background-color: var(--primary);
            border-color: var(--primary);
        }

        #btnFilter {
            background-color: var(--primary);
            color: white !important;
            border: none;
            transition: all 0.3s;
            font-weight: 500;
            border-radius: 30px;
        }

        #btnFilter:hover {
            background-color: var(--dark);
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
            .filter-section {
                margin-bottom: 30px;
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

<jsp:include page="../layout/header.jsp" />

<!-- Products Section Start -->
<div class="container-fluid py-5 mt-5">
    <div class="container py-5">
        <!-- Breadcrumb -->
        <div class="breadcrumb-container mb-4">
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item"><a href="/HomePage">Home</a></li>
                    <li class="breadcrumb-item active" aria-current="page">Danh Sách Sản Phẩm</li>
                </ol>
            </nav>
        </div>

        <!-- Search Form -->
        <div class="row mb-5">
            <div class="col-md-8 mx-auto">
                <form action="/main-products" method="get" class="search-form">
                    <div class="input-group">
                        <input type="text" class="form-control" name="search" placeholder="Nhập tên sản phẩm..." value="${param.search}">
                        <button type="submit" class="btn btn-primary">
                            <i class="fas fa-search"></i> Tìm kiếm
                        </button>
                        <c:if test="${not empty param.search}">
                            <a href="/main-products" class="btn btn-outline-secondary">
                                <i class="fas fa-times"></i> Xóa
                            </a>
                        </c:if>
                    </div>
                </form>
            </div>
        </div>

        <div class="row g-4">
            <!-- Filter Sidebar -->
            <div class="col-12 col-lg-3 filter-section">
                <!-- Address Filter -->
                <div class="filter-card" id="addressFilter">
                    <h4 class="filter-title">Địa chỉ</h4>
                    <div class="d-flex flex-column gap-2">
                        <div class="form-check">
                            <input class="form-check-input" type="checkbox" id="target-1" value="Hà Đông">
                            <label class="form-check-label" for="target-1">Hà Đông</label>
                        </div>
                        <div class="form-check">
                            <input class="form-check-input" type="checkbox" id="target-2" value="Thanh Trì">
                            <label class="form-check-label" for="target-2">Thanh Trì</label>
                        </div>
                        <div class="form-check">
                            <input class="form-check-input" type="checkbox" id="target-3" value="Mỹ Đình">
                            <label class="form-check-label" for="target-3">Mỹ Đình</label>
                        </div>
                    </div>
                </div>

                <!-- Price Filter -->
                <div class="filter-card" id="priceFilter">
                    <h4 class="filter-title">Mức giá</h4>
                    <div class="d-flex flex-column gap-2">
                        <div class="form-check">
                            <input class="form-check-input" type="checkbox" id="price-2" value="duoi-500-nghin">
                            <label class="form-check-label" for="price-2">Dưới 500 nghìn</label>
                        </div>
                        <div class="form-check">
                            <input class="form-check-input" type="checkbox" id="price-3" value="500-nghin-1-trieu">
                            <label class="form-check-label" for="price-3">Từ 500 nghìn - 1 triệu</label>
                        </div>
                        <div class="form-check">
                            <input class="form-check-input" type="checkbox" id="price-4" value="1-5-trieu">
                            <label class="form-check-label" for="price-4">Từ 1 - 5 triệu</label>
                        </div>
                        <div class="form-check">
                            <input class="form-check-input" type="checkbox" id="price-5" value="tren-5-trieu">
                            <label class="form-check-label" for="price-5">Trên 5 triệu</label>
                        </div>
                    </div>
                </div>

                <!-- Sort Options -->
                <div class="filter-card">
                    <h4 class="filter-title">Sắp xếp</h4>
                    <div class="d-flex flex-column gap-2">
                        <div class="form-check">
                            <input class="form-check-input" type="radio" id="sort-1" value="gia-tang-dan" name="radio-sort">
                            <label class="form-check-label" for="sort-1">Giá tăng dần</label>
                        </div>
                        <div class="form-check">
                            <input class="form-check-input" type="radio" id="sort-2" value="gia-giam-dan" name="radio-sort">
                            <label class="form-check-label" for="sort-2">Giá giảm dần</label>
                        </div>
                        <div class="form-check">
                            <input class="form-check-input" type="radio" id="sort-3" checked value="gia-nothing" name="radio-sort">
                            <label class="form-check-label" for="sort-3">Không sắp xếp</label>
                        </div>
                    </div>
                </div>

                <!-- Filter Button -->
                <button class="btn w-100 py-3 mb-4" id="btnFilter">
                    <i class="fas fa-filter me-2"></i> Lọc Sản Phẩm
                </button>
            </div>

            <!-- Product Grid -->
            <div class="col-12 col-lg-9">
                <div class="row g-4">
                    <c:forEach var="mainProduct" items="${listMainProducts}">
                        <div class="col-md-6 col-lg-4 mb-4">
                            <div class="product-card h-100 d-flex flex-column">
                                <div class="product-img">
                                    <img src="/images/product/${mainProduct.image}" class="img-fluid w-100" style="aspect-ratio: 4/3; object-fit: cover;" alt="${mainProduct.name}">
                                    <div class="product-badge">Sân</div>
                                </div>
                                <div class="product-content d-flex flex-column flex-grow-1">
                                    <h5 class="product-title">
                                        <a href="/mainProduct/${mainProduct.id}">${mainProduct.name}</a>
                                    </h5>
                                    <p class="product-desc">${mainProduct.shortDesc}</p>
                                    <div class="mt-auto">
                                        <c:choose>
                                            <c:when test="${mainProduct.sale > 0}">
                                                <p class="product-price mb-1">
                                                    <del><fmt:formatNumber type="number" value="${mainProduct.price}" /> VNĐ</del>
                                                </p>
                                                <p class="product-sale-price">
                                                    <fmt:formatNumber type="number" value="${mainProduct.price - (mainProduct.price * mainProduct.sale /100)}" /> VNĐ
                                                    <span class="badge bg-danger ms-2">-<fmt:formatNumber type="number" value="${mainProduct.sale}" />%</span>
                                                </p>
                                            </c:when>
                                            <c:otherwise>
                                                <p class="product-price mb-3">
                                                    <fmt:formatNumber type="number" value="${mainProduct.price}" /> VNĐ
                                                </p>
                                            </c:otherwise>
                                        </c:choose>
                                        <div class="text-center">
                                            <c:choose>
                                                <c:when test="${mainProduct.status == 'MAINTAINING'}">
                                                    <p class="text-danger fw-bold">Sân đang bảo trì</p>
                                                </c:when>

                                                <c:otherwise>
                                                    <div class="text-center">
                                                        <a href="/booking/${mainProduct.id}" class="btn book-btn">
                                                            <i class="fa fa-calendar-check me-2"></i> Book a course
                                                        </a>
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>

                <!-- Pagination -->
                <c:if test="${totalPages > 0}">
                    <div class="d-flex justify-content-center mt-5">
                        <ul class="pagination">
                            <li class="page-item">
                                <a class="${1 eq currentPage ? 'disabled page-link' : 'page-link'}"
                                   href="/main-products?page=${currentPage - 1}${not empty param.search ? '&search=' += param.search : ''}${queryString}"
                                   aria-label="Previous">
                                    <span aria-hidden="true">&laquo;</span>
                                </a>
                            </li>

                            <c:forEach begin="0" end="${totalPages - 1}" varStatus="loop">
                                <c:if test="${loop.index < totalPages}">
                                    <li class="page-item">
                                        <a class="${(loop.index + 1) eq currentPage ? 'active page-link' : 'page-link'}"
                                           href="/main-products?page=${loop.index + 1}${not empty param.search ? '&search=' += param.search : ''}${queryString}">
                                                ${loop.index + 1}
                                        </a>
                                    </li>
                                </c:if>
                            </c:forEach>

                            <li class="page-item">
                                <a class="${totalPages eq currentPage ? 'disabled page-link' : 'page-link'}"
                                   href="/main-products?page=${currentPage + 1}${not empty param.search ? '&search=' += param.search : ''}${queryString}"
                                   aria-label="Next">
                                    <span aria-hidden="true">&raquo;</span>
                                </a>
                            </li>
                        </ul>
                    </div>
                </c:if>
            </div>
        </div>
    </div>
</div>
<!-- Products Section End -->

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
<script>
    //handle filter products
    $('#btnFilter').click(function (event) {
        event.preventDefault();

        let factoryArr = [];
        let addressArr = [];
        let priceArr = [];

        //address filter (sửa từ 'target' thành 'address')
        $("#addressFilter .form-check-input:checked").each(function () {
            addressArr.push($(this).val());
        });

        //price filter
        $("#priceFilter .form-check-input:checked").each(function () {
            priceArr.push($(this).val());
        });

        //sort order
        let sortValue = $('input[name="radio-sort"]:checked').val();

        const currentUrl = new URL(window.location.href);
        const searchParams = currentUrl.searchParams;

        // Add or update query parameters
        searchParams.set('page', '1');
        searchParams.set('sort', sortValue);

        //reset old filters

        searchParams.delete('address'); // sửa từ target
        searchParams.delete('price');

        if (factoryArr.length > 0) {
            searchParams.set('factory', factoryArr.join(','));
        }
        if (addressArr.length > 0) {
            searchParams.set('address', addressArr.join(',')); // sửa từ target
        }
        if (priceArr.length > 0) {
            searchParams.set('price', priceArr.join(','));
        }

        // Update the URL and reload the page
        window.location.href = currentUrl.toString();
    });
</script>
<!-- Template Javascript -->
<script src="/client/js/main.js"></script>
</body>

</html>