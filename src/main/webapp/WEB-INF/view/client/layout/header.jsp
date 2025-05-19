<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!-- Navbar start -->
<div class="container-fluid fixed-top shadow-sm">
    <div class="container px-0">
        <nav class="navbar navbar-expand-xl navbar-light bg-white py-3">
            <a href="/HomePage" class="navbar-brand d-flex align-items-center">
                <i class="fas fa-shuttlecock text-primary me-2" style="font-size: 28px;"></i>
                <h1 class="m-0 text-primary">Badminton Court</h1>
            </a>

            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarCollapse">
                <span class="navbar-toggler-icon"></span>
            </button>

            <div class="collapse navbar-collapse" id="navbarCollapse">
                <div class="navbar-nav mx-auto py-0">
                    <a href="/HomePage" class="nav-item nav-link ${pageContext.request.requestURI eq '/HomePage' ? 'active' : ''}">
                        <i class="fas fa-home me-2"></i>Trang chủ
                    </a>
                    <a href="/main-products" class="nav-item nav-link ${pageContext.request.requestURI eq '/courts' ? 'active' : ''}">
                        <i class="fas fa-table-tennis me-2"></i>Sân cầu lông
                    </a>
                    <a href="/by-products" class="nav-item nav-link ${pageContext.request.requestURI eq '/racket' ? 'active' : ''}">
                        <i class="fas fa-shopping-basket me-2"></i>Vợt cầu lông
                    </a>
                </div>

                <div class="d-flex align-items-center">
                    <c:if test="${not empty pageContext.request.userPrincipal}">
                        <div class="dropdown">
                            <a href="#" class="dropdown-toggle d-flex align-items-center" id="userDropdown" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                <div class="rounded-circle overflow-hidden me-2" style="width: 40px; height: 40px;">
                                    <img src="/images/avatar/${sessionScope.avatar}" class="img-fluid" alt="User Avatar">
                                </div>
                                <span class="d-none d-lg-inline-block text-dark">${sessionScope.fullName}</span>
                            </a>

                            <ul class="dropdown-menu dropdown-menu-end shadow-sm" aria-labelledby="userDropdown">
                                <li class="d-flex align-items-center flex-column p-3 border-bottom">
                                    <div class="rounded-circle overflow-hidden mb-3" style="width: 80px; height: 80px;">
                                        <img src="/images/avatar/${sessionScope.avatar}" class="img-fluid" alt="User Avatar">
                                    </div>
                                    <h6 class="fw-bold mb-0">${sessionScope.fullName}</h6>
                                </li>

                                <li><a class="dropdown-item py-2" href="/profile"><i class="fas fa-user-cog me-2 text-primary"></i>Quản lý tài khoản</a></li>
                                <li><a class="dropdown-item py-2" href="/booking-history"><i class="fas fa-history me-2 text-primary"></i>Lịch sử đặt sân</a></li>
                                <li><a class="dropdown-item py-2" href="/rental-history"><i class="fas fa-clipboard-list me-2 text-primary"></i>Lịch sử thuê dụng cụ</a></li>

                                <li><hr class="dropdown-divider"></li>

                                <li>
                                    <form method="post" action="/logout" class="px-2">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                        <button class="dropdown-item py-2 text-danger"><i class="fas fa-sign-out-alt me-2"></i>Đăng xuất</button>
                                    </form>
                                </li>
                            </ul>
                        </div>
                    </c:if>

                    <c:if test="${empty pageContext.request.userPrincipal}">
                        <a href="/login" class="btn btn-outline-primary rounded-pill px-4">
                            <i class="fas fa-sign-in-alt me-2"></i>Đăng nhập
                        </a>
                    </c:if>
                </div>
            </div>
        </nav>
    </div>
</div>
<!-- Navbar End -->

<!-- SweetAlert2 Library -->
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

<!-- Notification Script -->
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
                        timer: undefined, // Tự động đóng sau 5 giây (nếu cần)
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