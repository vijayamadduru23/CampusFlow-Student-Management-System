package com.studentmanagement.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides connections to the StudentManagementDB MySQL database.
 */
public class DBConnection {

    private static final String DEFAULT_DATABASE_URL =
            "jdbc:mysql://localhost:3306/StudentManagementDB";

    private DBConnection() {
        // This utility class is not meant to be instantiated.
    }

    /**
     * Opens a database connection using DB_URL, DB_USERNAME, and DB_PASSWORD.
     * DB_URL defaults to the local development database.
     *
     * @return an open connection to the database
     * @throws SQLException if credentials are not configured or a connection cannot be made
     */
    public static Connection getConnection() throws SQLException {
        String databaseUrl = environmentOrDefault("DB_URL", DEFAULT_DATABASE_URL);
        String databaseUser = requiredEnvironment("DB_USERNAME");
        String password = requiredEnvironment("DB_PASSWORD");

        try {
            return DriverManager.getConnection(databaseUrl, databaseUser, password);
        } catch (SQLException exception) {
            System.err.println("Unable to connect to the StudentManagementDB database.");
            throw exception;
        }
    }

    private static String environmentOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static String requiredEnvironment(String name) throws SQLException {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new SQLException("Database configuration is missing. Set the " + name + " environment variable.");
        }
        return value;
    }
}
