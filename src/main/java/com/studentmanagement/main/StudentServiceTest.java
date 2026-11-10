package com.studentmanagement.main;

import com.studentmanagement.model.Student;
import com.studentmanagement.service.StudentService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

public class StudentServiceTest {

    public static void main(String[] args) {
        StudentService studentService = new StudentService();

        Student validStudent = new Student(
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

        try {
            int studentId = studentService.addStudent(validStudent);
            System.out.println("Student added successfully. Generated Student ID: " + studentId);

            System.out.println("\nAll students:");
            ArrayList<Student> students = studentService.getAllStudents();
            if (students.isEmpty()) {
                System.out.println("No students found.");
            } else {
                for (Student student : students) {
                    printStudent(student);
                }
            }

            System.out.println("\nSearching for the new student:");
            Student foundStudent = studentService.getStudentById(studentId);
            if (foundStudent == null) {
                System.out.println("Student not found.");
            } else {
                printStudent(foundStudent);
            }

            validStudent.setStudentId(studentId);
            validStudent.setMarks(BigDecimal.valueOf(92.0));
            if (studentService.updateStudent(validStudent)) {
                System.out.println("\nStudent updated successfully.");
                Student updatedStudent = studentService.getStudentById(studentId);
                if (updatedStudent != null) {
                    if (updatedStudent.getMarks().compareTo(BigDecimal.valueOf(92.0)) == 0) {
                        System.out.println("Verified: updated marks are " + updatedStudent.getMarks());
                    } else {
                        System.out.println("Update verification failed. Retrieved marks: "
                                + updatedStudent.getMarks());
                    }
                } else {
                    System.out.println("Student could not be found to verify the update.");
                }
            } else {
                System.out.println("\nStudent update failed.");
            }

            System.out.println("\nStudent statistics:");
            Map<String, Object> statistics = studentService.getStatistics();
            System.out.println("Total Students: " + statistics.get("totalStudents"));
            System.out.println("Total Courses: " + statistics.get("totalCourses"));
            System.out.println("Average Marks: " + statistics.get("averageMarks"));
            System.out.println("Highest Marks: " + statistics.get("highestMarks"));
            System.out.println("Lowest Marks: " + statistics.get("lowestMarks"));

            if (studentService.deleteStudent(studentId)) {
                System.out.println("\nTest student deleted successfully.");
                if (studentService.getStudentById(studentId) == null) {
                    System.out.println("Verified: the test student no longer exists.");
                } else {
                    System.out.println("Verification failed: the test student still exists.");
                }
            } else {
                System.out.println("\nTest student was not found for deletion.");
            }
        } catch (SQLException exception) {
            System.err.println("Student service database test failed: " + exception.getMessage());
            exception.printStackTrace();
        }

        testInvalidInput(studentService, "Name containing numbers",
                new Student(0, "Rahul123", 21, "Male", "B.Tech CSE",
                        "rahul.test@example.com", "9876543210", "Anantapur", BigDecimal.valueOf(85.5)));
        testInvalidInput(studentService, "Age below 16",
                new Student(0, "Rahul Kumar", 15, "Male", "B.Tech CSE",
                        "rahul.test@example.com", "9876543210", "Anantapur", BigDecimal.valueOf(85.5)));
        testInvalidInput(studentService, "Invalid email",
                new Student(0, "Rahul Kumar", 21, "Male", "B.Tech CSE",
                        "invalid-email", "9876543210", "Anantapur", BigDecimal.valueOf(85.5)));
        testInvalidInput(studentService, "Phone number with fewer than 10 digits",
                new Student(0, "Rahul Kumar", 21, "Male", "B.Tech CSE",
                        "rahul.test@example.com", "987654321", "Anantapur", BigDecimal.valueOf(85.5)));
        testInvalidInput(studentService, "Marks greater than 100",
                new Student(0, "Rahul Kumar", 21, "Male", "B.Tech CSE",
                        "rahul.test@example.com", "9876543210", "Anantapur", BigDecimal.valueOf(100.5)));
    }

    private static void testInvalidInput(StudentService studentService, String testDescription,
                                         Student student) {
        try {
            studentService.addStudent(student);
            System.out.println(testDescription + ": validation did not reject the input.");
        } catch (IllegalArgumentException exception) {
            System.out.println(testDescription + ": " + exception.getMessage());
        } catch (SQLException exception) {
            System.err.println(testDescription + ": database operation failed: "
                    + exception.getMessage());
            exception.printStackTrace();
        }
    }

    private static void printStudent(Student student) {
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
}
