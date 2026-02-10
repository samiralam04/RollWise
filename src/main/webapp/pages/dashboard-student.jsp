<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="true" %>
<%@ page import="java.sql.*, java.util.*" %>

<%
    // Get session values
    String userType = (String) session.getAttribute("role");
    String userEmail = (String) session.getAttribute("email");
    String userName = (String) session.getAttribute("username");

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

        <div style="display: flex; gap: 2rem; margin-bottom: 2rem; align-items: stretch;">
            <!-- Quick Actions -->
            <div class="activity-card" style="flex: 1; margin-bottom: 0; display: flex; flex-direction: column; justify-content: center; align-items: center; text-align: center;">
                <h3>Quick Actions</h3>
                <div style="display: flex; gap: 1rem; flex-wrap: wrap; justify-content: center; width: 100%;">
                    <a href="live_attendance.jsp" class="btn btn-primary" style="text-decoration: none; padding: 0.75rem 1.5rem; background-color: var(--primary); color: white; border-radius: 8px; font-weight: 500;">
                        <i class="fas fa-camera"></i> Mark Self-Attendance (AI)
                    </a>
                </div>
            </div>

            <!-- Excuse Upload Section -->
            <div class="activity-card" style="flex: 1; margin-bottom: 0;">
                <h3>Submit Excuse Letter</h3>
                <form id="excuseForm" enctype="multipart/form-data">
                    <input type="hidden" name="action" value="upload">
                    
                    <div class="row" style="display: flex; gap: 1rem; margin-bottom: 1rem;">
                        <div style="flex: 1;">
                            <label>Absence Date:</label>
                            <input type="date" name="absence_date" required class="form-control" style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;">
                        </div>
                        <div style="flex: 1;">
                            <label>Reason:</label>
                            <select name="reason" required class="form-control" style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;">
                                <option value="Medical">Medical Error/Sickness</option>
                                <option value="Family">Family Emergency</option>
                                <option value="Personal">Personal Issue</option>
                                <option value="Other">Other</option>
                            </select>
                        </div>
                    </div>

                    <div style="display: flex; gap: 1rem; align-items: center;">
                        <div style="flex: 1;">
                             <label>Upload Document (Image/PDF):</label>
                             <input type="file" name="document" accept=".jpg,.jpeg,.png,.pdf" required style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;">
                        </div>
                        <button type="submit" class="btn btn-primary" style="padding: 0.5rem 1rem; background-color: var(--primary); color: white; border: none; border-radius: 4px; cursor: pointer; align-self: flex-end;">
                            <i class="fas fa-upload"></i> Upload & Process
                        </button>
                    </div>
                    <div id="excuseLoading" style="display:none; color: #666; margin-top: 10px;">Processing request...</div>
                </form>
            </div>
        </div>

        <!-- Excuse Status Section -->
        <div class="activity-card" style="margin-bottom: 2rem;">
            <h3>My Excuse Requests</h3>
            <div class="table-container">
                <table style="width: 100%; border-collapse: collapse;">
                    <thead>
                        <tr style="background: #f8f9fa; border-bottom: 2px solid #eee;">
                            <th style="padding: 10px; text-align: left;">Date Applied</th>
                            <th style="padding: 10px; text-align: left;">Absence Date</th>
                            <th style="padding: 10px; text-align: left;">Reason</th>
                            <th style="padding: 10px; text-align: left;">Category (AI)</th>
                            <th style="padding: 10px; text-align: left;">Status</th>
                        </tr>
                    </thead>
                    <tbody id="excuseTableBody">
                        <tr><td colspan="5" style="padding: 10px; text-align: center;">Loading...</td></tr>
                    </tbody>
                </table>
            </div>
        </div>
        
        <script>
        document.getElementById('excuseForm').addEventListener('submit', function(e) {
            e.preventDefault();
            const formData = new FormData(this);
            document.getElementById('excuseLoading').style.display = 'block';
            
            fetch('${pageContext.request.contextPath}/ExcuseLetterServlet', {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                document.getElementById('excuseLoading').style.display = 'none';
                if (data.success) {
                    alert('Excuse uploaded successfully!');
                    loadMyExcuses(); // Refresh list
                    this.reset();
                } else {
                    alert('Error: ' + data.message);
                }
            })
            .catch(error => {
                document.getElementById('excuseLoading').style.display = 'none';
                console.error('Error:', error);
                alert('An error occurred during upload.');
            });
        });

        function loadMyExcuses() {
            const formData = new URLSearchParams();
            formData.append('action', 'list');

            fetch('${pageContext.request.contextPath}/ExcuseLetterServlet', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: formData
            })
            .then(res => res.json())
            .then(data => {
                const tbody = document.getElementById('excuseTableBody');
                tbody.innerHTML = '';
                if(data.success && data.excuses.length > 0) {
                    data.excuses.forEach(ex => {
                        let statusColor = '#f0ad4e'; // Pending
                        if(ex.status === 'APPROVED') statusColor = '#5cb85c';
                        if(ex.status === 'REJECTED') statusColor = '#d9534f';

                        const row = `
                            <tr style="border-bottom: 1px solid #eee;">
                                <td style="padding: 10px;">` + ex.created_at.split(' ')[0] + `</td>
                                <td style="padding: 10px;">` + (ex.absence_date || '-') + `</td>
                                <td style="padding: 10px;">` + (ex.reason || '-') + `</td>
                                <td style="padding: 10px;">` + ex.category + `</td>
                                <td style="padding: 10px;">
                                    <span style="background-color: ` + statusColor + `; color: white; padding: 4px 8px; border-radius: 4px; font-size: 0.85em;">
                                        ` + ex.status + `
                                    </span>
                                </td>
                            </tr>
                        `;
                        tbody.innerHTML += row;
                    });
                } else {
                    tbody.innerHTML = '<tr><td colspan="5" style="padding: 10px; text-align: center;">No excuse requests found.</td></tr>';
                }
            })
            .catch(err => console.error(err));
        }

        // Load on startup
        document.addEventListener('DOMContentLoaded', loadMyExcuses);
        </script>

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
                const attendedCount = <%= attendedClasses %>;
                const absentCount = <%= totalClasses - attendedClasses %>;
                
                new Chart(ctx, {
            type: 'pie',
            data: {
                labels: ['Present', 'Absent'],
                datasets: [{
                    data: [attendedCount, absentCount],
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