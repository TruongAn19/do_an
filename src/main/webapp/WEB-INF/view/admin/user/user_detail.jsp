<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="description" content="Dự án sancaulong" />
    <meta name="author" content="TruongAn" />
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no" />
    <title>User Details</title>
    <link href="/css/styles.css" rel="stylesheet" />
    <script src="https://use.fontawesome.com/releases/v6.3.0/js/all.js" crossorigin="anonymous"></script>
    <style>
        .user-card {
            border-radius: 15px;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
            overflow: hidden;
            transition: transform 0.3s ease, box-shadow 0.3s ease;
            margin-bottom: 2rem;
        }

        .user-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
        }

        .user-header {
            background: linear-gradient(135deg, #4e73df 0%, #224abe 100%);
            color: white;
            padding: 1.5rem;
            font-weight: 600;
            font-size: 1.25rem;
        }

        .user-avatar {
            width: 150px;
            height: 150px;
            object-fit: cover;
            border-radius: 50%;
            border: 5px solid white;
            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
            transition: transform 0.3s ease;
        }

        .user-avatar:hover {
            transform: scale(1.05);
        }

        .user-info {
            padding: 1.5rem;
        }

        .info-label {
            font-weight: 600;
            color: #4e73df;
            margin-bottom: 0.25rem;
        }

        .info-value {
            margin-bottom: 1rem;
            padding-bottom: 0.5rem;
            border-bottom: 1px solid #e3e6f0;
        }

        .back-button {
            transition: all 0.3s ease;
            border-radius: 30px;
            padding: 0.5rem 1.5rem;
        }

        .back-button:hover {
            transform: translateX(-5px);
        }

        .breadcrumb-item a {
            color: #4e73df;
            text-decoration: none;
            transition: color 0.3s ease;
        }

        .breadcrumb-item a:hover {
            color: #224abe;
            text-decoration: underline;
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
                    <h1 class="fw-bold">User Details</h1>
                    <a href="/admin/user" class="btn btn-primary back-button">
                        <i class="fas fa-arrow-left me-2"></i>Back to Users
                    </a>
                </div>

                <ol class="breadcrumb mb-4">
                    <li class="breadcrumb-item"><a href="/admin">Dashboard</a></li>
                    <li class="breadcrumb-item"><a href="/admin/user">Users</a></li>
                    <li class="breadcrumb-item active">User #${id}</li>
                </ol>

                <div class="row">
                    <div class="col-lg-8 col-md-10 mx-auto">
                        <div class="user-card">
                            <div class="user-header">
                                User Profile
                            </div>
                            <div class="row g-0">
                                <div class="col-md-4 d-flex justify-content-center align-items-center p-4">
                                    <img src="/images/avatar/${user.avatar}" alt="${user.fullName}'s Avatar" class="user-avatar">
                                </div>
                                <div class="col-md-8">
                                    <div class="user-info">
                                        <div class="mb-4">
                                            <div class="info-label">User ID</div>
                                            <div class="info-value">${user.id}</div>
                                        </div>

                                        <div class="mb-4">
                                            <div class="info-label">Email Address</div>
                                            <div class="info-value">
                                                <i class="fas fa-envelope me-2 text-muted"></i>${user.email}
                                            </div>
                                        </div>

                                        <div class="mb-4">
                                            <div class="info-label">Full Name</div>
                                            <div class="info-value">
                                                <i class="fas fa-user me-2 text-muted"></i>${user.fullName}
                                            </div>
                                        </div>

                                        <div class="mb-2">
                                            <div class="info-label">Address</div>
                                            <div class="info-value">
                                                <i class="fas fa-map-marker-alt me-2 text-muted"></i>${user.address}
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="d-flex justify-content-between mt-4">
                            <a href="/admin/user/update_user/${user.id}" class="btn btn-warning">
                                <i class="fas fa-edit me-2"></i>Edit User
                            </a>
<%--                            <button type="button" class="btn btn-danger" data-bs-toggle="modal" data-bs-target="#deleteModal">--%>
<%--                                <i class="fas fa-trash-alt me-2"></i>Delete User--%>
<%--                            </button>--%>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Delete Confirmation Modal -->
            <div class="modal fade" id="deleteModal" tabindex="-1" aria-labelledby="deleteModalLabel" aria-hidden="true">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header bg-danger text-white">
                            <h5 class="modal-title" id="deleteModalLabel">Confirm Delete</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body">
                            Are you sure you want to delete user <strong>${user.fullName}</strong>? This action cannot be undone.
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                            <a href="/admin/user/delete/${user.id}" class="btn btn-danger">Delete User</a>
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