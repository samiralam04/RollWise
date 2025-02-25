package com.attendance.service;

import com.attendance.dao.AttendanceDAO;
import com.attendance.model.Attendance;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class AttendanceService {
    private AttendanceDAO attendanceDAO;

    // ✅ Dependency Injection (Good for testing)
    public AttendanceService(AttendanceDAO attendanceDAO) {
        this.attendanceDAO = attendanceDAO;
    }

    // ✅ Default Constructor
    public AttendanceService() {
        this(new AttendanceDAO());
    }

    // ✅ Mark attendance for a student
    public boolean markAttendance(int studentId, String date, boolean isPresent, int teacherId) {
        Attendance attendance = new Attendance();
        attendance.setStudentId(studentId);

        // ✅ Fix for LocalDate conversion with error handling
        try {
            attendance.setDate(LocalDate.parse(date));
        } catch (DateTimeParseException e) {
            System.err.println("Invalid date format: " + date);
            return false; // Exit if the date format is invalid
        }

        // ✅ Set "Present" or "Absent" instead of boolean
        attendance.setStatus(isPresent ? "Present" : "Absent");
        attendance.setTeacherId(teacherId); // Set teacher ID who recorded attendance

        return attendanceDAO.saveAttendance(attendance);
    }

    // ✅ Get attendance records for a specific student
    public List<Attendance> getStudentAttendance(int studentId) {
        return attendanceDAO.getAttendanceByStudent(studentId);
    }

    // ✅ Get attendance percentage for a student
    public double getAttendancePercentage(int studentId) {
        return attendanceDAO.calculateAttendancePercentage(studentId);
    }

    // ✅ Send warning SMS if attendance is below 75%
    public void checkAndSendWarning(int studentId) {
        double percentage = getAttendancePercentage(studentId);
        if (percentage < 75) {
            sendWarningSMS(studentId, percentage);
        }
    }

    // ✅ Send SMS notification (Placeholder)
    private void sendWarningSMS(int studentId, double percentage) {
        System.out.println("Sending warning SMS to student ID " + studentId +
                ". Your attendance is below 75% (" + percentage + "%). Please attend classes regularly.");
        // TODO: Implement actual SMS API integration here
    }
}
