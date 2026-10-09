package com.vvit.smartcollege.data.model

data class SubjectAttendance(
    val id: String,
    val subjectName: String,
    val subjectCode: String,
    val attendedClasses: Int,
    val totalClasses: Int,
    val percentage: Int,
    val isAlert: Boolean = false
)

data class StudentTimetableEntry(
    val id: String,
    val dayOfWeek: String, // Mon, Tue, Wed, Thu, Fri, Sat
    val time: String,
    val duration: String,
    val subjectName: String,
    val facultyName: String,
    val room: String,
    val isOngoing: Boolean = false,
    val isBreak: Boolean = false
)

enum class AssignmentStatus {
    PENDING,
    SUBMITTED,
    OVERDUE
}

data class StudentAssignment(
    val id: String,
    val subjectName: String,
    val title: String,
    val instructions: String,
    val dueDate: String,
    val status: AssignmentStatus = AssignmentStatus.PENDING,
    val submissionDate: String? = null,
    val maxMarks: Int = 100,
    val obtainedMarks: Int? = null,
    val submittedFileName: String? = null,
    val professorName: String = "",
    val fileName: String? = null,
    val fileUri: String? = null
)

data class StudentStudyMaterial(
    val id: String,
    val title: String,
    val subjectName: String,
    val sharedBy: String,
    val sharedDate: String,
    val fileSize: String = "Unknown",
    val fileType: String = "PDF",
    val downloadUrl: String = "",
    val fileName: String? = null,
    val fileUri: String? = null
)

data class GradeItem(
    val subjectName: String,
    val subjectCode: String,
    val internalMarks: Int,
    val externalMarks: Int,
    val totalMarks: Int,
    val grade: String
)

data class SemesterResult(
    val semesterName: String,
    val semesterNumber: Int,
    val sgpa: Double,
    val grades: List<GradeItem>
)

data class FeeDetails(
    val pendingAmount: Long,
    val totalInvoiced: Long,
    val paidTillDate: Long,
    val currentSemesterFee: Long,
    val semesterLabel: String
)

data class FeeTransaction(
    val id: String,
    val title: String,
    val transactionId: String,
    val date: String,
    val amount: Long,
    val status: String = "Success"
)

data class NoticeItem(
    val id: String,
    val title: String,
    val category: String, // ACADEMICS, EXAMS, EVENT, FACULTY COUNCIL
    val timeAgo: String,
    val description: String,
    val isUrgent: Boolean = false,
    val date: String
)

data class StudentLeaveApplication(
    val id: String,
    val studentName: String,
    val rollNo: String,
    val fromDate: String,
    val toDate: String,
    val durationDays: Int,
    val reason: String,
    val status: String = "Pending", // Pending, Approved, Rejected
    val appliedDate: String
)
