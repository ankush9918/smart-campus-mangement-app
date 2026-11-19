package com.vvit.smartcollege.data.model

data class TeacherScheduleItem(
    val id: String,
    val time: String,
    val subjectName: String,
    val room: String,
    val section: String,
    val lectureType: String = "General Lecture",
    val dayOfWeek: String = "Mon"
)

enum class StudentAttendanceStatus {
    UNMARKED,
    PRESENT,
    ABSENT,
    LEAVE
}

data class StudentRosterItem(
    val id: String,
    val rollNo: String,
    val studentName: String,
    val section: String,
    val status: StudentAttendanceStatus = StudentAttendanceStatus.UNMARKED,
    val email: String = "",
    val phone: String = "",
    val department: String = "Information Technology",
    val currentSemester: String = "Semester 3 (Year 2)",
    val attendancePercentage: Int = 0,
    val cgpa: Double = 0.0,
    val guardianName: String = "Guardian / Parent",
    val guardianContact: String = "+91 98765 43210",
    val photoUrl: String? = null,
    val totalClasses: Int = 0,
    val attendedClasses: Int = 0
)

data class TeacherSharedMaterial(
    val id: String,
    val fileName: String,
    val sharedDate: String,
    val targetSection: String,
    val fileSize: String
)

data class AtRiskStudent(
    val id: String,
    val studentName: String,
    val rollNo: String,
    val reason: String,
    val attendancePercent: Int,
    val isReminded: Boolean = false
)
