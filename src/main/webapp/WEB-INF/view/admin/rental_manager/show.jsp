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
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css" rel="stylesheet" crossorigin="anonymous" />
    <link href="/css/styles.css" rel="stylesheet" />
    <script src="https://use.fontawesome.com/releases/v6.3.0/js/all.js" crossorigin="anonymous"></script>
    <style>
        :root {
            --primary-color: #4f46e5;
            --primary-hover: #4338ca;
            --success-color: #10b981;
            --warning-color: #f59e0b;
            --danger-color: #ef4444;
            --light-bg: #f9fafb;
            --card-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
            --border-radius: 0.5rem;
        }

        body {
            background-color: var(--light-bg);
        }

        .card {
            border-radius: var(--border-radius);
            box-shadow: var(--card-shadow);
            border: none;
        }

        .card-header {
            background-color: #fff;
            border-bottom: 1px solid rgba(0, 0, 0, 0.08);
            padding: 1.25rem 1.5rem;
        }

        .table {
            margin-bottom: 0;
        }

        .table th {
            font-weight: 600;
            color: #374151;
            border-bottom-width: 1px;
        }

        .table td {
            vertical-align: middle;
        }

        .badge {
            font-weight: 500;
            padding: 0.35em 0.65em;
            border-radius: 0.25rem;
        }

        .badge-success {
            background-color: var(--success-color);
            color: white;
        }

        .badge-warning {
            background-color: var(--warning-color);
            color: white;
        }

        .badge-danger {
            background-color: var(--danger-color);
            color: white;
        }

        .btn-icon {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 2rem;
            height: 2rem;
            border-radius: 0.375rem;
        }

        .btn-success {
            background-color: var(--success-color);
            border-color: var(--success-color);
        }

        .btn-warning {
            background-color: var(--warning-color);
            border-color: var(--warning-color);
        }

        .btn-danger {
            background-color: var(--danger-color);
            border-color: var(--danger-color);
        }

        .page-title {
            font-weight: 700;
            color: #111827;
            margin-bottom: 0.5rem;
        }

        .breadcrumb {
            margin-bottom: 1.5rem;
        }

        .breadcrumb-item a {
            color: var(--primary-color);
            text-decoration: none;
        }

        .breadcrumb-item a:hover {
            color: var(--primary-hover);
            text-decoration: underline;
        }

        .search-container {
            position: relative;
            max-width: 300px;
        }

        .search-container .form-control {
            padding-right: 2.5rem;
            border-radius: var(--border-radius);
        }

        .search-container .btn-search {
            position: absolute;
            right: 0;
            top: 0;
            height: 100%;
            border: none;
            background: transparent;
        }

        .search-container .btn-clear {
            position: absolute;
            right: 2.5rem;
            top: 0;
            height: 100%;
            border: none;
            background: transparent;
            color: #6b7280;
        }

        .pagination {
            margin-bottom: 0;
        }

        .pagination .page-link {
            color: var(--primary-color);
            border-radius: 0.375rem;
            margin: 0 0.125rem;
        }

        .pagination .page-item.active .page-link {
            background-color: var(--primary-color);
            border-color: var(--primary-color);
        }

        .table-responsive {
            border-radius: var(--border-radius);
            border: 1px solid rgba(0, 0, 0, 0.08);
        }

        .action-buttons {
            display: flex;
            gap: 0.5rem;
        }

        .action-buttons .btn {
            padding: 0.25rem 0.5rem;
        }

        .action-buttons .btn i {
            font-size: 0.875rem;
        }
    </style>
</head>

<body class="sb-nav-fixed">
<jsp:include page="../layout/header.jsp" />
<div id="layoutSidenav">
    <jsp:include page="../layout/sidebar.jsp" />
    <div id="layoutSidenav_content">
        <main>
            <div class="container-fluid px-4 py-4">
                <h1 class="page-title mt-3">Quản Lý Vợt Thuê</h1>
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><a href="/admin">Dashboard</a></li>
                        <li class="breadcrumb-item active">Vợt Thuê</li>
                    </ol>
                </nav>

                <div class="card mb-4">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h5 class="mb-0">Bảng Vợt Thuê</h5>
                        <div class="search-container">
                            <form action="/admin/rental" method="get" class="d-flex">
                                <div class="position-relative w-100">
                                    <input type="text" class="form-control" name="search"
                                           placeholder="Tìm kiếm theo mã đặt..."
                                           value="${not empty searchTerm ? searchTerm : ''}">
                                    <button class="btn-search" type="submit">
                                        <i class="fas fa-search"></i>
                                    </button>
                                    <c:if test="${not empty searchTerm}">
                                        <a href="/admin/rental" class="btn-clear">
                                            <i class="fas fa-times"></i>
                                        </a>
                                    </c:if>
                                </div>
                                <input type="hidden" name="page" value="1">
                            </form>
                        </div>
                    </div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover align-middle">
                                <thead>
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
                                        <td><strong>${rental.rentalToolCode}</strong></td>
                                        <td>${rental.fullName}</td>
                                        <td>${rental.email}</td>
                                        <td>${rental.phone}</td>
                                        <td>${rental.rentalDate}</td>
                                        <td>${rental.quantity}</td>
                                        <td>

                                                    ${rental.status}

                                        </td>
                                        <td>
                                            <div class="action-buttons">
                                                <a href="/admin/rental/${rental.id}" class="btn btn-success btn-sm" title="Xem">
                                                    <i class="fas fa-eye"></i>
                                                </a>
                                                <a href="/admin/rental/update/${rental.id}" class="btn btn-warning btn-sm" title="Cập Nhật">
                                                    <i class="fas fa-edit"></i>
                                                </a>
                                                <a href="/admin/rental/delete/${rental.id}" class="btn btn-danger btn-sm" title="Xóa">
                                                    <i class="fas fa-trash"></i>
                                                </a>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                    <div class="card-footer">
                        <nav aria-label="Page navigation">
                            <ul class="pagination justify-content-center mb-0">
                                <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                    <a class="page-link" href="/admin/rental?page=${currentPage - 1}${not empty searchTerm ? '&search='.concat(searchTerm) : ''}" aria-label="Previous">
                                        <span aria-hidden="true">&laquo;</span>
                                    </a>
                                </li>

                                <c:forEach begin="1" end="${totalPages}" var="pageNum">
                                    <li class="page-item ${pageNum == currentPage ? 'active' : ''}">
                                        <a class="page-link" href="/admin/rental?page=${pageNum}${not empty searchTerm ? '&search='.concat(searchTerm) : ''}">${pageNum}</a>
                                    </li>
                                </c:forEach>

                                <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                    <a class="page-link" href="/admin/rental?page=${currentPage + 1}${not empty searchTerm ? '&search='.concat(searchTerm) : ''}" aria-label="Next">
                                        <span aria-hidden="true">&raquo;</span>
                                    </a>
                                </li>
                            </ul>
                        </nav>
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