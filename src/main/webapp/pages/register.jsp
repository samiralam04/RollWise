<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register - Student Attendance System</title>
    <link rel="stylesheet" href="assets/css/style.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
</head>
<body>
    <div class="container d-flex justify-content-center align-items-center min-vh-100">
        <div class="card p-4 shadow-lg" style="width: 350px;">
            <h3 class="text-center">Register</h3>
            <div id="error-msg" class="alert alert-danger d-none"></div>
            <div id="success-msg" class="alert alert-success d-none">Registration successful! Redirecting to login...</div>

            <form id="registerForm">
                <div class="mb-3">
                    <label for="name" class="form-label">Full Name</label>
                    <input type="text" class="form-control" id="name" name="name" required>
                </div>

                <div class="mb-3">
                    <label for="email" class="form-label">Email</label>
                    <input type="email" class="form-control" id="email" name="email" required>
                </div>

                <div class="mb-3">
                    <label for="password" class="form-label">Password</label>
                    <input type="password" class="form-control" id="password" name="password" required>
                </div>

                <div class="mb-3">
                    <label for="role" class="form-label">Register As</label>
                    <select class="form-control" id="role" name="role" required>
                        <option value="admin">Admin</option>
                        <option value="teacher">Teacher</option>
                        <option value="student">Student/Parent</option>
                    </select>
                </div>

                <button type="submit" class="btn btn-primary w-100">Register</button>
            </form>
            <p class="text-center mt-3">Already have an account? <a href="pages/login.jsp">Login here</a></p>
        </div>
    </div>

    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script>
        $(document).ready(function () {
            $("#registerForm").submit(function (event) {
                event.preventDefault(); // Prevent form submission

                let name = $("#name").val();
                let email = $("#email").val();
                let password = $("#password").val();
                let role = $("#role").val();

                $.ajax({
                    type: "POST",
                    url: "RegisterServlet",
                    data: { name: name, email: email, password: password, role: role },
                    success: function (response) {
                        if (response === "success") {
                            $("#success-msg").removeClass("d-none");
                            setTimeout(function () {
                                window.location.href = "pages/login.jsp";
                            }, 2000);
                        } else {
                            $("#error-msg").text(response).removeClass("d-none");
                        }
                    }
                });
            });
        });
    </script>
</body>
</html>
