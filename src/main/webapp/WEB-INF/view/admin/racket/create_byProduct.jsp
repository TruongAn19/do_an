<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no" />
    <title>Create Racket - Sân cầu lông</title>
    <link href="/css/styles.css" rel="stylesheet" />
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>
    <script>
        $(document).ready(() => {
            $("#racketImg").change(function (e) {
                const imgURL = URL.createObjectURL(e.target.files[0]);
                $("#avatarPreview").attr("src", imgURL).show();
            });
        });
    </script>
    <script src="https://use.fontawesome.com/releases/v6.3.0/js/all.js" crossorigin="anonymous"></script>
</head>

<body class="sb-nav-fixed">
<jsp:include page="../layout/header.jsp" />
<div id="layoutSidenav">
    <jsp:include page="../layout/sidebar.jsp" />
    <div id="layoutSidenav_content">
        <main>
            <div class="container-fluid px-4">
                <h1 class="mt-4">Manage Racket</h1>
                <ol class="breadcrumb mb-4">
                    <li class="breadcrumb-item"><a href="/admin">Dashboard</a></li>
                    <li class="breadcrumb-item active">Racket</li>
                </ol>
                <div class="mt-5">
                    <div class="row">
                        <div class="col-md-10 col-12 mx-auto">
                            <h3>Create a Racket</h3>
                            <hr />
                            <form:form method="post" action="/admin/racket/create"
                                       modelAttribute="newRacket" enctype="multipart/form-data">
                                <input type="hidden" name="productType" value="byProduct" />

                                <div class="row">
                                    <div class="col-md-6 mb-3">
                                        <label class="form-label">Name:</label>
                                        <form:input type="text" class="form-control" path="name" />
                                    </div>

                                    <div class="col-md-6 mb-3">
                                        <label class="form-label">Factory:</label>
                                        <form:input type="text" class="form-control" path="factory" />
                                    </div>

                                    <div class="col-md-6 mb-3">
                                        <label class="form-label">Price:</label>
                                        <form:input type="number" step="0.01" class="form-control" path="price" />
                                    </div>

                                    <div class="col-md-6 mb-3">
                                        <label class="form-label">Giá thuê tại sân:</label>
                                        <form:input type="number" step="0.01" class="form-control" path="rentalPricePerPlay" />
                                    </div>

                                    <div class="col-md-6 mb-3">
                                        <label class="form-label">Giá thuê theo ngày:</label>
                                        <form:input type="number" step="0.01" class="form-control" path="rentalPricePerDay" />
                                    </div>

                                    <div class="col-md-6 mb-3">
                                        <label class="form-label">Quantity:</label>
                                        <form:input type="number" class="form-control" path="quantity" />
                                    </div>

                                    <div class="col-md-6 mb-3 d-flex align-items-center">
                                        <div class="form-check">
                                            <form:checkbox class="form-check-input" path="available" id="available"/>
                                            <label class="form-check-label ms-2" for="available">Available</label>
                                        </div>
                                    </div>

                                    <div class="col-md-6 mb-3">
                                        <label class="form-label">Product:</label>
                                        <form:select path="product.id" class="form-control">
                                            <form:options items="${productList}" itemValue="id" itemLabel="name" />
                                        </form:select>
                                    </div>

                                    <div class="col-md-6 mb-3">
                                        <label for="racketImg" class="form-label">Image:</label>
                                        <input class="form-control" type="file" id="racketImg"
                                               accept=".png, .jpg, .jpeg. webp" name="racketImg" />
                                    </div>


                                </div>

                                <div class="text-center">
                                    <button type="submit" class="btn btn-primary px-5">Create</button>
                                </div>
                            </form:form>
                        </div>
                    </div>
                </div>
            </div>
        </main>
        <jsp:include page="../layout/footer.jsp" />
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"
        crossorigin="anonymous"></script>
<script src="/js/scripts.js"></script>
</body>

</html>
