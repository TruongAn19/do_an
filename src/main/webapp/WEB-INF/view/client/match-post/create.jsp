<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Tạo Bài Đăng Mới</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <!-- Google Web Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@400;500;600&family=Raleway:wght@600;800&display=swap" rel="stylesheet">

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

    <!-- Custom CSS for this page -->
    <style>
        :root {
            --primary-color: #4e73df;
            --secondary-color: #f8f9fc;
            --accent-color: #2e59d9;
            --text-color: #5a5c69;
        }

        body {
            background-color: #f8f9fc;
            font-family: 'Open Sans', sans-serif;
            color: var(--text-color);
        }

        .post-card {
            background: white;
            border-radius: 15px;
            box-shadow: 0 0.15rem 1.75rem 0 rgba(58, 59, 69, 0.15);
            padding: 2.5rem;
            margin-top: 2rem;
            margin-bottom: 3rem;
        }

        .form-header {
            text-align: center;
            margin-bottom: 2rem;
            color: var(--primary-color);
        }

        .form-header h1 {
            font-weight: 600;
            font-size: 2rem;
            margin-bottom: 0.5rem;
        }

        .form-header p {
            color: #858796;
            font-size: 1.1rem;
        }

        .form-control, .form-select {
            height: 50px;
            border-radius: 8px;
            border: 1px solid #d1d3e2;
            padding: 0.75rem 1rem;
            margin-bottom: 1.5rem;
        }

        .form-control:focus, .form-select:focus {
            border-color: var(--primary-color);
            box-shadow: 0 0 0 0.2rem rgba(78, 115, 223, 0.25);
        }

        textarea.form-control {
            height: auto;
            min-height: 120px;
        }

        .btn-primary {
            background-color: var(--primary-color);
            border-color: var(--primary-color);
            border-radius: 8px;
            padding: 12px 24px;
            font-weight: 600;
            letter-spacing: 0.5px;
            width: 100%;
            transition: all 0.3s;
        }

        .btn-primary:hover {
            background-color: var(--accent-color);
            border-color: var(--accent-color);
            transform: translateY(-2px);
        }

        label {
            font-weight: 600;
            margin-bottom: 0.5rem;
            color: #4a4b65;
        }

        .form-icon {
            position: relative;
        }

        .form-icon i {
            position: absolute;
            top: 15px;
            right: 15px;
            color: #d1d3e2;
        }

        .sport-icon {
            font-size: 1.5rem;
            margin-right: 10px;
            color: var(--primary-color);
        }

        @media (max-width: 768px) {
            .post-card {
                padding: 1.5rem;
            }

            .form-header h1 {
                font-size: 1.5rem;
            }
        }
    </style>
</head>
<body>
<jsp:include page="../layout/header.jsp"/>

