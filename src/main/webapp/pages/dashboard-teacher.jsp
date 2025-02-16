<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="true" %>
<%@ page import="java.sql.*, java.util.*" %>
<%
    String userType = (String) session.getAttribute("role");
    String userEmail = (String) session.getAttribute("email");

    if (userEmail == null || !userType.equals("teacher")) {
        response.sendRedirect("login.jsp");
        return;
    }

    Connection conn = (Connection) application.getAttribute("DBConnection");
    PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) AS total FROM students");
    ResultSet rs = ps.executeQuery();

    int totalStudents = 0;
    if (rs.next()) {
        totalStudents = rs.getInt("total");
    }
    rs.close();
    ps.close();
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
        <h2 class="text-center">Teacher Dashboard</h2>

        <div class="row">
            <!-- Attendance Upload Section -->
            <div class="col-md-6">
                <div class="card p-3">
                    <h4>Upload Attendance</h4>
                    <form action="UploadExcelServlet" method="post" enctype="multipart/form-data">
                        <input type="file" name="attendanceFile" class="form-control mb-2" required>
                        <button type="submit" class="btn btn-success">Upload</button>
                    </form>
                </div>
            </div>

            <!-- Attendance Marking Section -->
            <div class="col-md-6">
                <div class="card p-3">
                    <h4>Mark Attendance Manually</h4>
                    <form action="<%= request.getContextPath() %>/attendance" method="post">
                        <div class="mb-2">
                            <label>Student Id</label>
                            <input type="text" name="student_id" class="form-control" required>
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
                        <th>Student Id</th>
                        <th>Total Classes</th>
                        <th>Attended</th>
                        <th>Attendance %</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        ps = conn.prepareStatement("SELECT student_id, COUNT(*) AS total, SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END) AS present FROM attendance GROUP BY student_id");
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            String studentId = rs.getString("student_id");
                            int totalClasses = rs.getInt("total");
                            int attendedClasses = rs.getInt("present");
                            double percentage = (totalClasses > 0) ? (attendedClasses * 100.0 / totalClasses) : 0;
                        %>
                        <tr>
                            <td><%= studentId %></td>
                            <td><%= totalClasses %></td>
                            <td><%= attendedClasses %></td>
                            <td><%= String.format("%.2f", percentage) %> %</td>
                        </tr>
                        <% } rs.close(); ps.close(); %>
                </tbody>
            </table>
        </div>

        <!-- Download Report Button -->
        <div class="text-center mt-3">
            <a href="<%= request.getContextPath() %>/downloadReport">Download Report</a>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
