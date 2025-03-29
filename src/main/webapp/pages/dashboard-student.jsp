<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="true" %>
<%@ page import="java.sql.*, java.util.*" %>

<%
    // Debugging Output
    System.out.println("Debug: Entering dashboard.jsp");

    // Get session values
    String userType = (String) session.getAttribute("role");
    String userEmail = (String) session.getAttribute("email");

    System.out.println("Debug: userType=" + userType);
    System.out.println("Debug: userEmail=" + userEmail);

    // Redirect to login if session is invalid or user is not a student
    if (userEmail == null || !"student".equals(userType)) {
        System.out.println("Debug: Unauthorized access. Redirecting to login.");
        response.sendRedirect("login.jsp");
        return;
    }

    // Get DB Connection
    Connection conn = (Connection) application.getAttribute("DBConnection");
    if (conn == null) {
        out.println("<h3 style='color:red;'>Database connection error!</h3>");
        System.out.println("Debug: Database connection error!");
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

        System.out.println("Debug: Retrieved studentId = " + studentId);

    } catch (Exception e) {
        e.printStackTrace();
    }

    // If student ID is not found, show an error
    if (studentId == -1) {
        out.println("<h3 style='color:red;'>Student not found!</h3>");
        System.out.println("Debug: Student ID not found for email " + userEmail);
        return;
    }

    // Fetch Attendance Data
    int totalClasses = 0, attendedClasses = 0;
    double attendancePercentage = 0.0;

    try {
        PreparedStatement ps2 = conn.prepareStatement(
            "SELECT COUNT(*) AS total, SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END) AS present " +
            "FROM attendance WHERE student_id = ?");
        ps2.setInt(1, studentId);
        ResultSet rs2 = ps2.executeQuery();

        if (rs2.next()) {
            totalClasses = rs2.getInt("total");
            attendedClasses = rs2.getInt("present");
        }
        rs2.close();
        ps2.close();

        // Calculate attendance percentage
        attendancePercentage = (totalClasses > 0) ? (attendedClasses * 100.0 / totalClasses) : 0;

        System.out.println("Debug: Attendance Data - Total: " + totalClasses + ", Attended: " + attendedClasses + ", Percentage: " + attendancePercentage);

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
                    <p><strong>Student ID:</strong> <%= studentId %></p>
                    <p><strong>Total Classes:</strong> <%= totalClasses %></p>
                    <p><strong>Classes Attended:</strong> <%= attendedClasses %></p>
                    <p><strong>Attendance Percentage:</strong> <%= String.format("%.2f", attendancePercentage) %>%</p>

                    <% if (attendancePercentage < 75) { %>
                        <p class="text-danger"><strong>⚠ Warning:</strong> Your attendance is below 75%! Please attend classes.</p>
                    <% } else if (attendancePercentage >= 75 && attendancePercentage <= 78) { %>
                        <p class="text-warning"><strong>⚠ Alert:</strong> Your attendance is nearing 75%! Maintain regular attendance.</p>
                    <% } %>
                </div>
            </div>

            <div class="col-md-6">
                <!-- Attendance Chart -->
                <canvas id="attendanceChart" style="max-width: 550px; max-height: 550px;"></canvas>
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
                            PreparedStatement ps3 = conn.prepareStatement(
                                "SELECT date, status FROM attendance WHERE student_id = ? ORDER BY date DESC");
                            ps3.setInt(1, studentId);
                            ResultSet rs3 = ps3.executeQuery();

                            while (rs3.next()) {
                    %>
                    <tr>
                        <td><%= rs3.getDate("date") %></td>
                        <td><%= rs3.getString("status") %></td>
                    </tr>
                    <%
                            }
                            rs3.close();
                            ps3.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    %>
                </tbody>
            </table>
        </div>

        <!-- Logout Button -->
        <div class="text-center mb-3"style="padding-top: 20px;">
            <a href="logout.jsp" class="btn btn-danger">Logout</a>
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
