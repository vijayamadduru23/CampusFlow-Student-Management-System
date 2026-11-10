package com.studentmanagement.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides connections to the StudentManagementDB MySQL database.
 */
public class DBConnection {

    private static final String DATABASE_URL =
            "jdbc:mysql://localhost:3306/StudentManagementDB";
    private static final String DATABASE_USER = "root";
    private static final String PASSWORD_ENVIRONMENT_VARIABLE = "STUDENT_DB_PASSWORD";

    private DBConnection() {
        // This utility class is not meant to be instantiated.
    }

    /**
     * Opens a database connection. Set STUDENT_DB_PASSWORD in the environment
     * before starting the application.
     *
     * @return an open connection to the database
     * @throws SQLException if the password is not configured or a connection cannot be made
     */
    public static Connection getConnection() throws SQLException {
        String password = System.getenv(PASSWORD_ENVIRONMENT_VARIABLE);
        if (password == null || password.isEmpty()) {
            throw new SQLException(
                    "Database password is not configured. Set the STUDENT_DB_PASSWORD environment variable.");
        }

        try {
            return DriverManager.getConnection(DATABASE_URL, DATABASE_USER, password);
        } catch (SQLException exception) {
            System.err.println("Unable to connect to the StudentManagementDB database.");
            throw exception;
        }
    }
}
