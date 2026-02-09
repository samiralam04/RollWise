<%@ page import="com.attendance.util.DBConnection" %>
<%@ page import="java.sql.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head><title>DB Debug</title></head>
<body>
    <h1>Classes</h1>
    <table border="1">
        <tr><th>ID</th><th>Class Name</th></tr>
        <%
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM classes")) {
                while (rs.next()) {
                    out.println("<tr><td>" + rs.getInt("id") + "</td><td>" + rs.getString("class_name") + "</td></tr>");
                }
            } catch (Exception e) { out.println(e); }
        %>
    </table>

    <h1>Students</h1>
    <table border="1">
        <tr><th>ID</th><th>Name</th><th>Class ID</th><th>Class Name (Joined)</th></tr>
        <%
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT s.id, u.username, s.class_id, c.class_name FROM students s JOIN users u ON s.user_id = u.id LEFT JOIN classes c ON s.class_id = c.id")) {
                while (rs.next()) {
                    out.println("<tr><td>" + rs.getInt("id") + "</td><td>" + rs.getString("username") + "</td><td>" + rs.getInt("class_id") + "</td><td>" + rs.getString("class_name") + "</td></tr>");
                }
            } catch (Exception e) { out.println(e); }
        %>
    </table>
</body>
</html>
