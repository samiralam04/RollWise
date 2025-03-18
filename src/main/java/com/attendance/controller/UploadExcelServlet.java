package com.attendance.controller;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Date;
import java.util.logging.Logger;

@MultipartConfig
public class UploadExcelServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(UploadExcelServlet.class.getName());

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/attendance_system";
    private static final String DB_USER = "attendance_system";
    private static final String DB_PASSWORD = "mark47";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Part filePart = request.getPart("file");
        InputStream fileContent = filePart.getInputStream();

        try (Workbook workbook = new XSSFWorkbook(fileContent);
             Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header row
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            String insertSQL = "INSERT INTO attendance (student_id, date, status, recorded_at, teacher_id) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertSQL);

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (isRowEmpty(row)) {
                    continue; // Skip empty rows
                }

                String studentId = getCellValueAsString(row.getCell(0));
                String dateStr = getCellValueAsString(row.getCell(1)).trim();
                String status = getCellValueAsString(row.getCell(2));
                String teacherId = getCellValueAsString(row.getCell(3));

                if (studentId.isEmpty() || status.isEmpty() || teacherId.isEmpty()) {
                    logSkippedRow(row, "Missing required fields");
                    continue; // Skip rows with empty required fields
                }

                pstmt.setInt(1, Integer.parseInt(studentId));

                // **Fix for Date Issue**
                if (!dateStr.isEmpty()) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        java.util.Date parsedDate = sdf.parse(dateStr);
                        java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());
                        pstmt.setDate(2, sqlDate);
                    } catch (Exception e) {
                        logSkippedRow(row, "Invalid date format: " + dateStr);
                        continue; // Skip row with invalid date
                    }
                } else {
                    pstmt.setNull(2, java.sql.Types.DATE);
                }

                pstmt.setString(3, status);
                pstmt.setTimestamp(4, new Timestamp(new Date().getTime()));
                pstmt.setInt(5, Integer.parseInt(teacherId));

                pstmt.addBatch();
            }

            pstmt.executeBatch();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"success\": true, \"message\": \"Data uploaded successfully.\"}");

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"success\": false, \"message\": \"Error processing the file: " + e.getMessage() + "\"}");
        }
    }

    // Helper function to check if a row is empty
    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    // Helper function to extract cell values safely
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    return sdf.format(cell.getDateCellValue());
                } else {
                    return String.valueOf((int) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    // Logs skipped rows with reason
    private void logSkippedRow(Row row, String reason) {
        StringBuilder rowData = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            rowData.append(row.getCell(i) != null ? row.getCell(i).toString() : "NULL").append(" | ");
        }
        logger.warning("Skipping row " + row.getRowNum() + ": " + reason + " → " + rowData);
    }
}
