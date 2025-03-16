package com.attendance.service;

import com.attendance.model.User;
import com.attendance.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {

    // ✅ Register a User
    public boolean registerUser(User user) {
        String userQuery = "INSERT INTO users (username, email, password, role) VALUES (?, ?, ?, ?) RETURNING id";
        String attendanceQuery = "INSERT INTO attendance (student_id, date, status) VALUES (?, CURRENT_DATE, 'Absent')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement userStmt = conn.prepareStatement(userQuery);
             PreparedStatement attendanceStmt = conn.prepareStatement(attendanceQuery)) {

            // Insert user into the users table
            userStmt.setString(1, user.getName());
            userStmt.setString(2, user.getEmail());
            userStmt.setString(3, user.getPassword());
            userStmt.setString(4, user.getRole());

            ResultSet rs = userStmt.executeQuery(); // Get the generated ID

            if (rs.next()) {
                int userId = rs.getInt("id"); // Get newly created user ID

                // If role is student, insert into attendance table
                if ("student".equalsIgnoreCase(user.getRole())) {
                    attendanceStmt.setInt(1, userId);
                    attendanceStmt.executeUpdate();
                }

                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ✅ Validate User Login
    public User validateUser(String email, String password) {
        String query = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("role")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
