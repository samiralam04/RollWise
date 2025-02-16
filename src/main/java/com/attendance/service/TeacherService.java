package com.attendance.service;

import com.attendance.dao.TeacherDAO;
import com.attendance.model.Teacher;
import java.util.List;

public class TeacherService {
    private TeacherDAO teacherDAO;

    public TeacherService() {
        this.teacherDAO = new TeacherDAO();
    }

    // Add a new teacher
    public boolean addTeacher(Teacher teacher) {
        return teacherDAO.addTeacher(teacher);
    }

    // Get teacher details by ID
    public Teacher getTeacherById(int teacherId) {
        return teacherDAO.getTeacherById(teacherId);
    }

    // Get all teachers
    public List<Teacher> getAllTeachers() {
        return teacherDAO.getAllTeachers();
    }

    // Update teacher details
    public boolean updateTeacher(Teacher teacher) {
        return teacherDAO.updateTeacher(teacher);
    }

    // Delete teacher by ID
    public boolean deleteTeacher(int teacherId) {
        return teacherDAO.deleteTeacher(teacherId);
    }

    // Validate teacher login
    public boolean validateTeacher(String email, String password) {
        return teacherDAO.validateTeacher(email, password);
    }
}
