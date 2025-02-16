package com.attendance.service;

import com.attendance.dao.StudentDAO;
import com.attendance.model.Student;
import java.util.List;

public class StudentService {
    private StudentDAO studentDAO;

    public StudentService() {
        this.studentDAO = new StudentDAO();
    }

    // Add a new student
    public boolean addStudent(Student student) {
        return studentDAO.addStudent(student);
    }

    // Get student details by ID
    public Student getStudentById(int studentId) {
        return studentDAO.getStudentById(studentId);
    }

    // Get all students
    public List<Student> getAllStudents() {
        return studentDAO.getAllStudents();
    }

    // Update student details
    public boolean updateStudent(Student student) {
        return studentDAO.updateStudent(student);
    }

    // Delete student by ID
    public boolean deleteStudent(int studentId) {
        return studentDAO.deleteStudentById(studentId); // Now using studentId instead of rollNumber
    }

    // Get student's attendance percentage
    public double getAttendancePercentage(int studentId) {
        return studentDAO.getAttendancePercentage(studentId);
    }

    // Get students with low attendance (below 75%)
    public List<Student> getStudentsWithLowAttendance() {
        return studentDAO.getStudentsWithLowAttendance(75);
    }

    // Get students whose attendance is near 75% (for warning SMS)
    public List<Student> getStudentsNearThreshold() {
        return studentDAO.getStudentsWithAttendanceNearThreshold(75);
    }
}
