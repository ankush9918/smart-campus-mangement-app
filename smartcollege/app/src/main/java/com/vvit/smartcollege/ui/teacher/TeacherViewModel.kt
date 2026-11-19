package com.vvit.smartcollege.ui.teacher

import androidx.lifecycle.ViewModel
import com.vvit.smartcollege.data.model.*
import com.vvit.smartcollege.data.repository.TeacherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TeacherViewModel(
    private val teacherRepository: TeacherRepository = TeacherRepository()
) : ViewModel() {

    val profile: StateFlow<TeacherProfile> = teacherRepository.teacherProfile
    val schedule: StateFlow<List<TeacherScheduleItem>> = teacherRepository.schedule
    val attendanceRoster: StateFlow<List<StudentRosterItem>> = teacherRepository.attendanceRoster
    val assignments: StateFlow<List<StudentAssignment>> = teacherRepository.assignments
    val studyMaterials: StateFlow<List<StudentStudyMaterial>> = teacherRepository.studyMaterials
    val atRiskStudents: StateFlow<List<AtRiskStudent>> = teacherRepository.atRiskStudents
    val leaveApplications: StateFlow<List<StudentLeaveApplication>> = teacherRepository.leaveApplications
    val notices: StateFlow<List<NoticeItem>> = teacherRepository.notices

    private val _selectedClass = MutableStateFlow("IT - Sem 3")
    val selectedClass: StateFlow<String> = _selectedClass.asStateFlow()

    private val _selectedDate = MutableStateFlow(
        java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date())
    )
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    val assignedSubjects: StateFlow<List<String>> = teacherRepository.assignedSubjects

    private val _selectedSubject = MutableStateFlow(
        teacherRepository.assignedSubjects.value.firstOrNull() ?: "Discrete Structure & Theory of Logic (T) (BCS-303)"
    )
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    private val _selectedStudentDetail = MutableStateFlow<StudentRosterItem?>(null)
    val selectedStudentDetail: StateFlow<StudentRosterItem?> = _selectedStudentDetail.asStateFlow()

    private val _isStudentListDialogVisible = MutableStateFlow(false)
    val isStudentListDialogVisible: StateFlow<Boolean> = _isStudentListDialogVisible.asStateFlow()

    private val _studentSearchQuery = MutableStateFlow("")
    val studentSearchQuery: StateFlow<String> = _studentSearchQuery.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        refreshRoster()
        viewModelScope.launch {
            teacherRepository.assignedSubjects.collect { subjects ->
                if (subjects.isNotEmpty() && (!subjects.contains(_selectedSubject.value) || _selectedSubject.value.isBlank())) {
                    _selectedSubject.value = subjects.first()
                }
            }
        }
        viewModelScope.launch {
            teacherRepository.refreshRemoteData()
        }
    }

    fun refreshRoster() {
        teacherRepository.refreshRoster()
    }

    fun openStudentListDialog() {
        refreshRoster()
        viewModelScope.launch {
            teacherRepository.refreshRemoteData()
        }
        _isStudentListDialogVisible.value = true
    }

    fun closeStudentListDialog() {
        _isStudentListDialogVisible.value = false
        _studentSearchQuery.value = ""
    }

    fun onStudentSearchChange(query: String) {
        _studentSearchQuery.value = query
    }

    fun selectClass(className: String) {
        _selectedClass.value = className
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    fun selectSubject(subject: String) {
        if (teacherRepository.isSubjectAuthorized(subject)) {
            _selectedSubject.value = subject
        } else {
            _toastMessage.value = "You are not authorized to mark attendance for this subject."
        }
    }

    fun selectStudentForDetail(student: StudentRosterItem) {
        _selectedStudentDetail.value = student
    }

    fun closeStudentDetail() {
        _selectedStudentDetail.value = null
    }

    fun updateStudentAttendance(rosterId: String, status: StudentAttendanceStatus) {
        teacherRepository.updateStudentAttendance(rosterId, status, _selectedSubject.value)
    }

    fun markAllPresent() {
        teacherRepository.markAllPresent(_selectedSubject.value)
    }

    fun clearAttendance() {
        teacherRepository.resetAttendance()
    }

    fun submitAttendance() {
        val subject = _selectedSubject.value
        val className = _selectedClass.value
        val markedCount = attendanceRoster.value.count { it.status != StudentAttendanceStatus.UNMARKED }
        val totalCount = attendanceRoster.value.size

        if (markedCount == 0) {
            _toastMessage.value = "Please mark student attendance (Present/Absent/Leave) before submitting."
            return
        }

        if (!teacherRepository.isSubjectAuthorized(subject)) {
            _toastMessage.value = "You are not authorized to mark attendance for $subject."
            return
        }

        viewModelScope.launch {
            val result = teacherRepository.submitCurrentAttendance(className, subject)
            if (result.isSuccess) {
                val savedCount = result.getOrDefault(markedCount)
                teacherRepository.resetAttendance()
                _toastMessage.value = "Attendance for $subject ($className) submitted ($savedCount/$totalCount marked)! Updated in real-time."
            } else {
                val err = result.exceptionOrNull()?.message ?: "Failed to save attendance"
                _toastMessage.value = "Submission failed: $err"
            }
        }
    }

    fun createAssignment(
        title: String,
        instructions: String,
        maxMarks: Int,
        dueDate: String,
        subjectName: String = _selectedSubject.value,
        fileName: String? = null,
        fileUri: String? = null,
        fileBytes: ByteArray? = null,
        mimeType: String = "application/octet-stream"
    ) {
        if (title.isBlank()) {
            _toastMessage.value = "Please enter an assignment title."
            return
        }
        if (dueDate.isBlank()) {
            _toastMessage.value = "Please select a submission deadline."
            return
        }
        teacherRepository.createAssignment(
            title = title.trim(),
            instructions = instructions.trim(),
            dueDate = dueDate.trim(),
            subjectName = subjectName,
            fileName = fileName,
            fileUri = fileUri,
            fileBytes = fileBytes,
            mimeType = mimeType,
            maxMarks = maxMarks
        )
        _toastMessage.value = "Assignment '$title' uploaded successfully!"
    }

    fun uploadMaterial(
        title: String,
        subjectName: String,
        fileName: String? = null,
        fileUri: String? = null,
        fileBytes: ByteArray? = null,
        mimeType: String = "application/pdf",
        fileSize: String = "2.4 MB",
        fileType: String = "PDF"
    ) {
        if (title.isBlank()) {
            _toastMessage.value = "Please enter a study material title."
            return
        }
        teacherRepository.uploadStudyMaterial(
            title = title.trim(),
            subjectName = subjectName,
            fileName = fileName,
            fileUri = fileUri,
            fileBytes = fileBytes,
            mimeType = mimeType,
            fileSize = fileSize,
            fileType = fileType
        )
        _toastMessage.value = "Material '$title' shared with students!"
    }

    fun remindStudent(studentId: String) {
        teacherRepository.remindAtRiskStudent(studentId)
        _toastMessage.value = "Attendance & Performance reminder dispatched to student."
    }

    fun approveLeave(leaveId: String) {
        teacherRepository.updateLeaveStatus(leaveId, "Approved")
        _toastMessage.value = "Leave application approved."
    }

    fun rejectLeave(leaveId: String) {
        teacherRepository.updateLeaveStatus(leaveId, "Rejected")
        _toastMessage.value = "Leave application rejected."
    }

    fun publishNotice(title: String, description: String) {
        teacherRepository.publishNotice(title, description)
        _toastMessage.value = "Notice published to Department board!"
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
