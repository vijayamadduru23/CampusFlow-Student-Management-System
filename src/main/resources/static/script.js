"use strict";

const API_BASE = "/api/students";
const studentTableBody = document.getElementById("studentTableBody");
const studentCount = document.getElementById("studentCount");
const tableSummary = document.getElementById("tableSummary");
const tableSearch = document.getElementById("tableSearch");
const recentStudents = document.getElementById("recentStudents");
const studentDialog = document.getElementById("studentDialog");
const studentForm = document.getElementById("studentForm");
const toastRegion = document.getElementById("toastRegion");
const saveStudentButton = document.getElementById("saveStudentButton");
const menuToggle = document.getElementById("menuToggle");
const sidebar = document.getElementById("sidebar");
const sidebarScrim = document.getElementById("sidebarScrim");

const state = {
    students: [],
    editingStudentId: null,
    filteredStudents: [],
    toastTimerIds: new Set()
};

const avatarColors = [
    ["#eeeaff", "#6559ce"],
    ["#e6f7f4", "#228d7e"],
    ["#fff1e5", "#c67d36"],
    ["#eaf1ff", "#4d72bf"],
    ["#fcebf2", "#bb5d83"]
];

document.addEventListener("DOMContentLoaded", () => {
    setPageDate();
    bindEvents();
    refreshDashboard();
});

function bindEvents() {
    document.getElementById("addStudentTop").addEventListener("click", openAddDialog);
    document.getElementById("addStudentTable").addEventListener("click", openAddDialog);
    document.getElementById("spotlightAdd").addEventListener("click", openAddDialog);
    document.getElementById("closeDialog").addEventListener("click", closeDialog);
    document.getElementById("cancelDialog").addEventListener("click", closeDialog);
    document.getElementById("refreshButton").addEventListener("click", refreshDashboard);
    document.getElementById("idSearchForm").addEventListener("submit", searchStudentById);
    tableSearch.addEventListener("input", renderStudentTable);
    studentForm.addEventListener("submit", saveStudent);
    studentForm.addEventListener("input", clearFieldError);
    menuToggle.addEventListener("click", toggleSidebar);
    sidebarScrim.addEventListener("click", closeSidebar);
    sidebar.querySelectorAll(".nav-link").forEach((link) => {
        link.addEventListener("click", () => {
            sidebar.querySelectorAll(".nav-link").forEach((item) => item.classList.remove("active"));
            link.classList.add("active");
            closeSidebar();
        });
    });

    document.addEventListener("keydown", (event) => {
        if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === "k") {
            event.preventDefault();
            tableSearch.focus();
        }
        if (event.key === "Escape" && sidebar.classList.contains("open")) {
            closeSidebar();
        }
    });

    studentDialog.addEventListener("click", (event) => {
        if (event.target === studentDialog) closeDialog();
    });
}

function setPageDate() {
    const now = new Date();
    document.getElementById("todayLabel").textContent = new Intl.DateTimeFormat(undefined, {
        weekday: "short",
        month: "short",
        day: "numeric"
    }).format(now);
    document.getElementById("currentYear").textContent = now.getFullYear();
}

async function refreshDashboard() {
    const refreshButton = document.getElementById("refreshButton");
    refreshButton.disabled = true;
    refreshButton.classList.add("is-loading");
    refreshButton.setAttribute("aria-busy", "true");

    const [studentsResult, statisticsResult] = await Promise.allSettled([
        request(`${API_BASE}`),
        request(`${API_BASE}/statistics`)
    ]);

    if (studentsResult.status === "fulfilled" && Array.isArray(studentsResult.value)) {
        state.students = studentsResult.value;
        renderStudentTable();
        renderRecentStudents();
        renderAnalytics(state.students);
        updateStudentCount();
    } else {
        const error = studentsResult.status === "rejected"
            ? studentsResult.reason
            : new Error("The students endpoint returned an unexpected response.");
        showToast("Could not load students", error.message, "error");
        if (state.students.length === 0) {
            renderTableMessage("Student records could not be loaded.", "Check that the application and database are running, then try again.");
            recentStudents.innerHTML = '<div class="empty-state"><strong>Recent students are unavailable</strong>Refresh after the service is back online.</div>';
            renderAnalyticsUnavailable();
            studentCount.textContent = "—";
        } else {
            renderStudentTable();
            renderRecentStudents();
            renderAnalytics(state.students);
        }
    }

    if (statisticsResult.status === "fulfilled") {
        updateStatistics(statisticsResult.value);
    } else {
        setStatisticsUnavailable();
        showToast("Statistics unavailable", statisticsResult.reason.message, "error");
    }

    refreshButton.disabled = false;
    refreshButton.classList.remove("is-loading");
    refreshButton.removeAttribute("aria-busy");
}

