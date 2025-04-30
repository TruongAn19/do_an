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
    <link href="https://cdn.jsdelivr.net/npm/simple-datatables@7.1.2/dist/style.min.css" rel="stylesheet" />
    <link href="/css/styles.css" rel="stylesheet" />
    <script src="https://use.fontawesome.com/releases/v6.3.0/js/all.js" crossorigin="anonymous"></script>


    <%--                statistic--%>
    <title>Racket Statistics Dashboard</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        .card {
            transition: transform 0.3s;
            margin-bottom: 20px;
        }
        .card:hover {
            transform: translateY(-5px);
            box-shadow: 0 10px 20px rgba(0,0,0,0.1);
        }
        .stat-card {
            border-left: 4px solid #0d6efd;
        }
        .stat-icon {
            font-size: 2rem;
            opacity: 0.7;
        }
        .chart-container {
            position: relative;
            height: 300px;
            margin-bottom: 20px;
        }
        .top-rackets-table th, .top-rackets-table td {
            padding: 12px 15px;
        }
        .month-selector {
            max-width: 200px;
        }
    </style>
</head>

<body class="sb-nav-fixed">
<jsp:include page="../layout/header.jsp" />
<div id="layoutSidenav">
    <jsp:include page="../layout/sidebar.jsp" />
    <div id="layoutSidenav_content">
        <main>
            <div class="bg-light">
                <div class="container-fluid py-4">
                    <div class="row mb-4">
                        <div class="col">
                            <h1 class="h3 mb-0 text-gray-800">Racket Statistics Dashboard</h1>
                            <p class="text-muted">Overview of racket inventory and rental statistics</p>
                        </div>
                        <<div class="col-auto">
                        <form action="/admin/racket-statistics" method="get" class="d-flex align-items-center">
                            <select name="courtId" class="form-select me-2" style="width: 200px;">
                                <option value="">Select Court</option>
                                <c:forEach var="court" items="${listProduct}">
                                    <option value="${court.id}" ${court.id == courtId ? 'selected' : ''}>${court.name}</option>
                                </c:forEach>
                            </select>

                            <input type="date" name="startDate" value="${startDate}" class="form-control month-selector me-2">
                            <span class="mx-2">to</span>
                            <input type="date" name="endDate" value="${endDate}" class="form-control month-selector me-2">

                            <button type="submit" class="btn btn-primary btn-sm">
                                <i class="bi bi-filter"></i> Filter
                            </button>
                        </form>

                    </div>
                    </div>

                    <!-- Summary Statistics Cards -->
                    <div class="row">
                        <!-- Total Rackets Card -->
                        <div class="col-xl-3 col-md-6">
                            <div class="card stat-card border-0 shadow-sm">
                                <div class="card-body">
                                    <div class="row align-items-center">
                                        <div class="col">
                                            <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">
                                                Total Rackets Available
                                            </div>
                                            <div class="h3 mb-0 font-weight-bold text-gray-800">${totalRackets}</div>
                                            <div class="text-muted small">Số lượng hiện tại</div>
                                        </div>
                                        <div class="col-auto">
                                            <i class="bi bi-box stat-icon text-primary"></i>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Currently Rented Card -->
                        <div class="col-xl-3 col-md-6">
                            <div class="card stat-card border-0 shadow-sm">
                                <div class="card-body">
                                    <div class="row align-items-center">
                                        <div class="col">
                                            <div class="text-xs font-weight-bold text-success text-uppercase mb-1">
                                                Currently Rented
                                            </div>
                                            <div class="h3 mb-0 font-weight-bold text-gray-800">${currentlyRented}</div>
                                            <div class="text-muted small">Rackets out for rent</div>
                                        </div>
                                        <div class="col-auto">
                                            <i class="bi bi-arrow-right-circle stat-icon text-success"></i>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Monthly Rentals Card -->
                        <div class="col-xl-3 col-md-6">
                            <div class="card stat-card border-0 shadow-sm">
                                <div class="card-body">
                                    <div class="row align-items-center">
                                        <div class="col">
                                            <div class="text-xs font-weight-bold text-info text-uppercase mb-1">
                                                Monthly Rentals
                                            </div>
                                            <div class="h3 mb-0 font-weight-bold text-gray-800">${monthlyRentals}</div>
                                            <div class="text-muted small">
                                                Số lượng vợt đã được thuê
                                            </div>
                                        </div>
                                        <div class="col-auto">
                                            <i class="bi bi-calendar-check stat-icon text-info"></i>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Monthly Revenue Card -->
                        <div class="col-xl-3 col-md-6">
                            <div class="card stat-card border-0 shadow-sm">
                                <div class="card-body">
                                    <div class="row align-items-center">
                                        <div class="col">
                                            <div class="text-xs font-weight-bold text-warning text-uppercase mb-1">
                                                Monthly Revenue
                                            </div>
                                            <div class="h3 mb-0 font-weight-bold text-gray-800">
                                                <fmt:formatNumber value="${monthlyRevenue}" type="currency" currencySymbol="₫" />
                                            </div>
                                            <div class="text-muted small">
                                            </div>
                                        </div>
                                        <div class="col-auto">
                                            <i class="bi bi-cash-stack stat-icon text-warning"></i>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Charts Row -->
                    <div class="row">
                        <!-- Monthly Rentals Chart -->
                        <div class="col-lg-6">
                            <div class="card border-0 shadow-sm">
                                <div class="card-header bg-white border-0">
                                    <h6 class="m-0 font-weight-bold">Monthly Rental Trends</h6>
                                </div>
                                <div class="card-body">
                                    <div class="chart-container">
                                        <canvas id="rentalChart"></canvas>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Monthly Revenue Chart -->
                        <div class="col-lg-6">
                            <div class="card border-0 shadow-sm">
                                <div class="card-header bg-white border-0">
                                    <h6 class="m-0 font-weight-bold">Monthly Revenue Trends</h6>
                                </div>
                                <div class="card-body">
                                    <div class="chart-container">
                                        <canvas id="revenueChart"></canvas>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Top Rackets Table -->
                    <div class="row">
                        <div class="col-12">
                            <div class="card border-0 shadow-sm">
                                <div class="card-header bg-white border-0 d-flex justify-content-between align-items-center">
                                    <h6 class="m-0 font-weight-bold">Top 5 Most Rented Rackets</h6>
                                    <span class="badge bg-primary">
                            <fmt:formatDate value="${selectedMonth.atDay(1).toDate()}" pattern="MMMM yyyy" />
                        </span>
                                </div>
                                <div class="card-body">
                                    <div class="table-responsive">
                                        <table class="table table-hover top-rackets-table">
                                            <thead class="table-light">
                                            <tr>
                                                <th scope="col">#</th>
                                                <th scope="col">Racket Name</th>
                                                <th scope="col">Brand</th>
                                                <th scope="col">Rental Count</th>
                                                <th scope="col">Percentage</th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <c:forEach items="${topRackets}" var="racket" varStatus="status">
                                                <tr>
                                                    <th scope="row">${status.index + 1}</th>
                                                    <td>${racket.racketName}</td>
                                                    <td>${racket.racketBrand}</td>
                                                    <td>${racket.rentalCount}</td>
                                                    <td>
                                                        <div class="d-flex align-items-center">
                                                            <div class="progress flex-grow-1 me-2" style="height: 8px;">
                                                                <div class="progress-bar" role="progressbar"
                                                                     style="width: ${racket.rentalCount / monthlyRentals * 100}%;"
                                                                     aria-valuenow="${racket.rentalCount}" aria-valuemin="0"
                                                                     aria-valuemax="${monthlyRentals}"></div>
                                                            </div>
                                                            <span class="text-sm">
                                                        <fmt:formatNumber value="${racket.rentalCount / monthlyRentals * 100}"
                                                                          maxFractionDigits="1" />%
                                                    </span>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                            <c:if test="${empty topRackets}">
                                                <tr>
                                                    <td colspan="5" class="text-center">No rental data available for this period</td>
                                                </tr>
                                            </c:if>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </main>
        <jsp:include page="../layout/footer.jsp" />
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"
        crossorigin="anonymous"></script>
