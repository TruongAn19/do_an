<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đặt lại mật khẩu</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        body {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }

        .reset-password-container {
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            padding: 20px;
        }

        .reset-password-card {
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(10px);
            border-radius: 20px;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
            border: 1px solid rgba(255, 255, 255, 0.2);
            max-width: 450px;
            width: 100%;
            overflow: hidden;
        }

        .card-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            text-align: center;
            padding: 2rem 1.5rem 1.5rem;
            border: none;
        }

        .card-header i {
            font-size: 3rem;
            margin-bottom: 1rem;
            opacity: 0.9;
        }

        .card-header h2 {
            margin: 0;
            font-weight: 600;
            font-size: 1.5rem;
        }

        .card-header p {
            margin: 0.5rem 0 0;
            opacity: 0.9;
            font-size: 0.9rem;
        }

        .card-body {
            padding: 2rem 1.5rem;
        }

        .form-floating {
            margin-bottom: 1.5rem;
        }

        .form-control {
            border: 2px solid #e9ecef;
            border-radius: 12px;
            padding: 1rem 0.75rem;
            font-size: 1rem;
            transition: all 0.3s ease;
        }

        .form-control:focus {
            border-color: #667eea;
            box-shadow: 0 0 0 0.2rem rgba(102, 126, 234, 0.25);
        }

        .btn-submit {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border: none;
            border-radius: 12px;
            padding: 0.875rem 2rem;
            font-weight: 600;
            font-size: 1rem;
            width: 100%;
            transition: all 0.3s ease;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .btn-submit:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 25px rgba(102, 126, 234, 0.3);
        }

        .btn-submit:active {
            transform: translateY(0);
        }

        .alert-message {
            background: linear-gradient(135deg, #ff6b6b 0%, #ee5a52 100%);
            color: white;
            border: none;
            border-radius: 12px;
            padding: 1rem;
            margin-bottom: 1.5rem;
            font-weight: 500;
            text-align: center;
        }

        .password-requirements {
            background: #f8f9fa;
            border-radius: 12px;
            padding: 1rem;
            margin-bottom: 1.5rem;
            font-size: 0.875rem;
            color: #6c757d;
        }

        .password-requirements h6 {
            color: #495057;
            margin-bottom: 0.5rem;
            font-weight: 600;
        }

        .password-requirements ul {
            margin: 0;
            padding-left: 1.2rem;
        }

        .password-requirements li {
            margin-bottom: 0.25rem;
        }

        .security-info {
            background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
            color: white;
            border-radius: 12px;
            padding: 1rem;
            margin-bottom: 1.5rem;
            text-align: center;
            font-size: 0.875rem;
        }

        .back-link {
            text-align: center;
            margin-top: 1.5rem;
        }

        .back-link a {
            color: #667eea;
            text-decoration: none;
            font-weight: 500;
            transition: color 0.3s ease;
        }

        .back-link a:hover {
            color: #764ba2;
        }

        @media (max-width: 576px) {
            .reset-password-container {
                padding: 10px;
            }

            .card-header {
                padding: 1.5rem 1rem 1rem;
            }

            .card-body {
                padding: 1.5rem 1rem;
            }
        }
    </style>
</head>

<body>
<div class="reset-password-container">
    <div class="reset-password-card">
        <div class="card-header">
            <i class="fas fa-shield-alt"></i>
            <h2>Đặt lại mật khẩu</h2>
            <p>Tạo mật khẩu mới cho tài khoản của bạn</p>
        </div>

        <div class="card-body">
            <div class="security-info">
                <i class="fas fa-lock me-2"></i>
                Liên kết đặt lại mật khẩu hợp lệ. Vui lòng tạo mật khẩu mới.
            </div>

            <form action="reset-password" method="post">
                <input type="hidden" name="token" value="${token}" />

                <c:if test="${not empty message}">
                    <div class="alert-message">
                        <i class="fas fa-exclamation-triangle me-2"></i>
                            ${message}
                    </div>
                </c:if>

                <div class="password-requirements">
                    <h6><i class="fas fa-info-circle me-2"></i>Yêu cầu mật khẩu:</h6>
                    <ul>
                        <li>Ít nhất 8 ký tự</li>
                        <li>Bao gồm chữ hoa và chữ thường</li>
                        <li>Có ít nhất 1 số</li>
                        <li>Có ít nhất 1 ký tự đặc biệt</li>
                    </ul>
                </div>

                <div class="form-floating">
                    <input type="password"
                           class="form-control"
                           id="password"
                           name="password"
                           placeholder="Nhập mật khẩu mới"
                           required>
                    <label for="password">
                        <i class="fas fa-key me-2"></i>Mật khẩu mới
                    </label>
                </div>

                <button type="submit" class="btn btn-primary btn-submit">
                    <i class="fas fa-check me-2"></i>
                    Xác nhận
                </button>
            </form>

            <div class="back-link">
                <a href="/login">
                    <i class="fas fa-arrow-left me-1"></i>
                    Quay lại đăng nhập
                </a>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>

</html>