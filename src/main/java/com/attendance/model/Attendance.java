package com.attendance.model;

import java.time.LocalDate;  // ✅ Import LocalDate

public class Attendance {
    private int id;
    private int studentId;  // ✅ Add studentId field
    private String studentName;  // ✅ Add studentName field
    private LocalDate date;  // ✅ Add date field
    private boolean status;  // ✅ Add status field
    private String subject;
    private int totalClasses;
    private int attendedClasses;
    private boolean isPresent;

    // ✅ Constructor with all fields
    public Attendance(int id, int studentId, String studentName, LocalDate date, boolean status,
                      String subject, int totalClasses, int attendedClasses) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.date = date;
        this.status = status;
        this.subject = subject;
        this.totalClasses = totalClasses;
        this.attendedClasses = attendedClasses;
    }

    // ✅ Default Constructor (Required for JDBC)
    public Attendance() {}

    // ✅ Getter Methods
    public int getId() {
        return id;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getStudentName() {  // ✅ Add getStudentName()
        return studentName;
    }

    public LocalDate getDate() {  // ✅ Add getDate()
        return date;
    }

    public boolean getStatus() {  // ✅ Add getStatus()
        return status;
    }

    public String getSubject() {
        return subject;
    }

    public int getTotalClasses() {
        return totalClasses;
    }

    public int getAttendedClasses() {
        return attendedClasses;
    }

    // ✅ Setter Methods
    public void setId(int id) {
        this.id = id;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public void setStudentName(String studentName) {  // ✅ Add setStudentName()
        this.studentName = studentName;
    }

    public void setDate(LocalDate date) {  // ✅ Add setDate()
        this.date = date;
    }

    public void setStatus(boolean status) {  // ✅ Add setStatus()
        this.status = status;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setTotalClasses(int totalClasses) {
        this.totalClasses = totalClasses;
    }

    public void setAttendedClasses(int attendedClasses) {
        this.attendedClasses = attendedClasses;
    }

    public  void setPresent(boolean isPresent){
        this.isPresent = isPresent;
    }
}
