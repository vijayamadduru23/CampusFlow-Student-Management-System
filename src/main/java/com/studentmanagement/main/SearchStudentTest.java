package com.studentmanagement.main;

import com.studentmanagement.dao.StudentDAO;
import com.studentmanagement.model.Student;

import java.sql.SQLException;

public class SearchStudentTest {

    public static void main(String[] args) {
        StudentDAO studentDAO = new StudentDAO();

        try {
            Student student = studentDAO.getStudentById(1001);

            if (student == null) {
                System.out.println("Student not found.");
                return;
            }

            System.out.println("Student ID: " + student.getStudentId());
            System.out.println("Name: " + student.getName());
            System.out.println("Age: " + student.getAge());
            System.out.println("Gender: " + student.getGender());
            System.out.println("Course: " + student.getCourse());
            System.out.println("Email: " + student.getEmail());
            System.out.println("Phone: " + student.getPhone());
            System.out.println("Address: " + student.getAddress());
            System.out.println("Marks: " + student.getMarks());
        } catch (SQLException exception) {
            System.err.println("Failed to search for student: " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}
