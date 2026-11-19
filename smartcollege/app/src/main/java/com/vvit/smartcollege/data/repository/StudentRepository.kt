package com.vvit.smartcollege.data.repository

import com.vvit.smartcollege.data.local.DatabaseProvider
import com.vvit.smartcollege.data.local.dao.StudentDao
import com.vvit.smartcollege.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.vvit.smartcollege.data.remote.SupabaseApiClient
import java.util.UUID

import com.vvit.smartcollege.data.local.SessionManager
import kotlinx.coroutines.withContext

class StudentRepository(
    private val studentDao: StudentDao? = DatabaseProvider.studentDao
) {

    private val _studentProfile = MutableStateFlow(
        run {
            val local = studentDao?.getStudentProfile()
            val mock = MockDatabase.studentProfile.value
            if (local != null && !local.photoUrl.isNullOrBlank()) {
                local
            } else if (!mock.photoUrl.isNullOrBlank()) {
                mock
            } else {
                local ?: mock
            }
        }
    )
    val studentProfile: StateFlow<StudentProfile> = _studentProfile.asStateFlow()

    private val _attendanceList = MutableStateFlow(
        studentDao?.getAttendanceList() ?: MockDatabase.studentAttendance.value
    )
    val attendanceList: StateFlow<List<SubjectAttendance>> = _attendanceList.asStateFlow()

    private val _timetable = MutableStateFlow(
        studentDao?.getTimetable() ?: MockDatabase.studentTimetable.value
    )
    val timetable: StateFlow<List<StudentTimetableEntry>> = _timetable.asStateFlow()

    private val _assignments = MutableStateFlow(
        studentDao?.getAssignments() ?: MockDatabase.studentAssignments.value
    )
    val assignments: StateFlow<List<StudentAssignment>> = _assignments.asStateFlow()

    private val _studyMaterials = MutableStateFlow(
        studentDao?.getStudyMaterials() ?: MockDatabase.studyMaterials.value
    )
    val studyMaterials: StateFlow<List<StudentStudyMaterial>> = _studyMaterials.asStateFlow()

    private val _semesterResults = MutableStateFlow(
        studentDao?.getSemesterResults() ?: MockDatabase.semesterResults.value
    )
    val semesterResults: StateFlow<List<SemesterResult>> = _semesterResults.asStateFlow()

    private val _feeDetails = MutableStateFlow(
        studentDao?.getFeeDetails() ?: MockDatabase.studentFees.value
    )
    val feeDetails: StateFlow<FeeDetails> = _feeDetails.asStateFlow()

    private val _feeTransactions = MutableStateFlow(
        studentDao?.getFeeTransactions() ?: MockDatabase.feeTransactions.value
    )
    val feeTransactions: StateFlow<List<FeeTransaction>> = _feeTransactions.asStateFlow()

    private val _notices = MutableStateFlow(
        studentDao?.getNotices() ?: MockDatabase.notices.value
    )
    val notices: StateFlow<List<NoticeItem>> = _notices.asStateFlow()

    private val _leaveApplications = MutableStateFlow(
        studentDao?.getLeaveApplications() ?: MockDatabase.leaveApplications.value
    )
    val leaveApplications: StateFlow<List<StudentLeaveApplication>> = _leaveApplications.asStateFlow()

    private val _facultyList = MutableStateFlow<List<FacultyInfo>>(SupabaseApiClient.AUTHENTIC_DEFAULT_FACULTY)
    val facultyList: StateFlow<List<FacultyInfo>> = _facultyList.asStateFlow()

    private val _isFacultyLoading = MutableStateFlow(false)
    val isFacultyLoading: StateFlow<Boolean> = _isFacultyLoading.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            refreshFaculty()
            refreshRemoteData()
        }
    }

    suspend fun refreshFaculty() = withContext(Dispatchers.IO) {
        _isFacultyLoading.value = true
        try {
            val remoteFaculty = SupabaseApiClient.fetchAllFaculty()
            if (remoteFaculty.isNotEmpty()) {
                _facultyList.value = remoteFaculty
            }
        } catch (_: Exception) {
            // Keep existing state on transient error
        } finally {
            _isFacultyLoading.value = false
        }
    }

    suspend fun refreshRemoteData() = withContext(Dispatchers.IO) {
        try {
            val session = (DatabaseProvider.sessionManager ?: SessionManager.get())?.getSession()
                ?: MockDatabase.currentUser.value
            val email = session?.email
            val authId = session?.id

            // 1. Fetch live student profile from Supabase
            val remoteProfile = SupabaseApiClient.fetchStudentProfile(email, authId)
            if (remoteProfile != null) {
                // 2. Fetch live attendance for this student (calculated from actual records)
                val remoteAttendance = SupabaseApiClient.fetchStudentAttendance(
                    studentId = remoteProfile.id,
                    rollNumber = remoteProfile.rollNo,
                    overallPercentage = remoteProfile.attendancePercentage
                )
                val updatedProfile = remoteProfile.copy(attendancePercentage = remoteAttendance.calculatedPercentage)
                _studentProfile.value = updatedProfile
                MockDatabase.studentProfile.value = updatedProfile
                studentDao?.saveOrUpdateStudentProfile(updatedProfile)

                if (remoteAttendance.subjects.isNotEmpty()) {
                    _attendanceList.value = remoteAttendance.subjects
                    MockDatabase.studentAttendance.value = remoteAttendance.subjects
                    studentDao?.saveAttendanceList(remoteAttendance.subjects)
                }

                // 3. Fetch assignments from remote for this student's department
                val remoteAssignments = SupabaseApiClient.fetchAssignments(updatedProfile.department)
                if (!remoteAssignments.isNullOrEmpty()) {
                    studentDao?.saveAssignments(remoteAssignments)
                    val localAsg = studentDao?.getAssignments() ?: remoteAssignments
                    _assignments.value = localAsg
                    MockDatabase.studentAssignments.value = localAsg
                } else {
                    val localAsg = studentDao?.getAssignments() ?: emptyList()
                    _assignments.value = localAsg
                    MockDatabase.studentAssignments.value = localAsg
                }

                // 4. Fetch study materials from remote for this student's department
                val remoteMaterials = SupabaseApiClient.fetchStudyMaterials(updatedProfile.department)
                if (!remoteMaterials.isNullOrEmpty()) {
                    studentDao?.saveStudyMaterials(remoteMaterials)
                    val localMat = studentDao?.getStudyMaterials() ?: remoteMaterials
                    _studyMaterials.value = localMat
                    MockDatabase.studyMaterials.value = localMat
                } else {
                    val localMat = studentDao?.getStudyMaterials() ?: emptyList()
                    _studyMaterials.value = localMat
                    MockDatabase.studyMaterials.value = localMat
                }
            }

            // 5. Timetable
            val remoteTimetable = SupabaseApiClient.fetchStudentTimetable()
            if (!remoteTimetable.isNullOrEmpty()) {
                _timetable.value = remoteTimetable
            }

            // 5b. Faculty Directory (live from Supabase faculty table only)
            refreshFaculty()

            // 6. Circulars / Notices intended for STUDENTS (or ALL)
            val remoteNotices = SupabaseApiClient.fetchCirculars("STUDENTS")
            if (!remoteNotices.isNullOrEmpty()) {
                studentDao?.saveNotices(remoteNotices)
                _notices.value = remoteNotices
                MockDatabase.notices.value = remoteNotices
            } else {
                val localNotices = studentDao?.getNotices() ?: emptyList()
                _notices.value = localNotices
                MockDatabase.notices.value = localNotices
            }
        } catch (e: Exception) {
            // Silently fallback to SQLite local database
        }
    }

    fun submitAssignment(assignmentId: String, fileName: String) {
        studentDao?.submitAssignment(assignmentId, fileName)
        val current = (studentDao?.getAssignments() ?: _assignments.value).toMutableList()
        val index = current.indexOfFirst { it.id == assignmentId }
        if (index != -1) {
            val item = current[index]
            current[index] = item.copy(
                status = AssignmentStatus.SUBMITTED,
                submissionDate = "Just now",
                submittedFileName = fileName
            )
        }
        _assignments.value = current
        MockDatabase.studentAssignments.value = current
    }

    fun payOutstandingFee(amountToPay: Long): Boolean {
        val currentFees = _feeDetails.value
        val newPending = (currentFees.pendingAmount - amountToPay).coerceAtLeast(0L)
        val newPaid = currentFees.paidTillDate + amountToPay
        val updatedFees = currentFees.copy(
            pendingAmount = newPending,
            paidTillDate = newPaid
        )

        val txnId = "TXN-VVIT" + (10000..99999).random()
        val newTxn = FeeTransaction(
            id = UUID.randomUUID().toString(),
            title = currentFees.semesterLabel,
            transactionId = txnId,
            date = "Today",
            amount = amountToPay,
            status = "Success"
        )

        studentDao?.payFee(amountToPay, newTxn)

        _feeDetails.value = updatedFees
        MockDatabase.studentFees.value = updatedFees

        val txns = (studentDao?.getFeeTransactions() ?: _feeTransactions.value).toMutableList()
        if (!txns.any { it.id == newTxn.id }) {
            txns.add(0, newTxn)
        }
        _feeTransactions.value = txns
        MockDatabase.feeTransactions.value = txns
        return true
    }

    fun submitLeaveApplication(fromDate: String, toDate: String, days: Int, reason: String) {
        val profile = _studentProfile.value
        val newLeave = StudentLeaveApplication(
            id = UUID.randomUUID().toString(),
            studentName = profile.name,
            rollNo = profile.rollNo,
            fromDate = fromDate,
            toDate = toDate,
            durationDays = days,
            reason = reason,
            status = "Pending",
            appliedDate = "Today"
        )

        studentDao?.insertLeaveApplication(newLeave)

        val list = (studentDao?.getLeaveApplications() ?: _leaveApplications.value).toMutableList()
        if (!list.any { it.id == newLeave.id }) {
            list.add(0, newLeave)
        }
        _leaveApplications.value = list
        MockDatabase.leaveApplications.value = list
    }
}
