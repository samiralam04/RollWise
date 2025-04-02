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

        try (Connection conn = DBConnection.getConnection()) {
            boolean loginSuccess = false;
            String username = null;
            int userId = -1;

            // Query to fetch user data based on role
            String query = "SELECT id, username, password FROM users WHERE email = ? AND role = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, email);
                stmt.setString(2, role);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // User found in "users" table
                        String storedHashedPassword = rs.getString("password");
                        if (BCrypt.checkpw(password, storedHashedPassword)) {
                            loginSuccess = true;
                            userId = rs.getInt("id");
                            username = rs.getString("username");
                        }
                    }
                }
            }

            // If the role is "Teacher" and user is not found in "users" table, check "teacher" table
            if (!loginSuccess && "Teacher".equalsIgnoreCase(role)) {
                query = "SELECT id, name, password FROM teacher WHERE email = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, email);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            // User found in "teacher" table
                            String storedHashedPassword = rs.getString("password");
                            if (BCrypt.checkpw(password, storedHashedPassword)) {
                                loginSuccess = true;
                                userId = rs.getInt("id");
                                username = rs.getString("name");
                            }
                        }
                    }
                }
            }

            if (loginSuccess) {
                // Successful login
                HttpSession session = request.getSession(true);
                session.setAttribute("userId", userId);
                session.setAttribute("username", username);
                session.setAttribute("email", email);
                session.setAttribute("role", role);  // Store role

                // Additional session attribute for teachers
                if ("Teacher".equalsIgnoreCase(role)) {
                    session.setAttribute("loggedInTeacherId", userId);  // Store teacher ID for attendance verification
                }

                // Logging (for debugging)
                System.out.println("User logged in: " + email + ", Role: " + role + ", UserID: " + userId);

                // Redirect user to the appropriate dashboard
                String redirectUrl = request.getContextPath() + "/pages/dashboard-" + role.toLowerCase() + ".jsp";
                jsonResponse.put("status", "success");
                jsonResponse.put("redirect", redirectUrl);
            } else {
                // Login failed
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Invalid email or password.");
                System.out.println("Login failed for: " + email + ", Role: " + role);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Database Error: " + e.getMessage());
        }

        response.getWriter().write(jsonResponse.toString());
    }
}
