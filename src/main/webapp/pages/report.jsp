<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="true" %>
<%@ page import="java.sql.*, com.attendance.util.DBConnection" %>

<%
    // Retrieve session attributes
    String userRole = (String) session.getAttribute("role");
    String userEmail = (String) session.getAttribute("email");

    // Check if user is logged in
    if (userEmail == null || userRole == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    // Student ID (only applicable for students)
    int studentId = -1;
    if ("student".equals(userRole)) {
        Object studentIdObj = session.getAttribute("studentId");
        if (studentIdObj != null) {
            studentId = (Integer) studentIdObj;
        }
    }

    // Database connection & query
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
        conn = DBConnection.getConnection();
        String query;

        if ("student".equals(userRole)) {
            query = "SELECT a.date, a.status FROM attendance a WHERE a.student_id = ?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, studentId);
        } else {
            query = "SELECT s.id, s.roll_number, a.date, a.status " +
                    "FROM students s " +
                    "JOIN attendance a ON s.id = a.student_id";
            ps = conn.prepareStatement(query);
        }

        rs = ps.executeQuery();
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

        <% if ("student".equals(userRole)) { %>
            <div class="alert alert-info">Your attendance report is displayed below.</div>
        <% } else { %>
            <div class="alert alert-info">Viewing attendance records for all students.</div>
        <% } %>

        <!-- Display Attendance Report -->
        <div class="card p-3">
            <table class="table table-bordered">
                <thead>
                    <tr>
                        <% if (!"student".equals(userRole)) { %>
                            <th>Student ID</th>
                            <th>Roll Number</th>
                        <% } %>
                        <th>Date</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                    <% while (rs.next()) { %>
                    <tr>
                        <% if (!"student".equals(userRole)) { %>
                            <td><%= rs.getInt("id") %></td>
                            <td><%= rs.getString("roll_number") %></td>
                        <% } %>
                        <td><%= rs.getDate("date") %></td>
                        <td><%= rs.getString("status") %></td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
        </div>

        <div class="text-center mt-3">
            <form action="DownloadReportServlet" method="post">
                <button type="submit" class="btn btn-success">Download Report as Excel</button>
            </form>
            <a href="<%= "admin".equals(userRole) ? "dashboard-admin.jsp" : ("teacher".equals(userRole) ? "dashboard-teacher.jsp" : "dashboard-student.jsp") %>" class="btn btn-primary mt-2">Back to Dashboard</a>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

<%
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        // Close resources properly
        if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
        if (ps != null) try { ps.close(); } catch (SQLException ignore) {}
        if (conn != null) try { conn.close(); } catch (SQLException ignore) {} // ✅ Ensure DB connection is closed
    }
%>
