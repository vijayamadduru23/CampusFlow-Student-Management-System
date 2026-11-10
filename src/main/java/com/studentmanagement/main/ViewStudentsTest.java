package com.studentmanagement.main;

import com.studentmanagement.dao.StudentDAO;
import com.studentmanagement.model.Student;

import java.sql.SQLException;
import java.util.ArrayList;

public class ViewStudentsTest {

    public static void main(String[] args) {
        StudentDAO studentDAO = new StudentDAO();

        try {
            ArrayList<Student> students = studentDAO.getAllStudents();

            if (students.isEmpty()) {
                System.out.println("No students found.");
                return;
            }

            for (Student student : students) {
                System.out.println("Student ID: " + student.getStudentId());
                System.out.println("Name: " + student.getName());
                System.out.println("Age: " + student.getAge());
                System.out.println("Gender: " + student.getGender());
                System.out.println("Course: " + student.getCourse());
                System.out.println("Email: " + student.getEmail());
                System.out.println("Phone: " + student.getPhone());
                System.out.println("Address: " + student.getAddress());
                System.out.println("Marks: " + student.getMarks());
                System.out.println("------------------------------");
            }
        } catch (SQLException exception) {
            System.err.println("Failed to retrieve students: " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}
