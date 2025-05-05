<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@page import="java.time.LocalDate" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Thống kê doanh thu</title>
    <link href="/css/styles.css" rel="stylesheet" />
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
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
                        <input type="date" name="startDate" class="form-control" value="<fmt:formatDate value='${startDate}' pattern='yyyy-MM-dd'/>" required />
                    </div>
                    <div class="col-md-4">
                        <label>Đến ngày:</label>
                        <input type="date" name="endDate" class="form-control" value="<fmt:formatDate value='${endDate}' pattern='yyyy-MM-dd'/>" required />
                    </div>
                    <div class="col-md-4 align-self-end">
                        <button type="submit" class="btn btn-primary mt-2">Thống kê</button>
                    </div>
                </div>
            </form>

            <c:if test="${not empty revenueData}">
                <h2 class="mt-4">
                    Doanh thu từ
                    <fmt:formatDate value="${startDate}" pattern="dd/MM/yyyy"/>
                    đến
                    <fmt:formatDate value="${endDate}" pattern="dd/MM/yyyy"/>
                </h2>

                <table class="table table-bordered mt-3">
                    <thead>
                    <tr>
                        <th>Ngày</th>
                        <th>Doanh thu (VNĐ)</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${revenueData}" var="entry">
                        <tr>
                            <td>${entry.key}</td>
                            <td><fmt:formatNumber value="${entry.value}" type="currency" currencySymbol="₫"/></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>

                <!-- Biểu đồ doanh thu -->
                <canvas id="revenueChart" width="400" height="150" class="mt-4"></canvas>

                <script>
                    const labels = [
                        <c:forEach items="${revenueData}" var="entry" varStatus="loop">
                        "${entry.key}"<c:if test="${!loop.last}">,</c:if>
                        </c:forEach>
                    ];

                    const data = [
                        <c:forEach items="${revenueData}" var="entry" varStatus="loop">
                        ${entry.value}<c:if test="${!loop.last}">,</c:if>
                        </c:forEach>
                    ];

                    const ctx = document.getElementById('revenueChart').getContext('2d');
                    const revenueChart = new Chart(ctx, {
                        type: 'bar',
                        data: {
                            labels: labels,
                            datasets: [{
                                label: 'Doanh thu (VNĐ)',
                                data: data,
                                backgroundColor: 'rgba(54, 162, 235, 0.6)',
                                borderColor: 'rgba(54, 162, 235, 1)',
                                borderWidth: 1
                            }]
                        },
                        options: {
                            responsive: true,
                            scales: {
                                y: {
                                    beginAtZero: true,
                                    ticks: {
                                        callback: function(value) {
                                            return value.toLocaleString('vi-VN') + ' ₫';
                                        }
                                    }
                                }
                            },
                            plugins: {
                                tooltip: {
                                    callbacks: {
                                        label: function(context) {
                                            return context.parsed.y.toLocaleString('vi-VN') + ' ₫';
                                        }
                                    }
                                }
                            }
                        }
                    });
                </script>
            </c:if>

        </main>
        <jsp:include page="../layout/footer.jsp" />
    </div>
</div>
</body>
</html>
