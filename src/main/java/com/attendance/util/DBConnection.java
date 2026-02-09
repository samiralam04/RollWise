package com.attendance.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static String URL = "jdbc:postgresql://localhost:5432/attendance_system";
    private static String USER = "postgres";
    private static String PASSWORD = "mark47";
    private static final String DRIVER = "org.postgresql.Driver";

    static {
        loadEnv();
    }

    private static void loadEnv() {
        java.io.File envFile = new java.io.File(".env");
        if (!envFile.exists()) {
            System.err.println(".env file not found, using default credentials");
            return;
        }

        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(envFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#"))
                    continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    switch (key) {
                        case "DB_URL":
                            URL = value;
                            break;
                        case "DB_USER":
                            USER = value;
                            break;
                        case "DB_PASSWORD":
                            PASSWORD = value;
                            break;
                    }
                }
            }
            System.out.println("Loaded database configuration from .env");
        } catch (java.io.IOException e) {
            System.err.println("Error reading .env file: " + e.getMessage());
        }
    }

    // Method to establish and return a database connection
    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName(DRIVER);
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connected successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            e.printStackTrace();
        }
        return connection;
    }

    // Method to close the connection
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed successfully!");
            } catch (SQLException e) {
                System.err.println("Error closing database connection!");
                e.printStackTrace();
            }
        }
    }
}
