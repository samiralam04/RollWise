<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Prevent caching so users can't use the back button
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);

    // Invalidate session
    if (session != null) {
        session.invalidate();
    }

    // Redirect to login page
    response.sendRedirect("login.jsp");
%>
