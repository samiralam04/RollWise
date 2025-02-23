<%@ page import="java.util.List" %>
<%@ page import="com.attendance.model.Teacher" %>
<%@ page import="com.attendance.dao.TeacherDAO" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page pageEncoding="UTF-8" %>

<%
    // Fetch teacher list
    TeacherDAO teacherDAO = new TeacherDAO();
    List<Teacher> teacherList = teacherDAO.getAllTeachers();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Teachers</title>
    <link rel="stylesheet" href="styles.css"> <!-- Link your CSS -->
</head>
<body>
    <div class="container">
        <h2>Manage Teachers</h2>

        <!-- Add Teacher Form -->
        <form action="<%= request.getContextPath() %>/teacher" method="post">
           <input type="text" name="name" placeholder="Name" required>
               <input type="email" name="email" placeholder="Email" required>
               <input type="text" name="phone" placeholder="Phone" required>
               <input type="text" name="subject" placeholder="Subject" required>
               <input type="text" name="username" placeholder="Username" required>
               <input type="password" name="password" placeholder="Password" required>
               <button type="submit">Add Teacher</button>
        </form>

        <hr>

        <!-- Teacher List -->
        <h3>Teacher List</h3>
        <table border="1">
            <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Subject</th>
                <th>Username</th>
                <th>Actions</th>
            </tr>
            <% for (Teacher teacher : teacherList) { %>
                <tr>
                    <td><%= teacher.getId() %></td>
                    <td><%= teacher.getName() %></td>
                    <td><%= teacher.getEmail() %></td>
                    <td><%= teacher.getPhone() %></td>
                    <td><%= teacher.getSubject() %></td>
                    <td><%= teacher.getUsername() %></td>
                    <td>
                        <!-- Edit Button -->
                        <form action="<%= request.getContextPath() %>/teacher" method="get" style="display:inline;">
                            <input type="hidden" name="action" value="edit">
                            <input type="hidden" name="id" value="<%= teacher.getId() %>">
                            <button type="submit">Edit</button>
                        </form>

                        <!-- Delete Button -->
                        <form action="<%= request.getContextPath() %>/teacher" method="post" style="display:inline;">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="<%= teacher.getId() %>">
                            <button type="submit" onclick="return confirm('Are you sure?')">Delete</button>
                        </form>
                    </td>
                </tr>
            <% } %>
        </table>
    </div>
</body>
</html>
