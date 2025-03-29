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

    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
        conn = DBConnection.getConnection();
        String query = "SELECT student_id, teacher_id, status, date FROM attendance";
        ps = conn.prepareStatement(query);
        rs = ps.executeQuery();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Attendance Report</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
</head>
<body>
    <div class="container mt-4">
        <h2 class="text-center">Attendance Report</h2>
        <div class="alert alert-info">Viewing attendance records.</div>

        <!-- Display Attendance Report -->
        <div class="card p-3">
            <table class="table table-bordered">
                <thead>
                    <tr>
                        <th>Student ID</th>
                        <th>Teacher ID</th>
                        <th>Date</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                    <% while (rs.next()) { %>
                    <tr>
                        <td><%= rs.getInt("student_id") %></td>
                        <td><%= rs.getInt("teacher_id") %></td>
                        <td><%= rs.getDate("date") %></td>
                        <td><%= rs.getString("status") %></td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
        </div>

        <div class="text-center mt-3">

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
        if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
        if (ps != null) try { ps.close(); } catch (SQLException ignore) {}
        if (conn != null) try { conn.close(); } catch (SQLException ignore) {}
    }
%>
