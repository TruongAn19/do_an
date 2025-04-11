<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


                <!DOCTYPE html>
                <html lang="en">

                <head>
                    <meta charset="utf-8">
                    <title> Tài khoản cá nhân - Sân cầu lông</title>
                    <meta content="width=device-width, initial-scale=1.0" name="viewport">
                    <meta content="" name="keywords">
                    <meta content="" name="description">
                    <link rel="stylesheet" href="/client/css/profile_update.css">
                    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css">
                    <link rel="stylesheet"
                        href="https://stackpath.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.bundle.min.js">
                    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.min.js">
                    <link rel="stylesheet"
                        href="https://cdnjs.cloudflare.com/ajax/libs/MaterialDesign-Webfont/3.6.95/css/materialdesignicons.css">

                    <!-- Google Web Fonts -->
                    <link rel="preconnect" href="https://fonts.googleapis.com">
                    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                    <link
                        href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@400;600&family=Raleway:wght@600;800&display=swap"
                        rel="stylesheet">
                    <!-- Icon Font Stylesheet -->
                    <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.15.4/css/all.css" />
                    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.4.1/font/bootstrap-icons.css"
                        rel="stylesheet">
                    <!-- Libraries Stylesheet -->
                    <link href="/client/lib/lightbox/css/lightbox.min.css" rel="stylesheet">
                    <link href="/client/lib/owlcarousel/assets/owl.carousel.min.css" rel="stylesheet">
                    <!-- Customized Bootstrap Stylesheet -->
                    <link href="/client/css/bootstrap.min.css" rel="stylesheet">
                    <!-- Template Stylesheet -->
                    <link href="/client/css/style.css" rel="stylesheet">
                </head>

                <body>

                    <!-- Spinner Start -->
                    <div id="spinner"
                        class="show w-100 vh-100 bg-white position-fixed translate-middle top-50 start-50  d-flex align-items-center justify-content-center">
                        <div class="spinner-grow text-primary" role="status"></div>
                    </div>
                    <!-- Spinner End -->

                    <jsp:include page="../layout/header.jsp" />

                    <div class="container rounded bg-white mt-5 mb-5">
                        <form:form method="post" action="/update_profile" modelAttribute="updateUser"
                            enctype="multipart/form-data">
                            <div class="row">
                                <div class="col-md-5 border-right">
                                    <div class="d-flex flex-column align-items-center text-center p-3 py-5">
                                        <img class="rounded-circle mt-5" width="150px"
                                            src="/images/avatar/${updateUser.avatar}">
                                        <span class="font-weight-bold">${updateUser.fullName}</span>
                                        <span class="text-black-50">${updateUser.email}</span>
                                        <div class="mb-3 col-12 col-md-6">
                                            
                                            <!-- Ẩn input file gốc -->
                                            <input class="form-control d-none" type="file" id="avatarFile"
                                                accept=".png, .jpg, .jpeg" name="avatarFile"
                                                onchange="previewImage(event)" />
                                            <!-- Nút chọn ảnh tùy chỉnh -->
                                            <button type="button" class="btn btn-outline-primary"
                                                onclick="document.getElementById('avatarFile').click()">
                                                <i class="fas fa-upload"></i> Chọn ảnh
                                            </button>
                                            <!-- Hiển thị ảnh xem trước -->
                                            <div class="col-12 mt-3">
                                                <img style="max-height: 250px; display: none; border-radius: 10px;"
                                                    alt="avatar preview" id="avatarPreview" />
                                            </div>
                                        </div>

                                    </div>
                                </div>
                                <div class="col-md-5 border-right" style="margin-top: 50px;">
                                    <div class="mb-3">
                                        <label class="form-label">Full Name:</label>
                                        <form:input type="text" class="form-control" path="fullName" />
                                    </div>

                                    <div class="mb-3">
                                        <label class="form-label">Mobile Number:</label>
                                        <form:input type="text" class="form-control" path="phone"
                                            placeholder="enter phone number" />
                                    </div>
                                    <div class="mb-3">
                                        <label class="form-label">Address:</label>
                                        <form:input type="text" class="form-control" path="address"
                                            placeholder="enter address line 1" />
                                    </div>
                                    <div class="mb-3">
                                        <label class="form-label">Email:</label>
                                        <form:input type="email" class="form-control" path="email"
                                            placeholder="enter email" />
                                    </div>
                                    <button type="submit" class="btn btn-primary">Save Profile</button>

                                </div>
                            </div>
                        </form:form>
                    </div>


                    <jsp:include page="../layout/footer.jsp" />


                    <!-- Back to Top -->
                    <a href="#" class="btn btn-primary border-3 border-primary rounded-circle back-to-top"><i
                            class="fa fa-arrow-up"></i></a>


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
                        function previewImage(event) {
                            var input = event.target;
                            var preview = document.getElementById('avatarPreview');

                            if (input.files && input.files[0]) {
                                var reader = new FileReader();

                                reader.onload = function (e) {
                                    preview.src = e.target.result;
                                    preview.style.display = "block";
                                }

                                reader.readAsDataURL(input.files[0]);
                            }
                        }

                    </script>
                    <script src="https://use.fontawesome.com/releases/v6.3.0/js/all.js"
                        crossorigin="anonymous"></script>
                </body>

                </html>