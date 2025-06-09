<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

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

    <!-- Custom Stylesheet -->
    <link href="/client/css/styles.css" rel="stylesheet">

    <style>
        body {
            background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
            font-family: 'Inter', sans-serif;
            min-height: 100vh;
            color: #343a40;
        }

        .content-section {
            padding: 3rem 0;
            min-height: 100vh;
        }

        .page-header {
            text-align: center;
            margin-bottom: 3rem;
            padding: 2rem 0;
            position: relative;
        }

        .page-header h1 {
            background: linear-gradient(135deg, #4e54c8 0%, #8f94fb 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            font-weight: 800;
            font-size: 2.8rem;
            margin-bottom: 1rem;
            position: relative;
            display: inline-block;
        }

        .page-header h1::after {
            content: '';
            position: absolute;
            bottom: -10px;
            left: 50%;
            transform: translateX(-50%);
            width: 80px;
            height: 4px;
            background: linear-gradient(135deg, #4e54c8 0%, #8f94fb 100%);
            border-radius: 2px;
        }

        .page-header .lead {
            color: #6c757d;
            font-size: 1.25rem;
            font-weight: 400;
            max-width: 600px;
            margin: 1.5rem auto 0;
        }

        .search-card {
            background: #ffffff;
            border-radius: 16px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
            border: none;
            padding: 2.5rem;
            margin-bottom: 3.5rem;
            position: relative;
            overflow: hidden;
        }

        .search-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 5px;
            background: linear-gradient(90deg, #4e54c8 0%, #8f94fb 100%);
        }

        .form-label {
            font-weight: 600;
            color: #495057;
            margin-bottom: 0.75rem;
            font-size: 0.95rem;
        }

        .input-group-text {
            background: #4e54c8;
            color: white;
            border: none;
            border-radius: 10px 0 0 10px;
            width: 45px;
            justify-content: center;
        }

        .form-control, .form-select {
            border: 2px solid #e9ecef;
            border-radius: 0 10px 10px 0;
            padding: 0.75rem 1rem;
            font-size: 0.95rem;
            transition: all 0.3s ease;
            height: auto;
        }

        .form-control:focus, .form-select:focus {
            border-color: #4e54c8;
            box-shadow: 0 0 0 0.25rem rgba(78, 84, 200, 0.15);
        }

        .btn {
            border-radius: 10px;
            font-weight: 600;
            padding: 0.75rem 1.5rem;
            transition: all 0.3s ease;
            border: none;
            letter-spacing: 0.5px;
        }

        .btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 15px rgba(0, 0, 0, 0.1);
        }

        .btn-primary {
            background: linear-gradient(135deg, #4e54c8 0%, #8f94fb 100%);
        }

        .btn-success {
            background: linear-gradient(135deg, #2ecc71 0%, #27ae60 100%);
        }

        .btn-outline-primary {
            border: 2px solid #4e54c8;
            color: #4e54c8;
            background: transparent;
        }

        .btn-outline-primary:hover {
            background: #4e54c8;
            color: white;
        }

        .section-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 2.5rem;
            padding: 0;
        }

        .section-header h2 {
            color: #343a40;
            font-weight: 700;
            margin: 0;
            position: relative;
            padding-left: 15px;
        }

        .section-header h2::before {
            content: '';
            position: absolute;
            left: 0;
            top: 50%;
            transform: translateY(-50%);
            width: 5px;
            height: 25px;
            background: linear-gradient(to bottom, #4e54c8 0%, #8f94fb 100%);
            border-radius: 3px;
        }

        .alert {
            border-radius: 12px;
            border: none;
            padding: 1.25rem 1.5rem;
            margin-bottom: 2.5rem;
            font-weight: 500;
        }

        .alert-success {
            background: linear-gradient(135deg, #d4edda 0%, #c3e6cb 100%);
            color: #155724;
            box-shadow: 0 5px 15px rgba(21, 87, 36, 0.1);
        }

        .post-card {
            background: #ffffff;
            border-radius: 16px;
            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.06);
            border: none;
            padding: 1.75rem;
            transition: all 0.4s ease;
            position: relative;
            overflow: hidden;
            height: 100%;
            display: flex;
            flex-direction: column;
        }

        .post-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
        }

        .post-card.post-full {
            opacity: 0.85;
            background: #f8f9fa;
        }

        .post-card.post-full::after {
            content: 'Đã đủ người';
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%) rotate(-30deg);
            background: rgba(108, 117, 125, 0.9);
            color: white;
            padding: 5px 30px;
            font-weight: 700;
            font-size: 1.2rem;
            border-radius: 5px;
            z-index: 1;
        }

        .post-status-badge {
            position: absolute;
            top: 1.25rem;
            right: 1.25rem;
            padding: 0.5rem 1rem;
            border-radius: 25px;
            font-weight: 600;
            font-size: 0.8rem;
            color: white;
            box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
            z-index: 2;
        }

        .post-card h3 {
            color: #343a40;
            font-weight: 700;
            font-size: 1.5rem;
            margin-bottom: 1.25rem;
            margin-top: 0.5rem;
            padding-right: 80px;
        }

        .post-meta {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
            gap: 0.85rem;
            margin-bottom: 1.25rem;
        }

        .post-meta-item {
            display: flex;
            align-items: center;
            gap: 0.75rem;
            font-size: 0.9rem;
            color: #495057;
            background: #f8f9fa;
            padding: 0.65rem 0.85rem;
            border-radius: 10px;
            transition: all 0.3s ease;
        }

        .post-meta-item:hover {
            background: #e9ecef;
            transform: translateY(-2px);
        }

        .post-meta-item i {
            color: #4e54c8;
            width: 16px;
            text-align: center;
            font-size: 1rem;
        }

        .participants-info {
            display: flex;
            align-items: center;
            gap: 0.75rem;
            background: linear-gradient(135deg, #4e54c8 0%, #8f94fb 100%);
            color: white;
            padding: 0.85rem 1.25rem;
            border-radius: 12px;
            font-weight: 600;
            margin-bottom: 1.25rem;
            box-shadow: 0 5px 15px rgba(78, 84, 200, 0.2);
        }

        .post-card p {
            color: #6c757d;
            line-height: 1.7;
            flex-grow: 1;
            margin-bottom: 1.5rem;
            font-size: 0.95rem;
        }

        .no-posts {
            background: #ffffff;
            border-radius: 16px;
            box-shadow: 0 15px 35px rgba(0, 0, 0, 0.08);
            border: none;
            padding: 4rem 2rem;
            text-align: center;
        }

        .no-posts i {
            color: #adb5bd;
            margin-bottom: 1.5rem;
            font-size: 4rem;
        }

        .no-posts p {
            color: #6c757d;
            font-size: 1.2rem;
            margin-bottom: 2rem;
            max-width: 500px;
            margin-left: auto;
            margin-right: auto;
        }

        .form-group {
            margin-bottom: 1.75rem;
        }

        .search-form .row {
            align-items: end;
        }

        /* Pagination styling - Sửa để nằm ngang */
        .pagination-wrapper {
            margin-top: 3rem;
            padding: 2rem 0;
            border-top: 1px solid #e9ecef;
        }

        .pagination {
            margin: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            flex-wrap: wrap;
            gap: 0.5rem;
        }

        .page-item {
            margin: 0;
        }

        .page-item.active .page-link {
            background: linear-gradient(135deg, #4e54c8 0%, #8f94fb 100%);
            border-color: #4e54c8;
            color: white;
            box-shadow: 0 4px 12px rgba(78, 84, 200, 0.3);
        }

        .page-link {
            color: #4e54c8;
            border: 1px solid #dee2e6;
            border-radius: 8px;
            padding: 0.6rem 0.9rem;
            font-weight: 500;
            transition: all 0.3s ease;
            text-decoration: none;
            display: flex;
            align-items: center;
            justify-content: center;
            min-width: 40px;
            height: 40px;
        }

        .page-link:hover {
            background-color: #f8f9fa;
            color: #4e54c8;
            border-color: #4e54c8;
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
        }

        .page-item.disabled .page-link {
            color: #6c757d;
            background-color: #f8f9fa;
            border-color: #dee2e6;
            cursor: not-allowed;
        }

        .page-item.disabled .page-link:hover {
            transform: none;
            box-shadow: none;
        }

        .pagination-info {
            text-align: center;
            color: #6c757d;
            font-size: 0.9rem;
            margin-top: 1rem;
            padding: 0.5rem;
            background: #f8f9fa;
            border-radius: 8px;
            display: inline-block;
        }

        @media (max-width: 768px) {
            .page-header h1 {
                font-size: 2.2rem;
            }

            .search-card {
                padding: 1.75rem;
                margin-bottom: 2.5rem;
            }

            .section-header {
                flex-direction: column;
                gap: 1.25rem;
                text-align: center;
            }

            .section-header h2 {
                padding-left: 0;
            }

            .section-header h2::before {
                display: none;
            }

            .section-header .btn {
                width: 100%;
            }

            .post-meta {
                grid-template-columns: 1fr 1fr;
            }

            .post-status-badge {
                top: 1rem;
                right: 1rem;
            }

            .pagination {
                gap: 0.25rem;
            }

            .page-link {
                padding: 0.5rem 0.7rem;
                min-width: 35px;
                height: 35px;
                font-size: 0.9rem;
            }
        }

        @media (max-width: 576px) {
            .content-section {
                padding: 1.5rem 0;
            }

            .page-header {
                margin-bottom: 2rem;
                padding: 1.5rem 0;
            }

            .search-card {
                padding: 1.5rem;
            }

            .post-card {
                padding: 1.5rem;
            }

            .post-meta {
                grid-template-columns: 1fr;
            }

            .post-card h3 {
                padding-right: 0;
                margin-top: 2.5rem;
                font-size: 1.3rem;
            }

            .pagination {
                gap: 0.2rem;
            }

            .page-link {
                padding: 0.4rem 0.6rem;
                min-width: 32px;
                height: 32px;
                font-size: 0.85rem;
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
            background: linear-gradient(135deg, #4e54c8 0%, #8f94fb 100%);
            border-radius: 10px;
        }

        ::-webkit-scrollbar-thumb:hover {
            background: linear-gradient(135deg, #3a40a5 0%, #7a80e8 100%);
        }

        /* Button hover effects */
        .btn-primary:hover {
            background: linear-gradient(135deg, #3a40a5 0%, #7a80e8 100%);
        }

        .btn-success:hover {
            background: linear-gradient(135deg, #27ae60 0%, #2ecc71 100%);
        }

        /* Card hover effects */
        .search-card:hover {
            box-shadow: 0 15px 40px rgba(0, 0, 0, 0.1);
        }

        /* Form focus animation */
        .form-control:focus, .form-select:focus {
            animation: pulse 1.5s infinite;
        }

        @keyframes pulse {
            0% {
                box-shadow: 0 0 0 0 rgba(78, 84, 200, 0.2);
            }
            70% {
                box-shadow: 0 0 0 6px rgba(78, 84, 200, 0);
            }
            100% {
                box-shadow: 0 0 0 0 rgba(78, 84, 200, 0);
            }
        }
    </style>
</head>
<body>
<jsp:include page="../layout/header.jsp"/>

<div class="content-section">
    <div class="container">
        <div class="page-header">
            <h1><i class="fas fa-users me-2"></i>Tìm Bạn Chơi Cùng</h1>
            <p class="lead">Kết nối với những người cùng đam mê thể thao và tạo những trải nghiệm tuyệt vời</p>
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
                            <div class="input-group">
                                <span class="input-group-text"><i class="fas fa-trophy"></i></span>
                                <select id="skillLevel" name="skillLevel" class="form-select">
                                    <option value="" <c:if test="${empty searchSkillLevel}">selected</c:if>>Tất cả</option>
                                    <option value="Mới chơi" <c:if test="${searchSkillLevel == 'Mới chơi'}">selected</c:if>>Mới chơi</option>
                                    <option value="Trung bình" <c:if test="${searchSkillLevel == 'Trung bình'}">selected</c:if>>Trung bình</option>
                                    <option value="Khá" <c:if test="${searchSkillLevel == 'Khá'}">selected</c:if>>Khá</option>
                                    <option value="Tốt" <c:if test="${searchSkillLevel == 'Tốt'}">selected</c:if>>Tốt</option>
                                </select>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="form-group mt-4">
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
                <i class="fas fa-check-circle me-2"></i>${success}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <c:choose>
            <c:when test="${not empty posts}">
                <div class="row">
                    <c:forEach items="${posts}" var="post">
                        <div class="col-lg-6 mb-4">
                            <div class="post-card ${post.status == 'full' ? 'post-full' : ''}">
                                <c:choose>
                                    <c:when test="${post.status == 'expired'}">
                                        <div class="post-status-badge bg-danger">
                                            <i class="fas fa-clock me-1"></i>Đã hết hạn
                                        </div>
                                    </c:when>
                                    <c:when test="${post.status == 'open'}">
                                        <div class="post-status-badge bg-success">
                                            <i class="fas fa-check-circle me-1"></i>Đang mở
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="post-status-badge bg-secondary">
                                            <i class="fas fa-users me-1"></i>Đã đủ người
                                        </div>
                                    </c:otherwise>
                                </c:choose>

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
                                    <span>${post.currentParticipants}/${post.maxParticipants} người tham gia</span>
                                </div>
                                <p class="mb-4">${post.description}</p>
                                <a href="/match-posts/${post.id}" class="btn btn-outline-primary">
                                    <i class="fas fa-info-circle me-2"></i>Xem chi tiết
                                </a>
                            </div>
                        </div>
                    </c:forEach>
                </div>

                <!-- Phần phân trang đã sửa -->
                <c:if test="${page.totalPages > 1}">
                    <div class="pagination-wrapper">
                        <nav aria-label="Page navigation">
                            <ul class="pagination">
                                    <%-- Nút Previous --%>
                                <li class="page-item ${page.number == 0 ? 'disabled' : ''}">
                                    <a class="page-link"
                                       href="?area=${searchArea}&playDate=${searchDate}&skillLevel=${searchSkillLevel}&page=${page.number-1}&size=${page.size}"
                                       aria-label="Previous">
                                        <i class="fas fa-chevron-left"></i>
                                    </a>
                                </li>

                                    <%-- Các nút trang --%>
                                <c:forEach begin="0" end="${page.totalPages-1}" var="i">
                                    <c:choose>
                                        <c:when test="${i == page.number}">
                                            <li class="page-item active">
                                                <span class="page-link">${i+1}</span>
                                            </li>
                                        </c:when>
                                        <c:otherwise>
                                            <li class="page-item">
                                                <a class="page-link"
                                                   href="?area=${searchArea}&playDate=${searchDate}&skillLevel=${searchSkillLevel}&page=${i}&size=${page.size}">
                                                        ${i+1}
                                                </a>
                                            </li>
                                        </c:otherwise>
                                    </c:choose>
                                </c:forEach>

                                    <%-- Nút Next --%>
                                <li class="page-item ${page.number == page.totalPages-1 ? 'disabled' : ''}">
                                    <a class="page-link"
                                       href="?area=${searchArea}&playDate=${searchDate}&skillLevel=${searchSkillLevel}&page=${page.number+1}&size=${page.size}"
                                       aria-label="Next">
                                        <i class="fas fa-chevron-right"></i>
                                    </a>
                                </li>
                            </ul>
                        </nav>

                            <%-- Hiển thị thông tin trang hiện tại --%>
                        <div class="pagination-info">
                            Hiển thị ${page.number * page.size + 1} - ${page.number * page.size + page.numberOfElements}
                            trong tổng số ${page.totalElements} bài đăng
                        </div>
                    </div>
                </c:if>
            </c:when>
            <c:otherwise>
                <div class="no-posts">
                    <i class="fas fa-search fa-3x"></i>
                    <p>Không có bài đăng nào phù hợp với tiêu chí tìm kiếm của bạn</p>
                    <a href="/match-posts/create" class="btn btn-primary mt-3">
                        <i class="fas fa-plus me-2"></i>Tạo bài đăng mới ngay
                    </a>
                </div>
            </c:otherwise>
        </c:choose>

    </div>
</div>

<jsp:include page="../layout/footer.jsp"/>

</body>
</html>