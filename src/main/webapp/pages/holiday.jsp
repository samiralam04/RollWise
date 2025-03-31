<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="true" %>
<%@ page import="java.sql.*, java.util.*" %>

<%
    String adminName = (String) session.getAttribute("username");
    String userType = (String) session.getAttribute("role");
    String message = request.getParameter("message");

    if (adminName == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
        con = com.attendance.util.DBConnection.getConnection();
        String query = "SELECT reason, date FROM holiday ORDER BY date ASC";
        ps = con.prepareStatement(query);
        rs = ps.executeQuery();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Holidays</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/holiday.css">

</head>
<body>
    <div id="loadingScreen">
        <div class="loading-content">
            <div class="loading-spinner"></div>
            <h4>Processing Holiday Request</h4>
            <p class="loading-text">
                <i class="bi bi-envelope"></i> Sending holiday notification emails...<br>
                <i class="bi bi-database"></i> Updating holiday database...
            </p>
            <div class="progress mt-2" style="width: 80%;">
                <div class="progress-bar progress-bar-striped progress-bar-animated" style="width: 100%"></div>
            </div>
        </div>
    </div>

    <div class="container holiday-container">
        <div class="holiday-header">
            <h1><i class="bi bi-calendar-check"></i> Holiday Management</h1>
            <div class="admin-info">
                <div class="admin-icon">
                    <i class="bi bi-person-gear"></i>
                </div>
                <span class="admin-name"><%= adminName %></span>
            </div>
        </div>

        <% if (message != null) { %>
        <div class="toast-container position-fixed top-0 end-0 p-3">
            <div class="toast show align-items-center" role="alert" aria-live="assertive" aria-atomic="true">
                <div class="d-flex">
                    <div class="toast-body">
                        <i class="bi bi-check-circle-fill text-success me-2"></i>
                        <%= message %>
                    </div>
                    <button type="button" class="btn-close me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
                </div>
            </div>
        </div>
        <% } %>

        <% if ("admin".equals(userType)) { %>
        <div class="card holiday-form-card">
            <div class="card-header">
                <h3 class="mb-0"><i class="bi bi-plus-circle"></i> Add New Holiday</h3>
            </div>
            <div class="card-body">
                <form action="<%= request.getContextPath() %>/HolidayServlet" method="post" onsubmit="showLoading()" id="holidayForm">
                    <div class="mb-3">
                        <label class="form-label"><i class="bi bi-chat-square-text"></i> Holiday Reason</label>
                        <input type="text" name="reason" class="form-control" placeholder="e.g., Diwali, Christmas" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label"><i class="bi bi-calendar-date"></i> Date</label>
                        <input type="date" name="date" class="form-control" required>
                    </div>
                    <button type="submit" class="btn btn-primary" id="submitBtn">
                        <i class="bi bi-save"></i> Add Holiday
                    </button>
                </form>
            </div>
        </div>
        <% } else { %>
        <div class="alert alert-warning" role="alert">
            <i class="bi bi-exclamation-triangle-fill"></i> You do not have admin privileges to add holidays.
        </div>
        <% } %>

        <div class="card holiday-list-card">
            <div class="card-header">
                <h3 class="mb-0"><i class="bi bi-calendar-week"></i> Upcoming Holidays</h3>
            </div>
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table table-hover">
                        <thead class="table-light">
                            <tr>
                                <th><i class="bi bi-chat-text"></i> Holiday Reason</th>
                                <th><i class="bi bi-calendar"></i> Date</th>
                                <% if ("admin".equals(userType)) { %>
                                <th><i class="bi bi-gear"></i> Action</th>
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
                                    <button class="btn btn-sm btn-danger" onclick="confirmDelete('<%= holidayDate %>')">
                                        <i class="bi bi-trash"></i> Delete
                                    </button>
                                </td>
                                <% } %>
                            </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <div class="text-center mt-4">
            <a href="<%= request.getContextPath() %>/pages/dashboard-admin.jsp" class="btn btn-outline-primary">
                <i class="bi bi-arrow-left"></i> Back to Dashboard
            </a>
        </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div class="modal fade" id="deleteModal" tabindex="-1" aria-labelledby="deleteModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header bg-danger text-white">
                    <h5 class="modal-title" id="deleteModalLabel"><i class="bi bi-exclamation-triangle"></i> Confirm Delete</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <p>Are you sure you want to delete this holiday?</p>
                    <p class="text-muted">This action cannot be undone.</p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                        <i class="bi bi-x-circle"></i> Cancel
                    </button>
                    <form id="deleteForm" action="<%= request.getContextPath() %>/HolidayServlet" method="post">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="date" id="deleteDate">
                        <button type="submit" class="btn btn-danger">
                            <i class="bi bi-trash"></i> Delete
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        function showLoading() {
            document.body.classList.add('loading');
            const loadingScreen = document.getElementById("loadingScreen");
            loadingScreen.style.display = "flex";
            setTimeout(() => loadingScreen.style.opacity = "1", 10);

            // Disable submit button to prevent duplicate submissions
            const submitBtn = document.getElementById("submitBtn");
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<i class="bi bi-hourglass"></i> Processing...';
            }
        }

        function confirmDelete(date) {
            document.getElementById("deleteDate").value = date;
            const deleteModal = new bootstrap.Modal(document.getElementById('deleteModal'));

            // Add loading state to delete form submission
            const deleteForm = document.getElementById("deleteForm");
            deleteForm.addEventListener('submit', function(e) {
                showLoading();
                const deleteBtn = this.querySelector('button[type="submit"]');
                if (deleteBtn) {
                    deleteBtn.disabled = true;
                    deleteBtn.innerHTML = '<i class="bi bi-hourglass"></i> Deleting...';
                }
            });

            deleteModal.show();
        }

        // Auto-dismiss toast after 5 seconds
        document.addEventListener('DOMContentLoaded', function() {
            var toastEl = document.querySelector('.toast');
            if (toastEl) {
                setTimeout(function() {
                    const toast = new bootstrap.Toast(toastEl);
                    toast.hide();
                }, 5000);
            }

            // Ensure loading screen is hidden if page reloads
            document.body.classList.remove('loading');
            document.getElementById("loadingScreen").style.opacity = "0";
            setTimeout(() => {
                document.getElementById("loadingScreen").style.display = "none";
            }, 400);

            // Re-enable submit button if still disabled (e.g., back button navigation)
            const submitBtn = document.getElementById("submitBtn");
            if (submitBtn && submitBtn.disabled) {
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<i class="bi bi-save"></i> Add Holiday';
            }
        });
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