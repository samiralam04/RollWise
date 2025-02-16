package com.attendance.dao;

import com.attendance.model.Attendance;
import com.attendance.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

    // Insert or Update Attendance Record
    public boolean saveAttendance(Attendance attendance) {
        String query = "INSERT INTO attendance (student_id, subject, total_classes, attended_classes) " +
                "VALUES (?, ?, ?, ?) ON CONFLICT (student_id, subject) " +
                "DO UPDATE SET total_classes = attendance.total_classes + EXCLUDED.total_classes, " +
                "attended_classes = attendance.attended_classes + EXCLUDED.attended_classes";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, attendance.getStudentId());
            stmt.setString(2, attendance.getSubject());
            stmt.setInt(3, attendance.getTotalClasses());
            stmt.setInt(4, attendance.getAttendedClasses());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Get Attendance for a Student
    public List<Attendance> getAttendanceByStudentId(int studentId) {  // Fixed naming inconsistency
        List<Attendance> attendanceList = new ArrayList<>();
        String query = "SELECT * FROM attendance WHERE student_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Attendance attendance = new Attendance();
                attendance.setId(rs.getInt("id"));
                attendance.setStudentId(rs.getInt("student_id"));
                attendance.setSubject(rs.getString("subject"));
                attendance.setTotalClasses(rs.getInt("total_classes"));
                attendance.setAttendedClasses(rs.getInt("attended_classes"));
                attendanceList.add(attendance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attendanceList;
    }

    // Get Attendance Percentage for a Student
    public double calculateAttendancePercentage(int studentId) { // Fixed missing method issue
        String query = "SELECT SUM(attended_classes) AS attended, SUM(total_classes) AS total " +
                "FROM attendance WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int attended = rs.getInt("attended");
                int total = rs.getInt("total");
                if (total == 0) return 0;
                return (attended * 100.0) / total;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Get Students with Low Attendance (Below 75%)
    public List<Integer> getLowAttendanceStudents() {
        List<Integer> lowAttendanceStudents = new ArrayList<>();
        String query = "SELECT student_id FROM attendance " +
                "GROUP BY student_id HAVING AVG(attended_classes * 100.0 / total_classes) < 75";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lowAttendanceStudents.add(rs.getInt("student_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lowAttendanceStudents;
    }

    // Delete Attendance Record
    public boolean deleteAttendance(int studentId, String subject) {
        String query = "DELETE FROM attendance WHERE student_id = ? AND subject = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, studentId);
            stmt.setString(2, subject);
            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
