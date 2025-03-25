<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="true" %>
<%@ page import="java.sql.*, java.util.*" %>

<%
    String userType = (String) session.getAttribute("role");
    String userEmail = (String) session.getAttribute("email");

    if (userEmail == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Attendance Management</title>
    <link rel="stylesheet" href="assets/css/style.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <script src="https://kit.fontawesome.com/a076d05399.js"></script>
</head>
<body>
    <div class="container mt-4">
        <h2 class="text-center">Attendance Management</h2>

        <% if ("teacher".equals(userType)) { %>
        <!-- Teacher View: Mark Attendance -->
        <div class="card p-3 mt-3">
            <h4>Mark Attendance</h4>
            <form action="AttendanceServlet" method="post">
                <div class="mb-3">
                    <label for="date" class="form-label">Select Date:</label>
                    <input type="date" id="date" name="date" class="form-control" required>
                </div>
                <div class="mb-3">
                    <label for="studentId" class="form-label">Select Student:</label>
                    <select id="studentId" name="studentId" class="form-control">
                        <option value="">--Select Student--</option>
                        <%
                            Connection conn = (Connection) application.getAttribute("DBConnection");
                            if (conn != null) {
                                try (PreparedStatement stmt = conn.prepareStatement("SELECT id, name FROM students");
                                     ResultSet rs = stmt.executeQuery()) {
                                    while (rs.next()) {
                        %>
                        <option value="<%= rs.getInt("id") %>"><%= rs.getString("name") %></option>
                        <%
                                    }
                                }
                            } else {
                        %>
                        <option value="">Database connection error</option>
                        <% } %>
                    </select>
                </div>
                <div class="mb-3">
                    <label for="status" class="form-label">Attendance Status:</label>
                    <select id="status" name="status" class="form-control">
                        <option value="Present">Present</option>
                        <option value="Absent">Absent</option>
                    </select>
                </div>
                <button type="submit" class="btn btn-success">Submit Attendance</button>
            </form>
        </div>
        <% } %>

        <% if ("student".equals(userType) || "parent".equals(userType)) { %>
        <!-- Student/Parent View: View Attendance -->
        <div class="card p-3 mt-3">
            <h4>View Attendance</h4>
            <table class="table table-bordered">
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        if (conn != null) {
                            try (PreparedStatement ps = conn.prepareStatement("SELECT date, status FROM attendance WHERE student_email = ?")) {
                                ps.setString(1, userEmail);
                                try (ResultSet rs2 = ps.executeQuery()) {
                                    while (rs2.next()) {
                    %>
                    <tr>
                        <td><%= rs2.getDate("date") %></td>
                        <td><%= rs2.getString("status") %></td>
                    </tr>
                    <%
                                    }
                                }
                            }
                        }
                    %>
                </tbody>
            </table>
        </div>
        <% } %>

        <!-- Common: Download Report -->
        <div class="text-center mt-3">
            <a href="DownloadReportServlet" class="btn btn-primary"><i class="fas fa-download"></i> Download Report</a>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
