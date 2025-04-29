<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!-- Navbar start -->
<div class="container-fluid fixed-top">
    <div class="container px-0">
        <nav class="navbar navbar-light bg-white navbar-expand-xl">
            <a href="/HomePage" class="navbar-brand">
                <h1 class="text-primary display-6">Badminton Court</h1>
            </a>
            <button class="navbar-toggler py-2 px-3" type="button" data-bs-toggle="collapse"
                    data-bs-target="#navbarCollapse">
                <span class="fa fa-bars text-primary"></span>
            </button>
            <div class="collapse navbar-collapse bg-white justify-content-between mx-5" id="navbarCollapse">
                <div class="navbar-nav">
                    <a href="/HomePage" class="nav-item nav-link active">Trang chủ</a>
                    <a href="/main-products" class="nav-item nav-link">Sân cầu lông</a>
                    <a href="/by-products" class="nav-item nav-link">Phụ kiện</a>
                </div>
                <div class="d-flex m-3 me-0">
                    <c:if test="${not empty pageContext.request.userPrincipal}">
                        <a href="/cart" class="position-relative me-4 my-auto">
                            <i class="fa fa-shopping-bag fa-2x"></i>
                            <span
                                    class="position-absolute bg-secondary rounded-circle d-flex align-items-center justify-content-center text-dark px-1"
                                    style="top: -5px; left: 15px; height: 20px; min-width: 20px;">${sessionScope.sum}</span>
                        </a>
                        <div class="dropdown my-auto">
                            <a href="#" class="dropdown" role="button" id="dropdownMenuLink"
                               data-bs-toggle="dropdown" aria-expanded="false" data-bs-toggle="dropdown"
                               aria-expanded="false">
                                <i class="fas fa-user fa-2x"></i>
                            </a>

                            <ul class="dropdown-menu dropdown-menu-end p-4" aria-labelledby="dropdownMenuLink">
                                <li class="d-flex align-items-center flex-column" style="min-width: 300px;">
                                    <img style="width: 150px; height: 150px; border-radius: 50%; overflow: hidden;"
                                         src="/images/avatar/${sessionScope.avatar}"/>
                                    <div class="text-center my-3">
                                        <c:out value="${sessionScope.fullName}"/>
                                    </div>
                                </li>

                                <li><a class="dropdown-item" href="/profile">Quản lý tài khoản</a></li>

                                <li><a class="dropdown-item" href="/order-history">Lịch sử giao dịch</a></li>
                                <li><a class="dropdown-item" href="/booking-history">Lịch sử đặt sân</a></li>
                                <li><a class="dropdown-item" href="/rental-history">Lịch sử thuê dụng cụ</a></li>

                                <li>
                                    <hr class="dropdown-divider">
                                </li>
                                <li>
                                    <form method="post" action="/logout">
                                        <input type="hidden" name="${_csrf.parameterName}"
                                               value="${_csrf.token}"/>
                                        <button class="dropdown-item">Đăng xuất</button>
                                    </form>
                                </li>
                            </ul>
                        </div>
                    </c:if>
                    <c:if test="${empty pageContext.request.userPrincipal}">
                        <a href="/login" class="position-relative me-4 my-auto">
                            Đăng nhập
                        </a>
                    </c:if>
                </div>
            </div>
        </nav>
    </div>
</div>
<!-- Navbar End -->
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>


<%-- Noti --%>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        const userId = "${sessionScope.id}"; // Lấy userId từ session
        if (!userId) {
            console.warn("User ID không tồn tại trong session");
            return;
        }

        const topic = 'user-' + userId;
        let eventSource = new EventSource('/ntfy-sse/' + topic);

        eventSource.onmessage = function (event) {
            try {
                const data = JSON.parse(event.data); // Chắc chắn rằng dữ liệu nhận được là JSON
                console.log('SSE data nhận được:', data);

                // Kiểm tra nếu dữ liệu có event là 'message'
                if (data.event === 'message') {
                    // Hiển thị thông báo với thông tin nhận được
                    Swal.fire({
                        icon: 'info', // hoặc success, warning, error
                        title: data.title || "",
                        text: data.message || '',
                        showConfirmButton: true, // Có thể tắt nếu muốn tự động đóng
                        showCloseButton: true,
                        timer: 5000, // Tự động đóng sau 5 giây (nếu cần)
                        background: '#f0f8ff', // Màu nền tùy chỉnh
                        position: 'center' // Đây là mặc định rồi, nhưng có thể chỉ rõ
                    });

                } else {
                    console.warn("Không có event 'message' trong dữ liệu SSE.");
                }
            } catch (e) {
                console.error('Lỗi khi xử lý thông báo:', e);
                Swal.fire({
                    toast: true,
                    position: 'top-end',
                    icon: 'error',
                    title: 'Có lỗi xảy ra!',
                    showConfirmButton: false,
                    showCloseButton: true,
                    timer: 3000,
                    background: '#fff0f0'
                });
            }
        };

        window.addEventListener('beforeunload', function () {
            if (eventSource) {
                eventSource.close();
            }
        });
    });
</script>

