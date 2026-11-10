package com.studentmanagement.main;

import com.studentmanagement.dao.StudentDAO;
import com.studentmanagement.model.Student;

import java.math.BigDecimal;
import java.sql.SQLException;

public class UpdateStudentTest {

    public static void main(String[] args) {
        StudentDAO studentDAO = new StudentDAO();
        Student student = new Student(
                1001,
                "Rahul Kumar",
                21,
                "Male",
                "B.Tech CSE",
                "rahul.test@example.com",
                "9876543210",
                "Anantapur",
                BigDecimal.valueOf(92.0)
        );

        try {
            if (studentDAO.updateStudent(student)) {
                System.out.println("Student updated successfully!");
            } else {
                System.out.println("Student not found. Update failed.");
            }
        } catch (SQLException exception) {
            System.err.println("Failed to update student: " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}
