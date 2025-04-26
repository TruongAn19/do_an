<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

                <!DOCTYPE html>
                <html lang="en">

                <head>
                    <meta charset="utf-8">
                    <title> Sản Phẩm - Sân cầu lông</title>
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
                    <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.15.4/css/all.css" />
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

                    <jsp:include page="../layout/header.jsp" />

                    <!-- Single Product Start -->
                    <div class="container-fluid py-5 mt-5">
                        <div class="container py-5">
                            <div class="row g-4 mb-5">
                                <div>
                                    <nav aria-label="breadcrumb">
                                        <ol class="breadcrumb">
                                            <li class="breadcrumb-item"><a href="/HomePage">Home</a></li>
                                            <li class="breadcrumb-item active" aria-current="page">Danh Sách Sản Phẩm
                                            </li>
                                        </ol>
                                    </nav>
                                </div>

                                <div class="row g-4 fruite">
                                    <div class="row mb-4">
                                        <div class="col-md-6 mx-auto">
                                            <form action="/main-products" method="get" class="search-form">
                                                <div class="input-group">
                                                    <input type="text" class="form-control" name="search"
                                                        placeholder="Nhập tên sản phẩm..." value="${param.search}">
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
                                    <div class="col-12 col-md-4">
                                        <div class="row g-4">
                                            <div class="col-12" id="addressFilter">
                                                <div class="mb-2"><b>Địa chỉ</b></div>
                                                <div class="form-check form-check-inline">
                                                    <input class="form-check-input" type="checkbox" id="target-1"
                                                        value="Hà Đông">
                                                    <label class="form-check-label" for="target-1">Hà Đông</label>
                                                </div>

                                                <div class="form-check form-check-inline">
                                                    <input class="form-check-input" type="checkbox" id="target-2"
                                                        value="Thanh Trì">
                                                    <label class="form-check-label" for="target-2">Thanh Trì</label>
                                                </div>
                                                <div class="form-check form-check-inline">
                                                    <input class="form-check-input" type="checkbox" id="target-3"
                                                        value="Mỹ Đình">
                                                    <label class="form-check-label" for="target-3">Mỹ Đình</label>
                                                </div>
                                            </div>
                                            <div class="col-12" id="priceFilter">
                                                <div class="mb-2"><b>Mức giá</b></div>

                                                <div class="form-check form-check-inline">
                                                    <input class="form-check-input" type="checkbox" id="price-2"
                                                        value="duoi-500-nghin">
                                                    <label class="form-check-label" for="price-2">Dưới 500 nghìn</label>
                                                </div>

                                                <div class="form-check form-check-inline">
                                                    <input class="form-check-input" type="checkbox" id="price-3"
                                                        value="500-nghin-1-trieu">
                                                    <label class="form-check-label" for="price-3">Từ 500 nghìn - 1
                                                        triệu</label>
                                                </div>

                                                <div class="form-check form-check-inline">
                                                    <input class="form-check-input" type="checkbox" id="price-4"
                                                        value="1-5-trieu">
                                                    <label class="form-check-label" for="price-4">Từ 1 - 5
                                                        triệu</label>
                                                </div>

                                                <div class="form-check form-check-inline">
                                                    <input class="form-check-input" type="checkbox" id="price-5"
                                                        value="tren-5-trieu">
                                                    <label class="form-check-label" for="price-5">Trên 5 triệu</label>
                                                </div>
                                            </div>
                                            <div class="col-12">
                                                <div class="mb-2"><b>Sắp xếp</b></div>

                                                <div class="form-check form-check-inline">
                                                    <input class="form-check-input" type="radio" id="sort-1"
                                                        value="gia-tang-dan" name="radio-sort">
                                                    <label class="form-check-label" for="sort-1">Giá tăng dần</label>
                                                </div>

                                                <div class="form-check form-check-inline">
                                                    <input class="form-check-input" type="radio" id="sort-2"
                                                        value="gia-giam-dan" name="radio-sort">
                                                    <label class="form-check-label" for="sort-2">Giá giảm dần</label>
                                                </div>

                                                <div class="form-check form-check-inline">
                                                    <input class="form-check-input" type="radio" id="sort-3" checked
                                                        value="gia-nothing" name="radio-sort">
                                                    <label class="form-check-label" for="sort-3">Không sắp xếp</label>
                                                </div>

                                            </div>
                                            <div class="col-12">
                                                <button
                                                    class="btn border-secondary rounded-pill px-4 py-3 text-primary text-uppercase mb-4"
                                                    id="btnFilter">
                                                    Lọc Sản Phẩm
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-12 col-md-8 text-center">
                                        <div class="row g-4">
                                            <c:forEach var="mainProduct" items="${listMainProducts}">
                                                <div class="col-md-6 col-lg-4 d-flex">
                                                    <div
                                                        class="rounded position-relative fruite-item h-100 d-flex flex-column w-100 border border-secondary">
                                                        <div class="fruite-img">
                                                            <img src="/images/product/${mainProduct.image}"
                                                                class="img-fluid w-100 rounded-top"
                                                                style="object-fit: cover; aspect-ratio: 4/3;" alt="">
                                                        </div>
                                                        <div class="text-white bg-secondary px-3 py-1 rounded position-absolute"
                                                            style="top: 10px; left: 10px;">Sân
                                                        </div>
                                                        <div
                                                            class="p-4 border-top-0 rounded-bottom d-flex flex-column flex-grow-1">
                                                            <!-- Tên sản phẩm -->
                                                            <h4 style="font-size: 15px;">
                                                                <a href="/mainProduct/${mainProduct.id}">
                                                                    ${mainProduct.name}
                                                                </a>
                                                            </h4>

                                                            <!-- Mô tả sản phẩm -->
                                                            <p style="font-size: 13px;" class="flex-grow-1">
                                                                ${mainProduct.shortDesc}
                                                            </p>

                                                            <div class="mt-auto text-center">
                                                                <!-- Hiển thị giá -->
                                                                <p style="font-size: 15px; text-align: center; width: 100%;"
                                                                    class="text-dark fw-bold mb-3">
                                                                    <fmt:formatNumber type="number"
                                                                        value="${mainProduct.price}" /> đ
                                                                </p>

                                                                <!-- Nút thêm vào giỏ hàng -->
                                                                <!-- <form action="/add-product-to-cart/${mainProduct.id}" method="post">
                                                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                                                    <button class="btn border border-secondary rounded-pill px-3 text-primary">
                                                                        <i class="fa fa-shopping-bag me-2 text-primary"></i> Add to cart
                                                                    </button>
                                                                </form> -->
                                                                <a href="/booking/${mainProduct.id}"
                                                                    class="btn btn-outline-success rounded-pill px-3">
                                                                    <i class="fa fa-shopping-bag me-2"></i> Book a
                                                                    course
                                                                </a>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </c:forEach>

                                            <!-- Phân trang -->
                                            <div class="pagination d-flex justify-content-center mt-5">
                                                <c:if test="${totalPages > 0}">
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
                                                </c:if>
                                            </div>
                                        </div>

                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <!-- Single Product End -->

                    <jsp:include page="../layout/footer.jsp" />


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