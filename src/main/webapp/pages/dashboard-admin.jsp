<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="true" %>
<%@ page import="java.sql.*, java.util.*" %>
<%
    String adminEmail = (String) session.getAttribute("email");
    if (session.getAttribute("email") == null) {
        response.sendRedirect("login.jsp");
        return;
    }

%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard</title>
    <link rel="stylesheet" href="assets/css/style.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <script src="https://kit.fontawesome.com/a076d05399.js"></script>
</head>
<body>
    <div class="d-flex">
        <!-- Sidebar -->
        <div class="bg-dark text-white p-3" style="width: 250px; height: 100vh;">
            <h4 class="text-center">Admin Panel</h4>
            <ul class="nav flex-column">
                <li class="nav-item">
                    <a href="dashboard-admin.jsp" class="nav-link text-white"><i class="fas fa-home"></i> Dashboard</a>
                </li>
                <li class="nav-item">
                    <a href="manage-teachers.jsp" class="nav-link text-white"><i class="fas fa-chalkboard-teacher"></i> Manage Teachers</a>
                </li>
                <li class="nav-item">
                    <a href="report.jsp" class="nav-link text-white"><i class="fas fa-file-alt"></i> Student Reports</a>
                </li>
                <li class="nav-item">
                    <a href="holiday.jsp" class="nav-link text-white"><i class="fas fa-calendar-alt"></i> Manage Holidays</a>
                </li>
                <li class="nav-item">
                    <a href="emergency.jsp" class="nav-link text-white"><i class="fas fa-exclamation-triangle"></i> Emergency Alerts</a>
                </li>
                <li class="nav-item">
                    <a href="logout.jsp" class="nav-link text-danger"><i class="fas fa-sign-out-alt"></i> Logout</a>
                </li>
            </ul>
        </div>

        <!-- Main Content -->
        <div class="container-fluid p-4">
            <h2>Welcome, <%= adminEmail %>!</h2>
            <div class="row">
                <div class="col-md-4">
                    <div class="card shadow-sm p-3 text-center">
                        <h4>Manage Teachers</h4>
                        <p>Add, edit, or remove teachers</p>
                        <a href="manage-teachers.jsp" class="btn btn-primary">Go</a>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="card shadow-sm p-3 text-center">
                        <h4>Student Reports</h4>
                        <p>View student attendance reports</p>
                        <a href="report.jsp" class="btn btn-primary">Go</a>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="card shadow-sm p-3 text-center">
                        <h4>Manage Holidays</h4>
                        <p>Add or remove holidays</p>
                        <a href="holiday.jsp" class="btn btn-primary">Go</a>
                    </div>
                </div>
            </div>

            <div class="row mt-3">
                <div class="col-md-4">
                    <div class="card shadow-sm p-3 text-center">
                        <h4>Emergency Alerts</h4>
                        <p>Announce sudden holidays</p>
                        <a href="emergency.jsp" class="btn btn-danger">Go</a>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="card shadow-sm p-3 text-center">
                        <h4>Logout</h4>
                        <p>Sign out from the system</p>
                        <a href="logout.jsp" class="btn btn-secondary">Logout</a>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
