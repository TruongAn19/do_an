<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no" />
    <meta name="description" content="Quản lý vợt thuê" />
    <meta name="author" content="TruongAn" />
    <title>Update Racket Rental</title>
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
                <h1 class="mt-4">Cập Nhật Vợt Thuê</h1>
                <ol class="breadcrumb mb-4">
                    <li class="breadcrumb-item"><a href="/admin">Dashboard</a></li>
                    <li class="breadcrumb-item"><a href="/admin/rental">Vợt Thuê</a></li>
                    <li class="breadcrumb-item active">Cập Nhật</li>
                </ol>

                <div class="mt-5">
                    <div class="row">
                        <div class="col-md-8 mx-auto">
                            <div class="card">
                                <div class="card-header">
                                    <h4>Thông Tin Cập Nhật</h4>
                                </div>
                                <div class="card-body">
                                    <form:form method="POST" action="/admin/rental/update/${rentalTool.id}">
                                        <div class="row">
                                            <div class="col-md-6 mb-3">
                                                <label for="quantity" class="form-label">Số Lượng</label>
                                                <input type="number" class="form-control" id="quantity" name="quantity" value="${rentalTool.quantity}" required />
                                            </div>
                                            <div class="col-md-6 mb-3">
                                                <label for="rentalPrice" class="form-label">Giá Thuê</label>
                                                <input type="text" class="form-control" id="rentalPrice" name="rentalPrice" value="${rentalTool.rentalPrice}" required />
                                            </div>
                                        </div>

                                        <div class="row">
                                            <div class="col-md-6 mb-3">
                                                <label for="status" class="form-label">Trạng Thái</label>
                                                <select class="form-select" id="status" name="status" required>
                                                    <option value="PENDING" ${rentalTool.status == 'PENDING' ? 'selected' : ''}>Chờ</option>
                                                    <option value="PAID" ${rentalTool.status == 'PAID' ? 'selected' : ''}>Đã thanh toán</option>
                                                    <option value="COMPLETED" ${rentalTool.status == 'COMPLETED' ? 'selected' : ''}>Hoàn Thành</option>
                                                    <option value="CANCELLED" ${rentalTool.status == 'CANCELLED' ? 'selected' : ''}>Hủy</option>
                                                </select>
                                            </div>
                                            <div class="col-md-6 mb-3">
                                                <label for="price" class="form-label">Giá cọc trước:</label>
                                                <input type="text" class="form-control" id="price" name="price" value="${rentalTool.price}" required />
                                            </div>
                                        </div>

                                        <div class="mt-4">
                                            <h5>Thông Tin Vợt:</h5>
                                            <div class="row">
                                                <div class="col-md-6">
                                                    <p><strong>Tên Vợt:</strong> ${racket.name}</p>
                                                </div>
                                                <div class="col-md-6">
                                                    <p><strong>Giá Vợt:</strong> ${racket.price}</p>
                                                </div>
                                            </div>
                                            <div class="mb-3">
                                                <img src="/images/racket/${racket.image}" alt="Racket Image" class="img-fluid" style="max-width: 300px;">
                                            </div>
                                        </div>

                                        <button type="submit" class="btn btn-primary">Cập Nhật</button>
                                        <a href="/admin/rental" class="btn btn-secondary ms-2">Quay lại</a>
                                    </form:form>
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
