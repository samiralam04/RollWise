package com.attendance.controller;

import com.attendance.util.DBConnection;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;


@MultipartConfig(fileSizeThreshold = 2 * 1024 * 1024, // 2MB
        maxFileSize = 10 * 1024 * 1024, // 10MB
        maxRequestSize = 50 * 1024 * 1024) // 50MB
public class UploadExcelServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(UploadExcelServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Part filePart = request.getPart("file");

        if (filePart == null || filePart.getSize() == 0) {
            response.getWriter().write("Error: No file uploaded.");
            return;
        }

        if (!filePart.getSubmittedFileName().endsWith(".xlsx")) {
            response.getWriter().write("Error: Only .xlsx files are allowed.");
            return;
        }

        try (InputStream inputStream = filePart.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // Read the first sheet
            Iterator<Row> rowIterator = sheet.iterator();

            if (!rowIterator.hasNext()) {
                response.getWriter().write("Error: Uploaded file is empty.");
                return;
            }
            rowIterator.next(); // Skip header row

            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false); // Start transaction

                String insertQuery = "INSERT INTO attendance (student_id, subject, total_classes, attended_classes) " +
                        "VALUES (?, ?, ?, ?) ON CONFLICT (student_id, subject) " +
                        "DO UPDATE SET total_classes = attendance.total_classes + EXCLUDED.total_classes, " +
                        "attended_classes = attendance.attended_classes + EXCLUDED.attended_classes";

                try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                    while (rowIterator.hasNext()) {
                        Row row = rowIterator.next();
                        if (row.getCell(0) == null || row.getCell(1) == null ||
                                row.getCell(2) == null || row.getCell(3) == null) {
                            logger.warning("Skipping row due to missing values.");
                            continue;
                        }

                        int studentId = (int) row.getCell(0).getNumericCellValue();
                        String subject = row.getCell(1).getStringCellValue();
                        int totalClasses = (int) row.getCell(2).getNumericCellValue();
                        int attendedClasses = (int) row.getCell(3).getNumericCellValue();

                        stmt.setInt(1, studentId);
                        stmt.setString(2, subject);
                        stmt.setInt(3, totalClasses);
                        stmt.setInt(4, attendedClasses);
                        stmt.addBatch();
                    }

                    stmt.executeBatch();
                    conn.commit(); // Commit transaction
                    response.getWriter().write("Success: Attendance data uploaded.");
                } catch (SQLException e) {
                    conn.rollback(); // Rollback in case of error
                    logger.log(Level.SEVERE, "Database error", e);
                    response.getWriter().write("Database error: " + e.getMessage());
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Database connection error", e);
                response.getWriter().write("Database connection error.");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing the Excel file", e);
            response.getWriter().write("Error processing the Excel file.");
        }
    }
}
