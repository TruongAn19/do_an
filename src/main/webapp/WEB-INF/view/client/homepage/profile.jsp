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
        .profile-container {
            padding: 30px 0;
            background-color: #f8f9fa;
        }

        .profile-card {
            border-radius: 15px;
            overflow: hidden;
            box-shadow: 0 0 30px rgba(0, 0, 0, 0.1);
            margin-bottom: 30px;
            background-color: #fff;
        }

        .profile-sidebar {
            background: linear-gradient(135deg, #3CB815 0%, #2d8a10 100%);
            padding: 30px;
            color: white;
            text-align: center;
            height: 100%;
        }

        .profile-image {
            width: 150px;
            height: 150px;
            border-radius: 50%;
            border: 5px solid rgba(255, 255, 255, 0.3);
            object-fit: cover;
            margin: 0 auto 20px;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
            transition: all 0.3s ease;
        }

        .profile-image:hover {
            transform: scale(1.05);
            border-color: rgba(255, 255, 255, 0.5);
        }

        .profile-name {
            font-size: 24px;
            font-weight: 600;
            margin-bottom: 10px;
        }

        .profile-edit-icon {
            background-color: rgba(255, 255, 255, 0.2);
            width: 40px;
            height: 40px;
            border-radius: 50%;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            margin-top: 15px;
            transition: all 0.3s ease;
        }

        .profile-edit-icon:hover {
            background-color: rgba(255, 255, 255, 0.4);
            transform: scale(1.1);
        }

        .profile-content {
            padding: 30px;
        }

        .profile-section-title {
            color: #3CB815;
            font-weight: 600;
            font-size: 18px;
            margin-bottom: 20px;
            padding-bottom: 10px;
            border-bottom: 1px solid #e9ecef;
        }

        .profile-info-item {
            margin-bottom: 20px;
            padding-bottom: 15px;
            border-bottom: 1px dashed #e9ecef;
        }

        .profile-info-label {
            font-weight: 600;
            color: #6c757d;
            margin-bottom: 5px;
            font-size: 14px;
        }

        .profile-info-value {
            font-size: 16px;
            color: #212529;
        }

        .profile-actions {
            display: flex;
            gap: 15px;
            margin-top: 30px;
        }

        .btn-profile {
            padding: 10px 25px;
            border-radius: 50px;
            font-weight: 500;
            transition: all 0.3s ease;
        }

        .btn-profile:hover {
            transform: translateY(-3px);
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
        }

        .btn-password {
            background-color: #3CB815;
            border-color: #3CB815;
            color: white;
        }

        .btn-password:hover {
            background-color: #2d8a10;
            border-color: #2d8a10;
        }

        .modal-content {
            border-radius: 15px;
            overflow: hidden;
        }

        .modal-header {
            background-color: #3CB815;
            color: white;
            border-bottom: none;
        }

        .modal-body {
            padding: 30px;
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-control {
            border-radius: 8px;
            padding: 12px 15px;
            border: 1px solid #ced4da;
            transition: all 0.3s ease;
        }

        .form-control:focus {
            border-color: #3CB815;
            box-shadow: 0 0 0 0.25rem rgba(60, 184, 21, 0.25);
        }

        .password-toggle {
            position: absolute;
            right: 10px;
            top: 50%;
            transform: translateY(-50%);
            cursor: pointer;
            color: #6c757d;
        }

        .password-field-wrapper {
            position: relative;
        }

        .alert-password {
            border-radius: 8px;
            padding: 15px;
            margin-bottom: 20px;
        }

        .modal-footer {
            border-top: none;
            padding: 0 30px 30px;
        }
    </style>
</head>

<body>
<!-- Spinner Start -->
<div id="spinner" class="show w-100 vh-100 bg-white position-fixed translate-middle top-50 start-50 d-flex align-items-center justify-content-center">
    <div class="spinner-grow text-success" role="status"></div>
</div>
<!-- Spinner End -->

<jsp:include page="../layout/header.jsp" />

<div class="container-fluid profile-container py-5">
    <div class="container py-5">
        <div class="row justify-content-center">
            <div class="col-lg-10">
                <div class="profile-card">
                    <div class="row g-0">
                        <div class="col-md-4">
                            <div class="profile-sidebar h-100">
                                <img src="/images/avatar/${user.avatar}" class="profile-image" alt="User Profile Image">
                                <h5 class="profile-name">${user.fullName}</h5>
                                <p class="text-white-50 mb-3">Thành viên</p>
                                <a href="/update_profile/${user.id}" class="profile-edit-icon d-inline-block">
                                    <i class="fas fa-pencil-alt text-white"></i>
                                </a>
                            </div>
                        </div>
                        <div class="col-md-8">
                            <div class="profile-content">
                                <h4 class="profile-section-title">Thông tin cá nhân</h4>

                                <div class="profile-info-item">
                                    <div class="profile-info-label">
                                        <i class="fas fa-envelope me-2"></i>Email
                                    </div>
                                    <div class="profile-info-value">${user.email}</div>
                                </div>

                                <div class="profile-info-item">
                                    <div class="profile-info-label">
                                        <i class="fas fa-phone-alt me-2"></i>Số điện thoại
                                    </div>
                                    <div class="profile-info-value">${user.phone}</div>
                                </div>

                                <div class="profile-info-item">
                                    <div class="profile-info-label">
                                        <i class="fas fa-map-marker-alt me-2"></i>Địa chỉ
                                    </div>
                                    <div class="profile-info-value">${user.address}</div>
                                </div>

                                <div class="profile-actions">
                                    <button type="button" class="btn btn-password btn-profile" data-bs-toggle="modal" data-bs-target="#changePasswordModal">
                                        <i class="fas fa-key me-2"></i>Đổi mật khẩu
                                    </button>
                                    <a href="/HomePage" class="btn btn-outline-secondary btn-profile">
                                        <i class="fas fa-arrow-left me-2"></i>Quay lại
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Modal đổi mật khẩu -->
<div class="modal fade" id="changePasswordModal" tabindex="-1" role="dialog" aria-labelledby="changePasswordModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered" role="document">
        <form id="changePasswordForm" class="modal-content">
            <input type="hidden" name="_csrf" value="${_csrf.token}"/>
            <div class="modal-header">
                <h5 class="modal-title" id="changePasswordModalLabel">
                    <i class="fas fa-lock me-2"></i>Đổi mật khẩu
                </h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button>
            </div>
            <div class="modal-body">
                <div id="errorMessage" class="alert alert-danger alert-password" style="display:none;"></div>

                <div class="form-group">
                    <label for="oldPassword" class="form-label">Mật khẩu hiện tại</label>
                    <div class="password-field-wrapper">
                        <input type="password" class="form-control" id="oldPassword" name="oldPassword" required>
                        <span class="password-toggle" onclick="togglePassword('oldPassword')">
                                <i class="fas fa-eye"></i>
                            </span>
                    </div>
                </div>

                <div class="form-group">
                    <label for="newPassword" class="form-label">Mật khẩu mới</label>
                    <div class="password-field-wrapper">
                        <input type="password" class="form-control" id="newPassword" name="newPassword" required>
                        <span class="password-toggle" onclick="togglePassword('newPassword')">
                                <i class="fas fa-eye"></i>
                            </span>
                    </div>
                    <small class="text-muted">Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số</small>
                </div>

                <div class="form-group">
                    <label for="confirmPassword" class="form-label">Xác nhận mật khẩu mới</label>
                    <div class="password-field-wrapper">
                        <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required>
                        <span class="password-toggle" onclick="togglePassword('confirmPassword')">
                                <i class="fas fa-eye"></i>
                            </span>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="submit" class="btn btn-password">
                    <i class="fas fa-check me-2"></i>Xác nhận
                </button>
                <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">
                    <i class="fas fa-times me-2"></i>Hủy
                </button>
            </div>
        </form>
    </div>
</div>

<jsp:include page="../layout/footer.jsp" />

<!-- Back to Top -->
<a href="#" class="btn btn-success border-0 rounded-circle back-to-top">
    <i class="fa fa-arrow-up"></i>
</a>

<!-- JavaScript Libraries -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.4/jquery.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="/client/lib/easing/easing.min.js"></script>
<script src="/client/lib/waypoints/waypoints.min.js"></script>
<script src="/client/lib/lightbox/js/lightbox.min.js"></script>
<script src="/client/lib/owlcarousel/owl.carousel.min.js"></script>

<!-- Template Javascript -->
<script src="/client/js/main.js"></script>

<script>
    // Toggle password visibility
    function togglePassword(inputId) {
        const input = document.getElementById(inputId);
        const icon = input.nextElementSibling.querySelector('i');

        if (input.type === "password") {
            input.type = "text";
            icon.classList.remove("fa-eye");
            icon.classList.add("fa-eye-slash");
        } else {
            input.type = "password";
            icon.classList.remove("fa-eye-slash");
            icon.classList.add("fa-eye");
        }
    }

    // Form submission with AJAX
    $(document).ready(function () {
        $('#changePasswordForm').submit(function (e) {
            e.preventDefault();  // Prevent traditional form submission
            var formData = $(this).serialize();  // Get form data

            // Show loading state
            const submitBtn = $(this).find('button[type="submit"]');
            const originalText = submitBtn.html();
            submitBtn.html('<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Đang xử lý...');
            submitBtn.prop('disabled', true);

            $.ajax({
                url: '/change-password',
                type: 'POST',
                data: formData,
                success: function(response) {
                    if (response.success) {
                        // Create success alert
                        const successAlert = $('<div class="alert alert-success alert-password" role="alert"></div>').text(response.success);

                        // Replace form with success message
                        $('.modal-body').html(successAlert);
                        $('.modal-footer').html('<button type="button" class="btn btn-success" data-bs-dismiss="modal">Đóng</button>');

                        // Redirect after delay
                        setTimeout(function () {
                            window.location.href = "/login";
                        }, 2000);
                    }
                },
                error: function(response) {
                    // Reset button state
                    submitBtn.html(originalText);
                    submitBtn.prop('disabled', false);

                    if (response.responseJSON && response.responseJSON.error) {
                        $('#errorMessage').text(response.responseJSON.error).show();

                        // Shake the error message
                        $('#errorMessage').css('animation', 'shake 0.5s');
                        setTimeout(function() {
                            $('#errorMessage').css('animation', '');
                        }, 500);
                    } else {
                        $('#errorMessage').text('Đã xảy ra lỗi. Vui lòng thử lại sau.').show();
                    }
                }
            });
        });

        // Clear error message when modal is closed
        $('#changePasswordModal').on('hidden.bs.modal', function () {
            $('#errorMessage').hide();
            $('#changePasswordForm')[0].reset();
        });
    });
</script>

<style>
    @keyframes shake {
        0%, 100% { transform: translateX(0); }
        10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }
        20%, 40%, 60%, 80% { transform: translateX(5px); }
    }
</style>
</body>

</html>