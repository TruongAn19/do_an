<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8">
    <title>Tài khoản cá nhân - Sân cầu lông</title>
    <meta content="width=device-width, initial-scale=1.0" name="viewport">
    <meta content="" name="keywords">
    <meta content="" name="description">

    <!-- Google Web Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@400;600&family=Raleway:wght@600;800&display=swap" rel="stylesheet">

    <!-- Icon Font Stylesheet -->
    <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.15.4/css/all.css" />
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.4.1/font/bootstrap-icons.css" rel="stylesheet">

    <!-- Libraries Stylesheet -->
    <link href="/client/lib/lightbox/css/lightbox.min.css" rel="stylesheet">
    <link href="/client/lib/owlcarousel/assets/owl.carousel.min.css" rel="stylesheet">

    <!-- Customized Bootstrap Stylesheet -->
    <link href="/client/css/bootstrap.min.css" rel="stylesheet">

    <!-- Template Stylesheet -->
    <link href="/client/css/style.css" rel="stylesheet">

    <style>
        :root {
            --primary-color: #4A6FDC;
            --primary-dark: #3A5BBF;
            --primary-light: #EEF2FF;
            --secondary-color: #6C757D;
            --accent-color: #FF6B6B;
            --light-color: #F8F9FA;
            --dark-color: #343A40;
            --success-color: #28A745;
        }

        body {
            background-color: #F5F7FA;
        }

        .profile-card {
            border-radius: 15px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
            overflow: hidden;
            border: none;
            background-color: white;
        }

        .profile-header {
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
            color: white;
            padding: 25px;
            text-align: center;
            position: relative;
        }

        .profile-avatar {
            position: relative;
            margin-top: -50px;
            margin-bottom: 20px;
        }

        .profile-avatar img {
            width: 150px;
            height: 150px;
            object-fit: cover;
            border: 5px solid white;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
            transition: all 0.3s ease;
        }

        .avatar-upload-btn {
            position: absolute;
            bottom: 0;
            right: 0;
            background: var(--primary-color);
            color: white;
            border-radius: 50%;
            width: 40px;
            height: 40px;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            transition: all 0.3s ease;
            border: 3px solid white;
        }

        .avatar-upload-btn:hover {
            background: var(--primary-dark);
            transform: scale(1.1);
        }

        .form-control {
            border-radius: 10px;
            padding: 12px 15px;
            border: 1px solid #E1E5EA;
            transition: all 0.3s ease;
            background-color: #F9FAFC;
        }

        .form-control:focus {
            border-color: var(--primary-color);
            box-shadow: 0 0 0 0.25rem rgba(74, 111, 220, 0.25);
            background-color: white;
        }

        .input-group-text {
            background-color: var(--primary-light);
            border: 1px solid #E1E5EA;
            color: var(--primary-color);
            border-radius: 10px 0 0 10px;
        }

        .form-label {
            font-weight: 600;
            color: var(--dark-color);
            margin-bottom: 8px;
        }

        .btn-save {
            background: var(--primary-color);
            border: none;
            border-radius: 10px;
            padding: 12px 30px;
            font-weight: 600;
            transition: all 0.3s ease;
            color: white;
        }

        .btn-save:hover {
            background: var(--primary-dark);
            transform: translateY(-3px);
            box-shadow: 0 5px 15px rgba(74, 111, 220, 0.3);
        }

        .profile-preview {
            max-width: 100%;
            max-height: 200px;
            border-radius: 10px;
            margin-top: 15px;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
        }

        .user-info {
            text-align: center;
            margin-bottom: 20px;
        }

        .user-info h4 {
            margin-bottom: 5px;
            font-weight: 600;
            color: var(--dark-color);
        }

        .user-info p {
            color: var(--secondary-color);
            margin-bottom: 0;
        }

        .form-section {
            background: white;
            border-radius: 15px;
            padding: 30px;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.03);
        }

        .section-title {
            font-weight: 700;
            color: var(--dark-color);
            margin-bottom: 25px;
            padding-bottom: 10px;
            border-bottom: 2px solid var(--primary-light);
        }

        .back-to-top {
            background-color: var(--primary-color);
            border-color: var(--primary-color);
        }

        .back-to-top:hover {
            background-color: var(--primary-dark);
            border-color: var(--primary-dark);
        }

        @media (max-width: 767.98px) {
            .profile-header {
                padding: 20px;
            }

            .profile-avatar img {
                width: 120px;
                height: 120px;
            }

            .form-section {
                padding: 20px;
            }
        }
    </style>
</head>

<body>
<!-- Spinner Start -->
<div id="spinner" class="show w-100 vh-100 bg-white position-fixed translate-middle top-50 start-50 d-flex align-items-center justify-content-center">
    <div class="spinner-grow text-primary" role="status"></div>
