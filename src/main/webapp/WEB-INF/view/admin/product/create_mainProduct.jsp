<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no" />
    <meta name="description" content="Dự án sancaulong" />
    <meta name="author" content="TruongAn" />
    <title>Create Product - Sân cầu lông</title>
    <link href="/css/styles.css" rel="stylesheet" />
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>
    <script>
        $(document).ready(() => {
            const avatarFile = $("#avatarFile");
            avatarFile.change(function (e) {
                const imgURL = URL.createObjectURL(e.target.files[0]);
                $("#avatarPreview").attr("src", imgURL).css({ "display": "block" });
            });
        });

        // Function to add selected time slot to the input field

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
                <h1 class="mt-4">Manage Product</h1>
                <ol class="breadcrumb mb-4">
                    <li class="breadcrumb-item"><a href="/admin">Dashboard</a></li>
                    <li class="breadcrumb-item active">Product</li>
                </ol>
                <div class="mt-5">
                    <div class="row">
                        <div class="col-md-8 col-12 mx-auto">
                            <h3>Create a Product</h3>
                            <hr />
                            <form:form method="post" enctype="multipart/form-data" action="/admin/product/create" modelAttribute="newProduct" class="row">
                                <input type="hidden" name="productType" value="mainProduct" />

                                <div class="mb-3 col-12 col-md-6">
                                    <label class="form-label">Name:</label>
                                    <form:input type="text" class="form-control" path="name" />
                                </div>

                                <div class="mb-3 col-12 col-md-6">
                                    <label class="form-label">Price:</label>
                                    <form:input type="text" class="form-control" path="price" />
                                </div>

                                <div class="mb-3 col-12 col-md-6">
                                    <label class="form-label">Deposit Price:</label>
                                    <form:input type="text" class="form-control" path="depositPrice" />
                                </div>

                                <div class="mb-3 col-12">
                                    <label class="form-label">Detail description:</label>
                                    <form:textarea class="form-control" path="detailDesc" />
                                </div>

                                <div class="mb-3 col-12 col-md-6">
                                    <label class="form-label">Short description:</label>
                                    <form:input type="text" class="form-control" path="shortDesc" />
                                </div>

                                <div class="mb-3 col-12 col-md-6">
                                    <label class="form-label">Quantity:</label>
                                    <form:input type="text" class="form-control" path="quantity" />
                                </div>

                                <div class="mb-3 col-12 col-md-6">
                                    <label class="form-label">Sale:</label>
                                    <form:input type="text" class="form-control" path="sale" />
                                </div>

<%--                                --%>

                                <div class="mb-3 col-12">
                                    <label class="form-label">Address:</label>
                                    <form:input type="text" class="form-control" path="address" />
                                </div>

                                <div class="mb-3 col-12 col-md-6">
                                    <label for="avatarFile" class="form-label">Image:</label>
                                    <input class="form-control" type="file" id="avatarFile" accept=".png, .jpg, .jpeg" name="productImg" />
                                </div>

                                <div class="col-12 mb-3">
                                    <img style="max-height: 250px; display: none;" alt="avatar preview" id="avatarPreview" />
                                </div>

                                <div class="col-12 mb-5">
                                    <button type="submit" class="btn btn-primary">Create</button>
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

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js" crossorigin="anonymous"></script>
<script src="/js/scripts.js"></script>
</body>

</html>
