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
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/report.css">
</head>
<body>
    <div class="dashboard-container">
        <!-- Sidebar Implementation -->
        <div class="sidebar">
            <div class="sidebar-header">
                <h3>
                    <i class="bi bi-house-door"></i> Report
                </h3>
            </div>
            <nav class="nav-menu">
                <a href="dashboard-admin.jsp" class="nav-link <%= request.getRequestURI().endsWith("dashboard-admin.jsp") ? "active" : "" %>">
                    <i class="bi bi-speedometer2"></i> Dashboard

                <a href="manage-student.jsp" class="nav-link <%= request.getRequestURI().endsWith("students.jsp") ? "active" : "" %>">
                    <i class="bi bi-people"></i> Students
                </a>
                <a href="report.jsp" class="nav-link <%= request.getRequestURI().endsWith("report.jsp") ? "active" : "" %>">
                    <i class="bi bi-file-earmark-text"></i> Reports
                </a>
                <a href="logout.jsp" class="nav-link">
                    <i class="bi bi-box-arrow-right"></i> Logout
                </a>
            </nav>
        </div>

        <main class="main-content">
            <div class="content-header">
                <h1 class="page-title">
                    <i class="bi bi-calendar-check"></i> Attendance Report
                </h1>
                <div class="user-profile">
                    <span class="username"><%= userEmail %></span>
                    <i class="bi bi-person-circle user-icon"></i>
                </div>
            </div>

            <div class="alert alert-glass" role="alert">
                <i class="bi bi-info-circle-fill"></i> Viewing all attendance records in the system
            </div>

            <div class="card attendance-card">
                <div class="card-header">
                    <h3 class="card-title">
                        <i class="bi bi-table"></i> Attendance Records
                    </h3>
                </div>
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="table table-hover">
                            <thead class="table-header">
                                <tr>
                                    <th><i class="bi bi-person-vcard"></i> Student ID</th>
                                    <th><i class="bi bi-person-badge"></i> Teacher ID</th>
                                    <th><i class="bi bi-calendar-date"></i> Date</th>
                                    <th><i class="bi bi-check-circle"></i> Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% while (rs.next()) { %>
                                <tr>
                                    <td><%= rs.getInt("student_id") %></td>
                                    <td><%= rs.getInt("teacher_id") %></td>
                                    <td><%= rs.getDate("date") %></td>
                                    <td>
                                        <span class="status-badge <%= rs.getString("status").toLowerCase() %>">
                                            <%= rs.getString("status") %>
                                        </span>
                                    </td>
                                </tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <div class="action-buttons">
                <a href="<%= "admin".equals(userRole) ? "dashboard-admin.jsp" : ("teacher".equals(userRole) ? "dashboard-teacher.jsp" : "dashboard-student.jsp") %>"
                   class="btn btn-back">
                    <i class="bi bi-arrow-left"></i> Back to Dashboard
                </a>
            </div>
        </main>
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