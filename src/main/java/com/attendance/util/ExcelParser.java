package com.attendance.util;

import java.io.InputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javax.servlet.http.Part;

public class ExcelParser {

    /**
     * Parses an uploaded Excel file and returns a map of student attendance data.
     * @param filePart The uploaded file as Part.
     * @return Map with student ID as key and 1 (Present) or 0 (Absent) as value.
     */
    public static Map<String, Integer> parseExcel(Part filePart) throws IOException {
        Map<String, Integer> attendanceMap = new LinkedHashMap<>();

        try (InputStream fileInputStream = filePart.getInputStream();
             Workbook workbook = new XSSFWorkbook(fileInputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // Assume the first sheet contains attendance data
            boolean isFirstRow = true; // Skip header row

            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                // Read Student ID
                Cell studentIdCell = row.getCell(0);
                String studentId = studentIdCell != null ? studentIdCell.toString().trim() : null;

                // Read Attendance Status (Assume "Present" or "Absent")
                Cell statusCell = row.getCell(3);
                String status = (statusCell != null) ? statusCell.toString().trim() : "Absent";

                if (studentId != null && !studentId.isEmpty()) {
                    attendanceMap.put(studentId, status.equalsIgnoreCase("Present") ? 1 : 0);
                }
            }
        }
        return attendanceMap;
    }
}
