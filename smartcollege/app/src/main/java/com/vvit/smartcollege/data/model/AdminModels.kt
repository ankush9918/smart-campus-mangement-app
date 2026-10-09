package com.vvit.smartcollege.data.model

data class AdminKPIs(
    val totalStudents: Int,
    val totalFaculty: Int,
    val totalDepartments: Int,
    val totalCourses: Int,
    val pendingFacultyApprovals: Int,
    val newAdmissionsToday: Int,
    val feeCollectionPercent: Int,
    val passRatePercent: Int,
    val avgAttendancePercent: Int,
    val yoyGrowthPercent: Double
)

data class StudentDirectoryEntry(
    val id: String,
    val name: String,
    val rollNo: String,
    val department: String,
    val semester: String,
    val email: String,
    val guardianName: String,
    val guardianPhone: String,
    val cgpa: Double,
    val attendancePercent: Int,
    val isActive: Boolean = true
)

data class FacultyDirectoryEntry(
    val id: String,
    val name: String,
    val employeeId: String,
    val designation: String,
    val department: String,
    val status: String = "Active", // Active, On Leave, HOD
    val assignedSubjects: List<String>,
    val weeklyWorkloadHours: Int
)

data class DepartmentEntry(
    val id: String,
    val name: String,
    val hodName: String,
    val facultyCount: Int,
    val studentCount: Int,
    val isActive: Boolean = true
)

data class ScheduledExam(
    val id: String,
    val subjectName: String,
    val subjectCode: String,
    val examDate: String,
    val timeDuration: String,
    val venue: String
)

data class LibraryBook(
    val id: String,
    val title: String,
    val author: String,
    val isbn: String,
    val availableCopies: Int,
    val totalCopies: Int
)

data class IssuedBookRecord(
    val id: String,
    val studentName: String,
    val rollNo: String,
    val bookTitle: String,
    val dueDate: String,
    val isOverdue: Boolean = false
)

data class UserRoleGroup(
    val id: String,
    val roleName: String,
    val memberCount: Int,
    val permissions: List<String>
)

data class DepartmentFeeCollection(
    val departmentName: String,
    val collectedPercent: Int
)
