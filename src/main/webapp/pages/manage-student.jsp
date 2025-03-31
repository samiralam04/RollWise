<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.sql.*" %>

<!DOCTYPE html>
<html>
<head>
    <title>Manage Students</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container mt-4">
    <h2>Manage Students</h2>

    <!-- Student Registration Form -->
    <form action="<%= request.getContextPath() %>/student" method="post" class="mb-4">
        <div class="mb-3">
            <label for="name" class="form-label">Student Name</label>
            <input type="text" id="name" name="name" class="form-control" placeholder="Enter student name" required>
        </div>
        <div class="mb-3">
            <label for="email" class="form-label">Student Email</label>
            <input type="email" id="email" name="email" class="form-control" placeholder="Enter student email" required>
        </div>
        <div class="mb-3">
            <label for="password" class="form-label">Password</label>
            <input type="password" id="password" name="password" class="form-control" placeholder="Enter password" required>
        </div>
        <button type="submit" class="btn btn-primary">Add Student</button>
    </form>

    <!-- Student List Table -->
    <h3>Student List</h3>
    <table class="table table-bordered">
        <thead class="table-dark">
            <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Email</th>
            </tr>
        </thead>
        <tbody>
            <%
                Connection conn = (Connection) getServletContext().getAttribute("DBConnection");
                if (conn != null) {
                    try {
                        PreparedStatement ps = conn.prepareStatement("SELECT id, username, email FROM users WHERE role = 'student'");
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
            %>
            <tr>
                <td><%= rs.getInt("id") %></td>
                <td><%= rs.getString("username") %></td>
                <td><%= rs.getString("email") %></td>
            </tr>
            <%
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            %>
        </tbody>
    </table>
</div>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
