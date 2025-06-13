<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <title>Sản Phẩm - Sân cầu lông</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@400;500;600&family=Raleway:wght@600;800&display=swap" rel="stylesheet">

    <!-- Icons -->
    <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.15.4/css/all.css" />
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.4.1/font/bootstrap-icons.css" rel="stylesheet">
    <script src="https://use.fontawesome.com/releases/v6.3.0/js/all.js" crossorigin="anonymous"></script>

    <!-- CSS Libraries -->
    <link href="/client/lib/lightbox/css/lightbox.min.css" rel="stylesheet">
    <link href="/client/lib/owlcarousel/assets/owl.carousel.min.css" rel="stylesheet">

    <!-- Bootstrap & Custom Styles -->
    <link href="/client/css/bootstrap.min.css" rel="stylesheet">
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
            margin-top: 20px;
            margin-bottom: 20px;
            border-radius: 8px;
        }

        .breadcrumb-item a {
            color: var(--primary);
            text-decoration: none;
            font-weight: 500;
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
            display: flex;
            flex-direction: column;
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
            height: 220px; /* Fixed height for image container */
            position: relative;
        }

        .product-img img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            transition: transform 0.5s;
        }

        .product-card:hover .product-img img {
            transform: scale(1.05);
        }

        .product-content {
            padding: 20px;
            display: flex;
            flex-direction: column;
            flex-grow: 1;
            min-height: 180px; /* Ensure enough space for content */
        }

        .product-title {
            font-weight: 600;
            font-size: 16px;
            line-height: 1.4;
            margin-bottom: 15px;
            min-height: 45px; /* Fixed height for title */
        }

        .product-title a {
            color: var(--dark);
            text-decoration: none;
            transition: color 0.3s;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .product-title a:hover {
            color: var(--primary);
        }

        .product-price {
            font-size: 18px;
            font-weight: 700;
            color: var(--secondary);
            margin-bottom: 15px;
        }

        .rent-btn {
            background-color: var(--primary);
            color: white !important;
            border: none;
            transition: all 0.3s;
            font-weight: 500;
            padding: 10px 20px;
            border-radius: 30px;
            margin-top: 10px;
        }

        .rent-btn:hover {
            background-color: var(--dark);
            transform: translateY(-2px);
        }

        .pagination {
            display: flex;
            justify-content: center;
            align-items: center;
            margin-top: 2rem;
        }

        .pagination .page-item {
            margin: 0 3px;
        }

        .pagination .page-link {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 40px;
            height: 40px;
            border-radius: 50%;
            color: var(--primary);
            font-weight: 500;
            background-color: #fff;
            border: 1px solid #dee2e6;
            padding: 0;
        }

        .pagination .page-item.active .page-link {
            background-color: var(--primary);
            border-color: var(--primary);
            color: white;
        }

        .pagination .page-item.disabled .page-link {
            color: #6c757d;
            pointer-events: none;
            background-color: #fff;
            border-color: #dee2e6;
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

            .product-img {
                height: 200px;
            }

            .product-content {
                min-height: 160px;
            }
        }
    </style>
</head>

<body>

<!-- Spinner -->
<div id="spinner" class="show w-100 vh-100 bg-white position-fixed top-50 start-50 translate-middle d-flex align-items-center justify-content-center">
    <div class="spinner-grow text-primary" role="status"></div>
</div>

<jsp:include page="../layout/header.jsp" />

<!-- Product Section -->
<div class="container py-5 mt-5">
    <!-- Breadcrumb -->
    <div class="breadcrumb-container px-3 mb-4">
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb mb-0">
                <li class="breadcrumb-item"><a href="/HomePage">Trang Chủ</a></li>
                <li class="breadcrumb-item active" aria-current="page">Danh Sách Sản Phẩm</li>
            </ol>
        </nav>
    </div>

    <div class="row g-4">
        <div class="col-12 col-lg-3 filter-section">
            <!-- Phần form filter -->
            <form method="get" action="/by-products" id="filterForm">
                <input type="hidden" name="page" value="0" id="pageInput">

                <!-- Hãng sản xuất -->
                <div class="filter-card mb-4">
                    <h4 class="filter-title">Hãng sản xuất</h4>
                    <div class="d-flex flex-column gap-2">
                        <c:forEach var="brand" items="${['Yonex','Li-Ning','Victor','Babolat','Carlton','Apacs']}">
                            <div class="form-check">
                                <input class="form-check-input factory-checkbox"
                                       type="checkbox"
                                       name="factory"
                                       value="${brand}"
                                       id="brand-${brand}"
                                       <c:if test="${selectedFactories != null && selectedFactories.contains(brand)}">checked</c:if> />
                                <label class="form-check-label" for="brand-${brand}">${brand}</label>
                            </div>
                        </c:forEach>
                    </div>
                </div>

                <!-- Mức giá -->
<%--                <div class="filter-card mb-4">--%>
<%--                    <h4 class="filter-title">Mức giá thuê theo ngày</h4>--%>
<%--                    <div class="d-flex flex-column gap-2">--%>
<%--                        <c:forEach var="priceOption" items="${['duoi-500-nghin', '500-nghin-1-trieu', '1-5-trieu', 'tren-5-trieu']}">--%>
<%--                            <div class="form-check">--%>
<%--                                <input class="form-check-input price-checkbox"--%>
<%--                                       type="checkbox"--%>
<%--                                       name="price"--%>
<%--                                       value="${priceOption}"--%>
<%--                                       id="${priceOption}"--%>
<%--                                       <c:if test="${selectedPrices != null && selectedPrices.contains(priceOption)}">checked</c:if> />--%>
<%--                                <label class="form-check-label" for="${priceOption}">--%>
<%--                                    <c:choose>--%>
<%--                                        <c:when test="${priceOption eq 'duoi-500-nghin'}">Dưới 500 nghìn</c:when>--%>
<%--                                        <c:when test="${priceOption eq '500-nghin-1-trieu'}">500 nghìn - 1 triệu</c:when>--%>
<%--                                        <c:when test="${priceOption eq '1-5-trieu'}">1 - 5 triệu</c:when>--%>
<%--                                        <c:when test="${priceOption eq 'tren-5-trieu'}">Trên 5 triệu</c:when>--%>
<%--                                    </c:choose>--%>
<%--                                </label>--%>
<%--                            </div>--%>
<%--                        </c:forEach>--%>
<%--                    </div>--%>
<%--                </div>--%>

                <!-- Sắp xếp -->
                <div class="filter-card mb-4">
                    <h4 class="filter-title">Sắp xếp</h4>
                    <div class="d-flex flex-column gap-2">
                        <div class="form-check">
                            <input class="form-check-input sort-radio" type="radio" name="sort" id="sort-asc" value="gia-tang-dan"
                                   <c:if test="${selectedSort == 'gia-tang-dan'}">checked</c:if> />
                            <label class="form-check-label" for="sort-asc">Giá thuê tăng dần</label>
                        </div>
                        <div class="form-check">
                            <input class="form-check-input sort-radio" type="radio" name="sort" id="sort-desc" value="gia-giam-dan"
                                   <c:if test="${selectedSort == 'gia-giam-dan'}">checked</c:if> />
                            <label class="form-check-label" for="sort-desc">Giá thuê giảm dần</label>
                        </div>
                        <div class="form-check">
                            <input class="form-check-input sort-radio" type="radio" name="sort" id="sort-none" value="gia-nothing"
                                   <c:if test="${selectedSort == 'gia-nothing' || empty selectedSort}">checked</c:if> />
                            <label class="form-check-label" for="sort-none">Không sắp xếp</label>
                        </div>
                    </div>
                </div>

                <button type="submit" class="btn w-100 py-3 rounded-pill" id="btnFilter">
                    <i class="fas fa-filter me-2"></i>Lọc Sản Phẩm
                </button>
            </form>
            
        </div>


        <!-- Product Grid -->
        <div class="col-12 col-lg-9">
            <c:if test="${empty listRacket}">
                <div class="alert alert-info text-center py-4">
                    <i class="fas fa-info-circle fa-2x mb-3"></i>
                    <p class="mb-0">Không tìm thấy sản phẩm phù hợp với tiêu chí tìm kiếm.</p>
                </div>
            </c:if>

            <div class="row g-4">
                <c:forEach var="racket" items="${listRacket}">
                    <div class="col-md-6 col-lg-4 mb-4">
                        <div class="product-card">
                            <div class="product-img">
                                <img src="/images/racket/${racket.image}" alt="${racket.name}">
                            </div>
                            <div class="product-content">
                                <h5 class="product-title">
                                    <a href="/racket/${racket.id}">${racket.name}</a>
                                </h5>
                                <p>Số lượng cho thuê: ${racket.quantity}</p>
                                <p>${racket.product.name}</p>
                                <div class="mt-auto">
                                    <p class="product-price mb-3">Giá vợt:
                                        <fmt:formatNumber type="number" value="${racket.price}" /> VNĐ
                                    </p>
                                    <form action="/user/rental-page/${racket.id}" method="get">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                        <input type="hidden" name="type" value="DAILY" />
                                        <button class="btn rent-btn w-100">
                                            <i class="fa fa-shopping-bag me-2"></i>Thuê ngay
                                        </button>
                                    </form>
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
                               href="/by-products?page=${currentPage - 1}${not empty param.search ? '&search=' += param.search : ''}${queryString}"
                               aria-label="Previous">
                                <span aria-hidden="true">&laquo;</span>
                            </a>
                        </li>

                        <c:forEach begin="0" end="${totalPages - 1}" varStatus="loop">
                            <c:if test="${loop.index < totalPages}">
                                <li class="page-item">
                                    <a class="${(loop.index + 1) eq currentPage ? 'active page-link' : 'page-link'}"
                                       href="/by-products?page=${loop.index + 1}${not empty param.search ? '&search=' += param.search : ''}${queryString}">
                                            ${loop.index + 1}
                                    </a>
                                </li>
                            </c:if>
                        </c:forEach>

                        <li class="page-item">
                            <a class="${totalPages eq currentPage ? 'disabled page-link' : 'page-link'}"
                               href="/by-products?page=${currentPage + 1}${not empty param.search ? '&search=' += param.search : ''}${queryString}"
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

<jsp:include page="../layout/footer.jsp" />

<!-- Back to Top -->
<a href="#" class="btn rounded-circle back-to-top"><i class="fa fa-arrow-up"></i></a>


<!-- JS Libraries -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.4/jquery.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="/client/lib/easing/easing.min.js"></script>
<script src="/client/lib/waypoints/waypoints.min.js"></script>
<script src="/client/lib/lightbox/js/lightbox.min.js"></script>
<script src="/client/lib/owlcarousel/owl.carousel.min.js"></script>
<!-- JavaScript xử lý -->
<script>
    $('#btnFilter').click(function (event) {
        event.preventDefault();

        // Khai báo mảng chứa các lựa chọn
        let factoryArr = [];
        let priceArr = [];
        let sortValue = $('input[name="sort"]:checked').val();

        // Lấy danh sách hãng được chọn
        $('.factory-checkbox:checked').each(function () {
            factoryArr.push($(this).val());
        });

        // Lấy danh sách mức giá được chọn
        $('.price-checkbox:checked').each(function () {
            priceArr.push($(this).val());
        });

        // Tạo URL hiện tại và đối tượng query param
        const currentUrl = new URL(window.location.href);
        const searchParams = currentUrl.searchParams;

        // Reset lại các filter cũ
        searchParams.delete('factory');
        searchParams.delete('price');
        searchParams.delete('sort');
        searchParams.delete('page'); // reset về trang đầu

        // Gán lại các filter mới
        if (factoryArr.length > 0) {
            factoryArr.forEach(value => {
                searchParams.append('factory', value);
            });
        }

        if (priceArr.length > 0) {
            priceArr.forEach(value => {
                searchParams.append('price', value);
            });
        }

        if (sortValue) {
            searchParams.set('sort', sortValue);
        }

        searchParams.set('page', '0'); // luôn về trang đầu

        // Tải lại trang với URL mới
        window.location.href = currentUrl.toString();
    });

</script>
<script src="/client/js/main.js"></script>

</body>
</html>