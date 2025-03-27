<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="true" %>
<%@ page import="java.sql.*, java.util.*" %>

<%
    String userType = (String) session.getAttribute("role");
    String userEmail = (String) session.getAttribute("email");

    if (userEmail == null || !"teacher".equals(userType)) {
        response.sendRedirect("login.jsp");
        return;
    }

    Connection conn = (Connection) application.getAttribute("DBConnection");

    List<Map<String, String>> attendanceRecords = new ArrayList<>();
    if (conn != null) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT student_id, date, status FROM attendance ORDER BY date DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, String> record = new HashMap<>();
                record.put("student_id", rs.getString("student_id"));
                record.put("date", rs.getString("date"));
                record.put("status", rs.getString("status"));
                attendanceRecords.add(record);
            }
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
    <title>Teacher Dashboard</title>
    <link rel="stylesheet" href="assets/css/style.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <style>
        .loading-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(255, 255, 255, 0.8);
            display: none;
            justify-content: center;
            align-items: center;
            flex-direction: column;
            z-index: 9999;
        }
        .spinner-border {
            width: 3rem;
            height: 3rem;
        }
        .loading-message {
            margin-top: 10px;
            font-size: 18px;
            font-weight: bold;
            color: #333;
        }
    </style>
</head>
<body>
    <div class="loading-overlay" id="loadingSpinner">
        <div class="spinner-border text-primary" role="status"></div>
        <div class="loading-message" id="loadingMessage">Processing...</div>
    </div>

    <div class="container mt-4">
        <div class="text-end mb-3">
            <a href="logout.jsp" class="btn btn-danger">Logout</a>
        </div>

        <h2 class="text-center">Teacher Dashboard</h2>

        <div class="row">
            <div class="col-md-6">
                <div class="card p-3">
                    <h4>Upload Attendance</h4>
                    <form id="uploadForm" action="<%= request.getContextPath() %>/UploadExcelServlet" method="POST" enctype="multipart/form-data">
                        <input type="file" name="file" class="form-control mb-2" required>
                        <button type="submit" class="btn btn-success">Upload</button>
                    </form>
                </div>
            </div>

            <div class="col-md-6">
                <div class="card p-3">
                    <h4>Mark Attendance Manually</h4>
                    <form id="attendanceForm" action="<%= request.getContextPath() %>/attendance" method="post">
                        <div class="row">
                            <div class="col-md-6 mb-2">
                                <label>Student ID</label>
                                <input type="text" name="student_id" class="form-control" required>
                            </div>
                            <div class="col-md-6 mb-2">
                                <label>Teacher ID</label>
                                <input type="text" name="teacher_id" class="form-control" required>
                            </div>
                        </div>
                        <div class="mb-2">
                            <label>Date</label>
                            <input type="date" name="date" class="form-control" required>
                        </div>
                        <div class="mb-2">
                            <label>Status</label>
                            <select name="status" class="form-control">
                                <option value="Present">Present</option>
                                <option value="Absent">Absent</option>
                            </select>
                        </div>
                        <button type="submit" class="btn btn-primary">Mark Attendance</button>
                    </form>
                </div>
            </div>
        </div>

        <!-- Attendance Table -->
        <div class="card p-3 mt-4">
            <h4>Student Attendance Report</h4>
            <table class="table table-bordered">
                <thead>
                    <tr>
                        <th>Student ID</th>
                        <th>Teacher ID</th>
                        <th>Date</th>
                        <th>Parent Email</th>
                        <th>Total Classes</th>
                        <th>Attended</th>
                        <th>Attendance %</th>
                    </tr>
                </thead>
                <tbody>
                   <%
                       if (conn != null) {
                           try (PreparedStatement ps = conn.prepareStatement(
                                   "SELECT a.student_id, a.teacher_id, a.date, p.parent_email, " +
                                   "COUNT(a.student_id) OVER (PARTITION BY a.student_id) AS total_classes, " +
                                   "SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END) OVER (PARTITION BY a.student_id) AS attended_classes " +
                                   "FROM attendance a " +
                                   "LEFT JOIN parents p ON a.parent_email_id = p.id " +
                                   "ORDER BY a.date DESC"
                           );
                                ResultSet rs = ps.executeQuery()) {
                               while (rs.next()) {
                                   String studentId = rs.getString("student_id");
                                   String teacherId = rs.getString("teacher_id");
                                   String date = rs.getString("date");
                                   String parentEmail = rs.getString("parent_email");
                                   int totalClasses = rs.getInt("total_classes");
                                   int attendedClasses = rs.getInt("attended_classes");
                                   double percentage = (totalClasses > 0) ? (attendedClasses * 100.0 / totalClasses) : 0;
                   %>
                       <tr>
                           <td><%= studentId %></td>
                           <td><%= teacherId %></td>
                           <td><%= date %></td>
                           <td><%= (parentEmail != null && !parentEmail.isEmpty()) ? parentEmail : "N/A" %></td>
                           <td><%= totalClasses %></td>
                           <td><%= attendedClasses %></td>
                           <td><%= String.format("%.2f", percentage) %> %</td>
                       </tr>
                   <%
                               }
                           } catch (SQLException e) {
                               e.printStackTrace();
                           }
                       }
                   %>
                </tbody>
            </table>
        </div>

    </div>

    <script>
        function showLoading(message) {
            document.getElementById("loadingSpinner").style.display = "flex";
            document.getElementById("loadingMessage").innerText = message;
        }

        function hideLoading() {
            document.getElementById("loadingSpinner").style.display = "none";
        }

        document.getElementById("uploadForm").addEventListener("submit", function(event) {
            event.preventDefault();
            showLoading(" Attendance is being uploaded...");

            const formData = new FormData(this);
            fetch(this.action, { method: "POST", body: formData })
                .then(response => response.json())
                .then(data => {
                    hideLoading();
                    if (data.success) {
                        Swal.fire({
                            icon: "success",
                            title: "Success",
                            text: data.message,
                            confirmButtonColor: "#3085d6",
                        }).then(() => {
                            location.reload(); // Refresh table after upload
                        });
                    } else {
                        Swal.fire({
                            icon: "error",
                            title: "Error",
                            text: "Failed to upload attendance.",
                            confirmButtonColor: "#d33",
                        });
                    }
                })
                .catch(error => {
                    hideLoading();
                    Swal.fire({
                        icon: "error",
                        title: "Error",
                        text: "Something went wrong!",
                        confirmButtonColor: "#d33",
                    });
                });
        });

        document.getElementById("attendanceForm").addEventListener("submit", function(event) {
            event.preventDefault();
            showLoading("Marking Attendance...");

            const formData = new FormData(this);
            fetch(this.action, { method: "POST", body: formData })
                .then(response => response.json())
                .then(data => {
                    hideLoading();
                    if (data.success) {
                        Swal.fire({
                            icon: "success",
                            title: "Success",
                            text: data.message,
                            confirmButtonColor: "#3085d6",
                        }).then(() => {
                            location.reload(); // Refresh table after marking attendance
                        });
                    } else {
                        Swal.fire({
                            icon: "error",
                            title: "Error",
                            text: "Failed to mark attendance.",
                            confirmButtonColor: "#d33",
                        });
                    }
                })
                .catch(error => {
                    hideLoading();
                    Swal.fire({
                        icon: "error",
                        title: "Error",
                        text: "Something went wrong!",
                        confirmButtonColor: "#d33",
                    });
                });
        });
    </script>
</body>
</html>
