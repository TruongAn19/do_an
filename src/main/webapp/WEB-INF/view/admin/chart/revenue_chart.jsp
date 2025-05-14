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
    <style>
        .card {
            border-radius: 8px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
            margin-bottom: 20px;
        }
        .card-header {
            background-color: #f8f9fa;
            border-bottom: 1px solid #e3e6f0;
            padding: 15px 20px;
            border-top-left-radius: 8px;
            border-top-right-radius: 8px;
        }
        .card-body {
            padding: 20px;
        }
        .chart-container {
            position: relative;
            height: 400px;
            margin-top: 20px;
        }
        .date-range-form {
            background-color: #f8f9fa;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 20px;
        }
    </style>
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

            <div class="card date-range-form">
                <div class="card-body">
                    <form method="post" action="/admin/statistics/revenue">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                        <div class="row mb-3">
                            <div class="col-md-4">
                                <label class="form-label">Từ ngày:</label>
                                <input type="date" name="startDate" class="form-control" value="<fmt:formatDate value='${startDate}' pattern='yyyy-MM-dd'/>" required />
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Đến ngày:</label>
                                <input type="date" name="endDate" class="form-control" value="<fmt:formatDate value='${endDate}' pattern='yyyy-MM-dd'/>" required />
                            </div>
                            <div class="col-md-4 align-self-end">
                                <button type="submit" class="btn btn-primary mt-2">Thống kê</button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>

            <c:if test="${not empty revenueData}">
                <div class="card">
                    <div class="card-header">
                        <h2 class="mb-0">
                            Doanh thu từ
                            <fmt:formatDate value="${startDate}" pattern="dd/MM/yyyy"/>
                            đến
                            <fmt:formatDate value="${endDate}" pattern="dd/MM/yyyy"/>
                        </h2>
                    </div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-bordered table-striped">
                                <thead class="table-light">
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
                        </div>

                        <!-- Biểu đồ doanh thu -->
                        <div class="chart-container">
                            <canvas id="revenueChart"></canvas>
                        </div>

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
                                type: 'line', // Changed from 'bar' to 'line'
                                data: {
                                    labels: labels,
                                    datasets: [{
                                        label: 'Doanh thu (VNĐ)',
                                        data: data,
                                        backgroundColor: 'rgba(54, 162, 235, 0.2)',
                                        borderColor: 'rgba(54, 162, 235, 1)',
                                        borderWidth: 2,
                                        tension: 0.2, // Adds slight curve to the line
                                        pointBackgroundColor: 'rgba(54, 162, 235, 1)',
                                        pointBorderColor: '#fff',
                                        pointBorderWidth: 1,
                                        pointRadius: 4,
                                        pointHoverRadius: 6,
                                        fill: true // Fills area under the line
                                    }]
                                },
                                options: {
                                    responsive: true,
                                    maintainAspectRatio: false,
                                    scales: {
                                        y: {
                                            beginAtZero: true,
                                            grid: {
                                                color: 'rgba(0, 0, 0, 0.05)'
                                            },
                                            ticks: {
                                                callback: function(value) {
                                                    return value.toLocaleString('vi-VN') + ' ₫';
                                                }
                                            }
                                        },
                                        x: {
                                            grid: {
                                                color: 'rgba(0, 0, 0, 0.05)'
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
                                        },
                                        legend: {
                                            position: 'top',
                                            labels: {
                                                font: {
                                                    size: 14
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                        </script>
                    </div>
                </div>
            </c:if>
        </main>
        <jsp:include page="../layout/footer.jsp" />
    </div>
</div>
</body>
</html>