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


<div class="mb-3 col-12">--%>
    <%--                                    <label class="form-label">Select Time Slot:</label>--%>
    <%--                                    <select id="timeSlot" class="form-select">--%>
    <%--                                        <option value="">Chọn khung giờ</option>--%>
    <%--                                        <option value="08:00 AM">08:00 AM</option>--%>
    <%--                                        <option value="09:00 AM">09:00 AM</option>--%>
    <%--                                        <option value="10:00 AM">10:00 AM</option>--%>
    <%--                                        <option value="11:00 AM">11:00 AM</option>--%>
    <%--                                        <option value="12:00 PM">12:00 PM</option>--%>
    <%--                                        <option value="01:00 PM">01:00 PM</option>--%>
    <%--                                        <option value="02:00 PM">02:00 PM</option>--%>
    <%--                                        <option value="03:00 PM">03:00 PM</option>--%>
    <%--                                        <option value="04:00 PM">04:00 PM</option>--%>
    <%--                                        <option value="05:00 PM">05:00 PM</option>--%>
    <%--                                        <option value="06:00 PM">06:00 PM</option>--%>
    <%--                                        <option value="07:00 PM">07:00 PM</option>--%>
    <%--                                        <option value="08:00 PM">08:00 PM</option>--%>
    <%--                                        <option value="09:00 PM">09:00 PM</option>--%>
    <%--                                    </select>--%>
    <%--                                </div>--%>

    <%--                                <div class="mb-3 col-12">--%>
    <%--                                    <button class="btn btn-secondary" onclick="addTimeSlot(event)">Thêm Khung Giờ</button>--%>
    <%--                                </div>--%>

    <%--                                <div class="mb-3 col-12">--%>
    <%--                                    <label class="form-label">Khung giờ đã chọn:</label>--%>
    <%--                                    <input type="text" id="timeSlotsInput" class="form-control" readonly />--%>
<%--                                </div>
<%--
function addTimeSlot(event) {
            event.preventDefault();  // Prevent form submission

            // Get the selected time slot value
            const timeSlot = document.getElementById("timeSlot").value;

            // If a time slot is selected, add it to the input field
            if (timeSlot) {
                // Get the existing value of the input field
                let currentValue = document.getElementById("timeSlotsInput").value;

                // Check if the input already contains the time slot
                if (currentValue.includes(timeSlot)) {
                    alert("Khung giờ này đã được thêm.");
                    return;
                }

                // If input is empty, just add the new time slot
                if (currentValue === "") {
                    document.getElementById("timeSlotsInput").value = timeSlot;
                } else {
                    // Otherwise, append the time slot to the existing value
                    document.getElementById("timeSlotsInput").value += ", " + timeSlot;
                }
            } else {
                alert("Vui lòng chọn một khung giờ.");
            }
        }--%>