# CampusFlow — Student Management System

CampusFlow is a student administration web application built with a Java Spring Boot backend and a responsive HTML, CSS, and JavaScript frontend. It provides a dashboard for managing student records and reviewing enrollment and marks information backed by MySQL.

## Features

- Add, view, edit, and delete student records.
- Search the student directory by name and other displayed fields in the browser.
- Retrieve an individual student by student ID.
- View dashboard statistics: student count, course count, and average, highest, and lowest marks.
- View course-wise enrollment and marks-distribution charts generated from student records returned by the backend.
- Validate student details in the frontend and service layer.
- Protect the student management workspace with administrator sign-in.
- Use the interface on desktop, tablet, and mobile screen sizes.

## Technologies

| Area | Technologies |
| --- | --- |
| Frontend | HTML, CSS, JavaScript |
| Backend | Java 17, Spring Boot 3.5.6, Spring Web, Spring Security |
| Persistence | Spring Data JPA, Hibernate |
| Database | MySQL, MySQL Connector/J |
| Build | Apache Maven |

The interface also loads the DM Sans and Manrope typefaces from Google Fonts.

## Architecture

The browser serves the static frontend from Spring Boot's `static` resources. The frontend calls the `/api/students` REST API using JSON. The API controller delegates student operations to the service, which uses a Spring Data JPA repository to persist the `Student` entity in MySQL.

```text
Browser (HTML / CSS / JavaScript)
                |
                | JSON over HTTP
                v
StudentController (/api/students)
                |
                v
StudentService
                |
                v
StudentRepository (Spring Data JPA)
                |
                v
MySQL (StudentManagementDB.students)
```

The project also contains a JDBC `StudentDAO`, a JDBC connection utility, and standalone console demonstration classes. The web API's persistence path uses Spring Data JPA.

## Project structure

```text
demo/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   ├── com/studentmanagement/
        │   │   ├── StudentManagementApplication.java
        │   │   ├── controller/       # REST API endpoints
        │   │   ├── dao/              # JDBC DAO
        │   │   ├── main/             # Standalone console examples
        │   │   ├── model/            # Student JPA entity
        │   │   ├── repository/       # Spring Data JPA repository
        │   │   ├── service/          # Business logic and validation
        │   │   └── util/             # JDBC connection utility
        │   └── com/example/          # Additional standalone Java entry point
        └── resources/
            ├── application.properties
            └── static/
                ├── index.html
                ├── script.js
                └── style.css
```

## Prerequisites

- JDK 17
- Apache Maven
- MySQL Server

## MySQL setup

The datasource reads its connection details from `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. For local development, `DB_URL` defaults to `jdbc:mysql://localhost:3306/StudentManagementDB`; set all three variables in your shell. For cloud deployment, configure `DB_URL` to the managed MySQL JDBC URL and provide the database username and password using the hosting platform's secret manager. Do not put credentials in source control.

Create the database if it does not already exist:

```sql
CREATE DATABASE IF NOT EXISTS StudentManagementDB;
```

The application uses `spring.jpa.hibernate.ddl-auto=validate`. Hibernate checks the existing schema at startup; it does **not** create, alter, or delete database tables. For a new database, manually review and apply [`database/migrations/001_create_students.sql`](database/migrations/001_create_students.sql) before starting the application. It creates the table only when it does not already exist. It will not repair an existing mismatched table; resolve schema differences explicitly before startup.

The schema represented by the entity and script is:

| Column | Entity field | MySQL type | Constraints |
| --- | --- | --- | --- |
| `student_id` | `studentId` | `INT` | Primary key, `NOT NULL`, `AUTO_INCREMENT` (JPA `IDENTITY`) |
| `name` | `name` | `VARCHAR(255)` | `NOT NULL` |
| `age` | `age` | `INT` | `NOT NULL`; application validates 16–60 |
| `gender` | `gender` | `VARCHAR(255)` | `NOT NULL` |
| `course` | `course` | `VARCHAR(255)` | `NOT NULL` |
| `email` | `email` | `VARCHAR(255)` | `NOT NULL` |
| `phone` | `phone` | `VARCHAR(255)` | `NOT NULL` |
| `address` | `address` | `VARCHAR(255)` | `NOT NULL` |
| `marks` | `marks` | `DECIMAL(5,2)` | `NOT NULL`; application validates 0–100 |

