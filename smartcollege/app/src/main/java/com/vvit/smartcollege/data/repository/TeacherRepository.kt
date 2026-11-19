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
        if (list.isNotEmpty()) return list

        val name = profile.name.trim()
        val emp = profile.employeeId.trim()
        val email = profile.email.trim()

        val matchedDefault = SupabaseApiClient.AUTHENTIC_DEFAULT_FACULTY.find {
            (emp.isNotBlank() && it.employeeId.equals(emp, ignoreCase = true)) ||
            (name.isNotBlank() && (it.name.contains(name, ignoreCase = true) || name.contains(it.name, ignoreCase = true))) ||
            (email.isNotBlank() && it.email.equals(email, ignoreCase = true))
        }
        if (matchedDefault != null && matchedDefault.subjects.isNotEmpty()) {
            return matchedDefault.subjects.map { MasterSubjectRegistry.normalize(it) }
        }

        return listOf(
            "Discrete Structure & Theory of Logic (T) (BCS-303)"
        )
    }

    private fun filterAssignmentsForTeacher(allAsg: List<StudentAssignment>, assigned: List<String>): List<StudentAssignment> {
        if (assigned.isEmpty()) return allAsg
        val teacherName = _teacherProfile.value.name.trim()
        return allAsg.filter { asg ->
            MasterSubjectRegistry.isAuthorized(assigned, asg.subjectName) ||
            (teacherName.isNotBlank() && asg.professorName.isNotBlank() && asg.professorName.contains(teacherName, ignoreCase = true))
        }
    }

    private fun filterNoticesForTeacher(allNotices: List<NoticeItem>, assigned: List<String>): List<NoticeItem> {
        if (assigned.isEmpty()) return allNotices
        val teacherName = _teacherProfile.value.name.trim()
        return allNotices.filter { notice ->
            val cat = notice.category.uppercase().trim()
            val isAsgNotice = cat == "ASSIGNMENT" ||
                             cat == "STUDY_MATERIAL" ||
                             notice.title.contains("Assignment", ignoreCase = true) ||
                             notice.description.contains("Assignment", ignoreCase = true)
            if (isAsgNotice) {
                MasterSubjectRegistry.isAuthorized(assigned, notice.description) ||
                MasterSubjectRegistry.isAuthorized(assigned, notice.title) ||
                (teacherName.isNotBlank() && (notice.description.contains(teacherName, ignoreCase = true) || notice.title.contains(teacherName, ignoreCase = true)))
            } else {
                true
            }
        }
    }

    private fun filterMaterialsForTeacher(allMat: List<StudentStudyMaterial>, assigned: List<String>): List<StudentStudyMaterial> {
        if (assigned.isEmpty()) return allMat
        val teacherName = _teacherProfile.value.name.trim()
        return allMat.filter { mat ->
            MasterSubjectRegistry.isAuthorized(assigned, mat.subjectName) ||
            (teacherName.isNotBlank() && mat.sharedBy.isNotBlank() && mat.sharedBy.contains(teacherName, ignoreCase = true))
        }
    }

    /**
     * Backend Security Check: Enforces that a teacher can only mark attendance
     * for subjects officially assigned to them by the Admin.
     */
    fun isSubjectAuthorized(subject: String): Boolean {
        val assigned = _assignedSubjects.value
        return MasterSubjectRegistry.isAuthorized(assigned, subject)
    }

    companion object {
        val MASTER_SCHEDULE = listOf(
            // Monday
            TeacherScheduleItem("tsch_1", "10:00 AM - 10:50 AM", "Python Programming (BCC-302)", "LH-1", "IT-3rd Sem", "General Lecture", "Mon"),
            TeacherScheduleItem("tsch_2", "10:50 AM - 11:40 AM", "Data Structure (BCS-301)", "LH-1", "IT-3rd Sem", "General Lecture", "Mon"),
            TeacherScheduleItem("tsch_3", "11:40 AM - 12:30 PM", "Digital Electronics (BOE-310)", "LH-1", "IT-3rd Sem", "General Lecture", "Mon"),
            TeacherScheduleItem("tsch_4", "12:30 PM - 01:20 PM", "Python Programming (BCC-302)", "LH-1", "IT-3rd Sem", "General Lecture", "Mon"),
            TeacherScheduleItem("tsch_5", "02:20 PM - 03:10 PM", "Computer Organization & Architecture (BCS-302)", "LH-1", "IT-3rd Sem", "General Lecture", "Mon"),
            TeacherScheduleItem("tsch_6", "03:10 PM - 04:00 PM", "Digital Electronics (BOE-310)", "LH-1", "IT-3rd Sem", "General Lecture", "Mon"),
            TeacherScheduleItem("tsch_7", "04:00 PM - 04:50 PM", "Discrete Structure & Theory of Logic (BCS-303)", "LH-1", "IT-3rd Sem", "Tutorial Session", "Mon"),

            // Tuesday
            TeacherScheduleItem("tsch_8", "10:00 AM - 10:50 AM", "Technical Communication (BAS-301)", "LH-1", "IT-3rd Sem", "General Lecture", "Tue"),
            TeacherScheduleItem("tsch_9", "10:50 AM - 11:40 AM", "Discrete Structure & Theory of Logic (BCS-303)", "LH-1", "IT-3rd Sem", "General Lecture", "Tue"),
            TeacherScheduleItem("tsch_10", "11:40 AM - 12:30 PM", "Computer Organization & Architecture (BCS-302)", "LH-1", "IT-3rd Sem", "General Lecture", "Tue"),
            TeacherScheduleItem("tsch_11", "12:30 PM - 01:20 PM", "Data Structure (BCS-301)", "LH-1", "IT-3rd Sem", "General Lecture", "Tue"),
            TeacherScheduleItem("tsch_12", "02:20 PM - 03:10 PM", "Digital Electronics (BOE-310)", "LH-1", "IT-3rd Sem", "General Lecture", "Tue"),
            TeacherScheduleItem("tsch_13", "03:10 PM - 04:50 PM", "Computer Organization & Architecture (BCS-302)", "COA Lab", "IT-3rd Sem", "Practical Lab", "Tue"),

            // Wednesday
            TeacherScheduleItem("tsch_14", "10:00 AM - 10:50 AM", "Technical Communication (BAS-301)", "LH-1", "IT-3rd Sem", "General Lecture", "Wed"),
            TeacherScheduleItem("tsch_15", "10:50 AM - 12:30 PM", "Python Programming (BCC-302)", "COA Lab", "IT-3rd Sem", "Practical Lab", "Wed"),
            TeacherScheduleItem("tsch_16", "12:30 PM - 01:20 PM", "Data Structure (BCS-301)", "LH-1", "IT-3rd Sem", "General Lecture", "Wed"),
            TeacherScheduleItem("tsch_17", "04:00 PM - 04:50 PM", "Computer Organization & Architecture (BCS-302)", "LH-1", "IT-3rd Sem", "General Lecture", "Wed"),

            // Thursday
            TeacherScheduleItem("tsch_18", "10:00 AM - 10:50 AM", "Technical Communication (BAS-301)", "LH-1", "IT-3rd Sem", "General Lecture", "Thu"),
            TeacherScheduleItem("tsch_19", "10:50 AM - 11:40 AM", "Discrete Structure & Theory of Logic (BCS-303)", "LH-1", "IT-3rd Sem", "General Lecture", "Thu"),
            TeacherScheduleItem("tsch_20", "11:40 AM - 01:20 PM", "Digital Electronics (BOE-310)", "Web Tech Lab", "IT-3rd Sem", "Workshop", "Thu"),
            TeacherScheduleItem("tsch_21", "02:20 PM - 04:00 PM", "Data Structure (BCS-301)", "DS Lab", "IT-3rd Sem", "Practical Lab", "Thu"),
            TeacherScheduleItem("tsch_22", "04:00 PM - 04:50 PM", "Computer Organization & Architecture (BCS-302)", "LH-1", "IT-3rd Sem", "General Lecture", "Thu"),

            // Friday
            TeacherScheduleItem("tsch_23", "10:00 AM - 10:50 AM", "Data Structure (BCS-301)", "LH-1", "IT-3rd Sem", "General Lecture", "Fri"),
            TeacherScheduleItem("tsch_24", "10:50 AM - 12:30 PM", "Python Programming (BCC-302)", "Project Lab", "IT-3rd Sem", "Project Review", "Fri"),
            TeacherScheduleItem("tsch_25", "12:30 PM - 01:20 PM", "Digital Electronics (BOE-310)", "LH-1", "IT-3rd Sem", "Tutorial Session", "Fri"),
            TeacherScheduleItem("tsch_26", "02:20 PM - 04:00 PM", "Data Structure (BCS-301)", "DS Lab", "IT-3rd Sem", "Practical Lab", "Fri"),

            // Saturday
            TeacherScheduleItem("tsch_27", "10:00 AM - 11:30 AM", "Python Programming (BCC-302)", "LH-1", "IT-3rd Sem", "Workshop", "Sat"),
            TeacherScheduleItem("tsch_28", "11:30 AM - 01:00 PM", "Discrete Structure & Theory of Logic (BCS-303)", "LH-1", "IT-3rd Sem", "Tutorial Session", "Sat"),
            TeacherScheduleItem("tsch_29", "02:00 PM - 03:30 PM", "Digital Electronics (BOE-310)", "LH-1", "IT-3rd Sem", "Mentorship", "Sat"),
            TeacherScheduleItem("tsch_30", "03:30 PM - 04:30 PM", "Technical Communication (BAS-301)", "LH-1", "IT-3rd Sem", "Seminar", "Sat")
        )
    }

    private fun filterScheduleForTeacher(assigned: List<String>): List<TeacherScheduleItem> {
        if (assigned.isEmpty()) return MASTER_SCHEDULE
        val filtered = MASTER_SCHEDULE.filter { item ->
            MasterSubjectRegistry.isAuthorized(assigned, item.subjectName)
        }
        return if (filtered.isNotEmpty()) filtered else MASTER_SCHEDULE
    }

    private fun updateDataForTeacher(profile: TeacherProfile) {
        val assigned = calculateAssignedSubjects(profile)
        _assignedSubjects.value = assigned
        val filtered = filterScheduleForTeacher(assigned)
        _schedule.value = filtered
        MockDatabase.teacherSchedule.value = filtered
        teacherDao?.saveSchedule(filtered)

        val allAsg = teacherDao?.getAssignments() ?: MockDatabase.studentAssignments.value
        _assignments.value = filterAssignmentsForTeacher(allAsg, assigned)

        val allNotices = DatabaseProvider.studentDao?.getNotices() ?: teacherDao?.getNotices() ?: MockDatabase.notices.value
        _notices.value = filterNoticesForTeacher(allNotices, assigned)

        val allMat = teacherDao?.getStudyMaterials() ?: DatabaseProvider.studentDao?.getStudyMaterials() ?: MockDatabase.studyMaterials.value
        _studyMaterials.value = filterMaterialsForTeacher(allMat, assigned)
    }

    private val _schedule = MutableStateFlow<List<TeacherScheduleItem>>(
        filterScheduleForTeacher(calculateAssignedSubjects(_teacherProfile.value))
    )
    val schedule: StateFlow<List<TeacherScheduleItem>> = _schedule.asStateFlow()

    private val _attendanceRoster = MutableStateFlow(
        teacherDao?.getAttendanceRoster() ?: MockDatabase.teacherAttendanceRoster.value
    )
    val attendanceRoster: StateFlow<List<StudentRosterItem>> = _attendanceRoster.asStateFlow()

    private val _assignments = MutableStateFlow<List<StudentAssignment>>(
        filterAssignmentsForTeacher(
            teacherDao?.getAssignments() ?: MockDatabase.studentAssignments.value,
            calculateAssignedSubjects(_teacherProfile.value)
        )
    )
    val assignments: StateFlow<List<StudentAssignment>> = _assignments.asStateFlow()

    private val _studyMaterials = MutableStateFlow(
        filterMaterialsForTeacher(
            teacherDao?.getStudyMaterials() ?: DatabaseProvider.studentDao?.getStudyMaterials() ?: MockDatabase.studyMaterials.value,
            calculateAssignedSubjects(_teacherProfile.value)
        )
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
        filterNoticesForTeacher(
            DatabaseProvider.studentDao?.getNotices() ?: MockDatabase.notices.value,
            calculateAssignedSubjects(_teacherProfile.value)
        )
    )
    val notices: StateFlow<List<NoticeItem>> = _notices.asStateFlow()

    init {
        updateDataForTeacher(_teacherProfile.value)
        CoroutineScope(Dispatchers.IO).launch {
            refreshRemoteData()
        }
    }

    suspend fun refreshRemoteData() = withContext(Dispatchers.IO) {
        try {
            val session = (DatabaseProvider.sessionManager ?: SessionManager.get())?.getSession()
                ?: MockDatabase.currentUser.value
            val email = session?.email?.ifBlank { null } ?: session?.identifier
            val authId = session?.id

            val remoteProfile = SupabaseApiClient.fetchTeacherProfile(email, authId)
            if (remoteProfile != null) {
                _teacherProfile.value = remoteProfile
                MockDatabase.teacherProfile.value = remoteProfile
                teacherDao?.saveOrUpdateTeacherProfile(remoteProfile)
                updateDataForTeacher(remoteProfile)
            } else {
                updateDataForTeacher(_teacherProfile.value)
            }

            val remoteSchedule = SupabaseApiClient.fetchTeacherSchedule()
            if (!remoteSchedule.isNullOrEmpty()) {
                val assigned = _assignedSubjects.value
                val filtered = remoteSchedule.filter { MasterSubjectRegistry.isAuthorized(assigned, it.subjectName) }
                if (filtered.isNotEmpty()) {
                    _schedule.value = filtered
                    MockDatabase.teacherSchedule.value = filtered
                    teacherDao?.saveSchedule(filtered)
                }
            }

            // Circulars / Notices intended for FACULTY (or ALL)
            val assigned = _assignedSubjects.value
            val remoteNotices = SupabaseApiClient.fetchCirculars("FACULTY") ?: emptyList()
            val localNotices = teacherDao?.getNotices() ?: DatabaseProvider.studentDao?.getNotices() ?: MockDatabase.notices.value

            val isGenuineNotice: (NoticeItem) -> Boolean = { item ->
                !item.category.equals("STUDY_MATERIAL", ignoreCase = true) &&
                !item.description.trim().startsWith("{\"instructions\"") &&
                !item.description.trim().startsWith("{\"title\"")
            }
            val cleanLocal = localNotices.filter(isGenuineNotice)
            val cleanRemote = remoteNotices.filter(isGenuineNotice)

            val combinedNotices = mutableListOf<NoticeItem>()
            val seen = mutableSetOf<String>()
            for (n in cleanLocal) {
                if (seen.add(n.id) && seen.add(n.title.trim().lowercase())) {
                    combinedNotices.add(n)
                }
            }
            for (n in cleanRemote) {
                if (seen.add(n.id) && seen.add(n.title.trim().lowercase())) {
                    combinedNotices.add(n)
                }
            }
            val filtered = filterNoticesForTeacher(combinedNotices, assigned)
            _notices.value = filtered
            MockDatabase.notices.value = filtered
            teacherDao?.saveNotices(filtered)
            DatabaseProvider.studentDao?.saveNotices(filtered)

            // Fetch actual registered students from Supabase (added by Admin)
            val remoteStudents = SupabaseApiClient.fetchAllStudents(null)
            val baseRoster = teacherDao?.getAttendanceRoster() ?: MockDatabase.teacherAttendanceRoster.value

            val combined = mutableListOf<StudentRosterItem>()
            val seenKeys = mutableSetOf<String>()

            // 1. Add all students returned from Supabase
            if (!remoteStudents.isNullOrEmpty()) {
                for (s in remoteStudents) {
                    val key = s.rollNo.ifBlank { s.id }.lowercase().trim()
                    if (seenKeys.add(key)) {
                        combined.add(s)
                    }
                }
            }

            // 2. Add local authentic default students if not already in Supabase
            for (s in baseRoster) {
                val key = s.rollNo.ifBlank { s.id }.lowercase().trim()
                if (seenKeys.add(key)) {
                    combined.add(s)
                }
            }

            if (combined.isNotEmpty()) {
                val currentStatuses = _attendanceRoster.value.associate { (it.rollNo.ifBlank { it.id }.lowercase().trim()) to it.status }
                val merged = combined.map { s ->
                    val key = s.rollNo.ifBlank { s.id }.lowercase().trim()
                    val existingStatus = currentStatuses[key] ?: StudentAttendanceStatus.UNMARKED
                    s.copy(status = existingStatus)
                }
                _attendanceRoster.value = merged
                MockDatabase.teacherAttendanceRoster.value = merged
                teacherDao?.saveAttendanceRoster(merged)

                // Synchronize total students count in professor profile
                val updatedProfile = _teacherProfile.value.copy(totalStudents = merged.size)
                _teacherProfile.value = updatedProfile
                MockDatabase.teacherProfile.value = updatedProfile

                // Dynamically update at-risk students based on actual registered student attendance (only when classes > 0)
                val realAtRisk = merged.filter { it.totalClasses > 0 && it.attendancePercentage < 75 }.map { s ->
                    AtRiskStudent(
                        id = "risk_${s.id}",
                        studentName = s.studentName,
                        rollNo = s.rollNo,
                        reason = "Low Attendance (${s.attendancePercentage}%) & Academic Alert",
                        attendancePercent = s.attendancePercentage,
                        isReminded = false
                    )
                }
                _atRiskStudents.value = realAtRisk
                MockDatabase.atRiskStudents.value = realAtRisk
            }

            // Sync remote assignments and study materials
            val remoteAssignments = SupabaseApiClient.fetchAssignments(_teacherProfile.value.department)
            if (!remoteAssignments.isNullOrEmpty()) {
                DatabaseProvider.studentDao?.saveAssignments(remoteAssignments)
                val localAsg = teacherDao?.getAssignments() ?: remoteAssignments
                val filtered = filterAssignmentsForTeacher(localAsg, assigned)
                _assignments.value = filtered
                MockDatabase.studentAssignments.value = localAsg
            } else {
                val localAsg = teacherDao?.getAssignments() ?: emptyList()
                val filtered = filterAssignmentsForTeacher(localAsg, assigned)
                _assignments.value = filtered
            }
            val remoteMaterials = SupabaseApiClient.fetchStudyMaterials(_teacherProfile.value.department)
            if (!remoteMaterials.isNullOrEmpty()) {
                DatabaseProvider.studentDao?.saveStudyMaterials(remoteMaterials)
                val localMat = teacherDao?.getStudyMaterials() ?: DatabaseProvider.studentDao?.getStudyMaterials() ?: remoteMaterials
                val filtered = filterMaterialsForTeacher(localMat, assigned)
                _studyMaterials.value = filtered
                MockDatabase.studyMaterials.value = localMat
            } else {
                val localMat = teacherDao?.getStudyMaterials() ?: DatabaseProvider.studentDao?.getStudyMaterials() ?: emptyList()
                val filtered = filterMaterialsForTeacher(localMat, assigned)
                _studyMaterials.value = filtered
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
        subject: String = _assignedSubjects.value.firstOrNull() ?: "Discrete Structure & Theory of Logic (T) (BCS-303)"
    ) {
        val currentList = _attendanceRoster.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == rosterId }
        if (index != -1) {
            val currentItem = currentList[index]
            val newStatus = if (currentItem.status == status) StudentAttendanceStatus.UNMARKED else status
            teacherDao?.updateRosterStatus(rosterId, newStatus)
            currentList[index] = currentItem.copy(status = newStatus)
            _attendanceRoster.value = currentList
            MockDatabase.teacherAttendanceRoster.value = currentList
        }
    }

    fun markAllPresent(subject: String = _assignedSubjects.value.firstOrNull() ?: "Discrete Structure & Theory of Logic (T) (BCS-303)") {
        teacherDao?.markAllPresent()
        val roster = _attendanceRoster.value.map {
            it.copy(status = StudentAttendanceStatus.PRESENT)
        }
        _attendanceRoster.value = roster
        MockDatabase.teacherAttendanceRoster.value = roster
    }

    suspend fun submitCurrentAttendance(
        className: String = "IT - Sem 3",
        subject: String = _assignedSubjects.value.firstOrNull() ?: "Discrete Structure & Theory of Logic (T) (BCS-303)"
    ): Result<Int> = withContext(Dispatchers.IO) {
        val assigned = _assignedSubjects.value
        val isAuth = if (assigned.isEmpty()) true else MasterSubjectRegistry.isAuthorized(assigned, subject)
        if (!isAuth) {
            val errMsg = "Security Reject: You are not assigned to $subject. (Assigned: ${assigned.joinToString()})"
            android.util.Log.e("TeacherRepository", errMsg)
            return@withContext Result.failure(Exception(errMsg))
        }

        val canonicalSub = MasterSubjectRegistry.normalize(subject)
        val roster = _attendanceRoster.value
        val marked = roster.filter { it.status != StudentAttendanceStatus.UNMARKED }
        if (marked.isEmpty()) {
            return@withContext Result.failure(Exception("No students marked. Please mark attendance before submitting."))
        }

        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        var savedCount = 0

        // 1. Immediately update Teacher's roster with recalculated percentages & reset status for next session
        val updatedRoster = roster.map { student ->
            val markedItem = marked.find { it.id == student.id || it.rollNo == student.rollNo }
            if (markedItem != null) {
                val isPresent = markedItem.status == StudentAttendanceStatus.PRESENT
                val prevTotal = if (student.totalClasses > 0) student.totalClasses else (if (student.attendancePercentage > 0) 1 else 0)
                val prevAttended = if (student.attendedClasses > 0) student.attendedClasses else (if (student.attendancePercentage == 100) 1 else 0)
                val newTotal = prevTotal + 1
                val newAttended = prevAttended + (if (isPresent) 1 else 0)
                val newPct = if (newTotal > 0) ((newAttended * 100) / newTotal).coerceIn(0, 100) else 0
                student.copy(
                    totalClasses = newTotal,
                    attendedClasses = newAttended,
                    attendancePercentage = newPct,
                    status = StudentAttendanceStatus.UNMARKED
                )
            } else {
                student.copy(status = StudentAttendanceStatus.UNMARKED)
            }
        }
        _attendanceRoster.value = updatedRoster
        MockDatabase.teacherAttendanceRoster.value = updatedRoster
        teacherDao?.saveAttendanceRoster(updatedRoster)

        // Update At-Risk Students list immediately based on newly updated percentages
        val newAtRisk = updatedRoster.filter { it.totalClasses > 0 && it.attendancePercentage < 75 }.map { s ->
            AtRiskStudent(
                id = "risk_${s.id}",
                studentName = s.studentName,
                rollNo = s.rollNo,
                reason = "Low Attendance (${s.attendancePercentage}%) & Academic Alert",
                attendancePercent = s.attendancePercentage,
                isReminded = false
            )
        }
        _atRiskStudents.value = newAtRisk
        MockDatabase.atRiskStudents.value = newAtRisk
        teacherDao?.saveAtRiskStudents(newAtRisk)

        // 2. Immediately update Student local database & MockDatabase for this subject
        try {
            val currentStudentAtt = DatabaseProvider.studentDao?.getAttendanceList() ?: MockDatabase.studentAttendance.value
            if (currentStudentAtt.isNotEmpty()) {
                val updatedStudentAtt = currentStudentAtt.map { subItem ->
                    if (MasterSubjectRegistry.isAuthorized(listOf(subItem.subjectName, subItem.subjectCode), canonicalSub)) {
                        val currentStdId = MockDatabase.studentProfile.value.id
                        val currentStdRoll = MockDatabase.studentProfile.value.rollNo
                        val markedForCurrent = marked.find { it.id == currentStdId || it.rollNo == currentStdRoll }
                        val isPresent = if (markedForCurrent != null) {
                            markedForCurrent.status == StudentAttendanceStatus.PRESENT
                        } else {
                            marked.any { it.status == StudentAttendanceStatus.PRESENT }
                        }
                        val newTotal = subItem.totalClasses + 1
                        val newAttended = subItem.attendedClasses + (if (isPresent) 1 else 0)
                        val newPct = ((newAttended * 100) / newTotal).coerceIn(0, 100)
                        subItem.copy(
                            totalClasses = newTotal,
                            attendedClasses = newAttended,
                            percentage = newPct,
                            isAlert = newTotal > 0 && newPct < 75
                        )
                    } else {
                        subItem
                    }
                }
                MockDatabase.studentAttendance.value = updatedStudentAtt
                DatabaseProvider.studentDao?.saveAttendanceList(updatedStudentAtt)

                val sumTotal = updatedStudentAtt.sumOf { it.totalClasses }
                val sumAttended = updatedStudentAtt.sumOf { it.attendedClasses }
                val newOverall = if (sumTotal > 0) ((sumAttended * 100) / sumTotal).coerceIn(0, 100) else MockDatabase.studentProfile.value.attendancePercentage
                val updatedStudentProf = MockDatabase.studentProfile.value.copy(attendancePercentage = newOverall)
                MockDatabase.studentProfile.value = updatedStudentProf
                DatabaseProvider.studentDao?.saveOrUpdateStudentProfile(updatedStudentProf)
            }
        } catch (e: Exception) {
            android.util.Log.e("TeacherRepository", "Error updating student local attendance", e)
        }

        // 3. Post teacher attendance session to Supabase attendance_faculty table
        try {
            val nowTime = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
            SupabaseApiClient.postFacultyAttendance(
                facultyId = _teacherProfile.value.id,
                facultyName = _teacherProfile.value.name,
                employeeId = _teacherProfile.value.employeeId,
                department = _teacherProfile.value.department,
                date = todayStr,
                status = "PRESENT",
                checkInTime = nowTime
            )
        } catch (e: Exception) {
            android.util.Log.e("TeacherRepository", "Error posting faculty attendance", e)
        }

        // 4. Post each student session to Supabase attendance_student in background
        marked.forEach { item ->
            try {
                val ok = SupabaseApiClient.postStudentAttendance(
                    studentId = item.id,
                    studentName = item.studentName,
                    rollNumber = item.rollNo,
                    subject = canonicalSub,
                    date = todayStr,
                    status = item.status.name,
                    teacherId = _teacherProfile.value.id,
                    markedBy = _teacherProfile.value.name
                )
                if (ok) savedCount++
            } catch (e: Exception) {
                android.util.Log.e("TeacherRepository", "Error posting student attendance for ${item.studentName}", e)
            }
        }

        Result.success(savedCount)
    }

    fun resetAttendance() {
        teacherDao?.resetAllAttendance()
        val roster = _attendanceRoster.value.map {
            it.copy(status = StudentAttendanceStatus.UNMARKED)
        }
        _attendanceRoster.value = roster
        MockDatabase.teacherAttendanceRoster.value = roster
        teacherDao?.saveAttendanceRoster(roster)
    }

    fun createAssignment(
        title: String, 
        instructions: String, 
        dueDate: String,
        subjectName: String = _assignedSubjects.value.firstOrNull() ?: "Discrete Structure & Theory of Logic (T) (BCS-303)",
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
        val teacherName = _teacherProfile.value.name.ifBlank { "Faculty Member" }
        val newNotice = NoticeItem(
            id = UUID.randomUUID().toString(),
            title = title,
            category = category,
            timeAgo = "Just now",
            description = description,
            date = "Today",
            publishedBy = teacherName
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
