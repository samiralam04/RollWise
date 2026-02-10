<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.attendance.model.User" %>
<%
    User navUser = (User) session.getAttribute("user");
    String navRole = (navUser != null) ? navUser.getRole() : "";
    String dashboardLink = "login.jsp";
    if ("admin".equalsIgnoreCase(navRole)) dashboardLink = "pages/dashboard-admin.jsp";
    else if ("teacher".equalsIgnoreCase(navRole)) dashboardLink = "pages/dashboard-teacher.jsp";
    else if ("student".equalsIgnoreCase(navRole)) dashboardLink = "pages/dashboard-student.jsp";
%>
<nav class="navbar navbar-expand-lg navbar-dark bg-dark mb-4">
  <div class="container-fluid">
    <a class="navbar-brand" href="#">Attendance AI</a>
    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
      <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse" id="navbarNav">
      <ul class="navbar-nav ms-auto">
        <li class="nav-item">
          <a class="nav-link active" href="<%= request.getContextPath() %>/pages/login.jsp">Dashboard</a>
        </li>
        <% if (navUser != null) { %>
            <li class="nav-item">
              <a class="nav-link btn btn-danger text-white px-3" href="<%= request.getContextPath() %>/pages/logout.jsp">Logout</a>
            </li>
        <% } %>
      </ul>
    </div>
  </div>
</nav>
