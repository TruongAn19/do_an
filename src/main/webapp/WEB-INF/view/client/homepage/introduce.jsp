<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Giới Thiệu Sân Cầu Lông</title>

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

        .about-section {
            padding: 120px 0 80px;
        }

        .about-container {
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(10px);
            border-radius: 20px;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
            border: 1px solid rgba(255, 255, 255, 0.2);
            padding: 3rem;
            margin-bottom: 3rem;
            position: relative;
            overflow: hidden;
        }

        .about-container::before {
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
            margin-bottom: 2rem;
            color: #212529;
            position: relative;
            display: inline-block;
        }

        .page-title::after {
            content: '';
            position: absolute;
            bottom: -10px;
            left: 0;
            width: 80px;
            height: 4px;
            background: linear-gradient(90deg, #667eea, #764ba2);
            border-radius: 2px;
        }

        .intro-text {
            font-size: 1.1rem;
            margin-bottom: 2rem;
            color: #6c757d;
        }

        .highlight-box {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 2rem;
            border-radius: 15px;
            margin: 2.5rem 0;
            position: relative;
            overflow: hidden;
        }

        .highlight-box::before {
            content: '"';
            position: absolute;
            top: -20px;
            left: 20px;
            font-size: 150px;
            opacity: 0.1;
            font-family: serif;
            color: white;
        }

        .highlight-box strong {
            font-size: 1.2rem;
            display: block;
            margin-bottom: 0.5rem;
        }

        .section-title {
            font-size: 1.8rem;
            font-weight: 600;
            margin: 3rem 0 1.5rem;
            color: #212529;
            position: relative;
            padding-left: 15px;
            border-left: 5px solid #667eea;
        }

        .services-list {
            margin: 2rem 0;
        }

        .service-item {
            background: white;
            border-radius: 12px;
            padding: 1.5rem;
            margin-bottom: 1rem;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
            border: 1px solid #e9ecef;
            display: flex;
            align-items: flex-start;
            gap: 1rem;
        }

        .service-icon {
            font-size: 1.5rem;
            color: #667eea;
            flex-shrink: 0;
            margin-top: 0.2rem;
        }

        .service-content {
            flex: 1;
        }

        .service-text {
            margin: 0;
            color: #495057;
            font-size: 1rem;
        }

        .contact-card {
            background: white;
            border-radius: 15px;
            padding: 2rem;
            box-shadow: 0 10px 20px rgba(0, 0, 0, 0.05);
            margin: 2rem 0;
            border: 1px solid #e9ecef;
        }

        .contact-item {
            display: flex;
            align-items: center;
            margin-bottom: 1rem;
        }

        .contact-icon {
            width: 40px;
            height: 40px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 1rem;
            flex-shrink: 0;
        }

        .thank-you {
            font-size: 1.1rem;
            font-weight: 500;
            text-align: center;
            margin: 3rem 0 1rem;
            color: #495057;
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
        }

        .back-to-top:hover {
            transform: translateY(-5px);
            box-shadow: 0 10px 20px rgba(102, 126, 234, 0.6);
        }

        .image-gallery {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
            gap: 1rem;
            margin: 2rem 0;
        }

        .gallery-item {
            border-radius: 10px;
            overflow: hidden;
            height: 200px;
            position: relative;
        }

        .gallery-item img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            transition: transform 0.5s ease;
        }

        .gallery-item:hover img {
            transform: scale(1.05);
        }

        @media (max-width: 992px) {
            .about-section {
                padding: 100px 0 60px;
            }

            .about-container {
                padding: 2rem;
            }

            .page-title {
                font-size: 2.2rem;
            }
        }

        @media (max-width: 768px) {
            .about-section {
                padding: 80px 0 40px;
            }

            .page-title {
                font-size: 2rem;
            }

            .highlight-box {
                padding: 1.5rem;
            }

            .service-item {
                flex-direction: column;
                text-align: center;
                gap: 0.5rem;
            }

            .service-icon {
                margin-top: 0;
            }
        }

        @media (max-width: 576px) {
            .about-container {
                padding: 1.5rem;
                border-radius: 15px;
            }

            .page-title {
                font-size: 1.8rem;
            }

            .section-title {
                font-size: 1.5rem;
            }
        }
    </style>
</head>
<body>
<jsp:include page="../layout/header.jsp"/>

<section class="about-section">
    <div class="container">
        <div class="about-container">
            <h1 class="page-title">🎉 Chào mừng đến với web đặt sân cầu lông</h1>

            <p class="intro-text">
                Sân Cầu Lông ABC là điểm đến lý tưởng cho những ai yêu thích môn thể thao cầu lông. Với không gian rộng rãi, cơ sở vật chất hiện đại và đội ngũ nhân viên thân thiện, chúng tôi cam kết mang đến trải nghiệm tuyệt vời nhất cho bạn.
            </p>

            <div class="highlight-box">
                <strong>Mục tiêu của chúng tôi:</strong>
                Cung cấp môi trường chơi cầu lông chuyên nghiệp, an toàn, và thân thiện cho mọi lứa tuổi.
            </div>

            <h2 class="section-title">Dịch Vụ Của Chúng Tôi</h2>

            <div class="services-list">
                <div class="service-item">
                    <span class="service-icon">⏱️</span>
                    <div class="service-content">
                        <p class="service-text">Đặt sân nhanh chóng, tiện lợi qua hệ thống online.</p>
                    </div>
                </div>

                <div class="service-item">
                    <span class="service-icon">🏸</span>
                    <div class="service-content">
                        <p class="service-text">Cho thuê vợt và dụng cụ thi đấu chất lượng cao.</p>
                    </div>
                </div>

                <div class="service-item">
                    <span class="service-icon">👨‍🏫</span>
                    <div class="service-content">
                        <p class="service-text">Có thể giao lưu dễ dàng với những người yêu thích cầu lông.</p>
                    </div>
                </div>

                <div class="service-item">
                    <span class="service-icon">💡</span>
                    <div class="service-content">
                        <p class="service-text">Hệ thống đèn chiếu sáng đạt chuẩn, có thể chơi buổi tối.</p>
                    </div>
                </div>
            </div>

            <h2 class="section-title">Thông Tin Liên Hệ</h2>

            <div class="contact-card">
                <div class="contact-item">
                    <div class="contact-icon">
                        <i class="fas fa-map-marker-alt"></i>
                    </div>
                    <div>
                        <strong>Địa chỉ:</strong><br>
                        ngõ 83, Tân Triều, Thanh Trì, Hà Nội
                    </div>
                </div>

                <div class="contact-item">
                    <div class="contact-icon">
                        <i class="fas fa-phone-alt"></i>
                    </div>
                    <div>
                        <strong>Hotline:</strong><br>
                        0909 123 456
                    </div>
                </div>

                <div class="contact-item">
                    <div class="contact-icon">
                        <i class="fas fa-envelope"></i>
                    </div>
                    <div>
                        <strong>Email:</strong><br>
                        sancaulongabc@gmail.com
                    </div>
                </div>
            </div>

            <p class="thank-you">Cảm ơn bạn đã tin tưởng và đồng hành cùng chúng tôi! Chúc bạn có những giờ phút chơi thể thao vui vẻ và bổ ích!</p>
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