<div class="container py-5">
    <div class="row justify-content-center" style="margin-top: 100px">
        <div class="col-lg-8">
            <div class="create-post-card">
                <div class="form-header text-center mb-4">
                    <h1><i class="fas fa-users sport-icon"></i>Tạo Bài Đăng Tìm Bạn Chơi Cùng</h1>
                    <p class="text-muted">Điền thông tin để tìm kiếm đối tác chơi thể thao cùng bạn</p>
                </div>

                <form action="/match-posts/create" method="post" class="needs-validation" novalidate>
                    <div class="row">
                        <div class="col-md-6">
                            <div class="mb-3">
                                <label for="playDate" class="form-label"><i class="fas fa-calendar-day"></i> Ngày chơi</label>
                                <input type="date" id="playDate" name="playDate" class="form-control" required>
                                <div class="invalid-feedback">Vui lòng chọn ngày chơi</div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="mb-3">
                                <label for="timeSlot" class="form-label"><i class="far fa-clock"></i> Khung giờ</label>
                                <select id="timeSlot" name="timeSlot" class="form-select" required>
                                    <option value="" disabled selected>Chọn khung giờ</option>
                                    <option value="Sáng (6h - 11h)">Sáng (6h - 11h)</option>
                                    <option value="Chiều (12h - 17h)">Chiều (12h - 17h)</option>
                                    <option value="Tối (18h - 22h)">Tối (18h - 22h)</option>
                                </select>
                                <div class="invalid-feedback">Vui lòng chọn khung giờ</div>
                            </div>
                        </div>
                    </div>

                    <div class="mb-3">
                        <label for="area" class="form-label"><i class="fas fa-map-marker-alt"></i> Khu vực</label>
                        <input type="text" id="area" name="area" class="form-control" placeholder="VD: Sân bóng Quận 1" required>
                        <div class="invalid-feedback">Vui lòng nhập khu vực</div>
                    </div>

                    <div class="row">
                        <div class="col-md-6">
                            <div class="mb-3">
                                <label for="maxParticipants" class="form-label"><i class="fas fa-users"></i> Số người cần</label>
                                <input type="number" id="maxParticipants" name="maxParticipants" class="form-control" min="1" value="4" required>
                                <div class="invalid-feedback">Vui lòng nhập số người tối thiểu là 1</div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="mb-3">
                                <label for="skillLevel" class="form-label"><i class="fas fa-trophy"></i> Trình độ</label>
                                <select id="skillLevel" name="skillLevel" class="form-select" required>
                                    <option value="" disabled selected>Chọn trình độ</option>
                                    <option value="Mới chơi">Mới chơi</option>
                                    <option value="Trung bình">Trung bình</option>
                                    <option value="Khá">Khá</option>
                                    <option value="Tốt">Tốt</option>
                                </select>
                                <div class="invalid-feedback">Vui lòng chọn trình độ</div>
                            </div>
                        </div>
                    </div>

                    <div class="mb-3">
                        <label for="description" class="form-label"><i class="far fa-comment-dots"></i> Mô tả thêm</label>
                        <textarea id="description" name="description" class="form-control" rows="4" placeholder="Mô tả chi tiết về buổi chơi, yêu cầu đặc biệt..."></textarea>
                    </div>

                    <div class="d-grid gap-2 mt-4">
                        <button type="submit" class="btn btn-primary btn-lg">
                            <i class="fas fa-paper-plane me-2"></i> Đăng bài ngay
                        </button>
                        <a href="/match-posts" class="btn btn-outline-secondary">
                            <i class="fas fa-arrow-left me-2"></i> Quay lại
                        </a>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<script>
    // Example starter JavaScript for disabling form submissions if there are invalid fields
    (function () {
        'use strict'

        // Fetch all the forms we want to apply custom Bootstrap validation styles to
        var forms = document.querySelectorAll('.needs-validation')

        // Loop over them and prevent submission
        Array.prototype.slice.call(forms)
            .forEach(function (form) {
                form.addEventListener('submit', function (event) {
                    if (!form.checkValidity()) {
                        event.preventDefault()
                        event.stopPropagation()
                    }

                    form.classList.add('was-validated')
                }, false)
            })
    })()
</script>

<jsp:include page="../layout/footer.jsp"/>

<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1.5.0/dist/sockjs.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
<script>
    // Kết nối WebSocket
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);

        // Subscribe vào kênh thông báo cá nhân
        stompClient.subscribe('/user/queue/notifications', function(message) {
            showNotification(JSON.parse(message.body));
        });
    });

    function showNotification(message) {
        // Sử dụng Toastr để hiển thị thông báo đẹp
        if (typeof toastr !== 'undefined') {
            toastr.success(message, 'Thông báo mới', {
                closeButton: true,
                progressBar: true,
                positionClass: "toast-top-right",
                timeOut: 5000
            });
        } else {
            // Fallback nếu không có toastr
            alert('Thông báo: ' + message);
        }
    }

    // Set min date to today
    document.getElementById('playDate').min = new Date().toISOString().split('T')[0];
</script>

<!-- Toastr Notification -->
<link href="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/toastr.min.css" rel="stylesheet">
<script src="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/toastr.min.js"></script>
</body>
</html>