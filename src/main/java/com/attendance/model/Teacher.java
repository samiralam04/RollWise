package com.attendance.model;

import java.util.Optional;

public class Teacher {
    private int id;
    private String name;
    private String email;
    private Optional<String> phone;
    private Optional<String> subject;
    private String username;
    private String password;

    // ✅ Constructor with all fields
    public Teacher(int id, String name, String email, String phone, String subject, String username, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = Optional.ofNullable(phone);
        this.subject = Optional.ofNullable(subject);
        this.username = username;
        this.password = password;
    }

    // ✅ Constructor with (id, name, email, phone, subject, username) [NEWLY ADDED]
    public Teacher(int id, String name, String email, String phone, String subject, String username) {
        this(id, name, email, phone, subject, username, null); // Password set as null
    }

    // ✅ Constructor with (id, name, email, subject)
    public Teacher(int id, String name, String email, String subject) {
        this(id, name, email, null, subject, null, null);
    }

    // ✅ Constructor with (id, name, email)
    public Teacher(int id, String name, String email) {
        this(id, name, email, null, null, null, null);
    }

    // ✅ Default Constructor (Required for JDBC)
    public Teacher() {}

    // ✅ Getter Methods
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Optional<String> getPhone() { return phone; }
    public Optional<String> getSubject() { return subject; }
    public String getUsername() { return username; }

    // ❌ Do not expose raw password in real applications
    public String getPassword() {
        return "********"; // Masked for security
    }

    // ✅ Setter Methods
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = Optional.ofNullable(phone); }
    public void setSubject(String subject) { this.subject = Optional.ofNullable(subject); }
    public void setUsername(String username) { this.username = username; }

    public void setPassword(String password) {
        // Ideally, hash the password before storing
        this.password = password;
    }

    // ✅ toString() for easy debugging
    @Override
    public String toString() {
        return "Teacher{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone=" + phone.orElse("N/A") +
                ", subject=" + subject.orElse("N/A") +
                ", username='" + username + '\'' +
                '}';
    }
}
