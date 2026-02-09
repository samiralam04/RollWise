<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.attendance.model.User" %>
<%@ page import="java.net.HttpURLConnection" %>
<%@ page import="java.net.URL" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.InputStreamReader" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"ADMIN".equals(user.getRole())) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>System Status</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .status-card { border-radius: 10px; padding: 20px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); margin-bottom: 20px; }
        .status-ok { background-color: #d1e7dd; color: #0f5132; }
        .status-err { background-color: #f8d7da; color: #842029; }
    </style>
</head>
<body>
    <jsp:include page="../navbar.jsp" />

    <div class="container mt-5">
        <h2 class="mb-4">System Status & Health</h2>
        
        <div class="row">
            <!-- Python Service Status -->
            <div class="col-md-6">
                <%
                    boolean pythonUp = false;
                    String aiMode = "UNKNOWN";
                    String message = "";
                    try {
                        URL url = new URL("http://localhost:5000/health");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setConnectTimeout(2000);
                        conn.setRequestMethod("GET");
                        int code = conn.getResponseCode();
                        if (code == 200) {
                            pythonUp = true;
                            // Check headers if available or parse body
                             try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                                StringBuilder sb = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) sb.append(line);
                                JSONObject json = new JSONObject(sb.toString());
                                message = json.optString("status");
                            }
                            // In a real call we get X-AI-MODE, here we might need a real call or just trust it's up.
                            // Let's force a call to verify-live-student with no data to check headers? 
                            // Or just assume REAL/MOCK based on config?
                            // For now, simple health check.
                        }
                    } catch (Exception e) {
                        message = e.getMessage();
                    }
                %>
                <div class="status-card <%= pythonUp ? "status-ok" : "status-err" %>">
                    <h4>Python AI Service</h4>
                    <p><strong>Status:</strong> <%= pythonUp ? "ONLINE" : "OFFLINE" %></p>
                    <p><strong>Message:</strong> <%= message %></p>
                </div>
            </div>

            <!-- Database Status -->
            <div class="col-md-6">
                <%
                    boolean dbUp = false;
                    try {
                        com.attendance.util.DBConnection.getConnection().close();
                        dbUp = true;
                    } catch (Exception e) { }
                %>
                <div class="status-card <%= dbUp ? "status-ok" : "status-err" %>">
                    <h4>Database Connection</h4>
                    <p><strong>Status:</strong> <%= dbUp ? "ONLINE" : "OFFLINE" %></p>
                    <p><strong>Database:</strong> PostgreSQL</p>
                </div>
            </div>
        </div>

        <div class="row mt-4">
             <div class="col-12">
                 <div class="card">
                     <div class="card-header">Configuration Checks</div>
                     <ul class="list-group list-group-flush">
                         <li class="list-group-item"><strong>Max Self-Attendance Attempts:</strong> 1 per day</li>
                         <li class="list-group-item"><strong>Face Match Threshold:</strong> 0.6</li>
                         <li class="list-group-item"><strong>Minimum Face Resolution:</strong> 100x100px</li>
                     </ul>
                 </div>
             </div>
        </div>
        
    </div>
</body>
</html>
