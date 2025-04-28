<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Racket Detail</title>
    <link href="/css/styles.css" rel="stylesheet" />
    <script src="https://use.fontawesome.com/releases/v6.3.0/js/all.js" crossorigin="anonymous"></script>
</head>
<body class="sb-nav-fixed">
<jsp:include page="../layout/header.jsp" />
<div id="layoutSidenav">
    <jsp:include page="../layout/sidebar.jsp" />
    <div id="layoutSidenav_content">
        <main>
            <div class="container-fluid px-4">
                <h1 class="mt-4">Manage Racket</h1>
                <ol class="breadcrumb mb-4">
                    <li class="breadcrumb-item"><a href="/admin">Dashboard</a></li>
                    <li class="breadcrumb-item active">Racket</li>
                </ol>

                <div class="container mt-5">
                    <div class="row">
                        <div class="col-md-8 mx-auto">
                            <hr />
                            <div class="card shadow-sm rounded">
                                <div class="card-header text-white bg-primary">
                                    Racket Information
                                </div>
                                <div class="row p-4">
                                    <!-- Ảnh -->
                                    <div class="col-md-6 d-flex justify-content-center align-items-center">
                                        <c:choose>
                                            <c:when test="${not empty racket.image}">
                                                <img src="/images/racket/${racket.image}" class="img-fluid rounded" alt="Racket Image" style="max-height:300px;">
                                            </c:when>
                                            <c:otherwise>
                                                <img src="/images/default.png" class="img-fluid rounded" alt="No Image" style="max-height:300px;">
                                            </c:otherwise>
                                        </c:choose>
                                    </div>

                                    <!-- Thông tin -->
                                    <div class="col-md-6">
                                        <ul class="list-group list-group-flush">
                                            <li class="list-group-item"><strong>Factory:</strong> ${racket.factory}</li>
                                            <li class="list-group-item"><strong>Name:</strong> ${racket.name}</li>
                                            <li class="list-group-item"><strong>Price:</strong> ${racket.price} VNĐ</li>
                                            <li class="list-group-item"><strong>Giá thuê theo ngày:</strong> ${racket.rentalPricePerDay} VNĐ</li>
                                            <li class="list-group-item"><strong>Giá thuê tại sân:</strong> ${racket.rentalPricePerPlay} VNĐ</li>
                                            <li class="list-group-item"><strong>Quantity:</strong> ${racket.quantity}</li>
                                            <li class="list-group-item"><strong>Available:</strong>
                                                <c:choose>
                                                    <c:when test="${racket.available}">
                                                        <span class="badge bg-success">Available</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge bg-danger">Not Available</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </li>
                                            <li class="list-group-item"><strong>Product:</strong> ${racket.product.name}</li>
                                        </ul>
                                    </div>
                                </div>
                            </div>

                            <a href="/admin/racket" class="btn btn-success mt-4">Back</a>
                        </div>
                    </div>
                </div>
            </div>
        </main>
        <jsp:include page="../layout/footer.jsp" />
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js" crossorigin="anonymous"></script>
<script src="/js/scripts.js"></script>
</body>
</html>
