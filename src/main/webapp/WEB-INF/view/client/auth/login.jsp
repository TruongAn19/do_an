<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
            <!DOCTYPE html>
            <!-- Created By CodingNepal -->
            <html lang="en" dir="ltr">

            <head>
                <meta charset="utf-8">
                <title>Login Form</title>
                <link rel="stylesheet" href="/client/css/login.css">
            </head>

            <body>
                <div class="wrapper">
                    <div class="title">
                        Login Form
                    </div>
                    <form method="post" action="/login">
                        <c:if test="${param.error != null}">
                            <div class="my-2" style="color: red;">Invalid email or password.</div>
                        </c:if>
                        <c:if test="${param.logout != null}">
                            <div class="my-2" style="color: red;">Logout Success</div>
                        </c:if>
                        <div class="field">
                            <input type="text" required name="username">
                            <label>Email Address</label>
                        </div>
                        <div class="field">
                            <input type="password" required name="password">
                            <label>Password</label>
                        </div>
                        <div><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                        </div>
                        <div class="content">
                            <!-- <div class="checkbox">
                                <input type="checkbox" id="remember-me">
                                <label for="remember-me">Remember me</label>
                            </div> -->
                            <div class="pass-link">
                                <a href="#">Forgot password?</a>
                            </div>
                        </div>
                        <div class="field">
                            <input type="submit" value="Login">
                        </div>
                        <div class="signup-link">
                            Not a member? <a href="/register">Signup now</a>
                        </div>
                    </form>
                </div>
            </body>

            </html>