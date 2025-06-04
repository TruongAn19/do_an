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
    <title>Update Racket</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/styles.css" rel="stylesheet" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <script src="https://use.fontawesome.com/releases/v6.3.0/js/all.js" crossorigin="anonymous"></script>
    <style>
        :root {
            --primary-color: #2c3e50;
            --secondary-color: #3498db;
            --accent-color: #e74c3c;
            --light-color: #ecf0f1;
            --dark-color: #2c3e50;
        }

        body {
            background-color: #f8f9fa;
        }

        .card {
            border-radius: 10px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
            border: none;
        }

        .form-control, .form-select {
            border-radius: 8px;
            padding: 10px 15px;
            border: 1px solid #ced4da;
        }

        .form-control:focus, .form-select:focus {
            border-color: var(--secondary-color);
            box-shadow: 0 0 0 0.25rem rgba(52, 152, 219, 0.25);
        }

        .btn-update {
            background-color: var(--secondary-color);
            border: none;
            padding: 10px 25px;
            font-weight: 500;
            transition: all 0.3s;
        }

        .btn-update:hover {
            background-color: #2980b9;
            transform: translateY(-2px);
        }

        .preview-container {
            border: 2px dashed #ddd;
            border-radius: 10px;
            padding: 20px;
            text-align: center;
            background-color: #f9f9f9;
            min-height: 250px;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .preview-image {
            max-height: 200px;
            max-width: 100%;
            border-radius: 8px;
            display: none;
        }

        .section-title {
            color: var(--primary-color);
            position: relative;
            padding-bottom: 10px;
            margin-bottom: 20px;
        }

        .section-title:after {
            content: '';
            position: absolute;
            left: 0;
            bottom: 0;
            width: 50px;
            height: 3px;
            background-color: var(--secondary-color);
        }

        .form-label {
            font-weight: 500;
            color: var(--dark-color);
            margin-bottom: 8px;
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
                <div class="d-flex justify-content-between align-items-center mt-4">
                    <h1 class="fw-bold">Update Racket</h1>
                    <nav aria-label="breadcrumb">
                        <ol class="breadcrumb">
                            <li class="breadcrumb-item"><a href="/admin"><i class="fas fa-home"></i> Dashboard</a></li>
                            <li class="breadcrumb-item"><a href="#">Products</a></li>
                            <li class="breadcrumb-item active" aria-current="page">Update</li>
                        </ol>
                    </nav>
                </div>

                <div class="row justify-content-center mt-4">
                    <div class="col-lg-10">
                        <div class="card">
                            <div class="card-body p-5">
                                <form:form method="post" action="/admin/racket/update_racket" modelAttribute="editRacket" enctype="multipart/form-data">
                                    <input type="hidden" name="productType" value="byProduct">

                                    <div class="row">
                                        <!-- Left Column -->
                                        <div class="col-md-6 pe-md-4">
                                            <h4 class="section-title">Racket Information</h4>

                                            <div class="mb-4">
                                                <label class="form-label">ID</label>
                                                <form:input type="text" class="form-control bg-light" path="id" readonly="true"/>
                                            </div>

                                            <div class="mb-4">
                                                <label class="form-label">Racket Name</label>
                                                <form:input type="text" class="form-control" path="name" placeholder="Enter racket name"/>
                                                <small class="text-muted">Enter the full name of the racket</small>
                                            </div>

                                            <div class="mb-4">
                                                <label class="form-label">Manufacturer</label>
                                                <form:input type="text" class="form-control" path="factory" placeholder="Enter manufacturer"/>
                                            </div>

                                            <div class="mb-4">
                                                <label class="form-label">Purchase Price (VND)</label>
                                                <div class="input-group">
                                                    <span class="input-group-text"><i class="fas fa-money-bill-wave"></i></span>
                                                    <form:input type="number" class="form-control" path="price" placeholder="Enter price"/>
                                                </div>
                                            </div>
                                        </div>

                                        <!-- Right Column -->
                                        <div class="col-md-6 ps-md-4">
                                            <h4 class="section-title">Rental Information</h4>

                                            <div class="mb-4">
                                                <label class="form-label">Daily Rental Price (VND)</label>
                                                <div class="input-group">
                                                    <span class="input-group-text"><i class="fas fa-calendar-day"></i></span>
                                                    <form:input type="number" class="form-control" path="rentalPricePerDay" placeholder="Enter daily rental price"/>
                                                </div>
                                            </div>

                                            <div class="mb-4">
                                                <label class="form-label">Per Session Rental Price (VND)</label>
                                                <div class="input-group">
                                                    <span class="input-group-text"><i class="fas fa-clock"></i></span>
                                                    <form:input type="number" class="form-control" path="rentalPricePerPlay" placeholder="Enter per session price"/>
                                                </div>
                                            </div>

                                            <div class="mb-4">
                                                <label class="form-label">Product Category</label>
                                                <form:select path="product.id" class="form-select">
                                                    <form:options items="${productList}" itemValue="id" itemLabel="name" />
                                                </form:select>
                                            </div>

                                            <div class="mb-4">
                                                <label for="status" class="form-label">Status</label>
                                                <form:select path="status" class="form-select" id="status">
                                                    <form:option value="AVAILABLE" label="Available" />
                                                    <form:option value="DELETED" label="Deleted" />
                                                </form:select>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="row mt-3">
                                        <div class="col-md-6">
                                            <div class="mb-4">
                                                <label for="racketImg" class="form-label">Racket Image</label>
                                                <input class="form-control" type="file" id="racketImg" accept=".png, .jpg, .jpeg" name="racketImg" onchange="previewImage(this)"/>
                                                <small class="text-muted">Upload a high-quality image of the racket</small>
                                            </div>
                                        </div>
                                        <div class="col-md-6">
                                            <div class="preview-container">
                                                <img id="avatarPreview" class="preview-image" alt="Racket preview"/>
                                                <div id="noPreview" class="text-muted">No image selected</div>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="d-flex justify-content-between mt-5">
                                        <a href="/admin/racket" class="btn btn-outline-secondary">
                                            <i class="fas fa-arrow-left me-2"></i>Back to List
                                        </a>
                                        <button type="submit" class="btn btn-update text-white">
                                            <i class="fas fa-save me-2"></i>Update Racket
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

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="/js/scripts.js"></script>

<script>
    function previewImage(input) {
        const preview = document.getElementById('avatarPreview');
        const noPreview = document.getElementById('noPreview');

        if (input.files && input.files[0]) {
            const reader = new FileReader();

            reader.onload = function(e) {
                preview.src = e.target.result;
                preview.style.display = 'block';
                noPreview.style.display = 'none';
            }

            reader.readAsDataURL(input.files[0]);
        } else {
            preview.style.display = 'none';
            noPreview.style.display = 'block';
        }
    }

    // Initialize preview if there's an existing image
    document.addEventListener('DOMContentLoaded', function() {
        const preview = document.getElementById('avatarPreview');
        if (preview.src) {
            preview.style.display = 'block';
            document.getElementById('noPreview').style.display = 'none';
        }
    });
</script>
</body>
</html>