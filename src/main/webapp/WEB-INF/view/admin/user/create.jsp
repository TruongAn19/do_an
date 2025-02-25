<%@page contentType="text/html" pageEncoding="UTF-8" %>
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
                <title>Create User - Hỏi Dân IT</title>
                <link href="/css/styles.css" rel="stylesheet" />
                <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>
                <script>
                    $(document).ready(() => {
                        const avatarFile = $("#avatarFile");
                        avatarFile.change(function (e) {
                            const imgURL = URL.createObjectURL(e.target.files[0]);
                            $("#avatarPreview").attr("src", imgURL);
                            $("#avatarPreview").css({ "display": "block" });
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
                                <h1 class="mt-4">Manage Users</h1>
                                <ol class="breadcrumb mb-4">
                                    <li class="breadcrumb-item"><a href="/admin">Dashboard</a></li>
                                    <li class="breadcrumb-item active">Users</li>
                                </ol>
                                <div class="mt-5">
                                    <div class="row">
                                        <div class="col-md-6 col-12 mx-auto">
                                            <h3>Create a user</h3>
                                            <hr />
                                            <form:form method="post" enctype="multipart/form-data"
                                                action="/admin/user/create" modelAttribute="newUser" class="row">
                                                <div class="mb-3 col-12 col-md-6">
                                                    <c:set var="errorsEmail">
                                                        <form:errors path="email" />
                                                    </c:set>
                                                    <label class="form-label">Email:</label>
                                                    <form:input type="email" class="form-control ${not empty errorsEmail? 'is-invalid':''}" path="email" />
                                                    <form:errors path="email" cssClass="invalid-feedback" />
                                                </div>
                                                <div class="mb-3 col-12 col-md-6">
                                                    <c:set var="errorsPassword">
                                                        <form:errors path="password" />
                                                    </c:set>
                                                    <label class="form-label">Password:</label>
                                                    <form:input type="password" class="form-control ${not empty errorsPassword? 'is-invalid':''}" path="password" />
                                                    <form:errors path="password" cssClass="invalid-feedback" />
                                                </div>
                                                <div class="mb-3 col-12 col-md-6">
                                                    <c:set var="errorsPhone">
                                                        <form:errors path="phone" />
                                                    </c:set>
                                                    <label class="form-label">Phone number:</label>
                                                    <form:input type="text" class="form-control ${not empty errorsPhone? 'is-invalid':''}" path="phone" />
                                                    <form:errors path="phone" cssClass="invalid-feedback" />
                                                </div>
                                                <div class="mb-3 col-12 col-md-6">
                                                    <c:set var="errorsFullName">
                                                        <form:errors path="fullName" />
                                                    </c:set>
                                                    <label class="form-label">Full Name:</label>
                                                    <form:input type="text" class="form-control ${not empty errorsFullName? 'is-invalid':''}" path="fullName" />
                                                    <form:errors path="fullName" cssClass="invalid-feedback" />
                                                </div>
                                                <div class="mb-3 col-12">
                                                    <c:set var="errorsAddress">
                                                        <form:errors path="address" />
                                                    </c:set>
                                                    <label class="form-label">Address:</label>
                                                    <form:input type="text" class="form-control ${not empty errorsAddress? 'is-invalid':''}" path="address" />
                                                    <form:errors path="address" cssClass="invalid-feedback" />
                                                </div>

                                                <div class="mb-3 col-12 col-md-6">
                                                    <label class="form-label">Role:</label>
                                                    <form:select class="form-select" path="role.name">
                                                        <form:option value="ADMIN">ADMIN</form:option>
                                                        <form:option value="USER">USER</form:option>
                                                    </form:select>
                                                </div>
                                                <div class="mb-3 col-12 col-md-6">
                                                    <label for="avatarFile" class="form-label">Avatar:</label>
                                                    <input class="form-control" type="file" id="avatarFile"
                                                        accept=".png, .jpg, .jpeg" name="avatarFile" />
                                                </div>
                                                <div class="col-12 mb-3">
                                                    <img style="max-height: 250px; display: none;" alt="avatar preview"
                                                        id="avatarPreview" />
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
                <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"
                    crossorigin="anonymous"></script>
                <script src="/js/scripts.js"></script>

            </body>

            </html>