async function request(url, options = {}) {
    let response;
    try {
        response = await fetch(url, {
            ...options,
            headers: {
                ...(options.body ? { "Content-Type": "application/json" } : {}),
                ...options.headers
            }
        });
    } catch (error) {
        throw new Error("Unable to reach the server. Check your connection and try again.");
    }

    if (!response.ok) {
        const message = await readErrorMessage(response);
        const error = new Error(message || `Request failed with status ${response.status}.`);
        error.status = response.status;
        throw error;
    }
    if (response.status === 204) return null;

    const contentType = response.headers.get("content-type") || "";
    return contentType.includes("application/json") ? response.json() : null;
}

async function readErrorMessage(response) {
    try {
        const payload = await response.json();
        return payload.error || payload.message || payload.detail || "";
    } catch (error) {
        return "";
    }
}

function updateStatistics(statistics) {
    if (!statistics || typeof statistics !== "object") {
        setStatisticsUnavailable();
        return;
    }
    setStat("totalStudents", formatCount(statistics.totalStudents));
    setStat("totalCourses", formatCount(statistics.totalCourses));
    setStat("averageMarks", formatMarks(statistics.averageMarks));
    setStat("highestMarks", formatMarks(statistics.highestMarks));
    setStat("lowestMarks", formatMarks(statistics.lowestMarks));
}

function setStatisticsUnavailable() {
    ["totalStudents", "totalCourses", "averageMarks", "highestMarks", "lowestMarks"]
        .forEach((id) => setStat(id, "—"));
}

function setStat(id, value) {
    document.getElementById(id).textContent = value;
}

function formatCount(value) {
    const number = Number(value);
    return Number.isFinite(number) ? new Intl.NumberFormat().format(number) : "—";
}

function formatMarks(value) {
    const number = Number(value);
    return Number.isFinite(number) ? number.toFixed(2).replace(/\.?0+$/, "") : "—";
}

function updateStudentCount() {
    const count = state.students.length;
    studentCount.textContent = formatCount(count);
    tableSummary.textContent = `Showing ${formatCount(state.filteredStudents.length)} of ${formatCount(count)} student${count === 1 ? "" : "s"}`;
}

