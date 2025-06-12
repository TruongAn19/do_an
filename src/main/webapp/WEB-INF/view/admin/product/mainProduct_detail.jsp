<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no" />
    <meta name="description" content="Dự án quản lý sân cầu lông" />
    <meta name="author" content="TruongAn" />
    <title>Product Details</title>
    <link href="/css/styles.css" rel="stylesheet" />
    <script src="https://use.fontawesome.com/releases/v6.3.0/js/all.js" crossorigin="anonymous"></script>
    <style>
        .product-card {
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
            margin-bottom: 2rem;
            background-color: #fff;
            transition: transform 0.3s ease;
        }

        .product-card:hover {
            transform: translateY(-5px);
        }

        .product-header {
            background: linear-gradient(135deg, #2c3e50 0%, #1a252f 100%);
            color: white;
            padding: 1.25rem;
            font-weight: 600;
            font-size: 1.25rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .product-id-badge {
            background-color: rgba(255, 255, 255, 0.2);
            padding: 0.25rem 0.75rem;
            border-radius: 50px;
            font-size: 0.875rem;
        }

        .product-body {
            padding: 0;
        }

        .product-image-container {
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 1.5rem;
            background-color: #f8f9fa;
        }

        .product-image {
            max-width: 100%;
            max-height: 300px;
            object-fit: contain;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            transition: transform 0.3s ease;
        }

        .product-image:hover {
            transform: scale(1.05);
        }

        .product-info {
            padding: 1.5rem;
        }

        .info-group {
            margin-bottom: 1.25rem;
            border-bottom: 1px solid #e9ecef;
            padding-bottom: 1rem;
        }

        .info-group:last-child {
            border-bottom: none;
            margin-bottom: 0;
        }

        .info-label {
            font-weight: 600;
            color: #6c757d;
            margin-bottom: 0.25rem;
            font-size: 0.875rem;
            text-transform: uppercase;
        }

        .info-value {
            font-size: 1.1rem;
            color: #212529;
        }

        .price-value {
            font-size: 1.5rem;
            font-weight: 700;
            color: #2c3e50;
        }

        .sale-badge {
            display: inline-block;
            background-color: #dc3545;
            color: white;
            padding: 0.25rem 0.75rem;
            border-radius: 50px;
            font-weight: 600;
            margin-left: 0.75rem;
            font-size: 0.875rem;
        }

        .quantity-badge {
            display: inline-block;
            background-color: #28a745;
            color: white;
            padding: 0.25rem 0.75rem;
            border-radius: 50px;
            font-weight: 600;
            margin-left: 0.75rem;
            font-size: 0.875rem;
        }

        .description-box {
            background-color: #f8f9fa;
            padding: 1rem;
            border-radius: 8px;
            max-height: 150px;
            overflow-y: auto;
        }

        .action-buttons {
            display: flex;
            gap: 1rem;
            margin-top: 2rem;
        }

        .btn-back {
            padding: 0.5rem 1.5rem;
            border-radius: 50px;
            font-weight: 500;
            display: flex;
            align-items: center;
            gap: 0.5rem;
            transition: all 0.3s ease;
        }

        .btn-back:hover {
            transform: translateX(-5px);
        }

        .btn-edit {
            padding: 0.5rem 1.5rem;
            border-radius: 50px;
            font-weight: 500;
            display: flex;
            align-items: center;
            gap: 0.5rem;
            background-color: #f39c12;
            border-color: #f39c12;
            color: white;
            transition: all 0.3s ease;
        }

        .btn-edit:hover {
            background-color: #e67e22;
            border-color: #e67e22;
            transform: translateY(-3px);
        }
    </style>
</head>

<body class="sb-nav-fixed">
<jsp:include page="../layout/header.jsp" />
<div id="layoutSidenav">
    <jsp:include page="../layout/sidebar.jsp" />
    <div id="layoutSidenav_content">
        <main>
            <div class="container-fluid px-4">
                <div class="d-flex justify-content-between align-items-center mt-4 mb-2">
                    <h1 class="fw-bold">Product Details</h1>
                </div>

                <ol class="breadcrumb mb-4">
                    <li class="breadcrumb-item"><a href="/admin">Dashboard</a></li>
                    <li class="breadcrumb-item"><a href="/admin/mainProduct">Products</a></li>
                    <li class="breadcrumb-item active">Product #${id}</li>
                </ol>

                <div class="row">
                    <div class="col-xl-10 col-lg-12 mx-auto">
                        <div class="product-card">
                            <div class="product-header">
                                <span>Product Details</span>
                                <span class="product-id-badge">ID: ${product.id}</span>
                            </div>

                            <div class="product-body">
                                <div class="row g-0">
                                    <div class="col-lg-5">
                                        <div class="product-image-container">
                                            <img src="/images/product/${product.image}" class="product-image" alt="${product.name}">
                                        </div>
                                    </div>

                                    <div class="col-lg-7">
                                        <div class="product-info">
                                            <div class="info-group">
                                                <div class="info-label">Product Name</div>
                                                <div class="info-value fs-4 fw-bold">${product.name}</div>
                                            </div>

                                            <div class="info-group">
                                                <div class="info-label">Price</div>
                                                <div class="d-flex align-items-center">
                                                    <div class="price-value">${product.price}</div>
                                                    <c:if test="${product.sale > 0}">
                                                        <div class="sale-badge">
                                                            <i class="fas fa-tag me-1"></i>${product.sale}% OFF
                                                        </div>
                                                    </c:if>
                                                </div>
                                            </div>

                                            <div class="info-group">
                                                <div class="info-label">Availability</div>
                                                <div class="d-flex align-items-center">
                                                    <div class="info-value">${product.address}</div>
                                                    <div class="quantity-badge">
                                                        <i class="fas fa-cubes me-1"></i>${product.quantity} in stock
                                                    </div>
                                                </div>
                                            </div>

                                            <div class="info-group">
                                                <div class="info-label">Description</div>
                                                <div class="description-box">
                                                    ${product.detailDesc}
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="action-buttons">
                            <a href="/admin/mainProduct" class="btn btn-success btn-back">
                                <i class="fas fa-arrow-left"></i>
                                Back to Products
                            </a>

                            <a href="/admin/product/update_mainProduct/${product.id}" class="btn btn-edit">
                                <i class="fas fa-edit"></i>
                                Edit Product
                            </a>
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