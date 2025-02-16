package com.attendance.model;

import java.sql.Timestamp;

public class Emergency {
    private int id;
    private String title;
    private String description;
    private Timestamp date;  // Use Timestamp instead of Date

    // ✅ Constructor with all fields
    public Emergency(int id, String title, String description, Timestamp date) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
    }

    // ✅ Default Constructor (Required for JDBC)
    public Emergency() {}

    // ✅ Getter Methods
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Timestamp getDate() {  // Change return type to Timestamp
        return date;
    }

    // ✅ Setter Methods
    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(Timestamp date) {  // Change parameter type to Timestamp
        this.date = date;
    }
}
