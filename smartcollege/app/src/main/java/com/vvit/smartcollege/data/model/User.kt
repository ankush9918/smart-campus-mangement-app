package com.vvit.smartcollege.data.model

enum class UserRole {
    STUDENT,
    TEACHER,
    ADMIN
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val avatarUrl: String? = null,
    val identifier: String // Roll number or Employee ID
)

data class StudentProfile(
    val id: String,
    val name: String,
    val rollNo: String,
    val email: String,
    val phone: String,
    val dateOfBirth: String,
    val bloodGroup: String,
    val department: String,
    val currentSemester: String,
    val section: String,
    val admissionYear: String,
    val guardianName: String,
    val guardianRelationship: String,
    val guardianContact: String,
    val cgpa: Double,
    val attendancePercentage: Int,
    val photoUrl: String? = null
)

data class TeacherProfile(
    val id: String,
    val name: String,
    val employeeId: String,
    val designation: String,
    val department: String,
    val email: String,
    val phone: String,
    val weeklyWorkloadHours: Int,
    val activeSubjectsCount: Int,
    val totalStudents: Int,
    val classesToday: Int,
    val pendingTasks: Int,
    val photoUrl: String? = null,
    val subjects: List<String> = emptyList()
)

data class AdminProfile(
    val id: String,
    val name: String,
    val title: String,
    val email: String,
    val phone: String
)