function renderStudentTable() {
    const query = tableSearch.value.trim().toLocaleLowerCase();
    state.filteredStudents = state.students.filter((student) => {
        const searchableValues = [
            student.studentId,
            student.name,
            student.age,
            student.gender,
            student.course,
            student.email,
            student.phone,
            student.address,
            student.marks
        ];
        return searchableValues.some((value) => String(value ?? "").toLocaleLowerCase().includes(query));
    });

    if (state.filteredStudents.length === 0) {
        const hasSearch = query.length > 0;
        renderTableMessage(
            hasSearch ? "No matching students." : "Your directory is ready for its first student.",
            hasSearch ? "Try a different name, course, email, or ID." : "Add a student to get started."
        );
        updateStudentCount();
        return;
    }

    studentTableBody.innerHTML = state.filteredStudents.map((student) => `
        <tr>
            <td class="id-cell">#${escapeHtml(student.studentId)}</td>
            <td>
                <div class="name-cell">
                    <span class="student-avatar" style="${avatarStyle(student.name)}">${escapeHtml(getInitials(student.name))}</span>
                    <span>${escapeHtml(student.name)}</span>
                </div>
            </td>
            <td>${escapeHtml(student.age)}</td>
            <td>${escapeHtml(student.gender)}</td>
            <td><span class="course-pill" title="${escapeHtml(student.course)}">${escapeHtml(student.course)}</span></td>
            <td class="email-cell" title="${escapeHtml(student.email)}">${escapeHtml(student.email)}</td>
            <td>${escapeHtml(student.phone)}</td>
            <td class="marks-cell">${escapeHtml(formatMarks(student.marks))}</td>
            <td>
                <div class="action-group">
                    <button class="action-button edit" type="button" data-action="edit" data-id="${escapeHtml(student.studentId)}" aria-label="Edit ${escapeHtml(student.name)}" title="Edit student">✎</button>
                    <button class="action-button delete" type="button" data-action="delete" data-id="${escapeHtml(student.studentId)}" aria-label="Delete ${escapeHtml(student.name)}" title="Delete student">×</button>
                </div>
            </td>
        </tr>
    `).join("");

    studentTableBody.querySelectorAll("[data-action='edit']").forEach((button) => {
        button.addEventListener("click", () => {
            const student = findStudent(button.dataset.id);
            if (student) openEditDialog(student);
        });
    });
    studentTableBody.querySelectorAll("[data-action='delete']").forEach((button) => {
        button.addEventListener("click", () => deleteStudent(button.dataset.id));
    });
    updateStudentCount();
}

function renderTableMessage(title, description) {
    studentTableBody.innerHTML = `
        <tr><td colspan="9" class="table-message">
            <div class="empty-state"><strong>${escapeHtml(title)}</strong>${escapeHtml(description)}</div>
        </td></tr>`;
    state.filteredStudents = [];
    tableSummary.textContent = title;
}

function renderRecentStudents() {
    if (state.students.length === 0) {
        recentStudents.innerHTML = '<div class="empty-state"><strong>No students yet</strong>New student profiles will appear here.</div>';
        return;
    }
    const latestStudents = [...state.students]
        .sort((a, b) => Number(b.studentId) - Number(a.studentId))
        .slice(0, 4);
    recentStudents.innerHTML = latestStudents.map((student) => `
        <div class="recent-item">
            <span class="student-avatar" style="${avatarStyle(student.name)}">${escapeHtml(getInitials(student.name))}</span>
            <span class="recent-copy">
                <strong>${escapeHtml(student.name)}</strong>
                <small>${escapeHtml(student.course)} · Student #${escapeHtml(student.studentId)}</small>
            </span>
            <span class="recent-score">${escapeHtml(formatMarks(student.marks))} pts</span>
        </div>
    `).join("");
}

