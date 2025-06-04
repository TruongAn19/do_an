<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.time.format.DateTimeFormatter" %>

<%
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
%>

<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tìm Bạn Chơi Cùng</title>

    <!-- Google Web Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">

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

    <style>
        body {
            background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
            font-family: 'Inter', sans-serif;
            min-height: 100vh;
        }

        .content-section {
            padding: 2rem 0;
            min-height: 100vh;
        }

        .page-header {
            text-align: center;
            margin-bottom: 3rem;
            padding: 2rem 0;
        }

        .page-header h1 {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            font-weight: 700;
            font-size: 2.5rem;
            margin-bottom: 1rem;
        }

        .page-header .lead {
            color: #6c757d;
            font-size: 1.2rem;
            font-weight: 400;
        }

        .search-card {
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(10px);
            border-radius: 20px;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
            border: 1px solid rgba(255, 255, 255, 0.2);
            padding: 2rem;
            margin-bottom: 3rem;
        }

        .form-label {
            font-weight: 600;
            color: #495057;
            margin-bottom: 0.5rem;
        }

        .input-group-text {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 12px 0 0 12px;
        }

        .form-control, .form-select {
            border: 2px solid #e9ecef;
            border-radius: 0 12px 12px 0;
            padding: 0.75rem;
            transition: all 0.3s ease;
        }

        .form-control:focus, .form-select:focus {
            border-color: #667eea;
            box-shadow: 0 0 0 0.2rem rgba(102, 126, 234, 0.25);
        }

        .btn {
            border-radius: 12px;
            font-weight: 600;
            padding: 0.75rem 1.5rem;
            transition: all 0.3s ease;
            border: none;
        }

        .btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
        }

        .btn-primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }

        .btn-success {
            background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
        }

        .btn-outline-primary {
            border: 2px solid #667eea;
            color: #667eea;
            background: transparent;
        }

        .btn-outline-primary:hover {
            background: #667eea;
            color: white;
        }

        .section-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 2rem;
            padding: 1rem 0;
        }

        .section-header h2 {
            color: #495057;
            font-weight: 600;
            margin: 0;
        }

        .alert {
            border-radius: 12px;
            border: none;
            padding: 1rem 1.5rem;
            margin-bottom: 2rem;
        }

        .alert-success {
            background: linear-gradient(135deg, #d4edda 0%, #c3e6cb 100%);
            color: #155724;
        }

        .post-card {
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(10px);
            border-radius: 20px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
            border: 1px solid rgba(255, 255, 255, 0.2);
            padding: 1.5rem;
            transition: all 0.3s ease;
            position: relative;
            overflow: hidden;
            height: 100%;
            display: flex;
            flex-direction: column;
        }

        .post-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
        }

        .post-card.post-full {
            opacity: 0.7;
            background: rgba(248, 249, 250, 0.95);
        }

        .post-status-badge {
            position: absolute;
            top: 1rem;
            right: 1rem;
            padding: 0.5rem 1rem;
            border-radius: 25px;
            font-weight: 600;
            font-size: 0.8rem;
            color: white;
        }

        .post-card h3 {
            color: #495057;
            font-weight: 700;
            font-size: 1.4rem;
            margin-bottom: 1rem;
            margin-top: 0.5rem;
        }

        .post-meta {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
            gap: 0.75rem;
            margin-bottom: 1rem;
        }

        .post-meta-item {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            font-size: 0.9rem;
            color: #6c757d;
            background: #f8f9fa;
            padding: 0.5rem 0.75rem;
            border-radius: 8px;
        }

        .post-meta-item i {
            color: #667eea;
            width: 16px;
            text-align: center;
        }

        .participants-info {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 0.75rem 1rem;
            border-radius: 12px;
            font-weight: 600;
            margin-bottom: 1rem;
        }

        .post-card p {
            color: #6c757d;
            line-height: 1.6;
            flex-grow: 1;
            margin-bottom: 1.5rem;
        }

        .no-posts {
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(10px);
            border-radius: 20px;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
            border: 1px solid rgba(255, 255, 255, 0.2);
            padding: 3rem 2rem;
            text-align: center;
        }

        .no-posts i {
            color: #dee2e6;
            margin-bottom: 1.5rem;
        }

        .no-posts p {
            color: #6c757d;
            font-size: 1.1rem;
            margin-bottom: 1.5rem;
        }

        .form-group {
            margin-bottom: 1.5rem;
        }

        .search-form .row {
            align-items: end;
        }

        @media (max-width: 768px) {
            .page-header h1 {
                font-size: 2rem;
            }

            .search-card {
                padding: 1.5rem;
                margin-bottom: 2rem;
            }

            .section-header {
                flex-direction: column;
                gap: 1rem;
                text-align: center;
            }

            .section-header .btn {
                width: 100%;
            }

            .post-meta {
                grid-template-columns: 1fr;
            }

            .post-status-badge {
                position: static;
                display: inline-block;
                margin-bottom: 1rem;
            }
        }

        @media (max-width: 576px) {
            .content-section {
                padding: 1rem 0;
            }

            .page-header {
                margin-bottom: 2rem;
                padding: 1rem 0;
            }

            .search-card {
                padding: 1rem;
            }

            .post-card {
                padding: 1rem;
            }
        }

        /* Animation for new posts */
        @keyframes slideInUp {
            from {
                opacity: 0;
                transform: translateY(30px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        .post-card {
            animation: slideInUp 0.6s ease-out;
        }

        /* Custom scrollbar */
        ::-webkit-scrollbar {
            width: 8px;
        }

        ::-webkit-scrollbar-track {
            background: #f1f1f1;
            border-radius: 10px;
        }

        ::-webkit-scrollbar-thumb {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-radius: 10px;
        }

        ::-webkit-scrollbar-thumb:hover {
            background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
        }
    </style>
</head>
<body>
<jsp:include page="../layout/header.jsp"/>

<div class="content-section">
    <div class="container">
        <div class="page-header">
            <h1><i class="fas fa-users me-2"></i>Tìm Bạn Chơi Cùng</h1>
            <p class="lead">Kết nối với những người cùng đam mê thể thao</p>
        </div>

        <div class="search-card">
            <form method="get" class="search-form">
                <div class="row">
                    <div class="col-md-4">
                        <div class="form-group">
                            <label for="area" class="form-label">Khu vực</label>
                            <div class="input-group">
                                <span class="input-group-text"><i class="fas fa-map-marker-alt"></i></span>
                                <input type="text" id="area" name="area" class="form-control"
                                       placeholder="Nhập khu vực..." value="${searchArea}">
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="form-group">
                            <label for="playDate" class="form-label">Ngày chơi</label>
                            <div class="input-group">
                                <span class="input-group-text"><i class="far fa-calendar-alt"></i></span>
                                <input type="date" id="playDate" name="playDate" class="form-control" value="${searchDate}">
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="form-group">
                            <label for="skillLevel" class="form-label">Trình độ</label>
                            <select id="skillLevel" name="skillLevel" class="form-select">
                                <option value="">Tất cả</option>
                                <option value="Mới chơi" ${searchSkillLevel == 'Mới chơi' ? 'selected' : ''}>Mới chơi</option>
                                <option value="Trung bình" ${searchSkillLevel == 'Trung bình' ? 'selected' : ''}>Trung bình</option>
                                <option value="Khá" ${searchSkillLevel == 'Khá' ? 'selected' : ''}>Khá</option>
                                <option value="Tốt" ${searchSkillLevel == 'Tốt' ? 'selected' : ''}>Tốt</option>
                            </select>
                        </div>
                    </div>
                </div>
                <div class="form-group mt-3">
                    <button type="submit" class="btn btn-primary w-100">
                        <i class="fas fa-search me-2"></i>Tìm kiếm
                    </button>
                </div>
            </form>
        </div>

        <div class="section-header">
            <h2 class="h4">Danh sách bài đăng</h2>
            <a href="/match-posts/create" class="btn btn-success">
                <i class="fas fa-plus me-2"></i>Tạo bài đăng mới
            </a>
        </div>

        <c:if test="${not empty success}">
            <div class="alert alert-success alert-dismissible fade show">
                    ${success}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <c:choose>
            <c:when test="${not empty posts}">
                <div class="row">
                    <c:forEach items="${posts}" var="post">
                        <div class="col-lg-6 mb-4">
                            <div class="post-card ${post.status == 'full' ? 'post-full' : ''}">
                                <div class="post-status-badge ${post.status == 'open' ? 'bg-success' : 'bg-secondary'}">
                                        ${post.status.equals('open') ? 'Đang mở' : 'Đã đủ người'}
                                </div>
                                <h3>${post.area}</h3>
                                <div class="post-meta">
                                    <div class="post-meta-item">
                                        <i class="far fa-calendar-alt"></i>
                                        <span>${post.playDateStr}</span>
                                    </div>
                                    <div class="post-meta-item">
                                        <i class="far fa-clock"></i>
                                        <span>${post.timeSlot}</span>
                                    </div>
                                    <div class="post-meta-item">
                                        <i class="fas fa-trophy"></i>
                                        <span>${post.skillLevel}</span>
                                    </div>
                                </div>
                                <div class="participants-info">
                                    <i class="fas fa-users"></i>
                                    <span>${post.currentParticipants}/${post.maxParticipants} người</span>
                                </div>
                                <p class="mb-3">${post.description}</p>
                                <a href="/match-posts/${post.id}" class="btn btn-outline-primary">
                                    <i class="fas fa-info-circle me-2"></i>Xem chi tiết
                                </a>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="no-posts">
                    <i class="fas fa-info-circle fa-3x"></i>
                    <p>Không có bài đăng nào phù hợp với tiêu chí tìm kiếm của bạn</p>
                    <a href="/match-posts/create" class="btn btn-primary mt-3">
                        <i class="fas fa-plus me-2"></i>Tạo bài đăng đầu tiên
                    </a>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<jsp:include page="../layout/footer.jsp"/>

<!-- WebSocket Script -->
<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1.5.0/dist/sockjs.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>

<!-- Toastr Notification -->
<link href="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/toastr.min.css" rel="stylesheet">
<script src="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/toastr.min.js"></script>

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
        toastr.success(message, 'Thông báo mới', {
            closeButton: true,
            progressBar: true,
            positionClass: "toast-top-right",
            timeOut: 5000
        });
    }

    // Set min date to today
    document.getElementById('playDate').min = new Date().toISOString().split('T')[0];
</script>
</body>
</html>