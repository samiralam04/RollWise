<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="true" %>
<%@ page import="java.sql.*, java.util.*" %>

<%
    String adminName = (String) session.getAttribute("username");
    String userType = (String) session.getAttribute("role"); // Fetch user type from session
    String message = request.getParameter("message"); // Success/Error message from servlet

    if (adminName == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
        // Get database connection
        con = com.attendance.util.DBConnection.getConnection();

        // Fetch holidays
        String query = "SELECT reason, date FROM holiday";
        ps = con.prepareStatement(query);
        rs = ps.executeQuery();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Holidays</title>
    <link rel="stylesheet" href="assets/css/style.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
</head>
<body>
    <div class="container mt-4">
        <h2 class="text-center">Holiday Management</h2>

        <% if (message != null) { %>
        <!-- Toast Notification -->
        <div class="position-fixed top-0 end-0 p-3" style="z-index: 11">
            <div class="toast show align-items-center text-white bg-success border-0" role="alert">
                <div class="d-flex">
                    <div class="toast-body">
                        <%= message %>
                    </div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
                </div>
            </div>
        </div>
        <% } %>

        <p>Logged in as: <%= adminName %></p>
        <p>User Type: <%= (userType != null) ? userType : "Not Set" %></p>

        <% if ("admin".equals(userType)) { %>
        <div class="card p-3 mb-4">
            <h4>Add New Holiday</h4>
           <form action="<%= request.getContextPath() %>/HolidayServlet" method="post">
                <div class="mb-2">
                    <label>Holiday Reason</label>
                    <input type="text" name="reason" class="form-control" placeholder="e.g., Diwali, Christmas" required>
                </div>
                <div class="mb-2">
                    <label>Date</label>
                    <input type="date" name="date" class="form-control" required>
                </div>
                <button type="submit" class="btn btn-success">Add Holiday</button>
            </form>
        </div>
        <% } else { %>
        <p class="text-danger">You do not have admin privileges to add holidays.</p>
        <% } %>

        <div class="card p-3">
            <h4>Upcoming Holidays</h4>
            <table class="table table-bordered">
                <thead>
                    <tr>
                        <th>Holiday Reason</th>
                        <th>Date</th>
                        <% if ("admin".equals(userType)) { %>
                        <th>Action</th>
                        <% } %>
                    </tr>
                </thead>
                <tbody>
                    <% while (rs.next()) {
                        String holidayDate = rs.getDate("date").toString();
                    %>
                    <tr>
                        <td><%= rs.getString("reason") %></td>
                        <td><%= holidayDate %></td>
                        <% if ("admin".equals(userType)) { %>
                        <td>
                            <button class="btn btn-danger btn-sm" onclick="confirmDelete('<%= holidayDate %>')">
                                <i class="bi bi-trash"></i>
                            </button>
                        </td>
                        <% } %>
                    </tr>
                    <% } %>
                </tbody>
            </table>
        </div>

        <div class="text-center mt-3">
            <a href="<%= request.getContextPath() %>/pages/dashboard-admin.jsp" class="btn btn-primary">Back to Dashboard</a>
        </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div class="modal fade" id="deleteModal" tabindex="-1" aria-labelledby="deleteModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="deleteModalLabel">Confirm Delete</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    Are you sure you want to delete this holiday?
                </div>
                <div class="modal-footer">
                    <form id="deleteForm" action="<%= request.getContextPath() %>/HolidayServlet" method="post">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="date" id="deleteDate">
                        <button type="submit" class="btn btn-danger">Delete</button>
                    </form>
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        function confirmDelete(date) {
            document.getElementById("deleteDate").value = date;
            var deleteModal = new bootstrap.Modal(document.getElementById("deleteModal"));
            deleteModal.show();
        }
    </script>
</body>
</html>

<%
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
        if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
        if (con != null) try { con.close(); } catch (SQLException ignored) {}
    }
%>