<script src="js/scripts.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.8.0/Chart.min.js"
        crossorigin="anonymous"></script>
<script src="js/chart-area-demo.js"></script>
<script src="js/chart-bar-demo.js"></script>
<script src="https://cdn.jsdelivr.net/npm/simple-datatables@7.1.2/dist/umd/simple-datatables.min.js"
        crossorigin="anonymous"></script>
<script src="js/datatables-simple-demo.js"></script>

<%--          statistic--%>


<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
    // Prepare data for charts
    const months = [
        <c:forEach items="${rentalsByMonth}" var="entry" varStatus="status">
        "${entry.key.month.toString().substring(0, 3)} ${entry.key.year}"${!status.last ? ',' : ''}
        </c:forEach>
    ];

    const rentalData = [
        <c:forEach items="${rentalsByMonth}" var="entry" varStatus="status">
        ${entry.value}${!status.last ? ',' : ''}
        </c:forEach>
    ];

    const revenueData = [
        <c:forEach items="${revenueByMonth}" var="entry" varStatus="status">
        ${entry.value}${!status.last ? ',' : ''}
        </c:forEach>
    ];

    // Rental Chart
    const rentalCtx = document.getElementById('rentalChart').getContext('2d');
    new Chart(rentalCtx, {
        type: 'bar',
        data: {
            labels: months,
            datasets: [{
                label: 'Number of Rentals',
                data: rentalData,
                backgroundColor: 'rgba(78, 115, 223, 0.7)',
                borderColor: 'rgba(78, 115, 223, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        precision: 0
                    }
                }
            }
        }
    });

    // Revenue Chart
    const revenueCtx = document.getElementById('revenueChart').getContext('2d');
    new Chart(revenueCtx, {
        type: 'line',
        data: {
            labels: months,
            datasets: [{
                label: 'Revenue (₫)',
                data: revenueData,
                backgroundColor: 'rgba(28, 200, 138, 0.1)',
                borderColor: 'rgba(28, 200, 138, 1)',
                borderWidth: 2,
                pointBackgroundColor: 'rgba(28, 200, 138, 1)',
                pointBorderColor: '#fff',
                pointRadius: 4,
                fill: true,
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return '₫' + value.toLocaleString();
                        }
                    }
                }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return '₫' + context.parsed.y.toLocaleString();
                        }
                    }
                }
            }
        }
    });
</script>
</body>

</html>