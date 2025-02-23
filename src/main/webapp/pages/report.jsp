<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="true" %>
<%@ page import="java.sql.*, java.util.*" %>

<%
    String userType = (String) session.getAttribute("userType");
    String userEmail = (String) session.getAttribute("userEmail");

    // First, check if the user is logged in
    if (userEmail == null || userType == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    // Only check studentId if userType is not null
    int studentId = "student".equals(userType) ? (Integer) session.getAttribute("studentId") : -1;
%>


<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Attendance Report</title>
    <link rel="stylesheet" href="assets/css/style.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
</head>
<body>
    <div class="container mt-4">
        <h2 class="text-center">Attendance Report</h2>

        <% if (userType.equals("student")) { %>
        <div class="alert alert-info">Your attendance report is displayed below.</div>
        <% } else if (userType.equals("teacher") || userType.equals("admin")) { %>
        <div class="alert alert-info">Viewing attendance records for all students.</div>
        <% } %>

        <!-- Display Attendance Report -->
        <div class="card p-3">
            <table class="table table-bordered">
                <thead>
                    <tr>
                        <% if (!userType.equals("student")) { %>
                        <th>Student ID</th>
                        <th>Student Name</th>
                        <% } %>
                        <th>Subject</th>
                        <th>Total Classes</th>
                        <th>Attended Classes</th>
                        <th>Attendance (%)</th>
                    </tr>
                </thead>
                <tbody>
                    <% while (rs.next()) {
                        int total = rs.getInt("total_classes");
                        int attended = rs.getInt("attended_classes");
                        double percentage = (total == 0) ? 0 : ((double) attended / total) * 100;
                    %>
                    <tr>
                        <% if (!userType.equals("student")) { %>
                        <td><%= rs.getInt("id") %></td>
                        <td><%= rs.getString("name") %></td>
                        <% } %>
                        <td><%= rs.getString("subject") %></td>
                        <td><%= total %></td>
                        <td><%= attended %></td>
                        <td><%= String.format("%.2f", percentage) %> %</td>
                    </tr>
                    <% } rs.close(); ps.close(); %>
                </tbody>
            </table>
        </div>

        <div class="text-center mt-3">
            <form action="DownloadReportServlet" method="post">
                <button type="submit" class="btn btn-success">Download Report as Excel</button>
            </form>
            <a href="<%= userType.equals("admin") ? "dashboard-admin.jsp" : (userType.equals("teacher") ? "dashboard-teacher.jsp" : "dashboard-student.jsp") %>" class="btn btn-primary mt-2">Back to Dashboard</a>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
