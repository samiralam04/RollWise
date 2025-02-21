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

    // Fetching existing holidays
    PreparedStatement ps = conn.prepareStatement("SELECT * FROM holidays ORDER BY holiday_date DESC");
    ResultSet rs = ps.executeQuery();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Holidays</title>
    <link rel="stylesheet" href="assets/css/style.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
</head>
<body>
    <div class="container mt-4">
        <h2 class="text-center">Holiday Management</h2>

        <% if ("admin".equals(userType)) { %>
        <!-- Admin can add new holidays -->
        <div class="card p-3 mb-4">
            <h4>Add New Holiday</h4>
            <form action="HolidayServlet" method="post">
                <div class="mb-2">
                    <label>Holiday Name</label>
                    <input type="text" name="holidayName" class="form-control" placeholder="e.g., Diwali, Christmas" required>
                </div>
                <div class="mb-2">
                    <label>Date</label>
                    <input type="date" name="holidayDate" class="form-control" required>
                </div>
                <button type="submit" class="btn btn-success">Add Holiday</button>
            </form>
        </div>
        <% } %>

        <!-- Display Holidays -->
        <div class="card p-3">
            <h4>Upcoming Holidays</h4>
            <table class="table table-bordered">
                <thead>
                    <tr>
                        <th>Holiday Name</th>
                        <th>Date</th>
                    </tr>
                </thead>
                <tbody>
                    <% while (rs.next()) { %>
                    <tr>
                        <td><%= rs.getString("name") %></td>
                        <td><%= rs.getString("holiday_date") %></td>
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
