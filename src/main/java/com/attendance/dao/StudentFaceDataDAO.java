package com.attendance.dao;

import com.attendance.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class StudentFaceDataDAO {
    private static final Logger LOGGER = Logger.getLogger(StudentFaceDataDAO.class.getName());

    public boolean addFaceData(int studentId, String faceEncoding, String modelVersion) {
        String query = "INSERT INTO student_face_data (student_id, face_encoding, encoding_model_version) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, studentId);
            stmt.setString(2, faceEncoding);
            stmt.setString(3, modelVersion);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.severe("Error adding face data: " + e.getMessage());
            return false;
        }
    }

    public String getFaceEncoding(int studentId) {
        String query = "SELECT face_encoding FROM student_face_data WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("face_encoding");
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error fetching face data: " + e.getMessage());
        }
        return null;
    }
}
