package com.vvit.smartcollege.ui.student

import androidx.lifecycle.ViewModel
import com.vvit.smartcollege.data.model.*
import com.vvit.smartcollege.data.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class StudentViewModel(
    private val studentRepository: StudentRepository = StudentRepository()
) : ViewModel() {

    val profile: StateFlow<StudentProfile> = studentRepository.studentProfile
    val attendanceList: StateFlow<List<SubjectAttendance>> = studentRepository.attendanceList
    val timetable: StateFlow<List<StudentTimetableEntry>> = studentRepository.timetable
    val assignments: StateFlow<List<StudentAssignment>> = studentRepository.assignments
    val studyMaterials: StateFlow<List<StudentStudyMaterial>> = studentRepository.studyMaterials
    val semesterResults: StateFlow<List<SemesterResult>> = studentRepository.semesterResults
    val feeDetails: StateFlow<FeeDetails> = studentRepository.feeDetails
    val feeTransactions: StateFlow<List<FeeTransaction>> = studentRepository.feeTransactions
    val notices: StateFlow<List<NoticeItem>> = studentRepository.notices
    val leaveApplications: StateFlow<List<StudentLeaveApplication>> = studentRepository.leaveApplications

    private val _selectedTimetableDay = MutableStateFlow("Mon")
    val selectedTimetableDay: StateFlow<String> = _selectedTimetableDay.asStateFlow()

    private val _selectedSemester = MutableStateFlow(4)
    val selectedSemester: StateFlow<Int> = _selectedSemester.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            studentRepository.refreshRemoteData()
        }
    }

    fun selectTimetableDay(day: String) {
        _selectedTimetableDay.value = day
    }

    fun selectSemester(sem: Int) {
        _selectedSemester.value = sem
    }

    fun submitAssignment(assignmentId: String, fileName: String) {
        studentRepository.submitAssignment(assignmentId, fileName)
        _toastMessage.value = "Assignment submitted successfully!"
    }

    fun payFees(amount: Long) {
        studentRepository.payOutstandingFee(amount)
        _toastMessage.value = "Fee payment of ₹$amount successful!"
    }

    fun submitLeave(fromDate: String, toDate: String, days: Int, reason: String) {
        studentRepository.submitLeaveApplication(fromDate, toDate, days, reason)
        _toastMessage.value = "Leave application submitted!"
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
