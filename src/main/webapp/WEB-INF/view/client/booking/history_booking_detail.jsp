<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="utf-8">
    <title>Lịch sử đặt sân - Sân cầu lông</title>
    <meta content="width=device-width, initial-scale=1.0" name="viewport">
    <meta content="" name="keywords">
    <meta content="" name="description">

    <!-- Google Web Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@400;600&family=Raleway:wght@600;800&display=swap"
          rel="stylesheet">

    <!-- Icon Font Stylesheet -->
    <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.15.4/css/all.css"/>
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
            --primary-color: #3b82f6;
            --primary-dark: #2563eb;
            --success-color: #10b981;
            --warning-color: #f59e0b;
            --danger-color: #ef4444;
            --gray-50: #f9fafb;
            --gray-100: #f3f4f6;
            --gray-200: #e5e7eb;
            --gray-300: #d1d5db;
            --gray-500: #6b7280;
            --gray-600: #4b5563;
            --gray-700: #374151;
            --gray-900: #111827;
            --border-radius: 12px;
            --shadow-sm: 0 1px 2px 0 rgb(0 0 0 / 0.05);
            --shadow-md: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
            --shadow-lg: 0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1);
        }

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background-color: var(--gray-50);
            color: var(--gray-900);
            line-height: 1.6;
        }

        .container-fluid {
            max-width: 1200px;
            margin: 0 auto;
            padding: 1.5rem;
        }

        /* Header Styles */
        .page-header {
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
            color: white;
            padding: 2rem;
            border-radius: var(--border-radius);
            margin-bottom: 2rem;
            box-shadow: var(--shadow-lg);
        }

        .page-header h1 {
            font-size: 2rem;
            font-weight: 700;
            margin-bottom: 0.5rem;
        }

        .order-id {
            background: rgba(255, 255, 255, 0.2);
            padding: 0.5rem 1rem;
            border-radius: 8px;
            display: inline-block;
            font-weight: 600;
            backdrop-filter: blur(10px);
        }

        /* Breadcrumb Styles */
        .breadcrumb {
            background: white;
            padding: 1rem 1.5rem;
            border-radius: var(--border-radius);
            box-shadow: var(--shadow-sm);
            margin-bottom: 1.5rem;
            border: 1px solid var(--gray-200);
        }

        .breadcrumb-item a {
            color: var(--primary-color);
            text-decoration: none;
            font-weight: 500;
            transition: color 0.2s ease;
        }

        .breadcrumb-item a:hover {
            color: var(--primary-dark);
        }

        .breadcrumb-item.active {
            color: var(--gray-600);
            font-weight: 500;
        }

        /* Card Styles */
        .modern-card {
            background: white;
            border-radius: var(--border-radius);
            box-shadow: var(--shadow-md);
            border: 1px solid var(--gray-200);
            margin-bottom: 2rem;
            overflow: hidden;
            transition: transform 0.2s ease, box-shadow 0.2s ease;
        }

        .modern-card:hover {
            transform: translateY(-2px);
            box-shadow: var(--shadow-lg);
        }

        .card-header-modern {
            background: linear-gradient(135deg, var(--gray-50) 0%, white 100%);
            padding: 1.5rem;
            border-bottom: 1px solid var(--gray-200);
        }

        .card-header-modern h3 {
            font-size: 1.25rem;
            font-weight: 600;
            color: var(--gray-900);
            margin: 0;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }

        .card-body-modern {
            padding: 1.5rem;
        }

        /* Button Styles */
        .btn-modern {
            padding: 0.75rem 1.5rem;
            border-radius: 8px;
            font-weight: 500;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            transition: all 0.2s ease;
            border: none;
            cursor: pointer;
        }

        .btn-primary-modern {
            background: var(--primary-color);
            color: white;
        }

        .btn-primary-modern:hover {
            background: var(--primary-dark);
            transform: translateY(-1px);
            color: white;
        }

        .btn-outline-modern {
            background: white;
            color: var(--primary-color);
            border: 2px solid var(--primary-color);
        }

        .btn-outline-modern:hover {
            background: var(--primary-color);
            color: white;
        }

        /* Product Card Styles */
        .product-item {
            background: white;
            border: 1px solid var(--gray-200);
            border-radius: var(--border-radius);
            padding: 1.5rem;
            margin-bottom: 1rem;
            transition: all 0.2s ease;
        }

        .product-item:hover {
            border-color: var(--primary-color);
            box-shadow: var(--shadow-md);
        }

        .product-image {
            width: 80px;
            height: 80px;
            object-fit: cover;
            border-radius: 8px;
            border: 1px solid var(--gray-200);
        }

        .product-name {
            font-weight: 600;
            color: var(--gray-900);
            text-decoration: none;
            font-size: 1.1rem;
        }

        .product-name:hover {
            color: var(--primary-color);
        }

        .price-text {
            font-weight: 700;
            color: var(--danger-color);
            font-size: 1.1rem;
        }

        /* Rental Tool Styles */
        .rental-item {
            background: white;
            border: 1px solid var(--gray-200);
            border-radius: var(--border-radius);
            padding: 1.5rem;
            margin-bottom: 1rem;
            transition: all 0.2s ease;
        }

        .rental-item:hover {
            border-color: var(--primary-color);
            box-shadow: var(--shadow-md);
        }

        .rental-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
            gap: 1rem;
            align-items: center;
        }

        .rental-field {
            text-align: center;
        }

        .rental-field .label {
            font-size: 0.875rem;
            color: var(--gray-500);
            margin-bottom: 0.25rem;
            font-weight: 500;
        }

        .rental-field .value {
            font-weight: 600;
            color: var(--gray-900);
        }

        /* Badge Styles */
        .badge-modern {
            padding: 0.5rem 1rem;
            border-radius: 20px;
            font-weight: 600;
            font-size: 0.875rem;
            display: inline-flex;
            align-items: center;
            gap: 0.25rem;
        }

        .badge-onsite {
            background: rgba(16, 185, 129, 0.1);
            color: var(--success-color);
            border: 1px solid rgba(16, 185, 129, 0.2);
        }

        .badge-daily {
            background: rgba(245, 158, 11, 0.1);
            color: var(--warning-color);
            border: 1px solid rgba(245, 158, 11, 0.2);
        }

        .badge-other {
            background: rgba(107, 114, 128, 0.1);
            color: var(--gray-600);
            border: 1px solid rgba(107, 114, 128, 0.2);
        }

        /* Summary Styles */
        .summary-card {
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
            color: white;
            border-radius: var(--border-radius);
            padding: 2rem;
            text-align: center;
            box-shadow: var(--shadow-lg);
        }

        .summary-total {
            font-size: 2.5rem;
            font-weight: 700;
            margin-bottom: 0.5rem;
        }

        .summary-label {
            font-size: 1.1rem;
            opacity: 0.9;
        }

        /* Empty State */
        .empty-state {
            text-align: center;
            padding: 3rem 1rem;
            color: var(--gray-500);
        }

        .empty-state i {
            font-size: 4rem;
            margin-bottom: 1rem;
            opacity: 0.5;
        }

        /* Responsive */
        @media (max-width: 768px) {
            .container-fluid {
                padding: 1rem;
            }

            .page-header {
                padding: 1.5rem;
            }

            .page-header h1 {
                font-size: 1.5rem;
            }

            .rental-grid {
                grid-template-columns: repeat(2, 1fr);
            }

            .summary-total {
                font-size: 2rem;
            }
        }

        @media (max-width: 480px) {
            .rental-grid {
                grid-template-columns: 1fr;
            }
        }

        /* Animation */
        .fade-in {
            animation: fadeIn 0.5s ease-in;
        }

        @keyframes fadeIn {
            from {
                opacity: 0;
                transform: translateY(20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
    </style>
</head>

<body>
<jsp:include page="../layout/header.jsp" />

<div class="container-fluid fade-in">
    <!-- Page Header -->
    <div class="page-header">
        <div class="d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center">
            <div>
                <h1><i class="fas fa-receipt me-2"></i>Chi tiết đơn hàng</h1>
<%--                <div class="order-id">--%>
<%--                    <i class="fas fa-hashtag me-1"></i>Mã đơn hàng: ${id}--%>
<%--                </div>--%>
            </div>
            <a href="/booking-history" class="btn-modern btn-outline-modern mt-3 mt-md-0">
                <i class="fas fa-arrow-left"></i>
                Quay lại
            </a>
        </div>
    </div>

    <!-- Breadcrumb -->
    <nav aria-label="breadcrumb">
        <ol class="breadcrumb">
            <li class="breadcrumb-item">
                <a href="/booking-history">
                    <i class="fas fa-shopping-cart me-1"></i>Đơn hàng
                </a>
            </li>
            <li class="breadcrumb-item active" aria-current="page">Chi tiết đơn hàng</li>
        </ol>
    </nav>

    <!-- Products Section -->
    <div class="modern-card">
        <div class="card-header-modern">
            <h3>
                <i class="fas fa-box text-primary"></i>
                Sản phẩm đã đặt
            </h3>
        </div>
        <div class="card-body-modern">
            <c:choose>
                <c:when test="${empty bookingDetails}">
                    <div class="empty-state">
                        <i class="fas fa-shopping-cart"></i>
                        <h4>Không có sản phẩm</h4>
                        <p>Đơn hàng này chưa có sản phẩm nào được đặt.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <c:set var="totalProductPrice" value="0" />
                    <c:forEach var="bookingDetail" items="${bookingDetails}">
                        <div class="product-item">
                            <div class="row align-items-center">
                                <div class="col-auto">
                                    <img src="/images/product/${bookingDetail.product.image}"
                                         class="product-image"
                                         alt="${bookingDetail.product.name}"
                                         onerror="this.src='/placeholder.svg?height=80&width=80'">
                                </div>
                                <div class="col">
                                    <a href="/mainProduct/${bookingDetail.product.id}"
                                       class="product-name" target="_blank">
                                            ${bookingDetail.product.name}
                                    </a>
                                    <div class="text-muted mt-1">
                                        <small>Giá gốc: <fmt:formatNumber type="number" value="${bookingDetail.product.price}" /> đ</small>
                                    </div>
                                </div>
                                <div class="col-auto text-end">
                                    <div class="price-text">
                                        <fmt:formatNumber type="number" value="${bookingDetail.price}" /> đ
                                    </div>
                                </div>
                            </div>
                        </div>
                        <c:set var="totalProductPrice" value="${totalProductPrice + bookingDetail.price}" />
                    </c:forEach>

                    <div class="border-top pt-3 mt-3">
                        <div class="row">
                            <div class="col">
                                <h5 class="mb-0">Tổng tiền sản phẩm:</h5>
                            </div>
                            <div class="col-auto">
                                <h5 class="price-text mb-0">
                                    <fmt:formatNumber type="number" value="${totalProductPrice}" /> đ
                                </h5>
                            </div>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <!-- Rental Tools Section -->
    <div class="modern-card">
        <div class="card-header-modern">
            <h3>
                <i class="fas fa-tools text-warning"></i>
                Thông tin thuê thiết bị
            </h3>
        </div>
        <div class="card-body-modern">
            <c:choose>
                <c:when test="${empty rentalTool}">
                    <div class="empty-state">
                        <i class="fas fa-tools"></i>
                        <h4>Không có thiết bị thuê</h4>
                        <p>Đơn hàng này không có thiết bị thuê nào.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <c:set var="totalRentalPrice" value="0" />
                    <c:forEach var="rental" items="${rentalTool}">
                        <div class="rental-item">
                            <div class="rental-grid">
                                <div class="rental-field">
                                    <div class="label">Mã thiết bị</div>
                                    <div class="value">#${rental.racketId}</div>
                                </div>
                                <div class="rental-field">
                                    <div class="label">Mã thuê</div>
                                    <div class="value">${rental.rentalToolCode}</div>
                                </div>
                                <div class="rental-field">
                                    <div class="label">Loại thuê</div>
                                    <div class="value">
                                        <c:choose>
                                            <c:when test="${rental.type == 'ON_SITE'}">
                                                    <span class="badge-modern badge-onsite">
                                                        <i class="fas fa-map-marker-alt"></i>
                                                        Thuê tại sân
                                                    </span>
                                            </c:when>
                                            <c:when test="${rental.type == 'DAILY'}">
                                                    <span class="badge-modern badge-daily">
                                                        <i class="fas fa-calendar-day"></i>
                                                        Thuê theo ngày
                                                    </span>
                                            </c:when>
                                            <c:otherwise>
                                                    <span class="badge-modern badge-other">
                                                        <i class="fas fa-question-circle"></i>
                                                        Khác
                                                    </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                                <div class="rental-field">
                                    <div class="label">Số lượng</div>
                                    <div class="value text-primary fw-bold">${rental.quantity}</div>
                                </div>
                                <div class="rental-field">
                                    <div class="label">Ngày thuê</div>
                                    <div class="value">${rental.rentalDate}</div>
                                </div>
                                <div class="rental-field">
                                    <div class="label">Tiền thuê</div>
                                    <div class="value price-text">
                                        <fmt:formatNumber value="${rental.rentalPrice}" type="number" /> đ
                                    </div>
                                </div>
                            </div>
                        </div>
                        <c:set var="totalRentalPrice" value="${totalRentalPrice + rental.rentalPrice}" />
                    </c:forEach>

                    <div class="border-top pt-3 mt-3">
                        <div class="row">
                            <div class="col">
                                <h5 class="mb-0">Tổng tiền thuê thiết bị:</h5>
                            </div>
                            <div class="col-auto">
                                <h5 class="price-text mb-0">
                                    <fmt:formatNumber type="number" value="${totalRentalPrice}" /> đ
                                </h5>
                            </div>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <!-- Total Summary -->
    <div class="summary-card">
        <div class="summary-total">
            <c:set var="grandTotal" value="${totalProductPrice + totalRentalPrice}" />
            <fmt:formatNumber type="number" value="${grandTotal}" /> đ
        </div>
        <div class="summary-label">
            <i class="fas fa-calculator me-2"></i>
            Tổng cộng đơn hàng
        </div>
        <div class="mt-2 opacity-75">
            <small>Bao gồm sản phẩm và thiết bị thuê</small>
        </div>
    </div>
</div>

<jsp:include page="../layout/footer.jsp" />

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
    // Add smooth scrolling and enhanced interactions
    document.addEventListener('DOMContentLoaded', function() {
        // Add loading animation
        const cards = document.querySelectorAll('.modern-card, .summary-card');
        cards.forEach((card, index) => {
            card.style.animationDelay = `${index * 0.1}s`;
        });

        // Add click effects for interactive elements
        const interactiveElements = document.querySelectorAll('.product-item, .rental-item');
        interactiveElements.forEach(element => {
            element.addEventListener('click', function(e) {
                if (!e.target.closest('a')) {
                    this.style.transform = 'scale(0.98)';
                    setTimeout(() => {
                        this.style.transform = '';
                    }, 150);
                }
            });
        });
    });
</script>
</body>

</html>