function renderAnalytics(students) {
    const courseCounts = new Map();
    students.forEach((student) => {
        const course = String(student.course ?? "").trim() || "Unspecified course";
        courseCounts.set(course, (courseCounts.get(course) || 0) + 1);
    });

    const courses = [...courseCounts.entries()]
        .sort((first, second) => second[1] - first[1] || first[0].localeCompare(second[0]));
    const courseChart = document.getElementById("courseChart");
    const courseSummary = document.getElementById("courseChartSummary");

    if (courses.length === 0) {
        courseSummary.textContent = "Course enrolment will appear when students are added.";
        courseChart.innerHTML = '<div class="chart-empty"><strong>No course data yet</strong>Add student records to see the course mix.</div>';
    } else {
        const largestCourse = courses[0][1];
        courseSummary.textContent = `${formatCount(students.length)} student${students.length === 1 ? "" : "s"} across ${formatCount(courses.length)} course${courses.length === 1 ? "" : "s"}`;
        courseChart.innerHTML = courses.map(([course, count]) => `
            <div class="course-chart-row" aria-label="${escapeHtml(course)}: ${formatCount(count)} student${count === 1 ? "" : "s"}">
                <div class="chart-row-heading">
                    <span class="chart-row-label" title="${escapeHtml(course)}">${escapeHtml(course)}</span>
                    <strong>${formatCount(count)}</strong>
                </div>
                <div class="chart-track" aria-hidden="true">
                    <span class="course-chart-bar" style="width:${(count / largestCourse) * 100}%"></span>
                </div>
            </div>
        `).join("");
    }

    const validMarks = students
        .map((student) => student.marks === null || student.marks === "" ? NaN : Number(student.marks))
        .filter((marks) => Number.isFinite(marks) && marks >= 0 && marks <= 100);
    const performanceChart = document.getElementById("performanceChart");
    const performanceSummary = document.getElementById("performanceChartSummary");

    if (validMarks.length === 0) {
        performanceSummary.textContent = "Student marks will appear when available.";
        performanceChart.innerHTML = '<div class="chart-empty"><strong>No marks to visualize</strong>Valid scores from 0 to 100 will appear here.</div>';
        return;
    }

    const average = validMarks.reduce((sum, marks) => sum + marks, 0) / validMarks.length;
    const ranges = [
        { label: "0–59", lowerBound: 0, upperBound: 60 },
        { label: "60–69", lowerBound: 60, upperBound: 70 },
        { label: "70–79", lowerBound: 70, upperBound: 80 },
        { label: "80–89", lowerBound: 80, upperBound: 90 },
        { label: "90–100", lowerBound: 90, upperBound: 101 }
    ];
    const distribution = ranges.map((range) => ({
        ...range,
        count: validMarks.filter((marks) => marks >= range.lowerBound && marks < range.upperBound).length
    }));

    performanceSummary.textContent = `Class average ${formatMarks(average)} / 100 · ${formatCount(validMarks.length)} score${validMarks.length === 1 ? "" : "s"}`;
    performanceChart.innerHTML = distribution.map((range) => {
        const percentage = (range.count / validMarks.length) * 100;
        return `
            <div class="performance-chart-row">
                <div class="chart-row-heading">
                    <span class="chart-row-label">${range.label} marks</span>
                    <strong>${formatCount(range.count)}</strong>
                </div>
                <div class="chart-track" role="meter" aria-label="${range.label} marks" aria-valuemin="0" aria-valuemax="${validMarks.length}" aria-valuenow="${range.count}">
                    <span class="performance-chart-bar" style="width:${percentage}%"></span>
                </div>
            </div>
        `;
    }).join("");
}

function renderAnalyticsUnavailable() {
    document.getElementById("courseChartSummary").textContent = "Course data is unavailable.";
    document.getElementById("courseChart").innerHTML = '<div class="chart-empty"><strong>Could not load course data</strong>Check the server connection, then refresh.</div>';
    document.getElementById("performanceChartSummary").textContent = "Marks data is unavailable.";
    document.getElementById("performanceChart").innerHTML = '<div class="chart-empty"><strong>Could not load marks data</strong>Check the server connection, then refresh.</div>';
}

function openAddDialog() {
    state.editingStudentId = null;
    studentForm.reset();
    clearAllErrors();
    document.getElementById("dialogEyebrow").textContent = "NEW STUDENT";
    document.getElementById("dialogTitle").textContent = "Add a student";
    document.getElementById("dialogDescription").textContent = "Enter the student’s profile details below.";
    saveStudentButton.innerHTML = '<span class="button-plus" aria-hidden="true">+</span> Save student';
    studentDialog.showModal();
    studentForm.elements.name.focus();
}

function openEditDialog(student) {
    state.editingStudentId = student.studentId;
    studentForm.reset();
    clearAllErrors();
    document.getElementById("dialogEyebrow").textContent = `EDITING STUDENT #${student.studentId}`;
    document.getElementById("dialogTitle").textContent = "Update student";
    document.getElementById("dialogDescription").textContent = "Make changes to this student’s profile.";
    saveStudentButton.textContent = "Save changes";
    studentForm.elements.name.value = student.name ?? "";
    studentForm.elements.age.value = student.age ?? "";
    studentForm.elements.gender.value = student.gender ?? "";
    studentForm.elements.course.value = student.course ?? "";
    studentForm.elements.email.value = student.email ?? "";
    studentForm.elements.phone.value = student.phone ?? "";
    studentForm.elements.address.value = student.address ?? "";
    studentForm.elements.marks.value = student.marks ?? "";
    studentDialog.showModal();
    studentForm.elements.name.focus();
}

