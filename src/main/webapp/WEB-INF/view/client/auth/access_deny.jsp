<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

            <!DOCTYPE html>
            <html lang="vi">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Truy cập bị từ chối</title>
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
                <style>
                    body {
                        background-color: #f8d7da;
                    }

                    .container {
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        height: 100vh;
                    }

                    .card {
                        max-width: 500px;
                        text-align: center;
                    }
                </style>
            </head>

            <body>
                <div class="container">
                    <div class="card shadow-lg border-danger">
                        <div class="card-header bg-danger text-white">
                            <h3>Truy cập bị từ chối</h3>
                        </div>
                        <div class="card-body">
                            <p class="fs-5">Bạn không có quyền truy cập vào trang này.</p>
                            <a href="/goHome" class="btn btn-primary">Quay lại trang chủ</a>
                        </div>
                    </div>
                </div>
            </body>

            </html>