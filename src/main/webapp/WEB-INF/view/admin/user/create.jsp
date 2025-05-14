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
    <title>Create User - Sân cầu lông</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css" rel="stylesheet" crossorigin="anonymous" />
    <link href="/css/styles.css" rel="stylesheet" />
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>
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

        .btn-primary {
            background-color: var(--primary-color);
            border-color: var(--primary-color);
        }

        .btn-primary:hover {
            background-color: var(--primary-hover);
            border-color: var(--primary-hover);
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

        .form-control:focus, .form-select:focus {
            border-color: var(--primary-color);
            box-shadow: 0 0 0 0.25rem rgba(79, 70, 229, 0.25);
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

        .role-badge {
            display: inline-block;
            padding: 0.35em 0.65em;
            font-size: 0.75em;
            font-weight: 500;
            line-height: 1;
            text-align: center;
            white-space: nowrap;
            vertical-align: baseline;
            border-radius: 0.25rem;
        }

        .role-badge.admin {
            background-color: #dbeafe;
            color: #2563eb;
        }

        .role-badge.user {
            background-color: #d1fae5;
            color: #059669;
        }

        .role-badge.staff {
            background-color: #fef3c7;
            color: #d97706;
        }

        .password-container {
            position: relative;
        }

        .password-toggle {
            position: absolute;
            right: 10px;
            top: 50%;
            transform: translateY(-50%);
            background: none;
            border: none;
            cursor: pointer;
            color: #6b7280;
        }

        .password-toggle:hover {
            color: #374151;
        }

        .form-floating {
            position: relative;
        }

        .form-floating > .form-control,
        .form-floating > .form-select {
            height: calc(3.5rem + 2px);
            padding: 1rem 0.75rem;
        }

        .form-floating > label {
            position: absolute;
            top: 0;
            left: 0;
            height: 100%;
            padding: 1rem 0.75rem;
            pointer-events: none;
            border: 1px solid transparent;
            transform-origin: 0 0;
            transition: opacity .1s ease-in-out,transform .1s ease-in-out;
        }

        .form-floating > .form-control:focus ~ label,
        .form-floating > .form-control:not(:placeholder-shown) ~ label,
        .form-floating > .form-select ~ label {
            opacity: .65;
            transform: scale(.85) translateY(-.5rem) translateX(.15rem);
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
                        <li class="breadcrumb-item active">Create User</li>
                    </ol>
                </nav>

                <div class="card mb-4">
                    <div class="card-header">
                        <h5 class="mb-0">
                            <i class="fas fa-user-plus me-2"></i>Create New User
                        </h5>
                    </div>
                    <div class="card-body">
                        <form:form method="post" enctype="multipart/form-data" action="/admin/user/create" modelAttribute="newUser" class="row g-3">
                            <div class="col-12">
                                <div class="form-section">
                                    <h6 class="form-section-title">Account Information</h6>

                                    <div class="row g-3">
                                        <div class="col-md-6">
                                            <c:set var="errorsEmail">
                                                <form:errors path="email" />
                                            </c:set>
                                            <div class="form-floating">
                                                <form:input type="email" class="form-control ${not empty errorsEmail? 'is-invalid':''}" id="email" placeholder="Email" path="email" />
                                                <label for="email" class="required-field">Email</label>
                                                <form:errors path="email" cssClass="invalid-feedback" />
                                            </div>
                                        </div>

                                        <div class="col-md-6">
                                            <c:set var="errorsPassword">
                                                <form:errors path="password" />
                                            </c:set>
                                            <div class="form-floating password-container">
                                                <form:input type="password" class="form-control ${not empty errorsPassword? 'is-invalid':''}" id="password" placeholder="Password" path="password" />
                                                <label for="password" class="required-field">Password</label>
                                                <button type="button" class="password-toggle" id="togglePassword">
                                                    <i class="fas fa-eye"></i>
                                                </button>
                                                <form:errors path="password" cssClass="invalid-feedback" />
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="col-12">
                                <div class="form-section">
                                    <h6 class="form-section-title">Personal Information</h6>

                                    <div class="row g-3">
                                        <div class="col-md-6">
                                            <c:set var="errorsFullName">
                                                <form:errors path="fullName" />
                                            </c:set>
                                            <div class="form-floating">
                                                <form:input type="text" class="form-control ${not empty errorsFullName? 'is-invalid':''}" id="fullName" placeholder="Full Name" path="fullName" />
                                                <label for="fullName" class="required-field">Full Name</label>
                                                <form:errors path="fullName" cssClass="invalid-feedback" />
                                            </div>
                                        </div>

                                        <div class="col-md-6">
                                            <c:set var="errorsPhone">
                                                <form:errors path="phone" />
                                            </c:set>
                                            <div class="form-floating">
                                                <form:input type="text" class="form-control ${not empty errorsPhone? 'is-invalid':''}" id="phone" placeholder="Phone Number" path="phone" />
                                                <label for="phone">Phone Number</label>
                                                <form:errors path="phone" cssClass="invalid-feedback" />
                                            </div>
                                        </div>

                                        <div class="col-12">
                                            <c:set var="errorsAddress">
                                                <form:errors path="address" />
                                            </c:set>
                                            <div class="form-floating">
                                                <form:input type="text" class="form-control ${not empty errorsAddress? 'is-invalid':''}" id="address" placeholder="Address" path="address" />
                                                <label for="address">Address</label>
                                                <form:errors path="address" cssClass="invalid-feedback" />
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="col-12">
                                <div class="form-section">
                                    <h6 class="form-section-title">Role & Avatar</h6>

                                    <div class="row g-3">
                                        <div class="col-md-6">
                                            <div class="form-floating">
                                                <form:select class="form-select" id="role" path="role.name">
                                                    <form:option value="ADMIN">ADMIN</form:option>
                                                    <form:option value="USER">USER</form:option>
                                                    <form:option value="STAFF">STAFF</form:option>
                                                </form:select>
                                                <label for="role" class="required-field">Role</label>
                                            </div>
                                            <div class="mt-2">
                                                <span class="role-badge admin">ADMIN</span>
                                                <span class="role-badge user">USER</span>
                                                <span class="role-badge staff">STAFF</span>
                                            </div>
                                        </div>

                                        <div class="col-md-6">
                                            <label for="avatarFile" class="form-label">Avatar:</label>
                                            <input class="form-control" type="file" id="avatarFile" accept=".png, .jpg, .jpeg" name="avatarFile" />
                                            <div class="form-text">Supported formats: JPG, JPEG, PNG</div>
                                        </div>

                                        <div class="col-12">
                                            <div class="avatar-preview-container">
                                                <img class="avatar-preview" style="max-height: 250px; display: none;" alt="avatar preview" id="avatarPreview" />
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="col-12">
                                <div class="form-buttons">
                                    <button type="submit" class="btn btn-primary">
                                        <i class="fas fa-save me-2"></i>Create User
                                    </button>
                                    <a href="/admin/user" class="btn btn-secondary">
                                        <i class="fas fa-times me-2"></i>Cancel
                                    </a>
                                </div>
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
    $(document).ready(() => {
        const avatarFile = $("#avatarFile");
        avatarFile.change(function (e) {
            const imgURL = URL.createObjectURL(e.target.files[0]);
            $("#avatarPreview").attr("src", imgURL);
            $("#avatarPreview").css({ "display": "block" });
        });

        // Toggle password visibility
        $("#togglePassword").click(function() {
            const passwordInput = $("#password");
            const icon = $(this).find("i");

            if (passwordInput.attr("type") === "password") {
                passwordInput.attr("type", "text");
                icon.removeClass("fa-eye").addClass("fa-eye-slash");
            } else {
                passwordInput.attr("type", "password");
                icon.removeClass("fa-eye-slash").addClass("fa-eye");
            }
        });
    });
</script>
</body>

</html>