<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
            <!DOCTYPE html>
            <html lang="en">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title> Responsive Registration Form | CodingLab </title>
                <link rel="stylesheet" href="/client/css/register.css">
                <link href='https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css' rel='stylesheet'>

            </head>

            <body>
                <div class="container">
                    <!-- Title section -->
                    <div class="title">Registration</div>
                    <div class="content">
                        <!-- Registration form -->
                        <form:form action="/register" method="post" enctype="multipart/form-data"
                            modelAttribute="registerUser">
                            <c:set var="errorsEmail">
                                <form:errors path="email" cssClass="invalid-feedback"/>
                            </c:set>
                            <c:set var="errorsPassword">
                                <form:errors path="password" cssClass="invalid-feedback"/>
                            </c:set>
                            <c:set var="errorsPhone">
                                <form:errors path="phone" cssClass="invalid-feedback"/>
                            </c:set>
                            <c:set var="errorsFirstName">
                                <form:errors path="firstName" cssClass="invalid-feedback"/>
                            </c:set>
                            <c:set var="errorsLastName">
                                <form:errors path="lastName" cssClass="invalid-feedback"/>
                            </c:set>
                            <c:set var="errorsConfirmPassword">
                                <form:errors path="confirmPassword" cssClass="invalid-feedback"/>
                            </c:set>
                            <div class="user-details">
                                <!-- Input for Full Name -->
                                <div class="input-box">
                                    <span class="details">First Name</span>
                                    <form:input type="text" placeholder="Enter your first name" path="firstName" />
                                    ${errorsFirstName}
                                </div>
                                <!-- Input for Username -->
                                <div class="input-box">
                                    <span class="details">Last Name</span>
                                    <form:input type="text" placeholder="Enter your last name" path="lastName" />
                                    ${errorsFirstName}
                                </div>
                                <!-- Input for Email -->
                                <div class="input-box">
                                    <span class="details">Email</span>
                                    <form:input type="email" placeholder="Enter your email" path="email" class="form-control ${not empty errorsEmail? 'is-invalid':''}" />
                                    ${errorsEmail}
                                </div>                                
                                <!-- Input for Phone Number -->
                                <div class="input-box">
                                    <span class="details">Phone Number</span>
                                    <form:input type="text" placeholder="Enter your number" path="phone" />
                                    ${errorsPhone}
                                </div>
                                <!-- Input for Password -->
                                <div class="input-box">
                                    <span class="details">Password</span>
                                    <form:input type="password" placeholder="Enter your password" path="password"
                                        class="form-control ${not empty errorsPassword? 'is-invalid':''}" />
                                    ${errorsPassword}
                                </div>
                                <!-- Input for Confirm Password -->
                                <div class="input-box">
                                    <span class="details">Confirm Password</span>
                                    <form:input type="password" placeholder="Confirm your password"
                                        path="confirmPassword" />
                                        ${errorsConfirmPassword}
                                </div>
                            </div>
                            <!-- Submit button -->
                            <div class="button">
                                <input type="submit" value="Register">
                            </div>
                        </form:form>
                    </div>
                </div>
            </body>

            </html>