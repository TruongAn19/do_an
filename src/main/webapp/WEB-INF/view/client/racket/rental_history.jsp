<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Lịch sử thuê vợt - Sân cầu lông</title>
    <meta content="width=device-width, initial-scale=1.0" name="viewport">

    <!-- Google Web Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@400;600&family=Raleway:wght@600;800&display=swap"
          rel="stylesheet">

    <!-- Icon Font Stylesheet -->
    <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.15.4/css/all.css"/>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.4.1/font/bootstrap-icons.css" rel="stylesheet">

    <!-- Libraries Stylesheet -->
    <link href="/client/lib/lightbox/css/lightbox.min.css" rel="stylesheet">
    <link href="/client/lib/owlcarousel/assets/owl.carousel.min.css" rel="stylesheet">

    <!-- Customized Bootstrap Stylesheet -->
    <link href="/client/css/bootstrap.min.css" rel="stylesheet">

    <!-- Template Stylesheet -->
    <link href="/client/css/style.css" rel="stylesheet">
</head>

<body>

<jsp:include page="../layout/header.jsp"/>

<div class="container mt-5">
    <h2 class="text-center mb-4">Lịch Sử Thuê Vợt</h2>

    <table class="table table-striped table-bordered">
        <thead class="table-dark text-center">
        <tr>
            <th>#</th>
            <th>Họ và Tên</th>
            <th>Số điện thoại</th>
            <th>Tên Vợt</th>
            <th>Loại Thuê</th>
            <th>Số lượng</th>
            <th>Ngày Thuê</th>
            <th>Tiền Thuê</th>
            <th>Trạng Thái</th>
            <th>Hành Động</th>
        </tr>
        </thead>
        <tbody class="text-center">
        <c:forEach items="${rentalHistories}" var="rental" varStatus="status">
            <tr>
                <td>${status.index + 1}</td>
                <td>${rental.fullName}</td>
                <td>${rental.phone}</td>
                <td>${rental.racketId}</td>
                <td>
                    <c:choose>
                        <c:when test="${rental.type == 'ON_SITE'}">Thuê tại sân</c:when>
                        <c:when test="${rental.type == 'DAILY'}">Thuê theo ngày</c:when>
                        <c:otherwise>Khác</c:otherwise>
                    </c:choose>
                </td>
                <td>${rental.quantity}</td>
                <td>${rental.rentalDate} </td>
                <td><fmt:formatNumber value="${rental.rentalPrice}" type="currency" currencySymbol="₫"/></td>
                <td>
                    <c:choose>
                        <c:when test="${rental.status == 'PENDING'}">
                            <span class="badge bg-warning text-dark">Chờ xác nhận</span>
                        </c:when>
                        <c:when test="${rental.status == 'COMPLETED'}">
                            <span class="badge bg-success">Đã thanh toán</span>
                        </c:when>
                        <c:when test="${rental.status == 'CANCELLED'}">
                            <span class="badge bg-danger">Hủy</span>
                        </c:when>
                        <c:when test="${rental.status == 'PAID'}">
                            <span class="badge bg-primary">Đã thanh toán</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge bg-secondary">Trạng thái không xác định</span>
                        </c:otherwise>
                    </c:choose>
                </td>

                <td>
                    <div class="dropdown">
                        <button class="btn btn-sm btn-secondary dropdown-toggle" type="button" id="dropdownMenuButton" data-bs-toggle="dropdown" aria-expanded="false">
                            <i class="fas fa-cogs"></i> Hành động
                        </button>
                        <ul class="dropdown-menu" aria-labelledby="dropdownMenuButton">
                            <li><a class="dropdown-item" href="#" data-bs-toggle="modal" data-bs-target="#viewDetailsModal" onclick="viewDetails(${rental.id})"><i class="fas fa-eye"></i> Xem</a></li>

                            <c:if test="${rental.status == 'PENDING'}">
                                <li>
                                    <form action="/rental/${rental.id}/cancel" method="post" onsubmit="return confirm('Bạn có chắc muốn hủy đơn này?')">
                                        <button type="submit" class="dropdown-item">
                                            <i class="fas fa-times"></i> Hủy
                                        </button>
                                    </form>
                                </li>
                                <li><a class="dropdown-item" href="#" data-bs-toggle="modal" data-bs-target="#updateRentalModal" onclick="loadRentalForUpdate(${rental.id})"><i class="fas fa-edit"></i> Sửa</a></li>
                            </c:if>

                            <c:if test="${rental.status == 'COMPLETED'}">
                                <li>
                                    <form action="/rental/${rental.id}/return" method="post">
                                        <button type="submit" class="dropdown-item">
                                            <i class="fas fa-undo"></i> Trả vợt
                                        </button>
                                    </form>
                                </li>
                            </c:if>
                        </ul>
                    </div>
                </td>

            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>


