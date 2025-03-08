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
import java.util.logging.Logger;

@MultipartConfig
public class UploadExcelServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(UploadExcelServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Part filePart = request.getPart("file");
        if (filePart == null || filePart.getSize() == 0) {
            response.getWriter().println("No file uploaded!");
            return;
        }

        Connection conn = null;
        PreparedStatement checkStudentStmt = null;
        PreparedStatement checkAttendanceStmt = null;
        PreparedStatement insertAttendanceStmt = null;

        int insertedCount = 0;
        int skippedCount = 0;

        try (InputStream fileStream = filePart.getInputStream();
             Workbook workbook = new XSSFWorkbook(fileStream);
             Connection dbConn = getDatabaseConnection()) {

            conn = dbConn;
            conn.setAutoCommit(false); // Enable transaction

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Validate header row
            if (rowIterator.hasNext()) {
                Row headerRow = rowIterator.next();
                if (!isValidHeader(headerRow)) {
                    response.getWriter().println("Invalid Excel format. Check column headers.");
                    return;
                }
            }

            // SQL queries (updated to check roll_number)
            String checkStudentQuery = "SELECT id FROM students WHERE roll_number = ?";
            checkStudentStmt = conn.prepareStatement(checkStudentQuery);

            String checkAttendanceQuery = "SELECT COUNT(*) FROM attendance WHERE student_id = ? AND date = ?";
            checkAttendanceStmt = conn.prepareStatement(checkAttendanceQuery);

            String insertAttendanceQuery = "INSERT INTO attendance (student_id, date, status, teacher_id) VALUES (?, ?, ?, ?)";
            insertAttendanceStmt = conn.prepareStatement(insertAttendanceQuery);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                // Read Roll Number (Modified to handle alphanumeric roll numbers)
                String rollNumber = getStringCellValue(row.getCell(0));
                if (rollNumber.isEmpty()) {
                    logSkippedRow(row, "Invalid Roll Number.");
                    skippedCount++;
                    continue;
                }

                // Read Date
                java.sql.Date sqlDate = getDateCellValue(row.getCell(1));
                if (sqlDate == null) {
                    logSkippedRow(row, "Invalid Date format.");
                    skippedCount++;
                    continue;
                }

                // Read Status (Should be "Present" or "Absent")
                String status = getStringCellValue(row.getCell(2));
                if (!status.equalsIgnoreCase("Present") && !status.equalsIgnoreCase("Absent")) {
                    logSkippedRow(row, "Status must be 'Present' or 'Absent'.");
                    skippedCount++;
                    continue;
                }

                // Read Teacher ID
                int teacherId = getNumericCellValue(row.getCell(3));
                if (teacherId == -1) {
                    logSkippedRow(row, "Invalid Teacher ID.");
                    skippedCount++;
                    continue;
                }

                // Check if student exists using roll_number
                checkStudentStmt.setString(1, rollNumber);
                ResultSet studentRs = checkStudentStmt.executeQuery();
                if (!studentRs.next()) {
                    logSkippedRow(row, "Roll Number " + rollNumber + " not found in database.");
                    skippedCount++;
                    continue;
                }
                int studentId = studentRs.getInt("id");

                // Check if attendance already exists for this student on this date
                checkAttendanceStmt.setInt(1, studentId);
                checkAttendanceStmt.setDate(2, sqlDate);
                ResultSet attendanceRs = checkAttendanceStmt.executeQuery();
                attendanceRs.next();
                if (attendanceRs.getInt(1) > 0) {
                    logSkippedRow(row, "Attendance already recorded for Roll Number " + rollNumber + " on " + sqlDate);
                    skippedCount++;
                    continue;
                }

                // Add to batch
                insertAttendanceStmt.setInt(1, studentId);
                insertAttendanceStmt.setDate(2, sqlDate);
                insertAttendanceStmt.setString(3, status);
                insertAttendanceStmt.setInt(4, teacherId);
                insertAttendanceStmt.addBatch();
                insertedCount++;
            }

            // Execute batch update
            if (insertedCount > 0) {
                insertAttendanceStmt.executeBatch();
                conn.commit();
            }

            // Send response
            response.getWriter().println("Records uploaded: " + insertedCount);
            response.getWriter().println("Rows skipped: " + skippedCount);

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.severe("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            response.getWriter().println("Error: " + e.getMessage());
            logger.severe("Exception: " + e.getMessage());
        } finally {
            closeResources(conn, checkStudentStmt, checkAttendanceStmt, insertAttendanceStmt);
        }
    }

    // Helper function to validate headers
    private boolean isValidHeader(Row headerRow) {
        return headerRow.getCell(0).getStringCellValue().trim().equalsIgnoreCase("roll_number") &&
                headerRow.getCell(1).getStringCellValue().trim().equalsIgnoreCase("date") &&
                headerRow.getCell(2).getStringCellValue().trim().equalsIgnoreCase("status") &&
                headerRow.getCell(3).getStringCellValue().trim().equalsIgnoreCase("teacher_id");
    }


    // Helper function to extract numeric values safely
    private int getNumericCellValue(Cell cell) {
        if (cell != null && cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }
        return -1; // Return -1 for invalid values
    }

    // Helper function to extract date values safely
    private java.sql.Date getDateCellValue(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                java.util.Date utilDate = cell.getDateCellValue();
                return new java.sql.Date(utilDate.getTime());
            } else if (cell.getCellType() == CellType.STRING) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                java.util.Date utilDate = sdf.parse(cell.getStringCellValue());
                return new java.sql.Date(utilDate.getTime());
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    // Helper function to extract string values safely
    private String getStringCellValue(Cell cell) {
        if (cell != null) {
            if (cell.getCellType() == CellType.STRING) {
                return cell.getStringCellValue().trim();
            } else if (cell.getCellType() == CellType.NUMERIC) {
                return String.valueOf((int) cell.getNumericCellValue());
            }
        }
        return "";
    }

    private void logSkippedRow(Row row, String reason) {
        StringBuilder rowData = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            rowData.append(row.getCell(i) != null ? row.getCell(i).toString() : "NULL").append(" | ");
        }
        logger.warning("Skipping row " + row.getRowNum() + ": " + reason + " → " + rowData);
    }

    private Connection getDatabaseConnection() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/attendance_system";
        String user = "postgres";
        String password = "mark47";
        return DriverManager.getConnection(url, user, password);
    }

    private void closeResources(Connection conn, PreparedStatement... statements) {
        try {
            for (PreparedStatement stmt : statements) {
                if (stmt != null) stmt.close();
            }
            if (conn != null) conn.close();
        } catch (SQLException e) {
            logger.severe("Error closing resources: " + e.getMessage());
        }
    }
}
