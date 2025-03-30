<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="true" %>
<%@ page import="java.sql.*, java.util.*" %>

<%
    // Get session values
    String userType = (String) session.getAttribute("role");
    String userEmail = (String) session.getAttribute("email");
    String userName = (String) session.getAttribute("name");

    // Redirect to login if session is invalid or user is not a student
    if (userEmail == null || !"student".equals(userType)) {
        response.sendRedirect("login.jsp");
        return;
    }

    // Get DB Connection
    Connection conn = (Connection) application.getAttribute("DBConnection");
    if (conn == null) {
        out.println("<h3>Database connection error!</h3>");
        return;
    }

    // Fetch Student ID from users table
    int studentId = -1;
    try {
        PreparedStatement ps1 = conn.prepareStatement("SELECT id FROM users WHERE email = ?");
        ps1.setString(1, userEmail);
        ResultSet rs1 = ps1.executeQuery();

        if (rs1.next()) {
            studentId = rs1.getInt("id");
        }
        rs1.close();
        ps1.close();
    } catch (Exception e) {
        e.printStackTrace();
    }

    // Fetch Attendance Data
    int totalClasses = 0, attendedClasses = 0;
    double attendancePercentage = 0.0;
    try {
        PreparedStatement ps2 = conn.prepareStatement(
            "SELECT COUNT(*) AS total, SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END) AS present FROM attendance WHERE student_id = ?");
        ps2.setInt(1, studentId);
        ResultSet rs2 = ps2.executeQuery();

        if (rs2.next()) {
            totalClasses = rs2.getInt("total");
            attendedClasses = rs2.getInt("present");
        }
        rs2.close();
        ps2.close();

        attendancePercentage = (totalClasses > 0) ? (attendedClasses * 100.0 / totalClasses) : 0;
    } catch (Exception e) {
        e.printStackTrace();
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Student Dashboard</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/student.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>
    <div class="container">
        <header class="dashboard-header">
            <h1 class="welcome-message">Welcome, <%= userName != null ? userName : "Student" %></h1>
        </header>

        <!-- Stats Overview -->
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-title">Attendance Percentage</div>
                <div class="stat-value"><%= String.format("%.0f", attendancePercentage) %>%</div>
                <div class="stat-actions">
                    <% if (attendancePercentage < 75) { %>
                        <span class="alert-danger" style="padding: 0.25rem 0.5rem; border-radius: 4px;">
                            <i class="fas fa-exclamation-circle"></i> Low
                        </span>
                    <% } else if (attendancePercentage >= 75 && attendancePercentage <= 78) { %>
                        <span class="alert-warning" style="padding: 0.25rem 0.5rem; border-radius: 4px;">
                            <i class="fas fa-exclamation-triangle"></i> Warning
                        </span>
                    <% } else { %>
                        <span style="color: var(--success); padding: 0.25rem 0.5rem; border-radius: 4px;">
                            <i class="fas fa-check-circle"></i> Good
                        </span>
                    <% } %>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-title">Classes Attended</div>
                <div class="stat-value"><%= attendedClasses %></div>
                <div class="stat-actions">
                    <span>Out of <%= totalClasses %> total</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-title">Absent Classes</div>
                <div class="stat-value"><%= totalClasses - attendedClasses %></div>
                <div class="stat-actions">
                    <span><%= totalClasses > 0 ? String.format("%.0f", (totalClasses - attendedClasses) * 100.0 / totalClasses) : 0 %>% of total</span>
                </div>
            </div>
        </div>

        <!-- Attendance Section -->
        <div class="attendance-section">
            <div class="attendance-card">
                <h3>Attendance Chart</h3>
                <div class="chart-container">
                    <canvas id="attendanceChart"></canvas>
                </div>
            </div>

            <div class="attendance-card">
                <h3>Attendance Summary</h3>
                <% if (attendancePercentage < 75) { %>
                    <div class="alert alert-danger">
                        <i class="fas fa-exclamation-circle"></i>
                        <span>Your attendance is below 75%! Please attend classes.</span>
                    </div>
                <% } else if (attendancePercentage >= 75 && attendancePercentage <= 78) { %>
                    <div class="alert alert-warning">
                        <i class="fas fa-exclamation-triangle"></i>
                        <span>Your attendance is nearing 75%! Maintain regular attendance.</span>
                    </div>
                <% } else { %>
                    <div class="alert" style="background-color: rgba(16, 185, 129, 0.1); border-left: 4px solid var(--success); color: var(--success);">
                        <i class="fas fa-check-circle"></i>
                        <span>Your attendance is good! Keep it up.</span>
                    </div>
                <% } %>
                <div class="attendance-percentage"><%= String.format("%.2f", attendancePercentage) %>%</div>
                <div style="text-align: center; color: var(--secondary);">
                    <%= attendedClasses %> out of <%= totalClasses %> classes attended
                </div>
            </div>
        </div>

        <!-- Recent Activity -->
                        <div class="activity-card">
                            <h3>Recent Activity</h3>
                            <div class="activity-item">
                                <div class="activity-text">Attendance marked for today</div>
                                <div class="activity-time">Today</div>
                            </div>
                            <div class="activity-item">
                                <div class="activity-text">New notice posted</div>
                                <div class="activity-time">2 days ago</div>
                            </div>
                            <div class="activity-item">
                                <div class="activity-text">Monthly report generated</div>
                                <div class="activity-time">1 week ago</div>
                            </div>
                        </div>

        <!-- Attendance History -->
        <div class="activity-card" style="margin-top: 2rem; margin-bottom: 2rem;">
            <h3>Attendance History</h3>
            <div class="table-container">
                <table>
                    <thead>
                        <tr>
                            <th>Date</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% try {
                            PreparedStatement ps3 = conn.prepareStatement("SELECT date, status FROM attendance WHERE student_id = ? ORDER BY date DESC LIMIT 10");
                            ps3.setInt(1, studentId);
                            ResultSet rs3 = ps3.executeQuery();
                            while (rs3.next()) { %>
                        <tr>
                            <td><%= rs3.getDate("date") %></td>
                            <td><%= rs3.getString("status") %></td>
                        </tr>
                        <% }
                            rs3.close();
                            ps3.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } %>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="logout-container">
            <a href="logout.jsp" class="btn-logout">
                <i class="fas fa-sign-out-alt"></i> Logout
            </a>
        </div>
    </div>

    <script>
        const ctx = document.getElementById('attendanceChart').getContext('2d');
        new Chart(ctx, {
            type: 'pie',
            data: {
                labels: ['Present', 'Absent'],
                datasets: [{
                    data: [<%= attendedClasses %>, <%= totalClasses - attendedClasses %>],
                    backgroundColor: [
                        'rgba(79, 70, 229, 0.8)',
                        'rgba(239, 68, 68, 0.8)'
                    ],
                    borderColor: [
                        'rgba(79, 70, 229, 1)',
                        'rgba(239, 68, 68, 1)'
                    ],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            font: {
                                family: 'Inter',
                                size: 14
                            }
                        }
                    },
                    tooltip: {
                        bodyFont: {
                            family: 'Inter',
                            size: 14
                        }
                    }
                },
                cutout: '40%'
            }
        });
    </script>
</body>
</html>