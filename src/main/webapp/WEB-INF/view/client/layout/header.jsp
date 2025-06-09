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
                    <a href="/HomePage" class="nav-item nav-link ${pageContext.request.requestURI == '/HomePage' ? 'active' : ''}">
                        <i class="fas fa-home me-2"></i>Trang chủ
                    </a>

                    <a href="/main-products" class="nav-item nav-link ${pageContext.request.requestURI.startsWith('/courts') ? 'active' : ''}">
                        <i class="fas fa-table-tennis me-2"></i>Sân cầu lông
                    </a>

                    <a href="/by-products" class="nav-item nav-link ${pageContext.request.requestURI.startsWith('/racket') ? 'active' : ''}">
                        <i class="fas fa-shopping-basket me-2"></i>Vợt cầu lông
                    </a>

                    <a href="/match-posts" class="nav-item nav-link ${pageContext.request.requestURI.startsWith('/match-posts') ? 'active' : ''}">
                        <i class="fas fa-users me-2"></i>Tìm bạn chơi
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
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        const userEmail = "${sessionScope.email}";
        if (!userEmail) {
            console.log("Không có email người dùng - Bỏ qua kết nối WebSocket");
            return;
        }

        // === 1. Yêu cầu quyền Notification khi người dùng click (tuân thủ trình duyệt) ===
        function requestNotificationPermission() {
            if (Notification.permission === "granted") {
                return Promise.resolve();
            } else if (Notification.permission === "denied") {
                console.warn("Người dùng đã từ chối quyền thông báo!");
                return Promise.reject("Permission denied");
            } else {
                return Notification.requestPermission().then(permission => {
                    if (permission !== "granted") {
                        throw new Error("Người dùng không cho phép thông báo");
                    }
                });
            }
        }

        // Gắn sự kiện click để xin quyền (ví dụ: khi click vào nút "Cho phép thông báo")
        document.getElementById("enable-notifications-btn")?.addEventListener("click", () => {
            requestNotificationPermission()
                .then(() => console.log("Có quyền gửi thông báo"))
                .catch(err => console.error("Lỗi quyền thông báo:", err));
        });

        // === 2. Kết nối WebSocket và xử lý thông báo ===
        const socket = new SockJS("/ws");
        const stompClient = Stomp.over(socket);
        stompClient.debug = null; // Tắt log debug của Stomp

        stompClient.connect({}, function (frame) {
            console.log("✅ Đã kết nối WebSocket");

            stompClient.subscribe("/user/queue/notifications", function (message) {
                const content = message.body;
                console.log("📩 Nhận thông báo:", content);

                let notificationData;
                try {
                    notificationData = JSON.parse(content);
                } catch (e) {
                    console.error("Lỗi parse JSON thông báo:", e);
                    notificationData = { message: content }; // fallback
                }

                showNotification(notificationData).catch(err => {
                    console.error("Không thể hiển thị Notification:", err);
                    if (typeof Swal !== "undefined") {
                        Swal.fire({
                            icon: 'info',
                            title: 'Thông báo mới',
                            text: notificationData.message || content,
                            timer: 5000
                        });
                    } else {
                        console.log("Nội dung thông báo:", notificationData.message || content);
                    }
                });
            });


        }, function (error) {
            console.error("❌ Lỗi kết nối WebSocket:", error);
        });

        // === 4. Hàm hiển thị Notification (xử lý đa nền tảng) ===
        async function showNotification(data) {
            if (!("Notification" in window)) {
                throw new Error("Trình duyệt không hỗ trợ Notification API");
            }

            await requestNotificationPermission();

            const options = {
                body: data.message || "Bạn có thông báo mới",
                icon: window.location.origin + "/images/icon.png",
                requireInteraction: true,
                vibrate: [200, 100, 200]
            };

            const notification = new Notification("Thông báo mới", options);

            const audio = new Audio("https://notificationsounds.com/storage/sounds/file-sounds-1154-pristine.mp3");
            audio.play().catch(e => console.warn("Không thể phát âm thanh:", e));

            notification.onclick = () => {
                window.focus();
                if (data.id) {
                    window.location.href = "/match-posts/" + data.id;
                } else {
                    window.location.href = "/match-posts";
                }
            };
        }


        // Ngắt kết nối khi đóng trang
        window.addEventListener('beforeunload', function () {
            if (stompClient?.connected) {
                stompClient.disconnect();
                console.log("🛑 Đã ngắt kết nối WebSocket");
            }
        });
    });
</script>