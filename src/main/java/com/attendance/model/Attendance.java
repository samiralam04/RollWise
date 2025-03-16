package com.attendance.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Attendance {
    private int id;
    private int studentId;
    private LocalDate date;
    private String status;
    private LocalDateTime recordedAt;
    private int teacherId;

    // ✅ Default Constructor
    public Attendance() {
    }

    // ✅ Parameterized Constructor
    public Attendance(int id, int studentId, LocalDate date, String status, LocalDateTime recordedAt, int teacherId) {
        this.id = id;
        this.studentId = studentId;
        this.date = date;
        this.status = status;
        this.recordedAt = recordedAt;
        this.teacherId = teacherId;
    }


    // ✅ Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    // ✅ Override toString() for debugging
    @Override
    public String toString() {
        return "Attendance{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", date=" + date +
                ", status='" + status + '\'' +
                ", recordedAt=" + recordedAt +
                ", teacherId=" + teacherId +
                '}';
    }
}
