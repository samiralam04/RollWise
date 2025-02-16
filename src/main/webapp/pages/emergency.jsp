<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="true" %>
<%@ page import="java.sql.*, java.util.*" %>

<%
    String userType = (String) session.getAttribute("userType");
    String userEmail = (String) session.getAttribute("userEmail");

    if (userEmail == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    Connection conn = (Connection) application.getAttribute("DBConnection");

    // Fetching existing emergency notices
    PreparedStatement ps = conn.prepareStatement("SELECT * FROM emergency ORDER BY declared_date DESC");
    ResultSet rs = ps.executeQuery();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Emergency Alerts</title>
    <link rel="stylesheet" href="assets/css/style.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
</head>
<body>
    <div class="container mt-4">
        <h2 class="text-center">Emergency Alerts</h2>

        <% if ("admin".equals(userType)) { %>
        <!-- Admin can declare new emergency -->
        <div class="card p-3 mb-4">
            <h4>Declare Emergency Holiday</h4>
            <form action="EmergencyServlet" method="post">
                <div class="mb-2">
                    <label>Reason</label>
                    <input type="text" name="reason" class="form-control" placeholder="e.g., Heavy Rain, Flood" required>
                </div>
                <div class="mb-2">
                    <label>Date</label>
                    <input type="date" name="date" class="form-control" required>
                </div>
                <button type="submit" class="btn btn-danger">Declare Emergency</button>
            </form>
        </div>
        <% } %>

        <!-- Display Emergency Notices -->
        <div class="card p-3">
            <h4>Active Emergency Notices</h4>
            <table class="table table-bordered">
                <thead>
                    <tr>
                        <th>Reason</th>
                        <th>Date</th>
                        <th>Declared On</th>
                    </tr>
                </thead>
                <tbody>
                    <% while (rs.next()) { %>
                    <tr>
                        <td><%= rs.getString("reason") %></td>
                        <td><%= rs.getString("emergency_date") %></td>
                        <td><%= rs.getString("declared_date") %></td>
                    </tr>
                    <% } rs.close(); ps.close(); %>
                </tbody>
            </table>
        </div>

        <div class="text-center mt-3">
            <a href="dashboard-admin.jsp" class="btn btn-primary">Back to Dashboard</a>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
