package com.attendance.dao;

import com.attendance.model.Emergency;
import com.attendance.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmergencyDAO {

    // Add a new emergency announcement
    public boolean addEmergency(Emergency emergency) {
        String query = "INSERT INTO emergency (title, description, date) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, emergency.getTitle());
            stmt.setString(2, emergency.getDescription());

            // Convert java.util.Date to java.sql.Timestamp
            if (emergency.getDate() != null) {
                stmt.setTimestamp(3, new java.sql.Timestamp(emergency.getDate().getTime()));
            } else {
                stmt.setTimestamp(3, null);
            }

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Get the latest emergency announcement
    public Emergency getLatestEmergency() {
        String query = "SELECT * FROM emergency ORDER BY date DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return new Emergency(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getTimestamp("date") // Automatically converts SQL Timestamp to Java Date
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Get all emergency announcements
    public List<Emergency> getAllEmergencies() {
        List<Emergency> emergencies = new ArrayList<>();
        String query = "SELECT * FROM emergency ORDER BY date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Emergency emergency = new Emergency(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getTimestamp("date") // SQL Timestamp to Java Date
                );
                emergencies.add(emergency);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return emergencies;
    }

    // Delete an emergency announcement by ID
    public boolean deleteEmergency(int id) {
        String query = "DELETE FROM emergency WHERE id = ?";
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
