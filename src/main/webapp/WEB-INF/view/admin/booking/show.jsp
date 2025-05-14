<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page import="java.time.format.DateTimeFormatter" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no"/>
    <meta name="description" content="Hỏi Dân IT - Dự án laptopshop"/>
    <meta name="author" content="Hỏi Dân IT"/>
    <title>Manager Booking - Sân cầu lông</title>
    <link href="/css/styles.css" rel="stylesheet"/>
    <script src="https://use.fontawesome.com/releases/v6.3.0/js/all.js"
            crossorigin="anonymous"></script>
    <style>
        .date-cell {
            min-width: 120px;
        }

        .action-cell {
            min-width: 200px;
        }
    </style>
</head>

<body class="sb-nav-fixed">
<jsp:include page="../layout/header.jsp"/>
<div id="layoutSidenav">
    <jsp:include page="../layout/sidebar.jsp"/>
    <div id="layoutSidenav_content">
        <main>
            <div class="container-fluid px-4">
                <h1 class="mt-4">Manage Booking</h1>
                <ol class="breadcrumb mb-4">
                    <li class="breadcrumb-item"><a href="/admin">Dashboard</a></li>
                    <li class="breadcrumb-item active">Booking</li>
                </ol>

                <!-- Filter Section -->
                <div class="card mb-4">
                    <div class="card-header">
                        <i class="fas fa-filter me-1"></i>
                        Filter Bookings
                    </div>
                    <div class="card-body">
                        <form action="/admin/booking" method="get" class="row g-3">
                            <div class="col-md-4">
                                <label for="date" class="form-label">Select Date</label>
                                <input type="date" name="date" id="date" class="form-control"
                                       value="${selectedDate != null ? selectedDate : ''}">
                            </div>
                            <div class="col-md-2 d-flex align-items-end">
                                <button type="submit" class="btn btn-primary me-2">
                                    <i class="fas fa-search me-1"></i> Filter
                                </button>
                                <c:if test="${selectedDate != null}">
                                    <a href="/admin/booking" class="btn btn-secondary">
                                        <i class="fas fa-times me-1"></i> Clear
                                    </a>
                                </c:if>
                            </div>
                        </form>
                    </div>
                </div>
                <div class="mb-3">
                    <form action="/admin/booking" method="get" class="row g-3">
                        <div>
                            <div class="input-group">
                                <input type="text" class="form-control" name="search"
                                       placeholder="Tìm kiếm sân theo mã đặt..."
                                       value="${not empty searchTerm ? searchTerm : ''}">
                                <button class="btn btn-outline-secondary" type="submit">
                                    <i class="fas fa-search"></i>
                                </button>
                                <c:if test="${not empty searchTerm}">
                                    <a href="/admin/booking"
                                       class="btn btn-outline-danger">
                                        <i class="fas fa-times"></i>
                                    </a>
                                </c:if>
                            </div>
                        </div>
                        <input type="hidden" name="page" value="1">
                    </form>
                </div>
                <!-- Booking Table -->
                <div class="card mb-4">
                    <div class="card-header">
                        <i class="fas fa-table me-1"></i>
                        Booking List
                        <c:if test="${selectedDate != null}">
                                                <span class="badge bg-info ms-2">
                                                    Filtered by: ${selectedDate}
                                                </span>
                        </c:if>
                    </div>
                    <div class="card-body">
                        <table class="table table-bordered table-hover table-striped">
                            <thead class="table-dark">
                            <tr>
                                <th>Booking Code</th>
                                <th>Customer</th>
                                <th>Phone</th>
                                <th>Booking Dates</th>
                                <th>Total Price</th>
                                <th>Status</th>
                                <th>RentalToolCode</th>
                                <th class="action-cell">Actions</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="booking" items="${bookings}">
                                <tr>
                                    <td>${booking.bookingCode}</td>
                                    <td>${booking.receiverName}</td>
                                    <td>${booking.receiverPhone}</td>
                                    <td class="date-cell">
                                        <c:forEach var="detail"
                                                   items="${booking.bookingDetails}" varStatus="loop">
                                            <c:choose>
                                                <c:when test="${not empty detail.date}">
                                                    ${detail.date.getDayOfMonth()}/${detail.date.getMonthValue()}/${detail.date.getYear()}
                                                </c:when>
                                                <c:otherwise>
                                                    N/A
                                                </c:otherwise>
                                            </c:choose>
                                            <c:if test="${!loop.last}">, </c:if>
                                        </c:forEach>
                                    </td>
                                    <td>
                                        <fmt:formatNumber type="number"
                                                          value="${booking.totalPrice}"/> đ
                                    </td>
                                    <td>
                                                                <span class="badge 
                                                    <c:choose>
                                                        <c:when test=" ${booking.status=='CONFIRMED' }">bg-success
                                                                    </c:when>
                                                                    <c:when test="${booking.status == 'PENDING'}">
                                                                        bg-warning text-dark</c:when>
                                                                    <c:when test="${booking.status == 'CANCELLED'}">
                                                                        bg-danger</c:when>
                                                                    <c:otherwise>bg-secondary</c:otherwise>
                                                                    </c:choose>">
                                                                        ${booking.status}
                                                                </span>
                                    </td>
                                    <td>
                                        <p>${booking.rentalToolCode}</p>
                                    </td>
                                    <td class="action-cell">
                                        <a href="/admin/booking/${booking.id}"
                                           class="btn btn-sm btn-success" title="View">
                                            <i class="fas fa-eye"></i>
                                        </a>
                                        <a href="/admin/booking/update/${booking.id}"
                                           class="btn btn-sm btn-warning mx-1" title="Edit">
                                            <i class="fas fa-edit"></i>
                                        </a>
                                        <a href="/admin/booking/delete/${booking.id}"
                                           class="btn btn-sm btn-danger" title="Delete"
                                           onclick="return confirm('Are you sure you want to delete this booking?')">
                                            <i class="fas fa-trash-alt"></i>
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>

                            <c:if test="${empty bookings}">
                                <tr>
                                    <td colspan="7" class="text-center text-muted py-4">
                                        <c:choose>
                                            <c:when test="${selectedDate != null}">
                                                No bookings found for the selected date.
                                            </c:when>
                                            <c:otherwise>
                                                No bookings available.
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:if>
                            </tbody>
                        </table>

                        <!-- Pagination Controls -->
                        <div class="d-flex justify-content-center mt-4">
                            <nav>
                                <ul class="pagination">
                                    <c:if test="${currentPage > 1}">
                                        <li class="page-item">
                                            <a class="page-link"
                                               href="?page=${currentPage - 1}
                       <c:if test='${selectedDate != null}'> &date=${selectedDate} </c:if>
                       <c:if test='${searchTerm != null}'> &search=${searchTerm} </c:if>">
                                                Previous
                                            </a>
                                        </li>
                                    </c:if>

                                    <c:forEach begin="1" end="${totalPages}" var="i">
                                        <li class="page-item ${i == currentPage ? 'active' : ''}">
                                            <a class="page-link"
                                               href="?page=${i}
                       <c:if test='${selectedDate != null}'> &date=${selectedDate} </c:if>
                       <c:if test='${searchTerm != null}'> &search=${searchTerm} </c:if>">
                                                    ${i}
                                            </a>
                                        </li>
                                    </c:forEach>

                                    <c:if test="${currentPage < totalPages}">
                                        <li class="page-item">
                                            <a class="page-link"
                                               href="?page=${currentPage + 1}
                       <c:if test='${selectedDate != null}'> &date=${selectedDate} </c:if>
                       <c:if test='${searchTerm != null}'> &search=${searchTerm} </c:if>">
                                                Next
                                            </a>
                                        </li>
                                    </c:if>
                                </ul>
                            </nav>
                        </div>


                    </div>
                </div>
            </div>
        </main>
        <jsp:include page="../layout/footer.jsp"/>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"
        crossorigin="anonymous"></script>
<script src="/js/scripts.js"></script>
<script>
    // Set today's date as default if no date is selected
    document.addEventListener('DOMContentLoaded', function () {
        const dateInput = document.getElementById('date');
        if (dateInput && !dateInput.value) {
            const today = new Date().toISOString().split('T')[0];
            dateInput.value = today;
        }
    });
</script>
</body>

</html>