<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="true" %>
<%@ page import="java.sql.*" %>

<%
    // Prevent caching
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);

    // Session validation
    String adminName = (String) session.getAttribute("username");
    if (adminName == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    // Get admin initials for avatar
    String initials = "";
    if (adminName != null && adminName.length() > 0) {
        String[] parts = adminName.split(" ");
        initials = parts[0].substring(0, 1).toUpperCase();
        if (parts.length > 1) {
            initials += parts[1].substring(0, 1).toUpperCase();
        }
    }

    // Get stats from database
    int teacherCount = 0;
    int studentCount = 0;
    int activeAlerts = 0;
    int holidayCount = 0;

    Connection conn = (Connection) getServletContext().getAttribute("DBConnection");
    if (conn != null) {
        try {
            // Get teacher count
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM teacher");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) teacherCount = rs.getInt(1);

            // Get student count
            ps = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE role = 'student'");
            rs = ps.executeQuery();
            if (rs.next()) studentCount = rs.getInt(1);

            ps = conn.prepareStatement("SELECT COUNT(*) FROM holiday");
                        rs = ps.executeQuery();
                        if (rs.next()) holidayCount = rs.getInt(1);

            // Get active alerts
            ps = conn.prepareStatement("SELECT COUNT(*) FROM emergency ");
            rs = ps.executeQuery();
            if (rs.next()) activeAlerts = rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard</title>

    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- FontAwesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css">
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css">
</head>
<body>
    <div class="admin-container">
        <!-- Sidebar -->
        <aside class="admin-sidebar">
            <div class="admin-sidebar-header">
                <h3>Admin Panel</h3>
            </div>

            <nav class="admin-sidebar-nav">
                <a href="dashboard-admin.jsp" class="admin-nav-link active">
                    <i class="fas fa-tachometer-alt"></i> Dashboard
                </a>
                <a href="manage-teachers.jsp" class="admin-nav-link">
                    <i class="fas fa-chalkboard-teacher"></i> Manage Teachers
                </a>

                <a href="report.jsp" class="admin-nav-link">
                    <i class="fas fa-file-alt"></i> Attendance Reports
                </a>
                <a href="holiday.jsp" class="admin-nav-link">
                    <i class="fas fa-calendar-alt"></i> Holiday Management
                </a>
                <a href="emergency.jsp" class="admin-nav-link">
                    <i class="fas fa-exclamation-triangle"></i> Emergency Alerts
                </a>

                <a href="logout.jsp" class="admin-nav-link text-danger">
                    <i class="fas fa-sign-out-alt"></i> Logout
                </a>
            </nav>
        </aside>

        <!-- Main Content -->
        <main class="admin-main">
            <header class="admin-header">
                <h1 class="admin-title">Welcome, <%= adminName %></h1>
                <div class="admin-user">
                    <div class="admin-user-avatar"><%= initials %></div>
                </div>
            </header>

            <!-- Stats Cards -->
            <div class="admin-card-grid">
                <div class="admin-card">
                    <div class="admin-card-header">
                        <div class="admin-card-icon">
                            <i class="fas fa-chalkboard-teacher"></i>
                        </div>
                        <h3 class="admin-card-title">Teachers</h3>
                    </div>
                    <div class="admin-card-body">
                        <p class="admin-card-text">Total teachers in the system</p>
                        <h2 class="text-success"><%= teacherCount %></h2>
                    </div>
                    <div class="admin-card-footer">
                        <a href="manage-teachers.jsp" class="admin-btn admin-btn-primary">
                            <i class="fas fa-arrow-right"></i> Manage
                        </a>
                    </div>
                </div>

                <div class="admin-card">
                    <div class="admin-card-header">
                        <div class="admin-card-icon">
                            <i class="fas fa-user-graduate"></i>
                        </div>
                        <h3 class="admin-card-title">Students</h3>
                    </div>
                    <div class="admin-card-body">
                        <p class="admin-card-text">Total students enrolled</p>
                        <h2 class="text-info"><%= studentCount %></h2>
                    </div>
                    <div class="admin-card-footer">
                        <a href="manage-student.jsp" class="admin-btn admin-btn-primary">
                            <i class="fas fa-arrow-right"></i> Manage
                        </a>
                    </div>
                </div>

                <div class="admin-card">
                                    <div class="admin-card-header">
                                        <div class="admin-card-icon">
                                            <i class="fas fa-calendar-alt"></i>
                                        </div>
                                        <h3 class="admin-card-title">Holidays</h3>
                                    </div>
                                    <div class="admin-card-body">
                                        <p class="admin-card-text">Upcoming Holidays</p>
                                        <h2 class="text-info"><%= holidayCount %></h2>
                                    </div>
                                    <div class="admin-card-footer">
                                        <a href="holiday.jsp" class="admin-btn admin-btn-primary">
                                            <i class="fas fa-arrow-right"></i> Manage
                                        </a>
                                    </div>
                                </div>





                <div class="admin-card">
                    <div class="admin-card-header">
                        <div class="admin-card-icon">
                            <i class="fas fa-exclamation-triangle"></i>
                        </div>
                        <h3 class="admin-card-title">Active Alerts</h3>
                    </div>
                    <div class="admin-card-body">
                        <p class="admin-card-text">Current emergency alerts</p>
                        <h2 class="text-danger"><%= activeAlerts %></h2>
                    </div>
                    <div class="admin-card-footer">
                        <a href="emergency.jsp" class="admin-btn admin-btn-danger">
                            <i class="fas fa-arrow-right"></i> View
                        </a>
                    </div>
                </div>
            </div>

            <!-- Quick Actions -->
            <div class="admin-card glass-effect mb-4">
                <h3 class="admin-card-title mb-3">Quick Actions</h3>
                <div class="d-flex flex-wrap gap-2">
                    <a href="manage-teachers.jsp" class="admin-btn admin-btn-secondary">
                        <i class="fas fa-plus"></i> Add Teacher
                    </a>
                    <a href="emergency.jsp" class="admin-btn admin-btn-secondary">
                        <i class="fas fa-plus"></i> Declare Alerts
                    </a>
                    <a href="report.jsp" class="admin-btn admin-btn-secondary">
                        <i class="fas fa-file-export"></i> View Report
                    </a>
                    <a href="holiday.jsp" class="admin-btn admin-btn-secondary">
                        <i class="fas fa-calendar-plus"></i> Add Holiday
                    </a>
                </div>
            </div>

            <!-- Recent Activity -->
            <div class="admin-card glass-effect">
                <h3 class="admin-card-title mb-3">Recent Activity</h3>
                <div class="list-group">
                    <div class="list-group-item glass-effect border-0 mb-2">
                        <div class="d-flex justify-content-between">
                            <span><i class="fas fa-user-plus text-success me-2"></i> New teacher added</span>
                            <small class="text-muted">2 hours ago</small>
                        </div>
                    </div>
                    <div class="list-group-item glass-effect border-0 mb-2">
                        <div class="d-flex justify-content-between">
                            <span><i class="fas fa-exclamation-circle text-danger me-2"></i> Emergency alert issued</span>
                            <small class="text-muted">5 hours ago</small>
                        </div>
                    </div>
                    <div class="list-group-item glass-effect border-0">
                        <div class="d-flex justify-content-between">
                            <span><i class="fas fa-file-import text-info me-2"></i> Attendance report generated</span>
                            <small class="text-muted">1 day ago</small>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <!-- Custom JS -->
    <script>
        // Toggle sidebar on mobile
        document.addEventListener('DOMContentLoaded', function() {
            // Add animation to cards
            const cards = document.querySelectorAll('.admin-card');
            cards.forEach((card, index) => {
                card.style.animationDelay = `${index * 0.1}s`;
            });

            // You can add more interactive features here
        });
    </script>
</body>
</html>