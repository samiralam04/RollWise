<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - Student Attendance System</title>
    <link rel="stylesheet" href="/StudentAttendanceManagementSystem/assets/css/style.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <style>
        body {
            background-color: #f8f9fa;
        }
        .login-container {
            max-width: 900px;
            background: white;
            border-radius: 10px;
            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
            padding: 30px;
        }
        .illustration {
            width: 100%;
        }
    </style>
</head>
<body>
    <div class="container d-flex justify-content-center align-items-center min-vh-100">
        <div class="row login-container p-4">
            <div class="col-md-6 d-flex flex-column justify-content-center">
                <h3 class="text-center">Sign In</h3>
                <div id="error-msg" class="alert alert-danger d-none"></div>
                <form id="loginForm">
                    <div class="mb-3">
                        <label for="email" class="form-label">Email</label>
                        <input type="email" class="form-control" id="email" name="email" required>
                    </div>
                    <div class="mb-3">
                        <label for="password" class="form-label">Password</label>
                        <input type="password" class="form-control" id="password" name="password" required>
                    </div>
                    <div class="mb-3">
                        <label for="role" class="form-label">Login As</label>
                        <select class="form-control" id="role" name="role" required>
                            <option value="admin">Admin</option>
                            <option value="teacher">Teacher</option>
                            <option value="student">Student/Parent</option>
                        </select>
                    </div>
                    <button type="submit" class="btn btn-primary w-100">Login</button>
                </form>
                <p class="text-center mt-3">New user? <a href="register.jsp">Register here</a></p>
            </div>
            <div class="col-md-6 d-flex align-items-center justify-content-center">
                <img src="<%= request.getContextPath() %>/assets/images/login.png" alt="Login Illustration" class="illustration">
            </div>
        </div>
    </div>

    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script>
        $(document).ready(function () {
            $("#loginForm").submit(function (event) {
                event.preventDefault();
                let email = $("#email").val();
                let password = $("#password").val();
                let role = $("#role").val();

                $.ajax({
                    type: "POST",
                    url: "<%= request.getContextPath() %>/LoginServlet",
                    data: { email: email, password: password, role: role },
                    dataType: "json",
                    success: function (response) {
                        if (response.status === "success") {
                            window.location.href = response.redirect;
                        } else {
                            $("#error-msg").text(response.message).removeClass("d-none");
                        }
                    },
                    error: function () {
                        $("#error-msg").text("Server Error. Please try again later.").removeClass("d-none");
                    }
                });
            });
        });
    </script>
</body>
</html>
