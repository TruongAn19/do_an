<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no" />
    <meta name="description" content="Quản lý vợt thuê" />
    <meta name="author" content="TruongAn" />
    <title>Manage Rackets</title>
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
                <h1 class="mt-4">Quản Lý Vợt Thuê</h1>
                <ol class="breadcrumb mb-4">
                    <li class="breadcrumb-item"><a href="/admin">Dashboard</a></li>
                    <li class="breadcrumb-item active">Vợt Thuê</li>
                </ol>

                <div class="mt-5">
                    <div class="row">
                        <div class="col-12 mx-auto">
                            <div class="d-flex justify-content-between align-items-center mb-3">
                                <h3>Bảng Vợt Thuê</h3>
                                <div class="mb-3">
                                    <form action="/admin/rental" method="get" class="row g-3">
                                        <div>
                                            <div class="input-group">
                                                <input type="text" class="form-control" name="search"
                                                       placeholder="Tìm kiếm sân theo mã đặt..."
                                                       value="${not empty searchTerm ? searchTerm : ''}">
                                                <button class="btn btn-outline-secondary" type="submit">
                                                    <i class="fas fa-search"></i>
                                                </button>
                                                <c:if test="${not empty searchTerm}">
                                                    <a href="/admin/rental"
                                                       class="btn btn-outline-danger">
                                                        <i class="fas fa-times"></i>
                                                    </a>
                                                </c:if>
                                            </div>
                                        </div>
                                        <input type="hidden" name="page" value="1">
                                    </form>
                                </div>
                                <a href="/admin/rental/create" class="btn btn-primary">Thêm Vợt Thuê</a>
                            </div>

                            <table class="table table-bordered table-hover">
                                <thead class="table-light">
                                <tr>
                                    <th>Code</th>
                                    <th>Họ và Tên</th>
                                    <th>Email</th>
                                    <th>Số Điện Thoại</th>
                                    <th>Ngày Thuê</th>
                                    <th>Số Lượng</th>
                                    <th>Trạng Thái</th>
                                    <th>Hành Động</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="rental" items="${rentals}">
                                    <tr>
                                        <td>${rental.rentalToolCode}</td>
                                        <td>${rental.fullName}</td>
                                        <td>${rental.email}</td>
                                        <td>${rental.phone}</td>
                                        <td>${rental.rentalDate}</td>
                                        <td>${rental.quantity}</td>
                                        <td>${rental.status}</td>
                                        <td>
                                            <a href="/admin/rental/${rental.id}" class="btn btn-success btn-sm">Xem</a>
                                            <a href="/admin/rental/update/${rental.id}" class="btn btn-warning btn-sm mx-1">Cập Nhật</a>
                                            <a href="/admin/rental/delete/${rental.id}" class="btn btn-danger btn-sm">Xóa</a>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>

                            <nav aria-label="Page navigation">
                                <ul class="pagination justify-content-center">
                                    <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                        <a class="page-link" href="/admin/rental?page=${currentPage - 1}" aria-label="Previous">
                                            <span aria-hidden="true">&laquo;</span>
                                        </a>
                                    </li>

                                    <c:forEach begin="1" end="${totalPages}" var="pageNum">
                                        <li class="page-item ${pageNum == currentPage ? 'active' : ''}">
                                            <a class="page-link" href="/admin/rental?page=${pageNum}">${pageNum}</a>
                                        </li>
                                    </c:forEach>

                                    <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                        <a class="page-link" href="/admin/rental?page=${currentPage + 1}" aria-label="Next">
                                            <span aria-hidden="true">&raquo;</span>
                                        </a>
                                    </li>
                                </ul>
                            </nav>

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