</div>
<!-- Spinner End -->

<jsp:include page="../layout/header.jsp" />

<!-- Profile Section Start -->
<div class="container py-5">
    <div class="row justify-content-center" style="margin-top: 100px">
        <div class="col-lg-10">
            <div class="profile-card">
                <div class="profile-header">
                    <h2 class="mb-0">Thông tin tài khoản</h2>
                    <p class="mb-0">Cập nhật thông tin cá nhân của bạn</p>
                </div>

                <form:form method="post" action="/update_profile" modelAttribute="updateUser" enctype="multipart/form-data" class="p-4">
                    <div class="row">
                        <div class="col-md-4">
                            <div class="text-center">
                                <div class="profile-avatar mx-auto position-relative">
                                    <img class="rounded-circle" src="/images/avatar/${updateUser.avatar}" alt="Avatar" id="currentAvatar">
                                    <label for="avatarFile" class="avatar-upload-btn">
                                        <i class="fas fa-camera"></i>
                                    </label>
                                    <input class="d-none" type="file" id="avatarFile" accept=".png, .jpg, .jpeg" name="avatarFile" onchange="previewImage(event)" />
                                </div>

                                <div class="user-info">
                                    <h4>${updateUser.fullName}</h4>
                                    <p>${updateUser.email}</p>
                                </div>

                                <img id="avatarPreview" class="profile-preview" style="display: none;" alt="Preview" />
                            </div>
                        </div>

                        <div class="col-md-8">
                            <div class="form-section">
                                <h4 class="section-title">Thông tin cá nhân</h4>

                                <div class="row">
                                    <div class="col-md-12 mb-3">
                                        <label class="form-label" for="fullName">Họ và tên</label>
                                        <div class="input-group">
                                            <span class="input-group-text"><i class="fas fa-user"></i></span>
                                            <form:input type="text" class="form-control" path="fullName" id="fullName" />
                                        </div>
                                    </div>

                                    <div class="col-md-6 mb-3">
                                        <label class="form-label" for="phone">Số điện thoại</label>
                                        <div class="input-group">
                                            <span class="input-group-text"><i class="fas fa-phone"></i></span>
                                            <form:input type="text" class="form-control" path="phone" id="phone" placeholder="Nhập số điện thoại" />
                                        </div>
                                    </div>

                                    <div class="col-md-6 mb-3">
                                        <label class="form-label" for="email">Email</label>
                                        <div class="input-group">
                                            <span class="input-group-text"><i class="fas fa-envelope"></i></span>
                                            <form:input type="email" class="form-control" path="email" id="email" placeholder="Nhập email" />
                                        </div>
                                    </div>

                                    <div class="col-md-12 mb-4">
                                        <label class="form-label" for="address">Địa chỉ</label>
                                        <div class="input-group">
                                            <span class="input-group-text"><i class="fas fa-map-marker-alt"></i></span>
                                            <form:input type="text" class="form-control" path="address" id="address" placeholder="Nhập địa chỉ" />
                                        </div>
                                    </div>

                                    <div class="col-12 text-end">
                                        <button type="submit" class="btn btn-save">
                                            <i class="fas fa-save me-2"></i>Lưu thông tin
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </form:form>
            </div>
        </div>
    </div>
</div>
<!-- Profile Section End -->

<jsp:include page="../layout/footer.jsp" />

<!-- Back to Top -->
<a href="#" class="btn btn-primary border-3 border-primary rounded-circle back-to-top"><i class="fa fa-arrow-up"></i></a>

<!-- JavaScript Libraries -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.4/jquery.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="/client/lib/easing/easing.min.js"></script>
<script src="/client/lib/waypoints/waypoints.min.js"></script>
<script src="/client/lib/lightbox/js/lightbox.min.js"></script>
<script src="/client/lib/owlcarousel/owl.carousel.min.js"></script>

<!-- Template Javascript -->
<script src="/client/js/main.js"></script>
<script src="https://use.fontawesome.com/releases/v6.3.0/js/all.js" crossorigin="anonymous"></script>

<script>
    function previewImage(event) {
        var input = event.target;
        var preview = document.getElementById('avatarPreview');
        var currentAvatar = document.getElementById('currentAvatar');

        if (input.files && input.files[0]) {
            var reader = new FileReader();

            reader.onload = function (e) {
                preview.src = e.target.result;
                preview.style.display = "block";
                currentAvatar.style.opacity = "0.5"; // Làm mờ avatar hiện tại
            }

            reader.readAsDataURL(input.files[0]);
        }
    }
</script>
</body>

</html>