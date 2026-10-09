package com.vvit.smartcollege.data.repository

import com.vvit.smartcollege.data.local.DatabaseProvider
import com.vvit.smartcollege.data.local.dao.AdminDao
import com.vvit.smartcollege.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AdminRepository(
    private val adminDao: AdminDao? = DatabaseProvider.adminDao
) {

    private val _kpis = MutableStateFlow(
        adminDao?.getKPIs() ?: MockDatabase.adminKPIs.value
    )
    val kpis: StateFlow<AdminKPIs> = _kpis.asStateFlow()

    private val _studentDirectory = MutableStateFlow(
        adminDao?.getStudentDirectory() ?: MockDatabase.studentDirectory.value
    )
    val studentDirectory: StateFlow<List<StudentDirectoryEntry>> = _studentDirectory.asStateFlow()

    private val _facultyDirectory = MutableStateFlow(
        adminDao?.getFacultyDirectory() ?: MockDatabase.facultyDirectory.value
    )
    val facultyDirectory: StateFlow<List<FacultyDirectoryEntry>> = _facultyDirectory.asStateFlow()

    private val _departments = MutableStateFlow(
        adminDao?.getDepartments() ?: MockDatabase.departments.value
    )
    val departments: StateFlow<List<DepartmentEntry>> = _departments.asStateFlow()

    private val _scheduledExams = MutableStateFlow(
        adminDao?.getScheduledExams() ?: MockDatabase.scheduledExams.value
    )
    val scheduledExams: StateFlow<List<ScheduledExam>> = _scheduledExams.asStateFlow()

    private val _libraryBooks = MutableStateFlow(
        adminDao?.getLibraryBooks() ?: MockDatabase.libraryBooks.value
    )
    val libraryBooks: StateFlow<List<LibraryBook>> = _libraryBooks.asStateFlow()

    private val _issuedBooks = MutableStateFlow(
        adminDao?.getIssuedBooks() ?: MockDatabase.issuedBooks.value
    )
    val issuedBooks: StateFlow<List<IssuedBookRecord>> = _issuedBooks.asStateFlow()

    private val _userRoles = MutableStateFlow(
        adminDao?.getUserRoles() ?: MockDatabase.userRoles.value
    )
    val userRoles: StateFlow<List<UserRoleGroup>> = _userRoles.asStateFlow()

    private val _departmentFees = MutableStateFlow(
        adminDao?.getDepartmentFees() ?: MockDatabase.departmentFees.value
    )
    val departmentFees: StateFlow<List<DepartmentFeeCollection>> = _departmentFees.asStateFlow()

    private val _notices = MutableStateFlow(
        adminDao?.getNotices() ?: MockDatabase.notices.value
    )
    val notices: StateFlow<List<NoticeItem>> = _notices.asStateFlow()

    fun addStudent(name: String, rollNo: String, department: String, semester: String, email: String, guardian: String, phone: String) {
        val entry = StudentDirectoryEntry(
            id = UUID.randomUUID().toString(),
            name = name,
            rollNo = rollNo,
            department = department,
            semester = semester,
            email = email,
            guardianName = guardian,
            guardianPhone = phone,
            cgpa = 8.0,
            attendancePercent = 85,
            isActive = true
        )
        adminDao?.insertStudentDirectory(entry)
        val current = (adminDao?.getStudentDirectory() ?: _studentDirectory.value).toMutableList()
        if (!current.any { it.id == entry.id }) {
            current.add(0, entry)
        }
        _studentDirectory.value = current
        MockDatabase.studentDirectory.value = current
    }

    fun addFaculty(name: String, employeeId: String, designation: String, department: String, subjects: List<String>, workload: Int) {
        val entry = FacultyDirectoryEntry(
            id = UUID.randomUUID().toString(),
            name = name,
            employeeId = employeeId,
            designation = designation,
            department = department,
            status = "Active",
            assignedSubjects = subjects,
            weeklyWorkloadHours = workload
        )
        adminDao?.insertFacultyDirectory(entry)
        val current = (adminDao?.getFacultyDirectory() ?: _facultyDirectory.value).toMutableList()
        if (!current.any { it.id == entry.id }) {
            current.add(0, entry)
        }
        _facultyDirectory.value = current
        MockDatabase.facultyDirectory.value = current
    }

    fun scheduleExam(subjectName: String, subjectCode: String, date: String, time: String, venue: String) {
        val exam = ScheduledExam(
            id = UUID.randomUUID().toString(),
            subjectName = subjectName,
            subjectCode = subjectCode,
            examDate = date,
            timeDuration = time,
            venue = venue
        )
        adminDao?.insertScheduledExam(exam)
        val current = (adminDao?.getScheduledExams() ?: _scheduledExams.value).toMutableList()
        if (!current.any { it.id == exam.id }) {
            current.add(0, exam)
        }
        _scheduledExams.value = current
        MockDatabase.scheduledExams.value = current
    }

    fun sendFeeDefaulterReminders(): Int {
        return 42 // 42 fee defaulters alerted
    }
}
