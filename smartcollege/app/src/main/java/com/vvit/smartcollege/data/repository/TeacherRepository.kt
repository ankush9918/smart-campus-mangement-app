package com.vvit.smartcollege.data.repository

import com.vvit.smartcollege.data.local.DatabaseProvider
import com.vvit.smartcollege.data.local.dao.TeacherDao
import com.vvit.smartcollege.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.vvit.smartcollege.data.remote.SupabaseApiClient
import com.vvit.smartcollege.data.local.SessionManager
import kotlinx.coroutines.withContext
import java.util.UUID
import android.net.Uri

class TeacherRepository(
    private val teacherDao: TeacherDao? = DatabaseProvider.teacherDao
) {

    private val _teacherProfile = MutableStateFlow(
        teacherDao?.getTeacherProfile() ?: MockDatabase.teacherProfile.value
    )
    val teacherProfile: StateFlow<TeacherProfile> = _teacherProfile.asStateFlow()

    private val _assignedSubjects = MutableStateFlow<List<String>>(
        calculateAssignedSubjects(_teacherProfile.value)
    )
    val assignedSubjects: StateFlow<List<String>> = _assignedSubjects.asStateFlow()

    private fun calculateAssignedSubjects(profile: TeacherProfile): List<String> {
        val list = profile.subjects.map { MasterSubjectRegistry.normalize(it) }.filter { it.isNotBlank() }
        return if (list.isNotEmpty()) list else listOf(
            "Computer Organization & Architecture (BCS-302)",
            "Data Structure (BCS-301)"
        )
    }

    /**
     * Backend Security Check: Enforces that a teacher can only mark attendance
     * for subjects officially assigned to them by the Admin.
     */
    fun isSubjectAuthorized(subject: String): Boolean {
        val assigned = _assignedSubjects.value
        return MasterSubjectRegistry.isAuthorized(assigned, subject)
    }

    private val _schedule = MutableStateFlow(
        teacherDao?.getSchedule() ?: MockDatabase.teacherSchedule.value
    )
    val schedule: StateFlow<List<TeacherScheduleItem>> = _schedule.asStateFlow()

    private val _attendanceRoster = MutableStateFlow(
        teacherDao?.getAttendanceRoster() ?: MockDatabase.teacherAttendanceRoster.value
    )
    val attendanceRoster: StateFlow<List<StudentRosterItem>> = _attendanceRoster.asStateFlow()

    private val _assignments = MutableStateFlow<List<StudentAssignment>>(
        teacherDao?.getAssignments() ?: MockDatabase.studentAssignments.value
    )
    val assignments: StateFlow<List<StudentAssignment>> = _assignments.asStateFlow()

    private val _studyMaterials = MutableStateFlow(
        DatabaseProvider.studentDao?.getStudyMaterials() ?: MockDatabase.studyMaterials.value
    )
    val studyMaterials: StateFlow<List<StudentStudyMaterial>> = _studyMaterials.asStateFlow()

    private val _atRiskStudents = MutableStateFlow(
        teacherDao?.getAtRiskStudents() ?: MockDatabase.atRiskStudents.value
    )
    val atRiskStudents: StateFlow<List<AtRiskStudent>> = _atRiskStudents.asStateFlow()

    private val _leaveApplications = MutableStateFlow(
        DatabaseProvider.studentDao?.getLeaveApplications() ?: MockDatabase.leaveApplications.value
    )
    val leaveApplications: StateFlow<List<StudentLeaveApplication>> = _leaveApplications.asStateFlow()

    private val _notices = MutableStateFlow(
        DatabaseProvider.studentDao?.getNotices() ?: MockDatabase.notices.value
    )
    val notices: StateFlow<List<NoticeItem>> = _notices.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            refreshRemoteData()
        }
    }

    suspend fun refreshRemoteData() = withContext(Dispatchers.IO) {
        try {
            val session = (DatabaseProvider.sessionManager ?: SessionManager.get())?.getSession()
                ?: MockDatabase.currentUser.value
            val email = session?.email
            val authId = session?.id

            val remoteProfile = SupabaseApiClient.fetchTeacherProfile(email, authId)
            if (remoteProfile != null) {
                _teacherProfile.value = remoteProfile
                _assignedSubjects.value = calculateAssignedSubjects(remoteProfile)
                MockDatabase.teacherProfile.value = remoteProfile
                teacherDao?.saveOrUpdateTeacherProfile(remoteProfile)
            }

            val remoteSchedule = SupabaseApiClient.fetchTeacherSchedule()
            if (!remoteSchedule.isNullOrEmpty()) {
                _schedule.value = remoteSchedule
            }

            // Circulars / Notices intended for FACULTY (or ALL)
            val remoteNotices = SupabaseApiClient.fetchCirculars("FACULTY")
            if (!remoteNotices.isNullOrEmpty()) {
                teacherDao?.saveNotices(remoteNotices)
                DatabaseProvider.studentDao?.saveNotices(remoteNotices)
                _notices.value = remoteNotices
                MockDatabase.notices.value = remoteNotices
            } else {
                val localNotices = DatabaseProvider.studentDao?.getNotices() ?: teacherDao?.getNotices() ?: emptyList()
                _notices.value = localNotices
                MockDatabase.notices.value = localNotices
            }

            // Fetch actual registered students from Supabase (added by Admin)
            val remoteStudents = SupabaseApiClient.fetchAllStudents(_teacherProfile.value.department)
            if (!remoteStudents.isNullOrEmpty()) {
                val currentStatuses = _attendanceRoster.value.associate { it.id to it.status }
                val merged = remoteStudents.map { s ->
                    val existingStatus = currentStatuses[s.id] ?: currentStatuses[s.rollNo] ?: StudentAttendanceStatus.UNMARKED
                    s.copy(status = existingStatus)
                }
                _attendanceRoster.value = merged
                MockDatabase.teacherAttendanceRoster.value = merged
                teacherDao?.saveAttendanceRoster(merged)

                // Synchronize total students count in professor profile
                val updatedProfile = _teacherProfile.value.copy(totalStudents = merged.size)
                _teacherProfile.value = updatedProfile
                MockDatabase.teacherProfile.value = updatedProfile

                // Dynamically update at-risk students based on actual registered student attendance < 75%
                val realAtRisk = merged.filter { it.attendancePercentage < 75 }.map { s ->
                    AtRiskStudent(
                        id = "risk_${s.id}",
                        studentName = s.studentName,
                        rollNo = s.rollNo,
                        reason = "Low Attendance (${s.attendancePercentage}%) & Academic Alert",
                        attendancePercent = s.attendancePercentage,
                        isReminded = false
                    )
                }
                if (realAtRisk.isNotEmpty()) {
                    _atRiskStudents.value = realAtRisk
                    MockDatabase.atRiskStudents.value = realAtRisk
                }
            }

            // Sync remote assignments and study materials
            val remoteAssignments = SupabaseApiClient.fetchAssignments(_teacherProfile.value.department)
            if (!remoteAssignments.isNullOrEmpty()) {
                DatabaseProvider.studentDao?.saveAssignments(remoteAssignments)
                val localAsg = teacherDao?.getAssignments() ?: remoteAssignments
                _assignments.value = localAsg
                MockDatabase.studentAssignments.value = localAsg
            } else {
                val localAsg = teacherDao?.getAssignments() ?: emptyList()
                _assignments.value = localAsg
                MockDatabase.studentAssignments.value = localAsg
            }
            val remoteMaterials = SupabaseApiClient.fetchStudyMaterials(_teacherProfile.value.department)
            if (!remoteMaterials.isNullOrEmpty()) {
                DatabaseProvider.studentDao?.saveStudyMaterials(remoteMaterials)
                _studyMaterials.value = DatabaseProvider.studentDao?.getStudyMaterials() ?: remoteMaterials
                MockDatabase.studyMaterials.value = _studyMaterials.value
            } else {
                val localMat = teacherDao?.getStudyMaterials() ?: DatabaseProvider.studentDao?.getStudyMaterials() ?: emptyList()
                _studyMaterials.value = localMat
                MockDatabase.studyMaterials.value = localMat
            }
        } catch (e: Exception) {
            // Silently fallback to SQLite local database
        }
    }

    fun refreshRoster() {
        val roster = teacherDao?.getAttendanceRoster() ?: MockDatabase.teacherAttendanceRoster.value
        _attendanceRoster.value = roster
        // Refresh from remote Supabase in background to sync newly registered students
        CoroutineScope(Dispatchers.IO).launch {
            refreshRemoteData()
        }
    }

    fun updateStudentAttendance(
        rosterId: String, 
        status: StudentAttendanceStatus, 
        subject: String = _assignedSubjects.value.firstOrNull() ?: "Computer Organization & Architecture (BCS-302)"
    ) {
        if (!isSubjectAuthorized(subject)) {
            android.util.Log.e("TeacherRepository", "Security Reject: Teacher ${_teacherProfile.value.name} is not assigned to subject $subject")
            return
        }
        val canonicalSub = MasterSubjectRegistry.normalize(subject)
        val currentList = _attendanceRoster.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == rosterId }
        if (index != -1) {
            val currentItem = currentList[index]
            val newStatus = if (currentItem.status == status) StudentAttendanceStatus.UNMARKED else status
            teacherDao?.updateRosterStatus(rosterId, newStatus)
            currentList[index] = currentItem.copy(status = newStatus)
            _attendanceRoster.value = currentList
            MockDatabase.teacherAttendanceRoster.value = currentList

            // Sync attendance update to Supabase in background
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                try {
                    com.vvit.smartcollege.data.remote.SupabaseApiClient.postStudentAttendance(
                        studentId = currentItem.id,
                        studentName = currentItem.studentName,
                        rollNumber = currentItem.rollNo,
                        subject = canonicalSub,
                        date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                        status = newStatus.name,
                        teacherId = _teacherProfile.value.id,
                        markedBy = _teacherProfile.value.name
                    )
                } catch (e: Exception) {
                    // Ignore background sync errors
                }
            }
        }
    }

    fun markAllPresent(subject: String = _assignedSubjects.value.firstOrNull() ?: "Computer Organization & Architecture (BCS-302)") {
        if (!isSubjectAuthorized(subject)) {
            android.util.Log.e("TeacherRepository", "Security Reject: Teacher ${_teacherProfile.value.name} is not assigned to subject $subject")
            return
        }
        val canonicalSub = MasterSubjectRegistry.normalize(subject)
        teacherDao?.markAllPresent()
        val roster = _attendanceRoster.value.map {
            it.copy(status = StudentAttendanceStatus.PRESENT)
        }
        _attendanceRoster.value = roster
        MockDatabase.teacherAttendanceRoster.value = roster

        // Sync all to Supabase in background
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            roster.forEach { item ->
                try {
                    com.vvit.smartcollege.data.remote.SupabaseApiClient.postStudentAttendance(
                        studentId = item.id,
                        studentName = item.studentName,
                        rollNumber = item.rollNo,
                        subject = canonicalSub,
                        date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                        status = "PRESENT",
                        teacherId = _teacherProfile.value.id,
                        markedBy = _teacherProfile.value.name
                    )
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    fun submitCurrentAttendance(
        className: String = "IT - Sem 3",
        subject: String = _assignedSubjects.value.firstOrNull() ?: "Computer Organization & Architecture (BCS-302)"
    ) {
        if (!isSubjectAuthorized(subject)) {
            android.util.Log.e("TeacherRepository", "Security Reject: Teacher ${_teacherProfile.value.name} is not assigned to subject $subject")
            return
        }
        val canonicalSub = MasterSubjectRegistry.normalize(subject)
        val roster = _attendanceRoster.value
        val marked = roster.filter { it.status != StudentAttendanceStatus.UNMARKED }
        if (marked.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                marked.forEach { item ->
                    try {
                        SupabaseApiClient.postStudentAttendance(
                            studentId = item.id,
                            studentName = item.studentName,
                            rollNumber = item.rollNo,
                            subject = canonicalSub,
                            date = todayStr,
                            status = item.status.name,
                            teacherId = _teacherProfile.value.id,
                            markedBy = _teacherProfile.value.name
                        )
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
            }
        }
    }

    fun resetAttendance() {
        teacherDao?.resetAllAttendance()
        val roster = (teacherDao?.getAttendanceRoster() ?: _attendanceRoster.value).map {
            it.copy(status = StudentAttendanceStatus.UNMARKED)
        }
        _attendanceRoster.value = roster
        MockDatabase.teacherAttendanceRoster.value = roster
    }

    fun createAssignment(
        title: String, 
        instructions: String, 
        dueDate: String,
        subjectName: String = _assignedSubjects.value.firstOrNull() ?: "Computer Organization & Architecture (BCS-302)",
        fileName: String? = null,
        fileUri: String? = null,
        fileBytes: ByteArray? = null,
        mimeType: String = "application/octet-stream",
        maxMarks: Int = 100
    ) {
        val canonicalSub = MasterSubjectRegistry.normalize(subjectName).ifBlank { subjectName }
        val newAsg = StudentAssignment(
            id = "asg_${System.currentTimeMillis()}",
            subjectName = canonicalSub,
            title = title,
            instructions = instructions,
            dueDate = dueDate,
            status = AssignmentStatus.PENDING,
            maxMarks = maxMarks,
            professorName = _teacherProfile.value.name,
            fileName = fileName,
            fileUri = fileUri
        )
        teacherDao?.createAssignment(newAsg)
        DatabaseProvider.studentDao?.saveAssignments(listOf(newAsg))
        val current = _assignments.value.toMutableList()
        current.removeAll { it.id == newAsg.id }
        current.add(0, newAsg)
        _assignments.value = current
        MockDatabase.studentAssignments.value = current

        // Sync assignment to Supabase in background, uploading real file to Storage first
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var bytes = fileBytes
                if (bytes == null && !fileUri.isNullOrBlank() && fileUri.startsWith("content://")) {
                    try {
                        val uri = Uri.parse(fileUri)
                        bytes = DatabaseProvider.appContext?.contentResolver?.openInputStream(uri)?.use { it.readBytes() }
                    } catch (_: Exception) {}
                }

                var remoteFileUrl: String? = null
                if (bytes != null && !fileName.isNullOrBlank()) {
                    val safeName = "${System.currentTimeMillis()}_${fileName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")}"
                    remoteFileUrl = SupabaseApiClient.uploadFileToStorage(
                        bucket = "assignments",
                        fileName = safeName,
                        fileBytes = bytes,
                        mimeType = mimeType
                    )
                }

                // If remote upload succeeded, use the public cloud URL.
                // If it failed or there is no remote file, ensure we NEVER send local content:// URIs to Supabase.
                val cloudFileUri = remoteFileUrl ?: (if (newAsg.fileUri?.startsWith("content://") == true) null else newAsg.fileUri)

                val asgToSave = if (cloudFileUri != newAsg.fileUri) {
                    val updatedAsg = newAsg.copy(fileUri = cloudFileUri)
                    teacherDao?.createAssignment(updatedAsg)
                    DatabaseProvider.studentDao?.saveAssignments(listOf(updatedAsg))
                    val updatedList = _assignments.value.map { if (it.id == newAsg.id) updatedAsg else it }
                    _assignments.value = updatedList
                    MockDatabase.studentAssignments.value = updatedList
                    updatedAsg
                } else {
                    newAsg
                }

                SupabaseApiClient.postAssignment(
                    assignment = asgToSave,
                    department = _teacherProfile.value.department,
                    createdBy = _teacherProfile.value.name
                )
            } catch (e: Exception) {
                // Silently ignore background network errors
            }
        }
    }

    fun uploadStudyMaterial(
        title: String,
        subjectName: String,
        fileName: String? = null,
        fileUri: String? = null,
        fileBytes: ByteArray? = null,
        mimeType: String = "application/pdf",
        fileSize: String = "2.4 MB",
        fileType: String = "PDF"
    ) {
        val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        val dateStr = dateFormat.format(java.util.Date())
        val canonicalSub = MasterSubjectRegistry.normalize(subjectName)
        val determinedFileType = when {
            fileType.isNotBlank() && fileType != "PDF" -> fileType.uppercase()
            fileName != null && fileName.contains(".") -> fileName.substringAfterLast(".").uppercase()
            else -> "PDF"
        }

        val newMat = StudentStudyMaterial(
            id = UUID.randomUUID().toString(),
            title = title,
            subjectName = canonicalSub,
            sharedBy = _teacherProfile.value.name.ifBlank { "Professor" },
            sharedDate = dateStr,
            fileSize = fileSize,
            fileType = determinedFileType,
            downloadUrl = fileUri ?: "",
            fileName = fileName ?: title,
            fileUri = fileUri
        )
        teacherDao?.uploadStudyMaterial(newMat)
        DatabaseProvider.studentDao?.saveStudyMaterials(listOf(newMat))
        val current = (teacherDao?.getStudyMaterials() ?: DatabaseProvider.studentDao?.getStudyMaterials() ?: _studyMaterials.value).toMutableList()
        if (!current.any { it.id == newMat.id }) {
            current.add(0, newMat)
        }
        _studyMaterials.value = current
        MockDatabase.studyMaterials.value = current

        // Sync study material to Supabase in background, uploading real file to Storage first
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var bytes = fileBytes
                if (bytes == null && !fileUri.isNullOrBlank() && fileUri.startsWith("content://")) {
                    try {
                        val uri = Uri.parse(fileUri)
                        bytes = DatabaseProvider.appContext?.contentResolver?.openInputStream(uri)?.use { it.readBytes() }
                    } catch (_: Exception) {}
                }

                var remoteFileUrl: String? = null
                if (bytes != null && !fileName.isNullOrBlank()) {
                    val safeName = "${System.currentTimeMillis()}_${fileName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")}"
                    remoteFileUrl = SupabaseApiClient.uploadFileToStorage(
                        bucket = "study-materials",
                        fileName = safeName,
                        fileBytes = bytes,
                        mimeType = mimeType
                    )
                }

                val cloudFileUri = remoteFileUrl ?: (if (newMat.fileUri?.startsWith("content://") == true) "" else (newMat.fileUri ?: ""))

                val matToSave = if (cloudFileUri != newMat.fileUri) {
                    val updatedMat = newMat.copy(fileUri = cloudFileUri, downloadUrl = cloudFileUri)
                    teacherDao?.uploadStudyMaterial(updatedMat)
                    DatabaseProvider.studentDao?.saveStudyMaterials(listOf(updatedMat))
                    val updatedList = _studyMaterials.value.map { if (it.id == newMat.id) updatedMat else it }
                    _studyMaterials.value = updatedList
                    MockDatabase.studyMaterials.value = updatedList
                    updatedMat
                } else {
                    newMat
                }

                SupabaseApiClient.postStudyMaterial(
                    material = matToSave,
                    department = _teacherProfile.value.department
                )
            } catch (e: Exception) {
                // Silently ignore background network errors
            }
        }
    }

    fun remindAtRiskStudent(studentId: String) {
        teacherDao?.updateAtRiskReminded(studentId)
        val list = (teacherDao?.getAtRiskStudents() ?: _atRiskStudents.value).toMutableList()
        val index = list.indexOfFirst { it.id == studentId }
        if (index != -1) {
            list[index] = list[index].copy(isReminded = true)
        }
        _atRiskStudents.value = list
        MockDatabase.atRiskStudents.value = list
    }

    fun updateLeaveStatus(leaveId: String, status: String) {
        teacherDao?.updateLeaveStatus(leaveId, status)
        val list = (DatabaseProvider.studentDao?.getLeaveApplications() ?: _leaveApplications.value).toMutableList()
        val index = list.indexOfFirst { it.id == leaveId }
        if (index != -1) {
            list[index] = list[index].copy(status = status)
        }
        _leaveApplications.value = list
        MockDatabase.leaveApplications.value = list
    }

    fun publishNotice(title: String, description: String, category: String = "FACULTY COUNCIL") {
        val newNotice = NoticeItem(
            id = UUID.randomUUID().toString(),
            title = title,
            category = category,
            timeAgo = "Just now",
            description = description,
            date = "Today"
        )
        teacherDao?.publishNotice(newNotice)
        DatabaseProvider.studentDao?.saveNotices(listOf(newNotice))
        val list = (DatabaseProvider.studentDao?.getNotices() ?: _notices.value).toMutableList()
        if (!list.any { it.id == newNotice.id }) {
            list.add(0, newNotice)
        }
        _notices.value = list
        MockDatabase.notices.value = list

        // Sync notice to Supabase circulars in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                SupabaseApiClient.postNotice(
                    notice = newNotice,
                    targetAudience = "ALL",
                    publishedBy = _teacherProfile.value.name
                )
            } catch (e: Exception) {
                // Silently ignore background network errors
            }
        }
    }
}
