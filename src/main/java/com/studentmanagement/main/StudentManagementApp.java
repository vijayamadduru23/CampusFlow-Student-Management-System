package com.studentmanagement.main;

import java.util.Scanner;

public class StudentManagementApp {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;

            while (running) {
                displayDashboard();
                System.out.print("Enter your choice: ");

                String input = scanner.nextLine().trim();
                int choice;

                try {
                    choice = Integer.parseInt(input);
                } catch (NumberFormatException exception) {
                    System.out.println("Invalid choice. Please enter a number from 1 to 7.");
                    continue;
                }

                switch (choice) {
                    case 1:
                        System.out.println("Add Student selected.");
                        break;
                    case 2:
                        System.out.println("View All Students selected.");
                        break;
                    case 3:
                        System.out.println("Search Student selected.");
                        break;
                    case 4:
                        System.out.println("Update Student selected.");
                        break;
                    case 5:
                        System.out.println("Delete Student selected.");
                        break;
                    case 6:
                        System.out.println("Statistics selected.");
                        break;
                    case 7:
                        System.out.println("Exiting Student Management System. Goodbye!");
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please select an option from 1 to 7.");
                }

                if (running) {
                    System.out.println();
                }
            }
        }
    }

    private static void displayDashboard() {
        System.out.println("==================================");
        System.out.println("     STUDENT MANAGEMENT SYSTEM");
        System.out.println("==================================");
        System.out.println("[1] Add Student");
        System.out.println("[2] View All Students");
        System.out.println("[3] Search Student");
        System.out.println("[4] Update Student");
        System.out.println("[5] Delete Student");
        System.out.println("[6] Statistics");
        System.out.println("[7] Exit");
        System.out.println("==================================");
    }
}