<!-- Modal Xem chi tiết -->
<div class="modal fade" id="viewDetailsModal" tabindex="-1" aria-labelledby="viewDetailsModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="viewDetailsModalLabel">Chi Tiết Đơn Thuê</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <!-- Nội dung chi tiết đơn thuê sẽ được hiển thị ở đây -->
                <div id="rentalDetailsContent"></div>
            </div>
        </div>
    </div>
</div>

<!-- Modal Cập nhật -->
<div class="modal fade" id="updateRentalModal" tabindex="-1" aria-labelledby="updateRentalModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="updateRentalModalLabel">Cập Nhật Đơn Thuê</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <form id="updateRentalForm">
                    <!-- Các trường cần cập nhật sẽ điền vào đây -->
                    <div class="mb-3">
                        <label for="rentalStatus" class="form-label">Trạng thái</label>
                        <select id="rentalStatus" class="form-select">
                            <option value="PENDING">Chờ xác nhận</option>
                            <option value="COMPLETED">Đã hoàn tất</option>
<%--                            <option value="PAID">Đã thanh toán</option>--%>
<%--                            <option value="CANCEL">Hủy</option>--%>
                        </select>
                    </div>
                    <!-- Thêm các trường khác nếu cần -->
                    <button type="submit" class="btn btn-primary">Cập Nhật</button>
                </form>
            </div>
        </div>
    </div>
</div>


<jsp:include page="../layout/footer.jsp"/>

<!-- Back to Top -->
<a href="#" class="btn btn-primary border-3 border-primary rounded-circle back-to-top"><i
        class="fa fa-arrow-up"></i></a>


<script>
    function viewDetails(rentalId) {
        // Gọi API để lấy chi tiết đơn thuê
        fetch(`/api/rentals/${rentalId}`)
            .then(response => response.json())
            .then(data => {
                // Điền chi tiết vào modal
                const detailsContent = `
                <p><strong>Họ và Tên:</strong> ${data.fullName}</p>
                <p><strong>Email:</strong> ${data.email}</p>
                <p><strong>Ngày Thuê:</strong> ${data.rentalDate}</p>
                <p><strong>Trạng Thái:</strong> ${data.status}</p>
                <!-- Thêm thông tin chi tiết khác nếu cần -->
            `;
                document.getElementById('rentalDetailsContent').innerHTML = detailsContent;
            });
    }

    function loadRentalForUpdate(rentalId) {
        // Gọi API để lấy chi tiết đơn thuê
        fetch(`/api/rentals/${rentalId}`)
            .then(response => response.json())
            .then(data => {
                // Điền thông tin vào form cập nhật
                document.getElementById('rentalStatus').value = data.status;
                // Điền các trường khác nếu cần
                // Có thể cần thêm các trường khác trong form
            });
    }

</script>

<!-- JavaScript Libraries -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.4/jquery.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="/client/lib/easing/easing.min.js"></script>
<script src="/client/lib/waypoints/waypoints.min.js"></script>
<script src="/client/lib/lightbox/js/lightbox.min.js"></script>
<script src="/client/lib/owlcarousel/owl.carousel.min.js"></script>

<!-- Template Javascript -->
<script src="/client/js/main.js"></script>

</body>
</html>
