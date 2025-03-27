<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="true" %>
<%@ page import="java.sql.*, java.util.*" %>

<%
    String userType = (String) session.getAttribute("role");
    String userEmail = (String) session.getAttribute("email");

    if (userEmail == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    Connection conn = (Connection) application.getAttribute("DBConnection");

    // Check if a delete request is made
    String deleteId = request.getParameter("deleteId");
    if (deleteId != null && "admin".equals(userType)) {
        try {
            PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM emergency WHERE id = ?");
            deleteStmt.setInt(1, Integer.parseInt(deleteId));
            deleteStmt.executeUpdate();
            deleteStmt.close();
            response.sendRedirect("emergency.jsp"); // Refresh the page after deletion
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Fetch existing emergency notices
    PreparedStatement ps = conn.prepareStatement("SELECT * FROM emergency ORDER BY date DESC");
    ResultSet rs = ps.executeQuery();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Emergency Alerts</title>
    <link rel="stylesheet" href="assets/css/style.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <style>
        /* Loading Screen Styles */
        #loadingOverlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.7);
            color: white;
            display: flex;
            justify-content: center;
            align-items: center;
            font-size: 1.5rem;
            font-weight: bold;
            z-index: 9999;
            display: none;
        }
    </style>
</head>
<body>
    <!-- Loading Screen -->
    <div id="loadingOverlay">Sending Alert... Please Wait</div>

    <div class="container mt-4">
        <h2 class="text-center">Emergency Alerts</h2>

        <% if ("admin".equals(userType)) { %>
        <!-- Admin can declare new emergency -->
        <div class="card p-3 mb-4">
            <h4>Declare Emergency Holiday</h4>
            <form action="${pageContext.request.contextPath}/emergency" method="post" onsubmit="showLoading()">
                <div class="mb-2">
                    <label>Reason</label>
                    <input type="text" name="title" class="form-control" placeholder="e.g., Heavy Rain, Flood" required>
                </div>
                <div class="mb-2">
                    <label>Description</label>
                    <textarea name="description" class="form-control" placeholder="Additional details" required></textarea>
                </div>
                <div class="mb-2">
                    <label>Date</label>
                    <input type="date" name="date" class="form-control" required>
                </div>
                <button type="submit" class="btn btn-danger">Declare Emergency</button>
            </form>
        </div>
        <% } %>

        <!-- Display Emergency Notices -->
        <div class="card p-3">
            <h4>Active Emergency Notices</h4>
            <table class="table table-bordered">
                <thead>
                    <tr>
                        <th>Reason</th>
                        <th>Description</th>
                        <th>Date</th>
                        <% if ("admin".equals(userType)) { %>
                            <th>Action</th>
                        <% } %>
                    </tr>
                </thead>
                <tbody>
                    <% while (rs.next()) { %>
                    <tr>
                        <td><%= rs.getString("title") != null ? rs.getString("title") : "N/A" %></td>
                        <td><%= rs.getString("description") != null ? rs.getString("description") : "N/A" %></td>
                        <td><%= rs.getDate("date") != null ? rs.getDate("date") : "N/A" %></td>
                        <% if ("admin".equals(userType)) { %>
                        <td>
                            <a href="emergency.jsp?deleteId=<%= rs.getInt("id") %>" class="btn btn-sm btn-danger"
                               onclick="return confirm('Are you sure you want to delete this alert?');">Delete</a>
                        </td>
                        <% } %>
                    </tr>
                    <% } rs.close(); ps.close(); %>
                </tbody>
            </table>
        </div>

        <div class="text-center mt-3">
            <a href="dashboard-admin.jsp" class="btn btn-primary">Back to Dashboard</a>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

    <script>
        function showLoading() {
            document.getElementById("loadingOverlay").style.display = "flex";
        }
    </script>
</body>
</html>
