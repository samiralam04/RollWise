<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="true" %>
<%@ page import="java.sql.*, java.util.*" %>

<%
    // Get session values
    String userType = (String) session.getAttribute("role");
    String userEmail = (String) session.getAttribute("email");

    // Redirect to login if not a student
    if (userEmail == null || !"student".equals(userType)) {
        response.sendRedirect("login.jsp");
        return;
    }

    // Get DB Connection
    Connection conn = (Connection) application.getAttribute("DBConnection");
    if (conn == null) {
        out.println("<h3 style='color:red;'>Database connection error!</h3>");
        return;
    }

    // Fetch Attendance Data
    int totalClasses = 0, attendedClasses = 0;
    double attendancePercentage = 0.0;

    try {
        PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) AS total, SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END) AS present FROM attendance WHERE student_email = ?");
        ps.setString(1, userEmail);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            totalClasses = rs.getInt("total");
            attendedClasses = rs.getInt("present");
        }
        rs.close();
        ps.close();

        // Calculate attendance percentage
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
    <link rel="stylesheet" href="assets/css/style.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>
    <div class="container mt-4">
        <h2 class="text-center">Student Dashboard</h2>

        <!-- Attendance Stats -->
        <div class="row">
            <div class="col-md-6">
                <div class="card p-3">
                    <h4>Attendance Overview</h4>
                    <p>Total Classes: <strong><%= totalClasses %></strong></p>
                    <p>Classes Attended: <strong><%= attendedClasses %></strong></p>
                    <p>Attendance Percentage: <strong><%= String.format("%.2f", attendancePercentage) %>%</strong></p>

                    <% if (attendancePercentage < 75) { %>
                        <p class="text-danger"><strong>⚠ Warning:</strong> Your attendance is below 75%! Please attend classes.</p>
                    <% } else if (attendancePercentage >= 75 && attendancePercentage <= 78) { %>
                        <p class="text-warning"><strong>⚠ Alert:</strong> Your attendance is nearing 75%! Maintain regular attendance.</p>
                    <% } %>
                </div>
            </div>

            <div class="col-md-6">
                <!-- Attendance Chart -->
                <canvas id="attendanceChart"></canvas>
            </div>
        </div>

        <!-- Attendance Table -->
        <div class="card p-3 mt-4">
            <h4>Attendance History</h4>
            <table class="table table-bordered">
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        try {
                            PreparedStatement ps = conn.prepareStatement("SELECT date, status FROM attendance WHERE student_email = ? ORDER BY date DESC");
                            ps.setString(1, userEmail);
                            ResultSet rs = ps.executeQuery();

                            while (rs.next()) {
                    %>
                    <tr>
                        <td><%= rs.getDate("date") %></td>
                        <td><%= rs.getString("status") %></td>
                    </tr>
                    <%
                            }
                            rs.close();
                            ps.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    %>
                </tbody>
            </table>
        </div>

        <!-- Download Report Button -->
        <div class="text-center mt-3">
             <a href="<%= request.getContextPath() %>/downloadReport">Download Report</a>
        </div>
    </div>

    <script>
        const ctx = document.getElementById('attendanceChart').getContext('2d');
        new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Present', 'Absent'],
                datasets: [{
                    data: [<%= attendedClasses %>, <%= totalClasses - attendedClasses %>],
                    backgroundColor: ['#28a745', '#dc3545']
                }]
            }
        });
    </script>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
