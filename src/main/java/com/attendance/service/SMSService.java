package com.attendance.service;

import com.attendance.dao.StudentDAO;
import com.attendance.model.Student;
import java.util.List;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class SMSService {
    private static final String API_KEY = "your_fast2sms_api_key";
    private static final String API_URL = "https://www.fast2sms.com/dev/bulkV2";

    private StudentDAO studentDAO;

    public SMSService() {
        this.studentDAO = new StudentDAO();
    }

    // Method to send SMS notification for low attendance
    public void sendLowAttendanceAlerts() {
        List<Student> students = studentDAO.getStudentsWithLowAttendance(75);
        for (Student student : students) {
            String message = "Dear Parent, your child " + student.getName() +
                    " has attendance below 75%. Please take necessary action.";
            sendSMS(student.getParentPhone(), message);  // ✅ FIXED METHOD
        }
    }

    // Method to send warning SMS before attendance drops below 75%
    public void sendWarningAlerts() {
        List<Student> students = studentDAO.getStudentsWithAttendanceNearThreshold(75);
        for (Student student : students) {
            String message = "Warning: Your attendance is close to dropping below 75%. Maintain regular attendance.";
            sendSMS(student.getPhone(), message);  // ✅ FIXED METHOD
        }
    }

    // Method to send emergency alerts (rain, flood, etc.)
    public void sendEmergencyAlert(String message) {
        List<Student> students = studentDAO.getAllStudents();
        for (Student student : students) {
            sendSMS(student.getPhone(), message);          // ✅ FIXED METHOD
            sendSMS(student.getParentPhone(), message);   // ✅ FIXED METHOD
        }
    }

    // Function to send SMS using Fast2SMS API
    private void sendSMS(String phoneNumber, String message) {
        try {
            String requestBody = "route=otp&message=" + message +
                    "&language=english&flash=0&numbers=" + phoneNumber;

            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("authorization", API_KEY);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            System.out.println("SMS sent to " + phoneNumber + " | Response Code: " + responseCode);

        } catch (Exception e) {
            System.out.println("Error sending SMS: " + e.getMessage());
        }
    }
}
