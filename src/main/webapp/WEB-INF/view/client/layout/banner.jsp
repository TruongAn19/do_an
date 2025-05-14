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
        <div class="row g-5 align-items-center">
            <div class="col-lg-7 col-md-12 pe-lg-5">
                <div class="hero-content">
                    <span class="text-secondary fw-semi-bold mb-3 d-inline-block">
                        <i class="fas fa-table-tennis me-2"></i>Sân cầu lông
                    </span>
                    <h1 class="display-4 mb-4 text-primary fw-bold">Sạch sẽ, thoáng mát, chất lượng</h1>
                    <p class="mb-4 text-secondary">Trải nghiệm sân cầu lông chất lượng cao với không gian rộng rãi, ánh sáng tốt và dịch vụ chuyên nghiệp. Đặt sân ngay hôm nay!</p>
                    <a href="/main-products" class="btn btn-primary rounded-pill py-3 px-5">
                        <i class="fas fa-calendar-check me-2"></i>Đặt sân ngay
                    </a>
                </div>
            </div>

            <div class="col-lg-5 col-md-12">
                <div class="hero-carousel-container position-relative">
                    <div class="hero-carousel-shape position-absolute" style="top: -15px; right: -15px; width: 100%; height: 100%; background-color: #3CB815; opacity: 0.1; border-radius: 10px; z-index: 0;"></div>

                    <div id="carouselId" class="carousel slide position-relative shadow-lg rounded-3 overflow-hidden" data-bs-ride="carousel">
                        <div class="carousel-inner" role="listbox">
                            <div class="carousel-item active">
                                <img src="/client/img/1.png" class="img-fluid w-100" style="height: 400px; object-fit: cover;" alt="Sân cầu lông 1">
                                <div class="carousel-caption d-none d-md-block">
                                    <div class="carousel-caption-content bg-primary bg-opacity-75 py-2 px-3 rounded">
                                        <h5 class="text-white mb-0">Sân 1</h5>
                                    </div>
                                </div>
                            </div>
                            <div class="carousel-item">
                                <img src="/client/img/2.jpg" class="img-fluid w-100" style="height: 400px; object-fit: cover;" alt="Sân cầu lông 2">
                                <div class="carousel-caption d-none d-md-block">
                                    <div class="carousel-caption-content bg-primary bg-opacity-75 py-2 px-3 rounded">
                                        <h5 class="text-white mb-0">Sân 2</h5>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <button class="carousel-control-prev" type="button" data-bs-target="#carouselId" data-bs-slide="prev">
                            <span class="carousel-control-prev-icon" aria-hidden="true"></span>
                            <span class="visually-hidden">Previous</span>
                        </button>
                        <button class="carousel-control-next" type="button" data-bs-target="#carouselId" data-bs-slide="next">
                            <span class="carousel-control-next-icon" aria-hidden="true"></span>
                            <span class="visually-hidden">Next</span>
                        </button>

                        <div class="carousel-indicators">
                            <button type="button" data-bs-target="#carouselId" data-bs-slide-to="0" class="active" aria-current="true" aria-label="Slide 1"></button>
                            <button type="button" data-bs-target="#carouselId" data-bs-slide-to="1" aria-label="Slide 2"></button>
                        </div>
                    </div>
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

    .hero-carousel-container {
        position: relative;
        z-index: 2;
    }

    .carousel-caption {
        right: auto;
        bottom: 20px;
        left: 20px;
        text-align: left;
    }

    .carousel-indicators {
        margin-bottom: 0.5rem;
    }

    .carousel-indicators button {
        width: 12px;
        height: 12px;
        border-radius: 50%;
        background-color: rgba(255, 255, 255, 0.5);
        border: none;
    }

    .carousel-indicators button.active {
        background-color: #fff;
    }

    @media (max-width: 768px) {
        .hero-header {
            padding-top: 6rem !important;
        }

        .hero-carousel-container {
            margin-top: 2rem;
        }
    }
</style>