package com.studentmanagement.main;

import com.studentmanagement.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class DBTest {

    public static void main(String[] args) {
        try (Connection connection = DBConnection.getConnection()) {
            System.out.println("Database Connected Successfully!");
        } catch (SQLException exception) {
            System.err.println("Database connection failed: " + exception.getMessage());
        }
    }
}
