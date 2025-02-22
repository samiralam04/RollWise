package com.attendance.controller;

import com.attendance.util.DBConnection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt; // Import this if using hashed passwords

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String role = request.getParameter("role");

        JSONObject jsonResponse = new JSONObject();

        if (email == null || password == null || role == null || email.isEmpty() || password.isEmpty()) {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Invalid Credentials");
            response.getWriter().write(jsonResponse.toString());
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id, username, password, role FROM users WHERE email = ?")) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                String storedRole = rs.getString("role");

                // Verify Password (use BCrypt if passwords are hashed)
                boolean passwordMatch = storedPassword.equals(password); // ❌ Plain text check
                // boolean passwordMatch = BCrypt.checkpw(password, storedPassword); // ✅ Use this if hashed

                if (passwordMatch && storedRole.equalsIgnoreCase(role)) {
                    HttpSession session = request.getSession();
                    session.setAttribute("userId", rs.getInt("id"));
                    session.setAttribute("username", rs.getString("username"));
                    session.setAttribute("email", email);
                    session.setAttribute("role", storedRole); // Store role from DB

                    // Debugging Logs (Remove in production)
                    System.out.println("User logged in: " + email + ", Role: " + storedRole);

                    // Determine redirect URL based on role
                    String redirectUrl = request.getContextPath() + "/pages/dashboard-" + storedRole.toLowerCase() + ".jsp";

                    jsonResponse.put("status", "success");
                    jsonResponse.put("redirect", redirectUrl);
                } else {
                    jsonResponse.put("status", "error");
                    jsonResponse.put("message", "Invalid Credentials");
                }
            } else {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Invalid Credentials");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Database Error: " + e.getMessage());
        }

        response.getWriter().write(jsonResponse.toString());
    }
}
