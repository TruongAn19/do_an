<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nội Quy Đặt Sân & Thuê Vợt</title>

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
            font-family: 'Inter', sans-serif;
            background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
            color: #495057;
            line-height: 1.7;
        }

        .rules-section {
            padding: 120px 0 80px;
        }

        .rules-container {
            max-width: 900px;
            margin: 0 auto;
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(10px);
            border-radius: 20px;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
            border: 1px solid rgba(255, 255, 255, 0.2);
            padding: 3rem;
            position: relative;
            overflow: hidden;
        }

        .rules-container::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 5px;
            background: linear-gradient(90deg, #667eea, #764ba2);
        }

        .page-title {
            font-size: 2.5rem;
            font-weight: 700;
            margin-bottom: 3rem;
            color: #212529;
            text-align: center;
            position: relative;
            display: inline-block;
            width: 100%;
        }

        .page-title::after {
            content: '';
            position: absolute;
            bottom: -15px;
            left: 50%;
            transform: translateX(-50%);
            width: 100px;
            height: 4px;
            background: linear-gradient(90deg, #667eea, #764ba2);
            border-radius: 2px;
        }

        .section-title {
            font-size: 1.5rem;
            font-weight: 600;
            margin: 2.5rem 0 1.5rem;
            color: #212529;
            position: relative;
            padding: 1rem 1.5rem;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border-radius: 12px;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }

        .section-title::before {
            content: '';
            width: 20px;
            height: 20px;
            background: rgba(255, 255, 255, 0.2);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
        }

        .rules-list {
            margin: 1.5rem 0;
            padding: 0;
            list-style: none;
        }

        .rules-list li {
            background: white;
            border-radius: 12px;
            padding: 1.25rem 1.5rem;
            margin-bottom: 1rem;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
            border: 1px solid #e9ecef;
            position: relative;
            padding-left: 3rem;
        }

        .rules-list li::before {
            content: '•';
            position: absolute;
            left: 1rem;
            top: 50%;
            transform: translateY(-50%);
            width: 24px;
            height: 24px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: bold;
            font-size: 1.2rem;
        }

        .rules-list li strong {
            color: #667eea;
            font-weight: 600;
        }

        .note {
            background: linear-gradient(135deg, #ffeaa7 0%, #fab1a0 100%);
            color: #2d3436;
            padding: 1.5rem;
            border-radius: 12px;
            margin-top: 3rem;
            font-weight: 500;
            text-align: center;
            position: relative;
            border: 2px solid rgba(255, 255, 255, 0.3);
        }

        .note::before {
            content: '⚠️';
            font-size: 1.5rem;
            margin-right: 0.5rem;
        }

        .back-to-top {
            position: fixed;
            bottom: 30px;
            right: 30px;
            width: 50px;
            height: 50px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border-radius: 50%;
            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
            z-index: 999;
            transition: all 0.3s ease;
            border: none;
            text-decoration: none;
        }

        .back-to-top:hover {
            transform: translateY(-5px);
            box-shadow: 0 10px 20px rgba(102, 126, 234, 0.6);
            color: white;
        }

        .icon-decoration {
            position: absolute;
            top: 20px;
            right: 20px;
            font-size: 3rem;
            color: rgba(102, 126, 234, 0.1);
            z-index: 1;
        }

        @media (max-width: 992px) {
            .rules-section {
                padding: 100px 0 60px;
            }

            .rules-container {
                padding: 2rem;
                margin: 0 1rem;
            }

            .page-title {
                font-size: 2.2rem;
            }
        }

        @media (max-width: 768px) {
            .rules-section {
                padding: 80px 0 40px;
            }

            .page-title {
                font-size: 2rem;
            }

            .section-title {
                font-size: 1.3rem;
                padding: 0.8rem 1rem;
            }

            .rules-list li {
                padding: 1rem 1rem 1rem 2.5rem;
            }

            .rules-list li::before {
                left: 0.5rem;
                width: 20px;
                height: 20px;
            }
        }

        @media (max-width: 576px) {
            .rules-container {
                padding: 1.5rem;
                border-radius: 15px;
            }

            .page-title {
                font-size: 1.8rem;
            }

            .section-title {
                font-size: 1.2rem;
            }

            .note {
                padding: 1rem;
            }
        }
    </style>
</head>
<body>
<jsp:include page="../layout/header.jsp"/>

<section class="rules-section">
    <div class="container">
        <div class="rules-container">
            <div class="icon-decoration">
                <i class="fas fa-clipboard-list"></i>
            </div>

            <h1 class="page-title">🏸 Nội Quy Đặt Sân & Thuê Vợt</h1>

            <h2 class="section-title">
                <i class="fas fa-calendar-check"></i>
                I. Đặt Sân Cầu Lông
            </h2>
            <ul class="rules-list">
                <li>Đặt sân trước tối thiểu <strong>1 tiếng</strong> so với giờ chơi mong muốn.</li>
                <li>Thời gian sử dụng tối thiểu mỗi lượt đặt sân là <strong>1 giờ</strong>.</li>
                <li>Vui lòng đến đúng giờ. Sau <strong>15 phút</strong> không đến sẽ bị <strong>hủy sân không hoàn tiền</strong>.</li>
                <li>Người dùng muốn huỷ sân hãy liên hệ cho số điện thoai ở phần giới thiệu trước <strong>2 tiếng.</strong> Nếu huỷ muộn sẽ không được giải quyết.</li>
            </ul>

            <h2 class="section-title">
                <i class="fas fa-table-tennis"></i>
                II. Thuê Vợt
            </h2>
            <ul class="rules-list">
                <li>Vợt cho thuê được kiểm tra trước khi giao.</li>
                <li>Người thuê chịu trách nhiệm nếu vợt bị hỏng do sử dụng sai cách.</li>
                <li>Phí thuê vợt theo từng loại. Thời gian thuê tính theo giờ đặt sân hoặc số ngày đã đặt</li>
                <li>Trả vợt đúng nơi quy định sau khi sử dụng xong.</li>
            </ul>

            <p class="note">* Vui lòng tuân thủ nội quy để đảm bảo công bằng và trải nghiệm tốt nhất cho tất cả người chơi.</p>
        </div>
    </div>
</section>

<jsp:include page="../layout/footer.jsp"/>
<!-- Back to Top -->
<a href="#" class="back-to-top"><i class="fa fa-arrow-up"></i></a>

<!-- JavaScript Libraries -->
<script src="https://code.jquery.com/jquery-3.4.1.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="/client/lib/easing/easing.min.js"></script>
<script src="/client/lib/waypoints/waypoints.min.js"></script>
<script src="/client/lib/lightbox/js/lightbox.min.js"></script>
<script src="/client/lib/owlcarousel/owl.carousel.min.js"></script>

<!-- Template Javascript -->
<script src="/client/js/main.js"></script>
</body>
</html>