function closeDialog() {
    if (studentDialog.open) studentDialog.close();
}

async function saveStudent(event) {
    event.preventDefault();
    clearAllErrors();
    const formData = new FormData(studentForm);
    const validationErrors = validateStudentForm(formData);
    if (Object.keys(validationErrors).length > 0) {
        displayFieldErrors(validationErrors);
        const firstInvalidField = studentForm.querySelector('[aria-invalid="true"]');
        if (firstInvalidField) firstInvalidField.focus();
        return;
    }

    const student = {
        name: formData.get("name").trim(),
        age: Number(formData.get("age")),
        gender: formData.get("gender").trim(),
        course: formData.get("course").trim(),
        email: formData.get("email").trim(),
        phone: formData.get("phone").trim(),
        address: formData.get("address").trim(),
        marks: Number(formData.get("marks"))
    };

    const editingId = state.editingStudentId;
    saveStudentButton.disabled = true;
    saveStudentButton.innerHTML = '<span class="loader"></span> Saving…';

    try {
        const savedStudent = editingId === null
            ? await request(API_BASE, { method: "POST", body: JSON.stringify(student) })
            : await request(`${API_BASE}/${encodeURIComponent(editingId)}`, {
                method: "PUT",
                body: JSON.stringify(student)
            });

        closeDialog();
        showToast(
            editingId === null ? "Student added" : "Student updated",
            editingId === null
                ? `Student record #${savedStudent?.studentId ?? "—"} was created successfully.`
                : `${student.name}’s profile was updated successfully.`
        );
        await refreshDashboard();
    } catch (error) {
        showToast(editingId === null ? "Could not add student" : "Could not update student", error.message, "error");
    } finally {
        saveStudentButton.disabled = false;
        saveStudentButton.innerHTML = editingId === null
            ? '<span class="button-plus" aria-hidden="true">+</span> Save student'
            : "Save changes";
    }
}

function validateStudentForm(formData) {
    const errors = {};
    const name = String(formData.get("name") ?? "").trim();
    const age = Number(formData.get("age"));
    const gender = String(formData.get("gender") ?? "").trim();
    const course = String(formData.get("course") ?? "").trim();
    const email = String(formData.get("email") ?? "").trim();
    const phone = String(formData.get("phone") ?? "").trim();
    const address = String(formData.get("address") ?? "").trim();
    const marks = Number(formData.get("marks"));

    if (!/^[\p{L} ]+$/u.test(name)) errors.name = "Use letters and spaces only.";
    if (!Number.isInteger(age) || age < 16 || age > 60) errors.age = "Age must be between 16 and 60.";
    if (!gender) errors.gender = "Enter a gender.";
    if (!course) errors.course = "Enter a course.";
    if (!/^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(email)) errors.email = "Enter a valid email address.";
    if (!/^\d{10}$/.test(phone)) errors.phone = "Enter exactly 10 digits.";
    if (!Number.isFinite(marks) || marks < 0 || marks > 100) errors.marks = "Marks must be between 0 and 100.";
    if (!address) errors.address = "Enter an address.";
    return errors;
}

function displayFieldErrors(errors) {
    Object.entries(errors).forEach(([fieldName, message]) => {
        const field = studentForm.elements[fieldName];
        const messageElement = studentForm.querySelector(`[data-error-for="${fieldName}"]`);
        field.setAttribute("aria-invalid", "true");
        messageElement.textContent = message;
    });
}

function clearFieldError(event) {
    const field = event.target;
    if (!field.name) return;
    field.removeAttribute("aria-invalid");
    const messageElement = studentForm.querySelector(`[data-error-for="${field.name}"]`);
    if (messageElement) messageElement.textContent = "";
}

function clearAllErrors() {
    studentForm.querySelectorAll("[aria-invalid='true']").forEach((field) => field.removeAttribute("aria-invalid"));
    studentForm.querySelectorAll(".field-error").forEach((message) => { message.textContent = ""; });
}

