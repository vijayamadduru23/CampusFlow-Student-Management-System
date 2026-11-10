package com.studentmanagement.main;

import com.studentmanagement.dao.StudentDAO;
import com.studentmanagement.model.Student;

import java.math.BigDecimal;
import java.sql.SQLException;

public class AddStudentTest {

    public static void main(String[] args) {
        // The constructor expects studentId first; the database generates the ID.
        Student student = new Student(
                0,
                "Rahul Kumar",
                21,
                "Male",
                "B.Tech CSE",
                "rahul.test@example.com",
                "9876543210",
                "Anantapur",
                BigDecimal.valueOf(85.5)
        );

        StudentDAO studentDAO = new StudentDAO();
        try {
            int studentId = studentDAO.addStudent(student);
            System.out.println("Student added successfully. Generated Student ID: " + studentId);
        } catch (SQLException exception) {
            System.err.println("Failed to add student: " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}
