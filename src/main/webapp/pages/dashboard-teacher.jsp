<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="true" %>
<%@ page import="java.sql.*, java.util.*" %>

<%
    // Get user session attributes
    String userType = (String) session.getAttribute("role");
    String userEmail = (String) session.getAttribute("email");

    // Redirect if user is not logged in or not a teacher
    if (userEmail == null || !"teacher".equals(userType)) {
        response.sendRedirect("login.jsp");
        return;
    }

    // Database connection retrieval
    Connection conn = (Connection) application.getAttribute("DBConnection");
    int totalStudents = 0;

    if (conn != null) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) AS total FROM students");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                totalStudents = rs.getInt("total");
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
</head>
<body>
    <div class="container mt-4">
        <!-- Logout Button -->
        <div class="text-end mb-3">
            <a href="logout.jsp" class="btn btn-danger">Logout</a>
        </div>

        <h2 class="text-center">Teacher Dashboard</h2>

        <div class="row">
            <!-- Attendance Upload Section -->
            <div class="col-md-6">
                <div class="card p-3">
                    <h4>Upload Attendance</h4>
                    <form id="uploadForm" action="<%= request.getContextPath() %>/UploadExcelServlet" method="POST" enctype="multipart/form-data">
                        <input type="file" name="file" class="form-control mb-2" required>
                        <button type="submit" class="btn btn-success">Upload</button>
                    </form>
                </div>
            </div>

            <!-- Attendance Marking Section -->
            <div class="col-md-6">
                <div class="card p-3">
                    <h4>Mark Attendance Manually</h4>
                    <form action="<%= request.getContextPath() %>/attendance" method="post" onsubmit="return showSuccessPopup()">
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
                            <input type="date" name="date" class="form-control" id="attendanceDate" required>
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

        <!-- Attendance Report Table -->
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
                                   "LEFT JOIN parents p ON a.parent_email_id = p.id " +  // Fixed join condition
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

        <!-- Download Report Button -->
        <div class="text-center mt-3">
            <a href="<%= request.getContextPath() %>/downloadReport" class="btn btn-secondary">Download Report</a>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

    <script>
        // Prevent selecting future dates
        document.addEventListener("DOMContentLoaded", function () {
            let today = new Date().toISOString().split('T')[0];
            document.getElementById("attendanceDate").setAttribute("max", today);
        });

        // Show success popup instead of redirecting
        function showSuccessPopup() {
            alert("Attendance marked successfully!");
            return true;
        }
    </script>

    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

    <script>
    document.getElementById("uploadForm").addEventListener("submit", function(event) {
        event.preventDefault();

        let formData = new FormData(this);

        fetch("<%= request.getContextPath() %>/UploadExcelServlet", {
            method: "POST",
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            Swal.fire({
                icon: data.success ? "success" : "error",
                title: data.success ? "Upload Successful" : "Upload Failed",
                text: data.message || "Something went wrong",
                confirmButtonText: "OK"
            });
        })
        .catch(() => {
            Swal.fire({
                icon: "error",
                title: "Error",
                text: "Something went wrong while uploading.",
                confirmButtonText: "Close"
            });
        });
    });
    </script>

</body>
</html>
