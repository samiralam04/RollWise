package com.attendance.model;

public class Teacher {
    private int id;
    private String name;
    private String email;
    private String phone;
    private String subject;
    private String username;
    private String password;

    // ✅ Constructor with all fields
    public Teacher(int id, String name, String email, String phone, String subject, String username, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.subject = subject;
        this.username = username;
        this.password = password;
    }

    // ✅ Constructor with (id, name, email, subject)
    public Teacher(int id, String name, String email, String subject) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.subject = subject;
    }

    // ✅ Constructor with (id, name, email) - 🔥 NEWLY ADDED 🔥
    public Teacher(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // ✅ Default Constructor (Required for JDBC)
    public Teacher() {}

    // ✅ Getter Methods
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getSubject() { return subject; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    // ✅ Setter Methods
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
}
