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
import org.mindrot.jbcrypt.BCrypt;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String role = request.getParameter("role");

        JSONObject jsonResponse = new JSONObject();

        // Input Validation
        if (email == null || password == null || role == null || email.isEmpty() || password.isEmpty() || role.isEmpty()) {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "All fields are required.");
            response.getWriter().write(jsonResponse.toString());
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id, username, password FROM users WHERE email = ? AND role = ?")) {

            stmt.setString(1, email);
            stmt.setString(2, role);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHashedPassword = rs.getString("password");
                boolean passwordMatch = BCrypt.checkpw(password, storedHashedPassword); // Secure password check

                if (passwordMatch) {
                    // Create session
                    HttpSession session = request.getSession();
                    session.setAttribute("userId", rs.getInt("id"));
                    session.setAttribute("username", rs.getString("username"));
                    session.setAttribute("email", email);
                    session.setAttribute("role", role); // Store role

                    // Debugging Logs (Remove in production)
                    System.out.println("User logged in: " + email + ", Role: " + role);

                    // Redirect user to the appropriate dashboard
                    String redirectUrl = request.getContextPath() + "/pages/dashboard-" + role.toLowerCase() + ".jsp";
                    jsonResponse.put("status", "success");
                    jsonResponse.put("redirect", redirectUrl);
                } else {
                    jsonResponse.put("status", "error");
                    jsonResponse.put("message", "Invalid email or password.");
                }
            } else {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "User not found or role mismatch.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Database Error: " + e.getMessage());
        }

        response.getWriter().write(jsonResponse.toString());
    }
}
