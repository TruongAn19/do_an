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
    <link href="/client/css/match_post.css" rel="stylesheet">


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
                        <c:if test="${post.status != 'closed'}">
                        <div class="col-lg-6 mb-4">
                            <div class="post-card ${post.status == 'full' ? 'post-full' : ''}">
                                <c:if test="${not empty currentUser and not empty post.user and currentUser.id eq post.user.id}">
                                    <div class="post-owner-badge bg-info">
                                        <i class="fas fa-user me-1"></i>Bài của bạn
                                    </div>
                                </c:if>

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
                        </c:if>
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