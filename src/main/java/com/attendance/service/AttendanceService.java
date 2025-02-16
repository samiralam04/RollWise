package com.attendance.service;

import com.attendance.dao.AttendanceDAO;
import com.attendance.model.Attendance;

import java.time.LocalDate;
import java.util.List;

public class AttendanceService {
    private AttendanceDAO attendanceDAO;

    public AttendanceService() {
        this.attendanceDAO = new AttendanceDAO();
    }

    // Mark attendance for a student
    public boolean markAttendance(int studentId, String date, boolean isPresent) {
        Attendance attendance = new Attendance();
        attendance.setStudentId(studentId);

        // Fix for LocalDate conversion
        attendance.setDate(LocalDate.parse(date));

        // Fix for missing setPresent method
        attendance.setPresent(isPresent);

        return attendanceDAO.saveAttendance(attendance);
    }

    // Get attendance records for a specific student
    public List<Attendance> getStudentAttendance(int studentId) {
        return attendanceDAO.getAttendanceByStudentId(studentId);
    }

    // Get attendance percentage for a student
    public double getAttendancePercentage(int studentId) {
        return attendanceDAO.calculateAttendancePercentage(studentId);
    }

    // Send warning SMS if attendance is below 75%
    public void checkAndSendWarning(int studentId) {
        double percentage = getAttendancePercentage(studentId);
        if (percentage < 75) {
            sendWarningSMS(studentId, percentage);
        }
    }

    // Send SMS notification
    private void sendWarningSMS(int studentId, double percentage) {
        System.out.println("Sending warning SMS to student ID " + studentId +
                ". Your attendance is below 75% (" + percentage + "%). Please attend classes regularly.");
        // Implement actual SMS API integration here
    }
}
