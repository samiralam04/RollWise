package com.attendance.service;

import com.attendance.dao.HolidayDAO;
import com.attendance.model.Holiday;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HolidayService {
    private HolidayDAO holidayDAO;

    public HolidayService() {
        this.holidayDAO = new HolidayDAO();
    }

    // Add a new holiday
    public boolean addHoliday(String name, String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = sdf.parse(dateStr);
            Holiday holiday = new Holiday();
            holiday.setReason(name);
            holiday.setDate(date);
            return holidayDAO.saveHoliday(holiday);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Remove a holiday by ID
    public boolean removeHoliday(int holidayId) {
        return holidayDAO.deleteHoliday(holidayId);
    }

    // Get all holidays
    public List<Holiday> getAllHolidays() {
        return holidayDAO.getAllHolidays();
    }

    // Check if a given date is a holiday
    public boolean isHoliday(String dateStr) {
        return holidayDAO.isHoliday(dateStr);
    }
}
