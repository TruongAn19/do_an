<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!-- Hero Start -->
<div class="container-fluid hero-header py-5 mb-5 position-relative overflow-hidden">
    <div class="hero-shape position-absolute">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1440 320">
            <path fill="#3CB815" fill-opacity="0.1" d="M0,288L48,272C96,256,192,224,288,197.3C384,171,480,149,576,165.3C672,181,768,235,864,250.7C960,267,1056,245,1152,224C1248,203,1344,181,1392,170.7L1440,160L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"></path>
        </svg>
    </div>

    <div class="container py-5">
        <div class="row justify-content-center">
            <div class="col-lg-10 col-md-12">
                <div class="hero-content text-center">
                    <span class="text-secondary fw-semi-bold mb-3 d-inline-block">
                        <i class="fas fa-table-tennis me-2"></i>Sân cầu lông
                    </span>
                    <h1 class="display-4 mb-4 text-primary fw-bold">Sạch sẽ, thoáng mát, chất lượng</h1>
                    <p class="mb-4 text-secondary mx-auto" style="max-width: 700px;">Trải nghiệm sân cầu lông chất lượng cao với không gian rộng rãi, ánh sáng tốt và dịch vụ chuyên nghiệp. Đặt sân ngay hôm nay!</p>

                    <div class="hero-features mb-4">
                        <div class="row g-3 justify-content-center">
                            <div class="col-lg-3 col-md-6 col-sm-6">
                                <div class="feature-item bg-light rounded p-3 text-center">
                                    <i class="fas fa-lightbulb text-primary mb-2"></i>
                                    <p class="mb-0">Ánh sáng tốt</p>
                                </div>
                            </div>
                            <div class="col-lg-3 col-md-6 col-sm-6">
                                <div class="feature-item bg-light rounded p-3 text-center">
                                    <i class="fas fa-wind text-primary mb-2"></i>
                                    <p class="mb-0">Thông thoáng</p>
                                </div>
                            </div>
                            <div class="col-lg-3 col-md-6 col-sm-6">
                                <div class="feature-item bg-light rounded p-3 text-center">
                                    <i class="fas fa-broom text-primary mb-2"></i>
                                    <p class="mb-0">Sạch sẽ</p>
                                </div>
                            </div>
                            <div class="col-lg-3 col-md-6 col-sm-6">
                                <div class="feature-item bg-light rounded p-3 text-center">
                                    <i class="fas fa-headset text-primary mb-2"></i>
                                    <p class="mb-0">Hỗ trợ 24/7</p>
                                </div>
                            </div>
                        </div>
                    </div>

                    <a href="/main-products" class="btn btn-primary rounded-pill py-3 px-5">
                        <i class="fas fa-calendar-check me-2"></i>Đặt sân ngay
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>
<!-- Hero End -->

<style>
    .hero-header {
        background-color: #f8f9fa;
        position: relative;
        overflow: hidden;
        padding: 80px 0;
    }

    .hero-shape {
        bottom: 0;
        left: 0;
        width: 100%;
        z-index: 1;
    }

    .hero-content {
        position: relative;
        z-index: 2;
    }

    .hero-content h1 {
        font-size: 3.5rem;
        margin-bottom: 1.5rem;
    }

    .hero-content p {
        font-size: 1.1rem;
        line-height: 1.8;
    }

    .feature-item {
        transition: all 0.3s ease;
        border: 1px solid rgba(0, 0, 0, 0.05);
        height: 100%;
    }

    .feature-item:hover {
        transform: translateY(-3px);
        box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
    }

    .feature-item i {
        font-size: 2rem;
        display: block;
        margin-bottom: 0.5rem;
    }

    .btn-primary {
        transition: all 0.3s ease;
        font-weight: 600;
        letter-spacing: 0.5px;
    }

    .btn-primary:hover {
        transform: translateY(-3px);
        box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
    }

    @media (max-width: 768px) {
        .hero-header {
            padding-top: 6rem !important;
        }

        .hero-content h1 {
            font-size: 2.5rem;
        }
    }

    @media (max-width: 576px) {
        .hero-content h1 {
            font-size: 2rem;
        }

        .feature-item {
            padding: 0.75rem;
        }

        .feature-item i {
            font-size: 1.5rem;
        }
    }
</style>