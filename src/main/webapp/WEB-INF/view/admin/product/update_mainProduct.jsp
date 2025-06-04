<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no" />
    <meta name="description" content="Dự án Sân cầu lông" />
    <meta name="author" content="TruongAn" />
    <title>Update Product - Admin Dashboard</title>
    <!-- Favicon -->
    <link rel="shortcut icon" href="/assets/img/favicon.ico" type="image/x-icon">
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Custom CSS -->
    <link href="/css/styles.css" rel="stylesheet" />
    <style>
        :root {
            --primary-color: #4361ee;
            --secondary-color: #3f37c9;
            --accent-color: #4895ef;
            --dark-color: #1a1a2e;
            --light-color: #f8f9fa;
            --success-color: #4cc9f0;
            --warning-color: #f8961e;
            --danger-color: #f72585;
        }

        body {
            font-family: 'Poppins', sans-serif;
            background-color: #f5f7fa;
        }

        .card {
            border: none;
            border-radius: 12px;
            box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
            transition: transform 0.3s ease, box-shadow 0.3s ease;
            overflow: hidden;
        }

        .card:hover {
            transform: translateY(-5px);
            box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
        }

        .card-header {
            background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
            color: white;
            font-weight: 600;
            padding: 1.25rem 1.5rem;
            border-bottom: none;
        }

        .form-label {
            font-weight: 500;
            color: var(--dark-color);
            margin-bottom: 0.5rem;
        }

        .form-control, .form-select {
            border: 1px solid #e0e0e0;
            border-radius: 8px;
            padding: 0.75rem 1rem;
            transition: all 0.3s;
        }

        .form-control:focus, .form-select:focus {
            border-color: var(--accent-color);
            box-shadow: 0 0 0 0.25rem rgba(67, 97, 238, 0.15);
        }

        .btn-update {
            background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
            border: none;
            border-radius: 8px;
            padding: 0.75rem 2rem;
            font-weight: 500;
            letter-spacing: 0.5px;
            transition: all 0.3s;
        }

        .btn-update:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(67, 97, 238, 0.25);
        }

        #avatarPreview {
            width: 100%;
            height: 200px;
            object-fit: cover;
            border-radius: 8px;
            border: 2px dashed #e0e0e0;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: #f8f9fa;
            overflow: hidden;
        }

        #avatarPreview img {
            max-width: 100%;
            max-height: 100%;
        }

        .preview-placeholder {
            color: #adb5bd;
            font-size: 0.9rem;
        }

        .upload-area {
            border: 2px dashed #e0e0e0;
            border-radius: 8px;
            padding: 1.5rem;
            text-align: center;
            cursor: pointer;
            transition: all 0.3s;
            background-color: #f8f9fa;
        }

        .upload-area:hover {
            border-color: var(--accent-color);
            background-color: rgba(72, 149, 239, 0.05);
        }

        .upload-icon {
            font-size: 2rem;
            color: var(--accent-color);
            margin-bottom: 0.5rem;
        }

        .breadcrumb {
            background-color: transparent;
            padding: 0.5rem 0;
        }

        .breadcrumb-item a {
            color: var(--primary-color);
            text-decoration: none;
            transition: color 0.3s;
        }

        .breadcrumb-item a:hover {
            color: var(--secondary-color);
            text-decoration: underline;
        }

        .page-title {
            color: var(--dark-color);
            font-weight: 600;
            margin-bottom: 1.5rem;
            position: relative;
            padding-bottom: 0.75rem;
        }

        .page-title::after {
            content: '';
            position: absolute;
            left: 0;
            bottom: 0;
            width: 60px;
            height: 4px;
            background: linear-gradient(90deg, var(--primary-color), var(--accent-color));
            border-radius: 2px;
        }

        /* Animation */
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(20px); }
            to { opacity: 1; transform: translateY(0); }
        }

        .animate-fade {
            animation: fadeIn 0.6s ease forwards;
        }

        /* Responsive adjustments */
        @media (max-width: 768px) {
            .card-body {
                padding: 1.5rem;
            }
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
                <h1 class="mt-4 page-title">Quản lý sản phẩm</h1>
                <ol class="breadcrumb mb-4">
                    <li class="breadcrumb-item"><a href="/admin"><i class="fas fa-home me-1"></i> Dashboard</a></li>
                    <li class="breadcrumb-item"><a href="/admin/product">Sản phẩm</a></li>
                    <li class="breadcrumb-item active">Cập nhật sản phẩm</li>
                </ol>

                <div class="row justify-content-center animate-fade">
                    <div class="col-lg-10 col-xl-8">
                        <div class="card mt-4 mb-5">
                            <div class="card-header d-flex justify-content-between align-items-center">
                                <span><i class="fas fa-edit me-2"></i>Cập nhật sản phẩm</span>
                                <span class="badge bg-primary">ID: ${editProduct.id}</span>
                            </div>
                            <div class="card-body p-4">
                                <form:form method="post" action="/admin/product/update_product"
                                           modelAttribute="editProduct" enctype="multipart/form-data">
                                    <input type="hidden" name="productType" value="mainProduct">
                                    <form:input type="hidden" path="id" />

                                    <div class="row g-4">
                                        <!-- Left Column -->
                                        <div class="col-md-6">
                                            <div class="mb-4">
                                                <label for="name" class="form-label">Tên sản phẩm</label>
                                                <form:input path="name" class="form-control" id="name" placeholder="Nhập tên sản phẩm" />
                                                <small class="text-muted">Tên sản phẩm phải rõ ràng và dễ hiểu</small>
                                            </div>

                                            <div class="mb-4">
                                                <label for="detailDesc" class="form-label">Mô tả chi tiết</label>
                                                <form:textarea path="detailDesc" class="form-control" id="detailDesc" rows="5" placeholder="Mô tả đầy đủ về sản phẩm"></form:textarea>
                                            </div>

                                            <div class="mb-4">
                                                <label for="address" class="form-label">Địa chỉ</label>
                                                <form:input path="address" class="form-control" id="address" placeholder="Nhập địa chỉ" />
                                            </div>
                                        </div>

                                        <!-- Right Column -->
                                        <div class="col-md-6">
                                            <div class="mb-4">
                                                <label for="status" class="form-label">Trạng thái</label>
                                                <form:select path="status" class="form-select" id="status">
                                                    <form:option value="AVAILABLE" label="Còn hoạt động" />
                                                    <form:option value="MAINTAINING" label="Đang bảo trì" />
                                                    <form:option value="DELETED" label="Đã xoá" />
                                                </form:select>
                                            </div>

                                            <div class="row g-3">
                                                <div class="col-md-6">
                                                    <div class="mb-4">
                                                        <label for="price" class="form-label">Giá (VND)</label>
                                                        <div class="input-group">
                                                            <span class="input-group-text"><i class="fas fa-money-bill-wave"></i></span>
                                                            <form:input path="price" class="form-control" id="price" placeholder="0" />
                                                        </div>
                                                    </div>
                                                </div>
                                                <div class="col-md-6">
                                                    <div class="mb-4">
                                                        <label for="sale" class="form-label">Giảm giá (%)</label>
                                                        <div class="input-group">
                                                            <span class="input-group-text"><i class="fas fa-percent"></i></span>
                                                            <form:input path="sale" class="form-control" id="sale" placeholder="0" />
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>

                                            <div class="mb-4">
                                                <label class="form-label">Hình ảnh sản phẩm</label>
                                                <div class="upload-area" onclick="document.getElementById('productImg').click()">
                                                    <div class="upload-icon">
                                                        <i class="fas fa-cloud-upload-alt"></i>
                                                    </div>
                                                    <h6 class="mb-2">Kéo thả ảnh vào đây hoặc click để chọn</h6>
                                                    <p class="text-muted small mb-0">Định dạng hỗ trợ: JPG, PNG, JPEG</p>
                                                    <input type="file" class="d-none" name="productImg" id="productImg" accept=".png, .jpg, .jpeg" />
                                                </div>
                                            </div>

                                            <div class="mb-4 text-center">
                                                <div id="avatarPreview" class="mb-3">
                                                    <c:if test="${not empty editProduct.image}">
                                                        <img src="/uploads/${editProduct.image}" alt="Current Product Image" class="img-fluid" />
                                                    </c:if>
                                                    <c:if test="${empty editProduct.image}">
                                                        <div class="preview-placeholder">Ảnh xem trước sẽ hiển thị tại đây</div>
                                                    </c:if>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="d-grid gap-2 d-md-flex justify-content-md-end mt-4">
                                        <a href="/admin/mainProduct" class="btn btn-outline-secondary me-md-2">
                                            <i class="fas fa-arrow-left me-1"></i> Quay lại
                                        </a>
                                        <button type="submit" class="btn btn-update text-white">
                                            <i class="fas fa-save me-1"></i> Cập nhật sản phẩm
                                        </button>
                                    </div>
                                </form:form>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </main>
        <jsp:include page="../layout/footer.jsp" />
    </div>
</div>

<!-- Bootstrap Bundle with Popper -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js" crossorigin="anonymous"></script>
<!-- Custom Scripts -->
<script src="/js/scripts.js"></script>

<script>
    // Hiển thị ảnh xem trước
    document.getElementById('productImg').addEventListener('change', function (e) {
        const [file] = e.target.files;
        if (file) {
            const preview = document.getElementById('avatarPreview');
            preview.innerHTML = ''; // Clear previous content

            const img = document.createElement('img');
            img.src = URL.createObjectURL(file);
            img.className = 'img-fluid';
            img.alt = 'Preview';

            preview.appendChild(img);

            // Add animation
            preview.style.animation = 'fadeIn 0.5s ease';
        }
    });

    // Format price input
    document.getElementById('price')?.addEventListener('input', function(e) {
        this.value = this.value.replace(/\D/g, '').replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    });
</script>
</body>
</html>