<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập - Sân cầu lông</title>

    <!-- Google Web Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@400;500;600;700&display=swap" rel="stylesheet">

    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    <style>
        :root {
            --primary-color: #4A6FDC;
            --primary-dark: #3A5BBF;
            --primary-light: #EEF2FF;
            --secondary-color: #6C757D;
            --success-color: #28A745;
            --danger-color: #DC3545;
            --warning-color: #FFC107;
            --light-color: #F8F9FA;
            --dark-color: #343A40;
            --body-bg: #F5F7FA;
        }

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: 'Open Sans', sans-serif;
        }

        body {
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            background: var(--body-bg);
            padding: 20px;
        }

        .login-container {
            width: 100%;
            max-width: 450px;
            background: #fff;
            border-radius: 20px;
            box-shadow: 0 15px 30px rgba(0, 0, 0, 0.08);
            overflow: hidden;
        }

        .login-header {
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
            padding: 30px;
            text-align: center;
            color: white;
            position: relative;
        }

        .login-header h2 {
            font-weight: 600;
            font-size: 28px;
            margin-bottom: 10px;
        }

        .login-header p {
            font-size: 16px;
            opacity: 0.9;
        }

        .login-header .logo {
            width: 70px;
            height: 70px;
            background: white;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 15px;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
        }

        .login-header .logo i {
            font-size: 30px;
            color: var(--primary-color);
        }

        .login-form {
            padding: 30px;
        }

        .alert {
            padding: 12px 15px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 14px;
            display: flex;
            align-items: center;
        }

        .alert i {
            margin-right: 10px;
            font-size: 16px;
        }

        .alert-danger {
            background-color: rgba(220, 53, 69, 0.1);
            color: var(--danger-color);
            border-left: 4px solid var(--danger-color);
        }

        .alert-success {
            background-color: rgba(40, 167, 69, 0.1);
            color: var(--success-color);
            border-left: 4px solid var(--success-color);
        }

        .form-group {
            margin-bottom: 25px;
            position: relative;
        }

        .form-group label {
            position: absolute;
            top: 50%;
            transform: translateY(-50%);
            left: 45px;
            color: var(--secondary-color);
            pointer-events: none;
            transition: all 0.3s ease;
        }

        .form-group input {
            width: 100%;
            height: 55px;
            padding: 0 45px;
            font-size: 16px;
            border: 1px solid #E1E5EA;
            border-radius: 10px;
            outline: none;
            transition: all 0.3s ease;
            background-color: #F9FAFC;
        }

        .form-group input:focus {
            border-color: var(--primary-color);
            box-shadow: 0 0 0 3px rgba(74, 111, 220, 0.2);
            background-color: white;
        }

        .form-group input:focus + label,
        .form-group input:valid + label {
            top: 0;
            left: 15px;
            font-size: 12px;
            padding: 0 5px;
            background-color: white;
        }

        .form-group i {
            position: absolute;
            top: 50%;
            transform: translateY(-50%);
            left: 15px;
            color: var(--primary-color);
            font-size: 18px;
        }

        .forgot-password {
            text-align: right;
            margin-bottom: 20px;
        }

        .forgot-password a {
            color: var(--primary-color);
            text-decoration: none;
            font-size: 14px;
            font-weight: 500;
            transition: all 0.3s ease;
        }

        .forgot-password a:hover {
            color: var(--primary-dark);
            text-decoration: underline;
        }

        .login-btn {
            width: 100%;
            height: 55px;
            background: var(--primary-color);
            border: none;
            border-radius: 10px;
            color: white;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            margin-bottom: 20px;
        }

        .login-btn:hover {
            background: var(--primary-dark);
            transform: translateY(-3px);
            box-shadow: 0 5px 15px rgba(74, 111, 220, 0.3);
        }

        .signup-link {
            text-align: center;
            font-size: 15px;
            color: var(--secondary-color);
        }

        .signup-link a {
            color: var(--primary-color);
            text-decoration: none;
            font-weight: 600;
            transition: all 0.3s ease;
        }

        .signup-link a:hover {
            color: var(--primary-dark);
            text-decoration: underline;
        }

        @media (max-width: 480px) {
            .login-container {
                border-radius: 15px;
            }

            .login-header {
                padding: 20px;
            }

            .login-header h2 {
                font-size: 24px;
            }

            .login-form {
                padding: 20px;
            }

            .form-group input {
                height: 50px;
                font-size: 15px;
            }
        }
    </style>
</head>

<body>
<div class="login-container">
    <div class="login-header">
        <div class="logo">
            <i class="fas fa-table-tennis"></i>
        </div>
        <h2>Chào mừng trở lại</h2>
        <p>Đăng nhập để tiếp tục</p>
    </div>

    <div class="login-form">
        <form method="post" action="/login">
            <c:if test="${param.error != null}">
                <div class="alert alert-danger">
                    <i class="fas fa-exclamation-circle"></i>
                    Email hoặc mật khẩu không chính xác.
                </div>
            </c:if>

            <c:if test="${param.logout != null}">
                <div class="alert alert-success">
                    <i class="fas fa-check-circle"></i>
                    Đăng xuất thành công.
                </div>
            </c:if>

            <c:if test="${param.expired != null}">
                <div class="alert alert-warning">
                    <i class="fas fa-exclamation-triangle"></i>
                    Tài khoản của bạn đã bị đăng nhập ở một thiết bị khác.
                </div>
            </c:if>

            <div class="form-group">
                <i class="fas fa-envelope"></i>
                <input type="text" id="username" name="username" required>
                <label for="username">Địa chỉ email</label>
            </div>

            <div class="form-group">
                <i class="fas fa-lock"></i>
                <input type="password" id="password" name="password" required>
                <label for="password">Mật khẩu</label>
            </div>

            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />

            <div class="forgot-password">
                <a href="/api/forgot-password">Quên mật khẩu?</a>
            </div>

            <button type="submit" class="login-btn">Đăng nhập</button>

            <div class="signup-link">
                Chưa có tài khoản? <a href="/register">Đăng ký ngay</a>
            </div>
        </form>
    </div>
</div>
</body>

</html>