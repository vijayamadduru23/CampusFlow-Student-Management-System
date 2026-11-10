package com.studentmanagement.main;

import com.studentmanagement.dao.StudentDAO;

import java.sql.SQLException;
import java.util.Map;

public class StatisticsTest {

    public static void main(String[] args) {
        StudentDAO studentDAO = new StudentDAO();

        try {
            Map<String, Object> statistics = studentDAO.getStatistics();

            System.out.println("Total Students: " + statistics.get("totalStudents"));
            System.out.println("Total Courses: " + statistics.get("totalCourses"));
            System.out.println("Average Marks: " + statistics.get("averageMarks"));
            System.out.println("Highest Marks: " + statistics.get("highestMarks"));
            System.out.println("Lowest Marks: " + statistics.get("lowestMarks"));
        } catch (SQLException exception) {
            System.err.println("Failed to retrieve student statistics: " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}
