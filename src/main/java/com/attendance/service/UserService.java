package com.attendance.service;

import com.attendance.model.User;
import com.attendance.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {

    // Register a User
    public boolean registerUser(User user) {
        String userQuery = "INSERT INTO users (username, email, password, role) VALUES (?, ?, ?, ?) RETURNING id";
        String studentQuery = "INSERT INTO students (id, user_id, roll_number, class_id, student_email) VALUES (?, ?, ?, ?, ?)";
        String attendanceQuery = "INSERT INTO attendance (student_id, date, status) VALUES (?, CURRENT_DATE, 'Absent')";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Use transaction
            try (PreparedStatement userStmt = conn.prepareStatement(userQuery);
                    PreparedStatement studentStmt = conn.prepareStatement(studentQuery);
                    PreparedStatement attendanceStmt = conn.prepareStatement(attendanceQuery)) {

                // 1. Insert into users table
                userStmt.setString(1, user.getName());
                userStmt.setString(2, user.getEmail());
                userStmt.setString(3, user.getPassword());
                userStmt.setString(4, user.getRole());

                ResultSet rs = userStmt.executeQuery();
                if (rs.next()) {
                    int userId = rs.getInt("id");

                    // 2. If role is student, insert into students table
                    if ("student".equalsIgnoreCase(user.getRole())) {
                        studentStmt.setInt(1, userId); // Force student ID to match user ID for easy admin management
                        studentStmt.setInt(2, userId);
                        studentStmt.setString(3, "ROLL-" + userId); // Default roll number
                        studentStmt.setInt(4, 1); // Default to Class ID 1 (Class 10A)
                        studentStmt.setString(5, user.getEmail());
                        studentStmt.executeUpdate();

                        // 3. Insert into attendance table (references users.id)
                        attendanceStmt.setInt(1, userId);
                        attendanceStmt.executeUpdate();
                    }

                    conn.commit();
                    return true;
                }
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Validate User Login
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
                        rs.getString("role"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Check if Admin Exists
    public boolean isAdminExists() {
        String query = "SELECT COUNT(*) FROM users WHERE role = 'Admin'";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
