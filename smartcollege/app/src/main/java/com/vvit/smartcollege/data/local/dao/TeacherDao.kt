package com.vvit.smartcollege.data.local.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.vvit.smartcollege.data.local.DbConstants
import com.vvit.smartcollege.data.model.*

class TeacherDao(private val db: SQLiteDatabase) {

    fun getTeacherProfile(): TeacherProfile {
        val cursor = db.query(DbConstants.TABLE_TEACHER_PROFILES, null, null, null, null, null, null)
        return cursor.use {
            if (it.moveToFirst()) {
                val photoUrlCol = it.getColumnIndex("photoUrl")
                val photoUrl = if (photoUrlCol >= 0 && !it.isNull(photoUrlCol)) it.getString(photoUrlCol) else null
                TeacherProfile(
                    id = it.getString(it.getColumnIndexOrThrow("id")),
                    name = it.getString(it.getColumnIndexOrThrow("name")),
                    employeeId = it.getString(it.getColumnIndexOrThrow("employeeId")),
                    designation = it.getString(it.getColumnIndexOrThrow("designation")),
                    department = it.getString(it.getColumnIndexOrThrow("department")),
                    email = it.getString(it.getColumnIndexOrThrow("email")),
                    phone = it.getString(it.getColumnIndexOrThrow("phone")),
                    weeklyWorkloadHours = it.getInt(it.getColumnIndexOrThrow("weeklyWorkloadHours")),
                    activeSubjectsCount = it.getInt(it.getColumnIndexOrThrow("activeSubjectsCount")),
                    totalStudents = it.getInt(it.getColumnIndexOrThrow("totalStudents")),
                    classesToday = it.getInt(it.getColumnIndexOrThrow("classesToday")),
                    pendingTasks = it.getInt(it.getColumnIndexOrThrow("pendingTasks")),
                    photoUrl = photoUrl
                )
            } else {
                TeacherProfile("tch_1", "Dr. Rajesh Kumar", "VVIT/CS/T-104", "Associate Professor", "Information Technology", "rajesh.kumar@vvit.edu", "+91 98123 45678", 16, 3, 248, 5, 12, null)
            }
        }
    }

    fun saveOrUpdateTeacherProfile(profile: TeacherProfile) {
        val cv = ContentValues().apply {
            put("id", profile.id)
            put("name", profile.name)
            put("employeeId", profile.employeeId)
            put("designation", profile.designation)
            put("department", profile.department)
            put("email", profile.email)
            put("phone", profile.phone)
            put("weeklyWorkloadHours", profile.weeklyWorkloadHours)
            put("activeSubjectsCount", profile.activeSubjectsCount)
            put("totalStudents", profile.totalStudents)
            put("classesToday", profile.classesToday)
            put("pendingTasks", profile.pendingTasks)
            put("photoUrl", profile.photoUrl)
        }
        db.delete(DbConstants.TABLE_TEACHER_PROFILES, null, null)
        db.insert(DbConstants.TABLE_TEACHER_PROFILES, null, cv)
    }

