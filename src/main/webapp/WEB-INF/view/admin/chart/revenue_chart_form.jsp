<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@page import="java.time.LocalDate" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Chọn khoảng thời gian</title>
    <link href="/css/styles.css" rel="stylesheet" />
    <script src="https://use.fontawesome.com/releases/v6.3.0/js/all.js"></script>
</head>
<body class="sb-nav-fixed">
<jsp:include page="../layout/header.jsp" />
<div id="layoutSidenav">
    <jsp:include page="../layout/sidebar.jsp" />
    <div id="layoutSidenav_content">
        <main class="container-fluid px-4">
            <h1 class="mt-4">Thống kê doanh thu</h1>
            <ol class="breadcrumb mb-4">
                <li class="breadcrumb-item active">Chọn khoảng thời gian</li>
            </ol>

            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>

            <form method="post" action="/admin/statistics/revenue">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <div class="row mb-3">
                    <div class="col-md-4">
                        <label>Từ ngày:</label>
                        <!-- Truyền giá trị startDate vào trường input -->
                        <input type="date" name="startDate" class="form-control" value="${startDate != null ? startDate.toLocalDate().toString() : ''}" required />
                    </div>
                    <div class="col-md-4">
                        <label>Đến ngày:</label>
                        <!-- Truyền giá trị endDate vào trường input -->
                        <input type="date" name="endDate" class="form-control" value="${endDate != null ? endDate.toLocalDate().toString() : ''}" required />
                    </div>
                    <div class="col-md-4 align-self-end">
                        <button type="submit" class="btn btn-primary mt-2">Thống kê</button>
                    </div>
                </div>
            </form>

        </main>
        <jsp:include page="../layout/footer.jsp" />
    </div>
</div>
</body>
</html>
