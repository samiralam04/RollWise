package com.attendance.service;

import com.attendance.dao.EmergencyDAO;
import com.attendance.model.Emergency;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.sql.Timestamp;

public class EmergencyService {
    private EmergencyDAO emergencyDAO;

    public EmergencyService() {
        this.emergencyDAO = new EmergencyDAO();
    }

    // Declare an emergency holiday
    public boolean declareEmergency(String title, String description, String dateStr) {
        Emergency emergency = new Emergency();
        emergency.setTitle(title);
        emergency.setDescription(description);

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date utilDate = dateFormat.parse(dateStr);  // Parse as java.util.Date
            Timestamp sqlTimestamp = new Timestamp(utilDate.getTime());  // ✅ Convert to java.sql.Timestamp
            emergency.setDate(sqlTimestamp);  // ✅ Set the correct type
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }

        return emergencyDAO.addEmergency(emergency);
    }

    // Get all declared emergency holidays
    public List<Emergency> getAllEmergencies() {
        return emergencyDAO.getAllEmergencies();
    }

    // Notify all students and teachers about the emergency holiday
    public void notifyEmergency(String title, String description, String date) {
        System.out.println("Sending emergency notification: " + title + " - " + description + " on " + date);
        // Implement actual SMS/Email notification here
    }
}
