package com.attendance.model;

public class Student {
    private int id;
    private String name;
    private String email;
    private String phone;
    private String rollNumber;
    private String className;
    private String parentPhone;

    // ✅ Constructor with 7 parameters (used in StudentDAO)
    public Student(int id, String name, String email, String phone, String rollNumber, String className, String parentPhone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.rollNumber = rollNumber;
        this.className = className;
        this.parentPhone = parentPhone;
    }

    // ✅ Constructor with 5 parameters (if needed)
    public Student(int id, String name, String email, String phone, String rollNumber) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.rollNumber = rollNumber;
    }

    // ✅ Default Constructor (Required for JDBC Operations)
    public Student() {}

    // ✅ Getter Methods
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public String getClassName() {
        return className;
    }

    public String getParentPhone() {
        return parentPhone;
    }

    // ✅ Setter Methods
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setParentPhone(String parentPhone) {
        this.parentPhone = parentPhone;
    }
}
