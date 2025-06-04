<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no"/>
    <meta name="description" content="Dự án sancaulong"/>
    <meta name="author" content="TruongAn"/>
    <title>Product</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css" rel="stylesheet"
          crossorigin="anonymous"/>
    <link href="/css/styles.css" rel="stylesheet"/>
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

        .btn-primary {
            background-color: var(--primary-color);
            border-color: var(--primary-color);
        }

        .btn-primary:hover {
            background-color: var(--primary-hover);
            border-color: var(--primary-hover);
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

        .pagination {
            margin-bottom: 0;
            margin-top: 1.5rem;
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
            padding: 0.375rem 0.75rem;
            display: inline-flex;
            align-items: center;
            gap: 0.25rem;
        }

        .action-buttons .btn i {
            font-size: 0.875rem;
        }

        .price-badge {
            background-color: #f3f4f6;
            color: #374151;
            padding: 0.35em 0.65em;
            font-weight: 500;
            border-radius: 0.25rem;
        }

        .sale-badge {
            background-color: #fee2e2;
            color: #ef4444;
            padding: 0.35em 0.65em;
            font-weight: 500;
            border-radius: 0.25rem;
        }

        .search-container {
            position: relative;
            max-width: 350px;
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

        .header-actions {
            display: flex;
            align-items: center;
            gap: 1rem;
        }

        .not-found-message {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            margin: 0;
            font-size: 0.875rem;
            color: var(--danger-color);
        }

        .not-found-message i {
            font-size: 1rem;
        }
    </style>

    <style>
        .table {
            border-collapse: separate;
            border-spacing: 0 10px;
        }

        .table thead th {
            background-color: #f8f9fa;
            border: none;
            font-weight: 600;
            text-transform: uppercase;
            font-size: 0.8rem;
            letter-spacing: 0.5px;
            padding: 12px 15px;
        }

        .table tbody tr {
            background-color: white;
            box-shadow: 0 2px 10px rgba(0,0,0,0.03);
            transition: all 0.3s ease;
            border-radius: 8px;
        }

        .table tbody tr:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(0,0,0,0.08);
        }

        .table td {
            padding: 15px;
            vertical-align: middle;
            border-top: none;
            border-bottom: 1px solid #f0f0f0;
        }

        .table td:first-child {
            border-top-left-radius: 8px;
            border-bottom-left-radius: 8px;
        }

        .table td:last-child {
            border-top-right-radius: 8px;
            border-bottom-right-radius: 8px;
        }

        .avatar-sm {
            transition: all 0.3s ease;
        }

        tr:hover .avatar-sm {
            transform: scale(1.1);
            background-color: var(--bs-primary-bg-subtle)!important;
        }

        .price-badge, .sale-badge {
            font-size: 0.85rem;
            min-width: 80px;
            display: inline-block;
        }
    </style>
</head>

<body class="sb-nav-fixed">
<jsp:include page="../layout/header.jsp"/>
<div id="layoutSidenav">
    <jsp:include page="../layout/sidebar.jsp"/>
    <div id="layoutSidenav_content">
        <main>
            <div class="container-fluid px-4 py-4">
                <h1 class="page-title mt-3">Manage Court</h1>
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><a href="/admin">Dashboard</a></li>
                        <li class="breadcrumb-item active">Product</li>
                    </ol>
                </nav>

                <div class="card mb-4">
                    <div class="card-header">
                        <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3">
                            <h5 class="mb-0">Table Court</h5>
                            <div class="header-actions">
                                <div class="search-container">
                                    <form action="/admin/mainProduct" method="get" class="d-flex">
                                        <div class="position-relative w-100">
                                            <input type="text" class="form-control" name="search"
                                                   placeholder="Tìm kiếm theo tên sản phẩm..."
                                                   value="${not empty searchTerm ? searchTerm : ''}">
                                            <button class="btn-search" type="submit">
                                                <i class="fas fa-search"></i>
                                            </button>
                                            <input type="hidden" name="page" value="1">
                                        </div>
                                    </form>
                                </div>
                                <a href="/admin/product/create_mainProduct" class="btn btn-primary">
                                    <i class="fas fa-plus me-2"></i>Create Court
                                </a>
                            </div>
                        </div>
                        <c:if test="${not empty searchTerm}">
                            <div class="mt-3">
                                <div class="d-flex align-items-center">
                                    <p class="not-found-message">
                                        <i class="fas fa-exclamation-circle"></i>
                                        Không Tìm Thấy Sản Phẩm
                                    </p>
                                    <a href="/admin/mainProduct" class="btn btn-outline-danger btn-sm ms-3">
                                        <i class="fas fa-times me-1"></i>Clear Search
                                    </a>
                                </div>
                            </div>
                        </c:if>
                    </div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover align-middle">
                                <thead class="table-light">
                                <tr>
                                    <th class="text-center" style="width: 20%">Product</th>
                                    <th class="text-center" style="width: 20%">Address</th>
                                    <th class="text-center" style="width: 15%">Price</th>
                                    <th class="text-center" style="width: 10%">Sale</th>
                                    <th class="text-center" style="width: 15%">Status</th>
                                    <th class="text-center" style="width: 20%">Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="product" items="${mainProducts}">
                                    <tr class="position-relative">
                                        <!-- Product Name Column -->
                                        <td class="text-center">
                                            <div class="d-flex align-items-center justify-content-center">
                                                <div class="avatar-sm bg-light rounded p-2 me-3 d-flex align-items-center justify-content-center shadow-sm"
                                                     style="width: 40px; height: 40px; background-color: #f8f9fa!important;">
                                                    <i class="fas fa-box text-primary"></i>
                                                </div>
                                                <div>
                                                    <h6 class="mb-0 fw-semibold">${product.name}</h6>
                                                </div>
                                            </div>
                                        </td>

                                        <!-- Address Column -->
                                        <td class="text-center">
                <span class="text-truncate d-inline-block" style="max-width: 200px;"
                      data-bs-toggle="tooltip" data-bs-placement="top" title="${product.address}">
                        ${product.address}
                </span>
                                        </td>

                                        <!-- Price Column -->
                                        <td class="text-center">
                <span class="price-badge bg-primary bg-opacity-10 text-primary px-3 py-1 rounded-pill fw-medium">
                    <fmt:formatNumber value="${product.price}" type="number" maxFractionDigits="0"/> VNĐ
                </span>
                                        </td>

                                        <!-- Sale Column -->
                                        <td class="text-center">
                <span class="sale-badge bg-success bg-opacity-10 text-success px-3 py-1 rounded-pill fw-medium">
                    ${product.sale}%
                </span>
                                        </td>

                                        <!-- Status Column -->
                                        <td class="text-center">
                                            <c:choose>
                                                <c:when test="${product.status == 'AVAILABLE'}">
                        <span class="badge bg-success bg-opacity-10 text-success py-2 px-3 rounded-pill">
                            <i class="fas fa-check-circle me-1"></i> Sẵn sàng
                        </span>
                                                </c:when>
                                                <c:when test="${product.status == 'DELETED'}">
                        <span class="badge bg-danger bg-opacity-10 text-danger py-2 px-3 rounded-pill">
                            <i class="fas fa-times-circle me-1"></i> Đã xoá
                        </span>
                                                </c:when>
                                                <c:when test="${product.status == 'MAINTAINING'}">
                        <span class="badge bg-warning bg-opacity-10 text-warning py-2 px-3 rounded-pill">
                            <i class="fas fa-tools me-1"></i> Đang bảo trì
                        </span>
                                                </c:when>
                                                <c:otherwise>
                        <span class="badge bg-secondary bg-opacity-10 text-secondary py-2 px-3 rounded-pill">
                            <i class="fas fa-question-circle me-1"></i> ${product.status}
                        </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>

                                        <!-- Actions Column -->
                                        <td class="text-center">
                                            <div class="d-flex justify-content-center gap-2">
                                                <a href="/admin/mainProduct/${product.id}"
                                                   class="btn btn-sm btn-outline-primary rounded-pill px-3 py-1 d-flex align-items-center"
                                                   data-bs-toggle="tooltip" data-bs-placement="top" title="View details">
                                                    <i class="fas fa-eye me-1"></i> View
                                                </a>
                                                <a href="/admin/product/update_mainProduct/${product.id}"
                                                   class="btn btn-sm btn-outline-warning rounded-pill px-3 py-1 d-flex align-items-center"
                                                   data-bs-toggle="tooltip" data-bs-placement="top" title="Edit product">
                                                    <i class="fas fa-edit me-1"></i> Edit
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
                                <li class="page-item ${1 eq currentPage ? 'disabled' : ''}">
                                    <a class="page-link"
                                       href="/main-products?page=${currentPage - 1}${not empty param.search ? '&search=' += param.search : ''}"
                                       aria-label="Previous">
                                        <span aria-hidden="true">&laquo;</span>
                                    </a>
                                </li>
                                <c:if test="${totalPages > 0}">
                                    <c:forEach begin="0" end="${totalPages - 1}" varStatus="loop">
                                        <li class="page-item ${(loop.index + 1) eq currentPage ? 'active' : ''}">
                                            <a class="page-link"
                                               href="/main-products?page=${loop.index + 1}${not empty param.search ? '&search=' += param.search : ''}">
                                                    ${loop.index + 1}
                                            </a>
                                        </li>
                                    </c:forEach>
                                </c:if>
                                <li class="page-item ${totalPages eq currentPage ? 'disabled' : ''}">
                                    <a class="page-link"
                                       href="/main-products?page=${currentPage + 1}${not empty param.search ? '&search=' += param.search : ''}"
                                       aria-label="Next">
                                        <span aria-hidden="true">&raquo;</span>
                                    </a>
                                </li>
                            </ul>
                        </nav>
                    </div>
                </div>
            </div>
        </main>
        <jsp:include page="../layout/footer.jsp"/>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"
        crossorigin="anonymous"></script>
<script src="/js/scripts.js"></script>
<script>
    // Initialize tooltips
    document.addEventListener('DOMContentLoaded', function() {
        var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
        var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl)
        })
    })
</script>
</body>

</html>