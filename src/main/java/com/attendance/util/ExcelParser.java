package com.attendance.util;

import java.io.InputStream;
import java.io.IOException;
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
        DataFormatter dataFormatter = new DataFormatter(); // To handle different cell types

        // ✅ Validate file type before processing
        String fileName = filePart.getSubmittedFileName();
        if (!fileName.endsWith(".xlsx")) {
            throw new IllegalArgumentException("Invalid file format. Please upload an Excel (.xlsx) file.");
        }

        try (InputStream fileInputStream = filePart.getInputStream();
             Workbook workbook = new XSSFWorkbook(fileInputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // ✅ First sheet contains attendance data
            boolean isFirstRow = true;

            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue; // ✅ Skip header row
                }

                if (row == null || row.getCell(0) == null) {
                    System.err.println("Skipping empty row...");
                    continue; // ✅ Skip empty rows
                }

                // ✅ Read Student ID properly
                Cell studentIdCell = row.getCell(0);
                String studentId = (studentIdCell != null) ? dataFormatter.formatCellValue(studentIdCell).trim() : null;

                // ✅ Read Attendance Status (Assume "Present" or "Absent")
                Cell statusCell = row.getCell(3);
                String status = (statusCell != null) ? dataFormatter.formatCellValue(statusCell).trim() : "Absent";

                if (studentId == null || studentId.isEmpty()) {
                    System.err.println("Skipping row: Invalid student ID");
                    continue;
                }

                attendanceMap.put(studentId, status.equalsIgnoreCase("Present") ? 1 : 0);
            }

        } catch (Exception e) {
            System.err.println("Error processing Excel file: " + e.getMessage());
        }

        return attendanceMap;
    }
}
