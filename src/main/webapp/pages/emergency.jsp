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
            response.sendRedirect("emergency.jsp");
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Check if form was submitted
    boolean formSubmitted = "POST".equals(request.getMethod()) && request.getParameter("title") != null;

    // Fetch existing emergency notices with SCROLLABLE ResultSet
    PreparedStatement ps = conn.prepareStatement(
        "SELECT * FROM emergency ORDER BY date DESC",
        ResultSet.TYPE_SCROLL_INSENSITIVE,
        ResultSet.CONCUR_READ_ONLY
    );
    ResultSet rs = ps.executeQuery();

    // Get count first
    int count = 0;
    if (rs != null) {
        rs.last();
        count = rs.getRow();
        rs.beforeFirst();
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Emergency Alerts</title>

    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/emergency-alerts.css">

    <!-- Custom CSS -->
</head>
<body class="emergency-page">
    <!-- Loading Overlay - Only shown when form is actually submitted -->
    <div id="loadingOverlay" class="emergency-loading" style="<%= formSubmitted ? "display: flex;" : "display: none;" %>">
        <div class="loading-content">
            <div class="spinner-border text-danger" role="status">
                <span class="visually-hidden">Loading...</span>
            </div>
            <h3 class="mt-3">Sending Emergency Alert...</h3>
            <p class="text-muted">Please wait while we process your request</p>
        </div>
    </div>

    <div class="container emergency-container py-4">
        <!-- Header Section -->
        <div class="emergency-header text-center mb-5">
            <h1 class="display-5 fw-bold">
                <i class="fas fa-exclamation-triangle me-2"></i>Emergency Alerts
            </h1>
            <p class="lead text-muted">Critical notifications and holiday declarations</p>
        </div>

        <% if ("admin".equals(userType)) { %>
        <!-- Emergency Declaration Card -->
        <div class="card emergency-declare-card mb-5">
            <div class="card-header bg-gradient-danger text-white">
                <h4 class="mb-0">
                    <i class="fas fa-bullhorn me-2"></i>Declare Emergency
                </h4>
            </div>
            <div class="card-body">
                <form action="${pageContext.request.contextPath}/emergency" method="post" class="emergency-form">
                    <div class="row g-3">
                        <div class="col-md-12">
                            <div class="form-floating">
                                <input type="text" name="title" class="form-control" id="emergencyTitle" placeholder="Reason" required>
                                <label for="emergencyTitle"><i class="fas fa-comment-medical me-2"></i>Reason</label>
                            </div>
                        </div>
                        <div class="col-md-12">
                            <div class="form-floating">
                                <textarea name="description" class="form-control" id="emergencyDesc" placeholder="Description" style="height: 100px" required></textarea>
                                <label for="emergencyDesc"><i class="fas fa-align-left me-2"></i>Description</label>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-floating">
                                <input type="date" name="date" class="form-control" id="emergencyDate" required>
                                <label for="emergencyDate"><i class="fas fa-calendar-day me-2"></i>Date</label>
                            </div>
                        </div>
                        <div class="col-12 text-center mt-3">
                            <button type="submit" class="btn btn-declare-emergency">
                                <i class="fas fa-bell me-2"></i>Declare Emergency
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
        <% } %>

        <!-- Active Alerts Section -->
        <div class="emergency-alerts-section">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h3 class="emergency-alerts-title">
                    <i class="fas fa-list-ol me-2"></i>Active Emergency Notices
                </h3>
                <div class="alerts-count-badge">
                    <span class="badge rounded-pill bg-danger">
                        <%= count %> Active Alert<%= count != 1 ? "s" : "" %>
                    </span>
                </div>
            </div>

            <div class="card emergency-alerts-card">
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-emergency mb-0">
                            <thead>
                                <tr>
                                    <th><i class="fas fa-comment-medical me-1"></i>Reason</th>
                                    <th><i class="fas fa-align-left me-1"></i>Description</th>
                                    <th><i class="fas fa-calendar-day me-1"></i>Date</th>
                                    <% if ("admin".equals(userType)) { %>
                                        <th><i class="fas fa-cog me-1"></i>Action</th>
                                    <% } %>
                                </tr>
                            </thead>
                            <tbody>
                                <% if (rs != null) {
                                    while (rs.next()) { %>
                                <tr class="emergency-alert-row">
                                    <td>
                                        <div class="alert-reason">
                                            <i class="fas fa-exclamation-circle text-danger me-2"></i>
                                            <%= rs.getString("title") != null ? rs.getString("title") : "N/A" %>
                                        </div>
                                    </td>
                                    <td><%= rs.getString("description") != null ? rs.getString("description") : "N/A" %></td>
                                    <td><%= rs.getDate("date") != null ? rs.getDate("date") : "N/A" %></td>
                                    <% if ("admin".equals(userType)) { %>
                                    <td>
                                        <a href="emergency.jsp?deleteId=<%= rs.getInt("id") %>"
                                           class="btn btn-action btn-delete-emergency"
                                           onclick="return confirm('Are you sure you want to delete this emergency alert?');">
                                            <i class="fas fa-trash-alt"></i> Remove
                                        </a>
                                    </td>
                                    <% } %>
                                </tr>
                                <% }
                                rs.close();
                                }
                                ps.close();
                                %>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>

        <div class="text-center mt-5">
            <a href="dashboard-admin.jsp" class="btn btn-back-dashboard">
                <i class="fas fa-arrow-left me-2"></i>Back to Dashboard
            </a>
        </div>
    </div>

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

    <script>
        // Only show loading when form is actually submitted
        document.querySelector('.emergency-form').addEventListener('submit', function(e) {
            if (this.checkValidity()) {
                document.getElementById('loadingOverlay').style.display = 'flex';
            }
        });
    </script>
</body>
</html>