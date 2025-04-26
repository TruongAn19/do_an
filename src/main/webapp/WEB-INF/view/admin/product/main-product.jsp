<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="utf-8" />
            <meta http-equiv="X-UA-Compatible" content="IE=edge" />
            <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no" />
            <meta name="description" content="Dự án sancaulong" />
            <meta name="author" content="TruongAn" />
            <title>Product</title>
            <link href="/css/styles.css" rel="stylesheet" />
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
                                    <div class="col-12 mx-auto">
                                        <div class="d-flex justify-content-between">
                                            <h3>Table Main Product</h3>
                                            <div class="mb-3">
                                                <form action="/admin/mainProduct" method="get" class="row g-3">
                                                    <div>
                                                        <div class="input-group">
                                                            <input type="text" class="form-control" name="search"
                                                                placeholder="Tìm kiếm theo tên sản phẩm..."
                                                                value="${not empty searchTerm ? searchTerm : ''}">
                                                            <button class="btn btn-outline-secondary" type="submit">
                                                                <i class="fas fa-search"></i>
                                                            </button>
                                                            <c:if test="${not empty searchTerm}">
                                                                <a href="/admin/mainProduct"
                                                                    class="btn btn-outline-danger">
                                                                    <i class="fas fa-times"></i>
                                                                </a>
                                                            </c:if>
                                                        </div>
                                                    </div>
                                                    <input type="hidden" name="page" value="1">
                                                </form>
                                            </div>
                                            <a href="/admin/product/create_mainProduct" class="btn btn-primary">Create
                                                Main Product</a>
                                        </div>

                                        <hr />
                                        <table class=" table table-bordered table-hover">
                                            <thead>
                                                <tr>
                                                    <th>Name</th>
                                                    <th>Address</th>
                                                    <th>Price</th>
                                                    <th>Sale</th>
                                                    <th>Action</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="product" items="${mainProducts}">

                                                    <tr>
                                                        <td>${product.name}</td>
                                                        <td>${product.address}</td>
                                                        <td>${product.price}</td>
                                                        <td>${product.sale}%</td>
                                                        <!-- <td>${user.role.name}</td> -->
                                                        <td>
                                                            <a href="/admin/mainProduct/${product.id}"
                                                                class="btn btn-success">View</a>
                                                            <a href="/admin/product/update_mainProduct/${product.id}"
                                                                class="btn btn-warning  mx-2">Update</a>
                                                            <a href="/admin/product/delete_product/${product.id}"
                                                                class="btn btn-danger">Delete</a>
                                                        </td>
                                                    </tr>

                                                </c:forEach>
                                            </tbody>
                                        </table>
                                        <nav aria-label="Page navigation example">
                                            <ul class="pagination justify-content-center">
                                                <li class="page-item">
                                                    <a class="${1 eq currentPage ? 'disabled page-link' : 'page-link'}"
                                                        href="/main-products?page=${currentPage - 1}${not empty param.search ? '&search=' += param.search : ''}"
                                                        aria-label="Previous">
                                                        <span aria-hidden="true">&laquo;</span>
                                                    </a>
                                                </li>
                                                <c:if test="${totalPages > 0}">
                                                    <c:forEach begin="0" end="${totalPages - 1}" varStatus="loop">
                                                        <li class="page-item">
                                                            <a class="${(loop.index + 1) eq currentPage ? 'active page-link' : 'page-link'}"
                                                                href="/main-products?page=${loop.index + 1}${not empty param.search ? '&search=' += param.search : ''}">
                                                                ${loop.index + 1}
                                                            </a>
                                                        </li>
                                                    </c:forEach>
                                                </c:if>


                                                <li class="page-item">
                                                    <a class="${totalPages eq currentPage ? 'disabled page-link' : 'page-link'}"
                                                       href="/main-products?page=${currentPage + 1}${not empty param.search ? '&search=' += param.search : ''}" 
                                                       aria-label="Next">
                                                        <span aria-hidden="true">&raquo;</span>
                                                    </a>
                                                </li>
                                            </ul>
                                        </nav>
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