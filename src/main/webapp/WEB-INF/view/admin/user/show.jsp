<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no" />
    <meta name="description" content="Dự án sancaulong" />
    <meta name="author" content="TruongAn" />
    <title>Dashboard</title>
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
            background-color: #e5e7eb;
            color: #374151;
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

        .user-avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            background-color: #e5e7eb;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #6b7280;
            font-weight: 600;
            margin-right: 0.75rem;
        }

        .user-info {
            display: flex;
            align-items: center;
        }

        .user-details {
            display: flex;
            flex-direction: column;
        }

        .user-name {
            font-weight: 600;
            color: #111827;
            margin-bottom: 0.25rem;
        }

        .user-email {
            font-size: 0.875rem;
            color: #6b7280;
        }

        .user-id {
            font-family: monospace;
            font-size: 0.875rem;
            color: #6b7280;
            background-color: #f3f4f6;
            padding: 0.25rem 0.5rem;
            border-radius: 0.25rem;
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
                        <li class="breadcrumb-item active">Users</li>
                    </ol>
                </nav>

                <div class="card mb-4">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h5 class="mb-0">Table Users</h5>
                        <a href="/admin/user/create" class="btn btn-primary">
                            <i class="fas fa-user-plus me-2"></i>Create a User
                        </a>
                    </div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover align-middle mb-0">
                                <thead class="bg-light">
                                <tr>
                                    <th>ID</th>
                                    <th>User</th>
                                    <th>Role</th>
                                    <th>Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="user" items="${users}">
                                    <tr>
                                        <td>
                                            <span class="user-id">${user.id}</span>
                                        </td>
                                        <td>
                                            <div class="user-info">
                                                <div class="user-avatar">
                                                        ${user.fullName.charAt(0)}
                                                </div>
                                                <div class="user-details">
                                                    <span class="user-name">${user.fullName}</span>
                                                    <span class="user-email">${user.email}</span>
                                                </div>
                                            </div>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${user.role.name eq 'ADMIN' || user.role.name eq 'Admin'}">
                                                    <span class="role-badge admin">${user.role.name}</span>
                                                </c:when>
                                                <c:when test="${user.role.name eq 'USER' || user.role.name eq 'User'}">
                                                    <span class="role-badge user">${user.role.name}</span>
                                                </c:when>
                                                <c:when test="${user.role.name eq 'STAFF' || user.role.name eq 'Staff'}">
                                                    <span class="role-badge staff">${user.role.name}</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="role-badge">${user.role.name}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div class="action-buttons">
                                                <a href="/admin/user/${user.id}" class="btn btn-success btn-sm">
                                                    <i class="fas fa-eye me-1"></i>View
                                                </a>
                                                <a href="/admin/user/update_user/${user.id}" class="btn btn-warning btn-sm">
                                                    <i class="fas fa-edit me-1"></i>Update
                                                </a>
                                                <a href="/admin/user/delete_user/${user.id}" class="btn btn-danger btn-sm">
                                                    <i class="fas fa-trash me-1"></i>Delete
                                                </a>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
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