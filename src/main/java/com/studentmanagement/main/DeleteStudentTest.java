package com.studentmanagement.main;

import com.studentmanagement.dao.StudentDAO;

import java.sql.SQLException;

public class DeleteStudentTest {

    public static void main(String[] args) {
        StudentDAO studentDAO = new StudentDAO();

        try {
            if (studentDAO.deleteStudent(1001)) {
                System.out.println("Student deleted successfully!");
            } else {
                System.out.println("Student not found. Delete failed.");
            }
        } catch (SQLException exception) {
            System.err.println("Failed to delete student: " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}