async function searchStudentById(event) {
    event.preventDefault();
    const idInput = document.getElementById("studentIdSearch");
    const findButton = event.currentTarget.querySelector("button[type='submit']");
    const studentId = idInput.value.trim();
    if (!/^[1-9]\d*$/.test(studentId)) {
        showToast("Enter a valid student ID", "Student ID must be a positive whole number.", "error");
        idInput.focus();
        return;
    }

    findButton.disabled = true;
    findButton.textContent = "Searching…";
    try {
        const student = await request(`${API_BASE}/${encodeURIComponent(studentId)}`);
        if (!student) {
            showToast("Student not found", `There is no student with ID ${studentId}.`, "error");
            return;
        }
        tableSearch.value = "";
        renderStudentTable();
        const row = [...studentTableBody.querySelectorAll("tr")]
            .find((tableRow) => tableRow.querySelector(".id-cell")?.textContent === `#${student.studentId}`);
        if (row) {
            row.classList.add("search-highlight");
            row.scrollIntoView({ behavior: "smooth", block: "center" });
            window.setTimeout(() => row.classList.remove("search-highlight"), 1800);
        }
        showToast("Student found", `${student.name} · Student #${student.studentId}`);
    } catch (error) {
        if (error.status === 404) {
            showToast("Student not found", `There is no student with ID ${studentId}.`, "error");
        } else {
            showToast("Search failed", error.message, "error");
        }
    } finally {
        findButton.disabled = false;
        findButton.textContent = "Find ID";
    }
}

async function deleteStudent(studentId) {
    const student = findStudent(studentId);
    if (!student) return;
    const confirmed = window.confirm(`Delete ${student.name} (Student #${student.studentId})? This action cannot be undone.`);
    if (!confirmed) return;

    try {
        await request(`${API_BASE}/${encodeURIComponent(studentId)}`, { method: "DELETE" });
        showToast("Student deleted", `${student.name}’s record was removed.`);
        await refreshDashboard();
    } catch (error) {
        showToast("Could not delete student", error.message, "error");
    }
}

function findStudent(studentId) {
    return state.students.find((student) => String(student.studentId) === String(studentId));
}

function getInitials(name) {
    const parts = String(name ?? "").trim().split(/\s+/).filter(Boolean);
    return parts.slice(0, 2).map((part) => part[0].toLocaleUpperCase()).join("") || "S";
}

function avatarStyle(name) {
    const seed = String(name ?? "").split("").reduce((total, character) => total + character.charCodeAt(0), 0);
    const [background, color] = avatarColors[seed % avatarColors.length];
    return `color:${color};background:${background}`;
}

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, (character) => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        "\"": "&quot;",
        "'": "&#39;"
    })[character]);
}

function showToast(title, message, type = "success") {
    const toast = document.createElement("div");
    toast.className = `toast${type === "error" ? " error" : ""}`;
    toast.setAttribute("role", type === "error" ? "alert" : "status");
    toast.innerHTML = `
        <span class="toast-mark" aria-hidden="true">${type === "error" ? "!" : "✓"}</span>
        <span><strong>${escapeHtml(title)}</strong><p>${escapeHtml(message)}</p></span>`;
    toastRegion.appendChild(toast);
    const timerId = window.setTimeout(() => {
        state.toastTimerIds.delete(timerId);
        toast.remove();
    }, 4200);
    state.toastTimerIds.add(timerId);
}

function toggleSidebar() {
    const isOpen = sidebar.classList.toggle("open");
    sidebarScrim.classList.toggle("visible", isOpen);
    menuToggle.setAttribute("aria-expanded", String(isOpen));
    menuToggle.setAttribute("aria-label", isOpen ? "Close navigation" : "Open navigation");
}

function closeSidebar() {
    sidebar.classList.remove("open");
    sidebarScrim.classList.remove("visible");
    menuToggle.setAttribute("aria-expanded", "false");
    menuToggle.setAttribute("aria-label", "Open navigation");
}
