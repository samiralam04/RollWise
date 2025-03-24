package com.attendance.dao;

import com.attendance.model.Attendance;
import com.attendance.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

    //  Save Attendance Record
    public boolean saveAttendance(Attendance attendance) {
        String query = "INSERT INTO attendance (student_id, date, status, recorded_at, teacher_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            System.out.println("Saving Attendance: " + attendance); // Log data before inserting

            stmt.setInt(1, attendance.getStudentId());
            stmt.setDate(2, Date.valueOf(attendance.getDate()));
            stmt.setString(3, attendance.getStatus());
            stmt.setTimestamp(4, Timestamp.valueOf(attendance.getRecordedAt()));
            stmt.setInt(5, attendance.getTeacherId());

            int rowsAffected = stmt.executeUpdate();
            System.out.println("Rows inserted: " + rowsAffected);

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    //  Get Attendance for a Student
    public List<Attendance> getAttendanceByStudent(int studentId) {
        List<Attendance> attendanceList = new ArrayList<>();
        String query = "SELECT * FROM attendance WHERE student_id = ? ORDER BY date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Attendance attendance = new Attendance();
                attendance.setId(rs.getInt("id"));
                attendance.setStudentId(rs.getInt("student_id"));
                attendance.setDate(rs.getDate("date").toLocalDate()); // Convert SQL Date to LocalDate
                attendance.setStatus(rs.getString("status"));
                attendance.setRecordedAt(rs.getTimestamp("recorded_at").toLocalDateTime()); // Convert SQL Timestamp to LocalDateTime
                attendance.setTeacherId(rs.getInt("teacher_id"));
                attendanceList.add(attendance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attendanceList;
    }

    //  Get Attendance Percentage for a Student
    public double calculateAttendancePercentage(int studentId) {
        String query = "SELECT COUNT(*) AS total, SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END) AS attended " +
                "FROM attendance WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int total = rs.getInt("total");
                int attended = rs.getInt("attended");
                if (total == 0) return 0; // Avoid division by zero
                return (attended * 100.0) / total;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    //  Get Students with Low Attendance (Below 75%)
    public List<Integer> getLowAttendanceStudents() {
        List<Integer> lowAttendanceStudents = new ArrayList<>();
        String query = "SELECT student_id FROM attendance " +
                "GROUP BY student_id HAVING (SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END) * 100.0) / COUNT(*) < 75";

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

    // Update Attendance Record
    public boolean updateAttendance(int id, String status) {
        String query = "UPDATE attendance SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status);
            stmt.setInt(2, id);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    //  Delete Attendance Record
    public boolean deleteAttendance(int id) {
        String query = "DELETE FROM attendance WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
