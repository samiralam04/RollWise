package com.attendance.dao;

import com.attendance.model.Holiday;
import com.attendance.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HolidayDAO {

    // Add a new holiday (Renamed to match HolidayService)
    public boolean saveHoliday(Holiday holiday) {
        String query = "INSERT INTO holiday (date, reason) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, new java.sql.Date(holiday.getDate().getTime()));
            stmt.setString(2, holiday.getReason());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Get all holidays
    public List<Holiday> getAllHolidays() {
        List<Holiday> holidays = new ArrayList<>();
        String query = "SELECT * FROM holiday ORDER BY date ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Holiday holiday = new Holiday(
                        rs.getInt("id"),
                        rs.getString("reason"),
                        new java.util.Date(rs.getDate("date").getTime())
                );
                holidays.add(holiday);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return holidays;
    }

    // Get holiday by date
    public Holiday getHolidayByDate(java.util.Date date) {
        String query = "SELECT * FROM holiday WHERE date = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, new java.sql.Date(date.getTime()));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Holiday(
                        rs.getInt("id"),
                        rs.getString("reason"),
                        rs.getDate("date")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Delete a holiday by ID
    public boolean deleteHoliday(int id) {
        String query = "DELETE FROM holiday WHERE id = ?";
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

    // Check if a given date is a holiday
    public boolean isHoliday(String dateStr) {
        String query = "SELECT COUNT(*) FROM holiday WHERE date = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, java.sql.Date.valueOf(dateStr)); // Convert string to SQL Date
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0; // If count > 0, it's a holiday
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
