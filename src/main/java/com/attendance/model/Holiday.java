package com.attendance.model;

import java.util.Date;

public class Holiday {
    private int id;
    private String reason;   // ✅ Ensure this exists
    private Date date;       // ✅ Ensure this exists

    // ✅ Constructor with all fields
    public Holiday(int id, String reason, Date date) {
        this.id = id;
        this.reason = reason;
        this.date = date;
    }

    // ✅ Default Constructor (Required for JDBC)
    public Holiday() {}

    // ✅ Getter Methods
    public int getId() {
        return id;
    }

    public String getReason() {  // ✅ Added getReason()
        return reason;
    }

    public Date getDate() {
        return date;
    }

    // ✅ Setter Methods
    public void setId(int id) {
        this.id = id;
    }

    public void setReason(String reason) {  // ✅ Added setReason()
        this.reason = reason;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
