package com.attendance.model;

public class Teacher {
    private int id;
    private String name;
    private String email;
    private String phone;  // ❌ Remove Optional here
    private String subject;
    private String username;
    private String password;

    // ✅ Constructor with all fields
    public Teacher(int id, String name, String email, String phone, String subject, String username, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;  // ✅ Store raw value
        this.subject = subject;
        this.username = username;
        this.password = password;
    }

    // ✅ Constructor with (id, name, email, phone, subject, username)
    public Teacher(int id, String name, String email, String phone, String subject, String username) {
        this(id, name, email, phone, subject, username, null);
    }

    // ✅ Constructor with (id, name, email, subject)
    public Teacher(int id, String name, String email, String subject) {
        this(id, name, email, null, subject, null, null);
    }

    // ✅ Constructor with (id, name, email)
    public Teacher(int id, String name, String email) {
        this(id, name, email, null, null, null, null);
    }

    // ✅ Default Constructor
    public Teacher() {}

    // ✅ Getter Methods
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    // ✅ Handle Optional at method level
    public String getPhone() { return phone != null ? phone : "N/A"; }
    public String getSubject() { return subject != null ? subject : "N/A"; }

    public String getUsername() { return username; }

    // ❌ Do not expose raw password in real applications
    public String getPassword() {
        return "********"; // Masked for security
    }

    // ✅ Setter Methods
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setUsername(String username) { this.username = username; }

    public void setPassword(String password) {
        // Ideally, hash the password before storing
        this.password = password;
    }

    // ✅ toString() for debugging
    @Override
    public String toString() {
        return "Teacher{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + getPhone() + '\'' +
                ", subject='" + getSubject() + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
