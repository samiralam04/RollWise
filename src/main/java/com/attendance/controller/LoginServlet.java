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
import com.attendance.model.User;
import com.attendance.service.UserService;

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
                     "SELECT id, username, password FROM users WHERE email = ? AND role = ?")) {

            stmt.setString(1, email);
            stmt.setString(2, role);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password"); // Assuming it's stored as plaintext or hashed

                // Verify password (If using plain text, compare directly; If hashed, use BCrypt check)
                if (storedPassword.equals(password)) { // Replace with BCrypt.checkpw(password, storedPassword) if hashed

                    HttpSession session = request.getSession();
                    session.setAttribute("userId", rs.getInt("id"));
                    session.setAttribute("username", rs.getString("username"));
                    session.setAttribute("email", email);
                    session.setAttribute("role", role);

                    // Construct redirect URL dynamically
                    String redirectUrl;
                    switch (role.toLowerCase()) {
                        case "admin":
                            redirectUrl = request.getContextPath() + "/pages/dashboard-admin.jsp";
                            break;
                        case "teacher":
                            redirectUrl = request.getContextPath() + "/pages/dashboard-teacher.jsp";
                            break;
                        case "student":
                            redirectUrl = request.getContextPath() + "/pages/dashboard-student.jsp";
                            break;
                        default:
                            redirectUrl = request.getContextPath() + "/pages/login.jsp";
                    }

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
            e.printStackTrace(); // This prints the actual error in logs
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Database Error: " + e.getMessage()); // Send detailed error message
        }


        response.getWriter().write(jsonResponse.toString());
    }
}

