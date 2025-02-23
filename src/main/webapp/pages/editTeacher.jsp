<%@ page import="com.attendance.model.Teacher" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page pageEncoding="UTF-8" %>

<%
    Teacher teacher = (Teacher) request.getAttribute("teacher");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Teacher</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
    <div class="container">
        <h2>Edit Teacher</h2>
        <form action="<%= request.getContextPath() %>/teacher" method="post">
            <input type="hidden" name="action" value="update">
            <input type="hidden" name="id" value="<%= teacher.getId() %>">
            <input type="text" name="name" value="<%= teacher.getName() %>" required>
            <input type="email" name="email" value="<%= teacher.getEmail() %>" required>
            <input type="text" name="phone" value="<%= teacher.getPhone() %>" required>
            <input type="text" name="subject" value="<%= teacher.getSubject() %>" required>
            <input type="text" name="username" value="<%= teacher.getUsername() %>" required>
            <button type="submit">Update Teacher</button>
        </form>
    </div>
</body>
</html>
