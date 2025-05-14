<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no" />
    <meta name="description" content="Dự án sancaulong" />
    <meta name="author" content="TruongAn" />
    <title>Update User</title>
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

        .btn-warning {
            background-color: var(--warning-color);
            border-color: var(--warning-color);
            color: white;
        }

        .btn-warning:hover {
            background-color: #d97706;
            border-color: #d97706;
            color: white;
        }

        .btn-secondary {
            background-color: #6b7280;
            border-color: #6b7280;
        }

        .btn-secondary:hover {
            background-color: #4b5563;
            border-color: #4b5563;
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

        .form-label {
            font-weight: 500;
            color: #374151;
            margin-bottom: 0.5rem;
        }

        .form-control:focus {
            border-color: var(--primary-color);
            box-shadow: 0 0 0 0.25rem rgba(79, 70, 229, 0.25);
        }

        .form-control:disabled {
            background-color: #f3f4f6;
            cursor: not-allowed;
        }

        .avatar-preview-container {
            margin-top: 1rem;
            margin-bottom: 1.5rem;
            text-align: center;
        }

        .avatar-preview {
            max-height: 250px;
            max-width: 100%;
            border-radius: 0.5rem;
            box-shadow: var(--card-shadow);
            border: 2px solid #e5e7eb;
            padding: 0.25rem;
            background-color: white;
        }

        .form-buttons {
            display: flex;
            gap: 1rem;
            margin-top: 1.5rem;
        }

        .form-section {
            margin-bottom: 2rem;
        }

        .form-section-title {
            font-size: 1.25rem;
            font-weight: 600;
            color: #111827;
            margin-bottom: 1rem;
            padding-bottom: 0.5rem;
            border-bottom: 1px solid #e5e7eb;
        }

        .required-field::after {
            content: "*";
            color: var(--danger-color);
            margin-left: 0.25rem;
        }

        .file-upload-container {
            position: relative;
        }

        .file-upload-preview {
            margin-top: 1rem;
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
                <h1 class="page-title mt-3">Manage Users</h1>
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><a href="/admin">Dashboard</a></li>
                        <li class="breadcrumb-item"><a href="/admin/user">Users</a></li>
                        <li class="breadcrumb-item active">Update User</li>
                    </ol>
                </nav>

                <div class="card mb-4">
                    <div class="card-header">
                        <h5 class="mb-0">
                            <i class="fas fa-user-edit me-2"></i>Update User
                        </h5>
                    </div>
                    <div class="card-body">
                        <form:form method="post" action="/admin/user/update_user" modelAttribute="newUser" enctype="multipart/form-data" cssClass="needs-validation" novalidate="true">

                            <div style="display: none;">
                                <form:input type="text" class="form-control" path="id" />
                            </div>

                            <div class="row">
                                <div class="col-md-8">
                                    <div class="form-section">
                                        <h6 class="form-section-title">User Information</h6>

                                        <div class="mb-3">
                                            <label class="form-label required-field">Email:</label>
                                            <form:input type="email" class="form-control" path="email" disabled="true" />
                                            <div class="form-text text-muted">Email cannot be changed</div>
                                        </div>

                                        <div class="row">
                                            <div class="col-md-6 mb-3">
                                                <label class="form-label required-field">Full Name:</label>
                                                <form:input type="text" class="form-control" path="fullName" required="required" />
                                                <div class="invalid-feedback">Please enter a full name</div>
                                            </div>

                                            <div class="col-md-6 mb-3">
                                                <label class="form-label">Phone number:</label>
                                                <form:input type="text" class="form-control" path="phone" placeholder="Enter phone number" />
                                            </div>
                                        </div>

                                        <div class="mb-3">
                                            <label class="form-label">Address:</label>
                                            <form:input type="text" class="form-control" path="address" placeholder="Enter address" />
                                        </div>
                                    </div>
                                </div>

                                <div class="col-md-4">
                                    <div class="form-section">
                                        <h6 class="form-section-title">Profile Image</h6>

                                        <div class="file-upload-container">
                                            <label for="avatarFile" class="form-label">Upload Image:</label>
                                            <input class="form-control" type="file" id="avatarFile" accept=".png, .jpg, .jpeg" name="avatarFile" />
                                            <div class="form-text">Supported formats: JPG, JPEG, PNG</div>
                                        </div>

                                        <div class="file-upload-preview">
                                            <div class="avatar-preview-container">
                                                <img class="avatar-preview" style="display: none;" alt="avatar preview" id="avatarPreview" />
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="form-buttons">
                                <button type="submit" class="btn btn-warning">
                                    <i class="fas fa-save me-2"></i>Update User
                                </button>
                                <a href="/admin/user" class="btn btn-secondary">
                                    <i class="fas fa-times me-2"></i>Cancel
                                </a>
                            </div>
                        </form:form>
                    </div>
                </div>
            </div>
        </main>
        <jsp:include page="../layout/footer.jsp" />
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js" crossorigin="anonymous"></script>
<script src="/js/scripts.js"></script>
<script>
    // Preview image before upload
    document.getElementById('avatarFile').addEventListener('change', function(event) {
        const file = event.target.files[0];
        const preview = document.getElementById('avatarPreview');

        if (file) {
            const reader = new FileReader();

            reader.onload = function(e) {
                preview.src = e.target.result;
                preview.style.display = 'block';
            }

            reader.readAsDataURL(file);
        } else {
            preview.style.display = 'none';
        }
    });

    // Form validation
    (function () {
        'use strict'

        // Fetch all the forms we want to apply custom Bootstrap validation styles to
        var forms = document.querySelectorAll('.needs-validation')

        // Loop over them and prevent submission
        Array.prototype.slice.call(forms)
            .forEach(function (form) {
                form.addEventListener('submit', function (event) {
                    if (!form.checkValidity()) {
                        event.preventDefault()
                        event.stopPropagation()
                    }

                    form.classList.add('was-validated')
                }, false)
            })
    })()
</script>
</body>

</html>