    fun getSchedule(): List<TeacherScheduleItem> {
        val list = mutableListOf<TeacherScheduleItem>()
        val cursor = db.query(DbConstants.TABLE_TEACHER_SCHEDULE, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    TeacherScheduleItem(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        time = it.getString(it.getColumnIndexOrThrow("time")),
                        subjectName = it.getString(it.getColumnIndexOrThrow("subjectName")),
                        room = it.getString(it.getColumnIndexOrThrow("room")),
                        section = it.getString(it.getColumnIndexOrThrow("section")),
                        lectureType = it.getString(it.getColumnIndexOrThrow("lectureType")),
                        dayOfWeek = it.getString(it.getColumnIndexOrThrow("dayOfWeek"))
                    )
                )
            }
        }
        return list
    }

    fun getAttendanceRoster(): List<StudentRosterItem> {
        val list = mutableListOf<StudentRosterItem>()
        val cursor = db.query(DbConstants.TABLE_ATTENDANCE_ROSTER, null, null, null, null, null, null)
        cursor.use {
            val photoCol = it.getColumnIndex("photoUrl")
            val emailCol = it.getColumnIndex("email")
            val phoneCol = it.getColumnIndex("phone")
            val deptCol = it.getColumnIndex("department")
            val semCol = it.getColumnIndex("currentSemester")
            val attCol = it.getColumnIndex("attendancePercentage")
            val cgpaCol = it.getColumnIndex("cgpa")
            val gNameCol = it.getColumnIndex("guardianName")
            val gPhoneCol = it.getColumnIndex("guardianContact")

            while (it.moveToNext()) {
                val id = it.getString(it.getColumnIndexOrThrow("id"))
                val rollNo = it.getString(it.getColumnIndexOrThrow("rollNo"))
                val studentName = it.getString(it.getColumnIndexOrThrow("studentName"))
                val section = it.getString(it.getColumnIndexOrThrow("section"))
                val statusStr = it.getString(it.getColumnIndexOrThrow("status"))
                val status = try {
                    StudentAttendanceStatus.valueOf(statusStr)
                } catch (e: Exception) {
                    StudentAttendanceStatus.UNMARKED
                }

                val rowPhoto = if (photoCol >= 0 && !it.isNull(photoCol)) it.getString(photoCol) else null
                var email = if (emailCol >= 0 && !it.isNull(emailCol)) it.getString(emailCol) else ""
                var phone = if (phoneCol >= 0 && !it.isNull(phoneCol)) it.getString(phoneCol) else ""
                var department = if (deptCol >= 0 && !it.isNull(deptCol)) it.getString(deptCol) else "Information Technology"
                var semester = if (semCol >= 0 && !it.isNull(semCol)) it.getString(semCol) else "Semester 3 (Year 2)"
                var cgpa = if (cgpaCol >= 0 && !it.isNull(cgpaCol)) it.getDouble(cgpaCol) else 8.0
                var attendancePct = if (attCol >= 0 && !it.isNull(attCol)) it.getInt(attCol) else 85
                var guardianName = if (gNameCol >= 0 && !it.isNull(gNameCol)) it.getString(gNameCol) else "Guardian / Parent"
                var guardianContact = if (gPhoneCol >= 0 && !it.isNull(gPhoneCol)) it.getString(gPhoneCol) else "+91 98765 43210"
                var photoUrl = rowPhoto

                if (email.isBlank()) {
                    val profileCursor = db.query(
                        DbConstants.TABLE_STUDENT_PROFILES,
                        null,
                        "rollNo = ? OR name = ?",
                        arrayOf(rollNo, studentName),
                        null, null, null
                    )
                    profileCursor.use { pc ->
                        if (pc.moveToFirst()) {
                            email = pc.getString(pc.getColumnIndexOrThrow("email"))
                            phone = pc.getString(pc.getColumnIndexOrThrow("phone"))
                            department = pc.getString(pc.getColumnIndexOrThrow("department"))
                            semester = pc.getString(pc.getColumnIndexOrThrow("currentSemester"))
                            cgpa = pc.getDouble(pc.getColumnIndexOrThrow("cgpa"))
                            attendancePct = pc.getInt(pc.getColumnIndexOrThrow("attendancePercentage"))
                            guardianName = pc.getString(pc.getColumnIndexOrThrow("guardianName"))
                            guardianContact = pc.getString(pc.getColumnIndexOrThrow("guardianContact"))
                            val pCol = pc.getColumnIndex("photoUrl")
                            if (photoUrl == null && pCol >= 0 && !pc.isNull(pCol)) {
                                photoUrl = pc.getString(pCol)
                            }
                        }
                    }
                }

                list.add(
                    StudentRosterItem(
                        id = id,
                        rollNo = rollNo,
                        studentName = studentName,
                        section = section,
                        status = status,
                        email = email,
                        phone = phone,
                        department = department,
                        currentSemester = semester,
                        attendancePercentage = attendancePct,
                        cgpa = cgpa,
                        guardianName = guardianName,
                        guardianContact = guardianContact,
                        photoUrl = photoUrl
                    )
                )
            }
        }
        return list
    }

    fun saveAttendanceRoster(students: List<StudentRosterItem>) {
        db.beginTransaction()
        try {
            db.delete(DbConstants.TABLE_ATTENDANCE_ROSTER, null, null)
            for (item in students) {
                val cv = ContentValues().apply {
                    put("id", item.id)
                    put("rollNo", item.rollNo)
                    put("studentName", item.studentName)
                    put("section", item.section)
                    put("status", item.status.name)
                    put("email", item.email)
                    put("phone", item.phone)
                    put("department", item.department)
                    put("currentSemester", item.currentSemester)
                    put("attendancePercentage", item.attendancePercentage)
                    put("cgpa", item.cgpa)
                    put("guardianName", item.guardianName)
                    put("guardianContact", item.guardianContact)
                    put("photoUrl", item.photoUrl)
                }
                db.insertWithOnConflict(DbConstants.TABLE_ATTENDANCE_ROSTER, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun updateRosterStatus(id: String, status: StudentAttendanceStatus) {
        val cv = ContentValues().apply {
            put("status", status.name)
        }
        db.update(DbConstants.TABLE_ATTENDANCE_ROSTER, cv, "id = ?", arrayOf(id))
    }

    fun markAllPresent() {
        val cv = ContentValues().apply {
            put("status", StudentAttendanceStatus.PRESENT.name)
        }
        db.update(DbConstants.TABLE_ATTENDANCE_ROSTER, cv, null, null)
    }

    fun resetAllAttendance() {
        val cv = ContentValues().apply {
            put("status", StudentAttendanceStatus.UNMARKED.name)
        }
        db.update(DbConstants.TABLE_ATTENDANCE_ROSTER, cv, null, null)
    }

    fun createAssignment(assignment: StudentAssignment) {
        val cv = ContentValues().apply {
            put("id", assignment.id)
            put("subjectName", assignment.subjectName)
            put("title", assignment.title)
            put("instructions", assignment.instructions)
            put("dueDate", assignment.dueDate)
            put("status", assignment.status.name)
            put("maxMarks", assignment.maxMarks)
            put("professorName", assignment.professorName)
            put("fileName", assignment.fileName)
            put("fileUri", assignment.fileUri)
        }
        db.insertWithOnConflict(DbConstants.TABLE_ASSIGNMENTS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getAssignments(): List<StudentAssignment> {
        val list = mutableListOf<StudentAssignment>()
        val cursor = db.query(DbConstants.TABLE_ASSIGNMENTS, null, null, null, null, null, "dueDate ASC")
        cursor.use {
            while (it.moveToNext()) {
                val profIdx = it.getColumnIndex("professorName")
                val fileIdx = it.getColumnIndex("fileName")
                val uriIdx = it.getColumnIndex("fileUri")
                list.add(
                    StudentAssignment(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        subjectName = it.getString(it.getColumnIndexOrThrow("subjectName")),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        instructions = it.getString(it.getColumnIndexOrThrow("instructions")),
                        dueDate = it.getString(it.getColumnIndexOrThrow("dueDate")),
                        status = try {
                            AssignmentStatus.valueOf(it.getString(it.getColumnIndexOrThrow("status")))
                        } catch (_: Exception) {
                            AssignmentStatus.PENDING
                        },
                        submissionDate = it.getString(it.getColumnIndexOrThrow("submissionDate")),
                        maxMarks = it.getInt(it.getColumnIndexOrThrow("maxMarks")),
                        obtainedMarks = if (it.isNull(it.getColumnIndexOrThrow("obtainedMarks"))) null else it.getInt(it.getColumnIndexOrThrow("obtainedMarks")),
                        submittedFileName = it.getString(it.getColumnIndexOrThrow("submittedFileName")),
                        professorName = if (profIdx >= 0 && !it.isNull(profIdx)) it.getString(profIdx) else "",
                        fileName = if (fileIdx >= 0 && !it.isNull(fileIdx)) it.getString(fileIdx) else null,
                        fileUri = if (uriIdx >= 0 && !it.isNull(uriIdx)) it.getString(uriIdx) else null
                    )
                )
            }
        }
        return list
    }

    fun uploadStudyMaterial(material: StudentStudyMaterial) {
        val cv = ContentValues().apply {
            put("id", material.id)
            put("title", material.title)
            put("subjectName", material.subjectName)
            put("sharedBy", material.sharedBy)
            put("sharedDate", material.sharedDate)
            put("fileSize", material.fileSize)
            put("fileType", material.fileType)
            put("downloadUrl", material.downloadUrl)
            put("fileName", material.fileName)
            put("fileUri", material.fileUri)
        }
        db.insertWithOnConflict(DbConstants.TABLE_STUDY_MATERIALS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getStudyMaterials(): List<StudentStudyMaterial> {
        val list = mutableListOf<StudentStudyMaterial>()
        val cursor = db.query(DbConstants.TABLE_STUDY_MATERIALS, null, null, null, null, null, "sharedDate DESC")
        cursor.use {
            while (it.moveToNext()) {
                val fileIdx = it.getColumnIndex("fileName")
                val uriIdx = it.getColumnIndex("fileUri")
                list.add(
                    StudentStudyMaterial(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        subjectName = it.getString(it.getColumnIndexOrThrow("subjectName")),
                        sharedBy = it.getString(it.getColumnIndexOrThrow("sharedBy")),
                        sharedDate = it.getString(it.getColumnIndexOrThrow("sharedDate")),
                        fileSize = it.getString(it.getColumnIndexOrThrow("fileSize")),
                        fileType = it.getString(it.getColumnIndexOrThrow("fileType")),
                        downloadUrl = it.getString(it.getColumnIndexOrThrow("downloadUrl")),
                        fileName = if (fileIdx >= 0 && !it.isNull(fileIdx)) it.getString(fileIdx) else null,
                        fileUri = if (uriIdx >= 0 && !it.isNull(uriIdx)) it.getString(uriIdx) else null
                    )
                )
            }
        }
        return list
    }

    fun getAtRiskStudents(): List<AtRiskStudent> {
        val list = mutableListOf<AtRiskStudent>()
        val cursor = db.query(DbConstants.TABLE_AT_RISK_STUDENTS, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    AtRiskStudent(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        studentName = it.getString(it.getColumnIndexOrThrow("studentName")),
                        rollNo = it.getString(it.getColumnIndexOrThrow("rollNo")),
                        reason = it.getString(it.getColumnIndexOrThrow("reason")),
                        attendancePercent = it.getInt(it.getColumnIndexOrThrow("attendancePercent")),
                        isReminded = it.getInt(it.getColumnIndexOrThrow("isReminded")) == 1
                    )
                )
            }
        }
        return list
    }

    fun updateAtRiskReminded(id: String) {
        val cv = ContentValues().apply {
            put("isReminded", 1)
        }
        db.update(DbConstants.TABLE_AT_RISK_STUDENTS, cv, "id = ?", arrayOf(id))
    }

    fun updateLeaveStatus(leaveId: String, status: String) {
        val cv = ContentValues().apply {
            put("status", status)
        }
        db.update(DbConstants.TABLE_LEAVE_APPLICATIONS, cv, "id = ?", arrayOf(leaveId))
    }

    fun publishNotice(notice: NoticeItem) {
        val cv = ContentValues().apply {
            put("id", notice.id)
            put("title", notice.title)
            put("category", notice.category)
            put("timeAgo", notice.timeAgo)
            put("description", notice.description)
            put("isUrgent", if (notice.isUrgent) 1 else 0)
            put("date", notice.date)
        }
        db.insert(DbConstants.TABLE_NOTICES, null, cv)
    }

    fun saveNotices(list: List<NoticeItem>) {
        if (list.isEmpty()) return
        for (notice in list) {
            val cv = ContentValues().apply {
                put("id", notice.id)
                put("title", notice.title)
                put("category", notice.category)
                put("timeAgo", notice.timeAgo)
                put("description", notice.description)
                put("isUrgent", if (notice.isUrgent) 1 else 0)
                put("date", notice.date)
            }
            val count = db.update(DbConstants.TABLE_NOTICES, cv, "id = ?", arrayOf(notice.id))
            if (count == 0) {
                db.insert(DbConstants.TABLE_NOTICES, null, cv)
            }
        }
    }

    fun getNotices(): List<NoticeItem> {
        val list = mutableListOf<NoticeItem>()
        val cursor = db.query(DbConstants.TABLE_NOTICES, null, null, null, null, null, "rowid DESC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    NoticeItem(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        category = it.getString(it.getColumnIndexOrThrow("category")),
                        timeAgo = it.getString(it.getColumnIndexOrThrow("timeAgo")),
                        description = it.getString(it.getColumnIndexOrThrow("description")),
                        isUrgent = it.getInt(it.getColumnIndexOrThrow("isUrgent")) == 1,
                        date = it.getString(it.getColumnIndexOrThrow("date"))
                    )
                )
            }
        }
        return list
    }
}