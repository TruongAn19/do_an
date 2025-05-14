<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no" />
    <meta name="description" content="Quản lý vợt thuê" />
    <meta name="author" content="TruongAn" />
    <title>Racket Rental Detail</title>
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
                <h1 class="mt-4">Chi Tiết Vợt Thuê</h1>
                <ol class="breadcrumb mb-4">
                    <li class="breadcrumb-item"><a href="/admin">Dashboard</a></li>
                    <li class="breadcrumb-item"><a href="/admin/rental">Vợt Thuê</a></li>
                    <li class="breadcrumb-item active">Chi Tiết</li>
                </ol>

                <div class="mt-5">
                    <div class="row">
                        <div class="col-12 mx-auto">
                            <div class="card">
                                <div class="card-header">
                                    <h4>Thông Tin Vợt Thuê</h4>
                                </div>
                                <div class="card-body">
                                    <div class="row">
                                        <div class="col-md-6">
                                            <p><strong>Họ và Tên:</strong> ${rentalTool.fullName}</p>
                                            <p><strong>Email:</strong> ${rentalTool.email}</p>
                                            <p><strong>Số Điện Thoại:</strong> ${rentalTool.phone}</p>
                                            <p><strong>Ngày Thuê:</strong> ${rentalTool.rentalDate}</p>
                                        </div>
                                        <div class="col-md-6">
                                            <p><strong>Số Lượng:</strong> ${rentalTool.quantity}</p>
                                            <p><strong>Trạng Thái:</strong> ${rentalTool.status}</p>

                                            <p><strong>Giá Thuê: </strong><fmt:formatNumber type="number" value=" ${rentalTool.rentalPrice}" /> đ</p>
                                        </div>
                                    </div>

                                    <div class="mt-4">
                                        <h5>Thông Tin Vợt:</h5>
                                        <p><strong>Tên Vợt:</strong> ${racket.name}</p>
                                        <p><strong>Ảnh Vợt:</strong></p>
                                        <img src="/images/racket/${racket.image}" alt="Racket Image" class="img-fluid" style="max-width: 300px;">
                                    </div>

                                    <a href="/admin/rental" class="btn btn-secondary mt-3">Quay Lại</a>
                                </div>
                            </div>
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