The `VARCHAR(255)` lengths are the JPA/Hibernate default for the entity's unannotated `String` fields. `marks` explicitly declares precision 5 and scale 2. The entity declares no unique constraints or secondary indexes; the script adds none. `NOT NULL` for the student data columns matches the service's required-field validation. The script does not add range `CHECK` constraints because those rules are currently enforced by the service.

## Configure database environment variables

Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` in the same environment used to launch the application. Only `DB_URL` has a local development default; set the username and password explicitly. Cloud providers should supply all three values, and the password should be stored as a secret.

**Windows PowerShell — current terminal:**

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/StudentManagementDB"
$env:DB_USERNAME = "<your-local-mysql-username>"
$env:DB_PASSWORD = "<your-local-mysql-password>"
```

**Windows Command Prompt — current terminal:**

```cmd
set DB_URL=jdbc:mysql://localhost:3306/StudentManagementDB
set DB_USERNAME=<your-local-mysql-username>
set DB_PASSWORD=<your-local-mysql-password>
```

**macOS / Linux — current terminal:**

```bash
export DB_URL="jdbc:mysql://localhost:3306/StudentManagementDB"
export DB_USERNAME="<your-local-mysql-username>"
export DB_PASSWORD="<your-local-mysql-password>"
```

Use your database provider's JDBC URL, username, and password in deployment; do not use the local URL there. The standalone JDBC utility uses these same variables.

## Run the application

The administrator account is configured using `CAMPUSFLOW_ADMIN_USERNAME` and `CAMPUSFLOW_ADMIN_PASSWORD` environment variables. Set both in the deployment platform's secret/configuration manager; never put the password in source control. The password must be at least 16 characters and no more than 72 UTF-8 bytes. The application stores a BCrypt hash in memory and requires these variables at startup.

For local development, set `SESSION_COOKIE_SECURE=false` when running over plain HTTP. Keep the secure-cookie default enabled behind HTTPS in deployment. The session cookie is HTTP-only and SameSite strict; login and all write requests use CSRF protection.

The server listens on the `PORT` environment variable when supplied and defaults to `8080` for local development.

Set the administrator account in your local shell before launching (use your own values; do not save them in project files):

```powershell
$env:CAMPUSFLOW_ADMIN_USERNAME = "<your-admin-username>"
$env:CAMPUSFLOW_ADMIN_PASSWORD = "<your-strong-admin-password>"
$env:SESSION_COOKIE_SECURE = "false"
```

From the project directory (the directory containing `pom.xml`), set the database and administrator environment variables as described above, then run:

```bash
mvn spring-boot:run
```

Open the website at **[http://localhost:8080](http://localhost:8080)** and sign in with the configured administrator credentials.

To package the application with Maven:

```bash
mvn clean package
```

## REST API

All student endpoints use the `/api/students` base path. Request and response bodies use JSON where applicable.

| Method | Endpoint | Description | Success response |
| --- | --- | --- | --- |
| `GET` | `/api/students` | Return all student records. | `200 OK` with a JSON array |
| `GET` | `/api/students/{id}` | Return one student by ID. | `200 OK` with a student; `404 Not Found` if absent |
| `POST` | `/api/students` | Create a student. | `201 Created` with the created student |
| `PUT` | `/api/students/{id}` | Update a student's details. | `200 OK` with the updated student; `404 Not Found` if absent |
| `DELETE` | `/api/students/{id}` | Delete a student. | `204 No Content`; `404 Not Found` if absent |
| `GET` | `/api/students/statistics` | Return student and course counts and marks statistics. | `200 OK` with a JSON object |

The statistics response contains `totalStudents`, `totalCourses`, `averageMarks`, `highestMarks`, and `lowestMarks`.

The directory's name search and the course and performance charts are implemented in the frontend using the student records returned by `GET /api/students`; there is no separate name-search REST endpoint.

### Student JSON example

```json
{
  "name": "Alex Morgan",
  "age": 21,
  "gender": "Female",
  "course": "Computer Science",
  "email": "alex@example.com",
  "phone": "9876543210",
  "address": "Example City",
  "marks": 88.5
}
```

## Screenshots

Add current application screenshots in `docs/screenshots/` and replace these placeholders:

- **Dashboard:** `docs/screenshots/dashboard.png`
- **Student directory:** `docs/screenshots/student-directory.png`
- **Add / edit student form:** `docs/screenshots/student-form.png`

## Future enhancements

- Add automated integration and API tests.
- Add pagination and server-side filtering for larger student directories.
- Add database migration tooling and versioned schema migrations.
- Add authentication and role-based access control.
- Add API documentation, such as an OpenAPI specification.
