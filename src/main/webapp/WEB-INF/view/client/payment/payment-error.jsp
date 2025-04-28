<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<html>
<head>
    <title>Thanh toán thất bại</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #f0f4f7;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
        }
        .container {
            background: #fff;
            width: 400px;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
            text-align: center;
            transition: all 0.3s ease-in-out;
        }
        .container:hover {
            box-shadow: 0 6px 25px rgba(0, 0, 0, 0.15);
        }
        h1 {
            color: #F44336;
            margin-bottom: 20px;
            font-size: 24px;
        }
        .message {
            color: #555;
            font-size: 18px;
            margin-bottom: 20px;
        }
        .btn {
            display: inline-block;
            background-color: #F44336;
            color: white;
            padding: 10px 20px;
            border-radius: 5px;
            text-decoration: none;
            font-size: 16px;
            transition: background-color 0.3s ease;
        }
        .btn:hover {
            background-color: #e53935;
        }
        .icon {
            font-size: 60px;
            color: #F44336;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="icon">&#10060;</div> <!-- Biểu tượng dấu x -->
    <h1>Thanh toán thất bại!</h1>
    <p class="message">${message}</p>
    <a href="/HomePage" class="btn">Quay lại trang chủ</a>
</div>
</body>
</html>
