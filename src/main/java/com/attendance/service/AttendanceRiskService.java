package com.attendance.service;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.StudentDAO;
import com.attendance.model.Attendance;
import com.attendance.model.Student;
import com.attendance.model.StudentRiskDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AttendanceRiskService {

    private AttendanceDAO attendanceDAO;
    private StudentDAO studentDAO;

    public AttendanceRiskService() {
        this.attendanceDAO = new AttendanceDAO();
        this.studentDAO = new StudentDAO();
    }

    public List<StudentRiskDTO> analyzeRisk() {
        List<StudentRiskDTO> riskReport = new ArrayList<>();
        List<Student> students = studentDAO.getAllStudents(); // Need to ensure this method exists or create it

        for (Student student : students) {
            List<Attendance> attendanceList = attendanceDAO.getAttendanceByStudent(student.getId());
            StudentRiskDTO riskDTO = calculateRisk(student, attendanceList);
            riskReport.add(riskDTO);
        }
        return riskReport;
    }

    private StudentRiskDTO calculateRisk(Student student, List<Attendance> attendanceRecords) {
        double currentAttendanceInfo = attendanceDAO.calculateAttendancePercentage(student.getId());

        // 1. Trend Analysis (Last 4 weeks vs Previous 4 weeks)
        LocalDate now = LocalDate.now();
        LocalDate fourWeeksAgo = now.minusWeeks(4);
        LocalDate eightWeeksAgo = now.minusWeeks(8);

        long presentLast4Weeks = attendanceRecords.stream()
                .filter(a -> !a.getDate().isBefore(fourWeeksAgo) && "Present".equalsIgnoreCase(a.getStatus()))
                .count();

        long presentPrev4Weeks = attendanceRecords.stream()
                .filter(a -> a.getDate().isBefore(fourWeeksAgo) && !a.getDate().isBefore(eightWeeksAgo)
                        && "Present".equalsIgnoreCase(a.getStatus()))
                .count();

        String trend = "STABLE";
        if (presentLast4Weeks < presentPrev4Weeks) {
            trend = "DOWN";
        } else if (presentLast4Weeks > presentPrev4Weeks) {
            trend = "UP";
        }

        // 2. Weekly Presence for Chart
        int[] weeklyPresence = new int[4];
        for (int i = 0; i < 4; i++) {
            LocalDate start = now.minusWeeks(i + 1);
            LocalDate end = now.minusWeeks(i);
            long count = attendanceRecords.stream()
                    .filter(a -> a.getDate().isAfter(start) && !a.getDate().isAfter(end)
                            && "Present".equalsIgnoreCase(a.getStatus()))
                    .count();
            weeklyPresence[3 - i] = (int) count; // Store in chronological order [4 weeks ago, ..., last week]
        }

        // 3. Repeated Absence on Same Weekday (Last 4 weeks)
        Map<java.time.DayOfWeek, Long> absenceByDay = attendanceRecords.stream()
                .filter(a -> !a.getDate().isBefore(fourWeeksAgo) && "Absent".equalsIgnoreCase(a.getStatus()))
                .collect(Collectors.groupingBy(a -> a.getDate().getDayOfWeek(), Collectors.counting()));

        boolean sameWeekdayAbsenceRisk = absenceByDay.values().stream().anyMatch(count -> count >= 3);

        // 4. Risk Score Calculation
        int riskScore = 0;
        if (currentAttendanceInfo < 70) {
            riskScore += 30;
        }
        if ("DOWN".equals(trend)) {
            riskScore += 20;
        }
        if (sameWeekdayAbsenceRisk) {
            riskScore += 15;
        }

        // 5. Risk Level
        String riskLevel = "SAFE";
        if (riskScore >= 45) {
            riskLevel = "AT-RISK";
        } else if (riskScore >= 25) {
            riskLevel = "WATCH";
        }

        // 6. Predict Final Attendance (Simple Linear Projection)
        // Avg change per week in last 4 weeks.
        // Simplified: (PresentLast4Weeks - PresentPrev4Weeks) / 4.0 is likely change
        // per
        // week?
        // Let's use a simpler current trend projection.
        // remaining weeks = approx 16 weeks semester - current week?
        // For now, let's just project based on current rate + trend impact.
        double projected = currentAttendanceInfo;
        if ("DOWN".equals(trend)) {
            projected -= 5.0; // Simulated drop
        } else if ("UP".equals(trend)) {
            projected += 2.0;
        }

        // 7. Suggested Action
        String action = "Keep up the good work.";
        if ("AT-RISK".equals(riskLevel)) {
            action = "Schedule a meeting immediately.";
        } else if ("WATCH".equals(riskLevel)) {
            if (sameWeekdayAbsenceRisk) {
                action = "Check for recurring issues on specific days.";
            } else {
                action = "Monitor attendance closely next week.";
            }
        }

        StudentRiskDTO dto = new StudentRiskDTO();
        dto.setStudentId(student.getId());
        dto.setName(student.getName());
        dto.setCurrentAttendance(currentAttendanceInfo);
        dto.setTrend(trend);
        dto.setRiskScore(riskScore);
        dto.setRiskLevel(riskLevel);
        dto.setPredictedFinalAttendance(projected);
        dto.setSuggestedAction(action);
        dto.setWeeklyPresence(weeklyPresence);

        return dto;
    }
}
