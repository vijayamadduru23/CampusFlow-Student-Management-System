package com.studentmanagement.service;

import com.studentmanagement.model.Student;
import com.studentmanagement.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = Objects.requireNonNull(studentRepository);
    }

    /**
     * Keeps the existing standalone test class source-compatible.
     * Database operations require the Spring-managed constructor instead.
     */
    @Deprecated
    public StudentService() {
        this.studentRepository = null;
    }

    /**
     * Validates and adds a student.
     *
     * @param student the student details to validate and save
     * @return the ID generated for the new student
     * @throws IllegalArgumentException if the student details are invalid
     * @throws SQLException retained for compatibility with existing callers
     */
    public int addStudent(Student student) throws SQLException {
        validateStudent(student);
        Student savedStudent = getStudentRepository().save(student);
        return savedStudent.getStudentId();
    }

    /**
     * Retrieves all students.
     *
     * @return all student records
     * @throws SQLException retained for compatibility with existing callers
     */
    public ArrayList<Student> getAllStudents() throws SQLException {
        return new ArrayList<>(getStudentRepository().findAll());
    }

    /**
     * Retrieves a student by ID.
     *
     * @param studentId the ID of the student to retrieve
     * @return the matching student, or null if no student has that ID
     * @throws SQLException retained for compatibility with existing callers
     */
    public Student getStudentById(int studentId) throws SQLException {
        return getStudentRepository().findById(studentId).orElse(null);
    }

    /**
     * Validates and updates an existing student.
     *
     * @param student the student details, including the ID to update
     * @return true if a student was updated; otherwise, false
     * @throws IllegalArgumentException if the student details are invalid
     * @throws SQLException retained for compatibility with existing callers
     */
    public boolean updateStudent(Student student) throws SQLException {
        validateStudent(student);
        StudentRepository repository = getStudentRepository();

        if (!repository.existsById(student.getStudentId())) {
            return false;
        }

        repository.save(student);
        return true;
    }

    /**
     * Deletes a student by ID.
     *
     * @param studentId the ID of the student to delete
     * @return true if a student was deleted; otherwise, false
     * @throws SQLException retained for compatibility with existing callers
     */
    public boolean deleteStudent(int studentId) throws SQLException {
        StudentRepository repository = getStudentRepository();

        if (!repository.existsById(studentId)) {
            return false;
        }

        repository.deleteById(studentId);
        return true;
    }

    /**
     * Retrieves summary statistics for all students.
     *
     * @return the student count, course count, and marks statistics
     * @throws SQLException retained for compatibility with existing callers
     */
    public Map<String, Object> getStatistics() throws SQLException {
        ArrayList<Student> students = getAllStudents();
        Set<String> courses = students.stream()
                .map(Student::getCourse)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        double averageMarks = students.stream()
                .mapToDouble(student -> student.getMarks().doubleValue())
                .average()
                .orElse(0.0);
        double highestMarks = students.stream()
                .mapToDouble(student -> student.getMarks().doubleValue())
                .max()
                .orElse(0.0);
        double lowestMarks = students.stream()
                .mapToDouble(student -> student.getMarks().doubleValue())
                .min()
                .orElse(0.0);

        Map<String, Object> statistics = new LinkedHashMap<>();
        statistics.put("totalStudents", (long) students.size());
        statistics.put("totalCourses", (long) courses.size());
        statistics.put("averageMarks", averageMarks);
        statistics.put("highestMarks", highestMarks);
        statistics.put("lowestMarks", lowestMarks);
        return statistics;
    }

    private void validateStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student details must not be null.");
        }
        if (student.getName() == null || !student.getName().matches("[\\p{L} ]+")) {
            throw new IllegalArgumentException("Name must contain only letters and spaces.");
        }
        if (student.getAge() < 16 || student.getAge() > 60) {
            throw new IllegalArgumentException("Age must be between 16 and 60.");
        }
        if (student.getEmail() == null
                || !student.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Email must have a valid email format.");
        }
        if (student.getPhone() == null || !student.getPhone().matches("\\d{10}")) {
            throw new IllegalArgumentException("Phone number must contain exactly 10 digits.");
        }
        if (student.getMarks() == null
                || student.getMarks().compareTo(BigDecimal.ZERO) < 0
                || student.getMarks().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Marks must be between 0 and 100.");
        }
        if (student.getGender() == null || student.getGender().trim().isEmpty()) {
            throw new IllegalArgumentException("Gender must not be empty.");
        }
        if (student.getCourse() == null || student.getCourse().trim().isEmpty()) {
            throw new IllegalArgumentException("Course must not be empty.");
        }
        if (student.getAddress() == null || student.getAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Address must not be empty.");
        }
    }

    private StudentRepository getStudentRepository() {
        if (studentRepository == null) {
            throw new IllegalStateException(
                    "StudentService database operations require a Spring-managed StudentRepository.");
        }
        return studentRepository;
    }
}
