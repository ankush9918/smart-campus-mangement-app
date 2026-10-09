package com.vvit.smartcollege.data.local.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.vvit.smartcollege.data.local.DbConstants
import com.vvit.smartcollege.data.model.*

class StudentDao(private val db: SQLiteDatabase) {

    fun getStudentProfile(): StudentProfile {
        val cursor = db.query(DbConstants.TABLE_STUDENT_PROFILES, null, null, null, null, null, null)
        return cursor.use {
            if (it.moveToFirst()) {
                val photoUrlCol = it.getColumnIndex("photoUrl")
                val photoUrl = if (photoUrlCol >= 0) it.getString(photoUrlCol) else null
                StudentProfile(
                    id = it.getString(it.getColumnIndexOrThrow("id")),
                    name = it.getString(it.getColumnIndexOrThrow("name")),
                    rollNo = it.getString(it.getColumnIndexOrThrow("rollNo")),
                    email = it.getString(it.getColumnIndexOrThrow("email")),
                    phone = it.getString(it.getColumnIndexOrThrow("phone")),
                    dateOfBirth = it.getString(it.getColumnIndexOrThrow("dateOfBirth")),
                    bloodGroup = it.getString(it.getColumnIndexOrThrow("bloodGroup")),
                    department = "Information Technology",
                    currentSemester = it.getString(it.getColumnIndexOrThrow("currentSemester")),
                    section = it.getString(it.getColumnIndexOrThrow("section")),
                    admissionYear = it.getString(it.getColumnIndexOrThrow("admissionYear")),
                    guardianName = it.getString(it.getColumnIndexOrThrow("guardianName")),
                    guardianRelationship = it.getString(it.getColumnIndexOrThrow("guardianRelationship")),
                    guardianContact = it.getString(it.getColumnIndexOrThrow("guardianContact")),
                    cgpa = it.getDouble(it.getColumnIndexOrThrow("cgpa")),
                    attendancePercentage = it.getInt(it.getColumnIndexOrThrow("attendancePercentage")),
                    photoUrl = photoUrl
                )
            } else {
                StudentProfile("std_1", "Aditya Sharma", "VVIT/CS/2021/045", "aditya.sharma@vvit.edu", "+91 98765 43210", "14th August 2003", "O+ Positive", "Information Technology", "Semester 5 (Year 3)", "Section B", "2021", "Ramesh Sharma", "Father", "+91 98111 22233", 8.41, 87, null)
            }
        }
    }

    fun saveOrUpdateStudentProfile(profile: StudentProfile) {
        val cv = ContentValues().apply {
            put("id", profile.id)
            put("name", profile.name)
            put("rollNo", profile.rollNo)
            put("email", profile.email)
            put("phone", profile.phone)
            put("dateOfBirth", profile.dateOfBirth)
            put("bloodGroup", profile.bloodGroup)
            put("department", "Information Technology")
            put("currentSemester", profile.currentSemester)
            put("section", profile.section)
            put("admissionYear", profile.admissionYear)
            put("guardianName", profile.guardianName)
            put("guardianRelationship", profile.guardianRelationship)
            put("guardianContact", profile.guardianContact)
            put("cgpa", profile.cgpa)
            put("attendancePercentage", profile.attendancePercentage)
            put("photoUrl", profile.photoUrl)
        }
        db.delete(DbConstants.TABLE_STUDENT_PROFILES, null, null)
        db.insert(DbConstants.TABLE_STUDENT_PROFILES, null, cv)
    }

    fun saveAttendanceList(list: List<SubjectAttendance>) {
        if (list.isEmpty()) return
        db.delete(DbConstants.TABLE_SUBJECT_ATTENDANCE, null, null)
        for (item in list) {
            val cv = ContentValues().apply {
                put("id", item.id)
                put("subjectName", item.subjectName)
                put("subjectCode", item.subjectCode)
                put("attendedClasses", item.attendedClasses)
                put("totalClasses", item.totalClasses)
                put("percentage", item.percentage)
                put("isAlert", if (item.isAlert) 1 else 0)
            }
            db.insert(DbConstants.TABLE_SUBJECT_ATTENDANCE, null, cv)
        }
    }

    fun getAttendanceList(): List<SubjectAttendance> {
        val list = mutableListOf<SubjectAttendance>()
        val cursor = db.query(DbConstants.TABLE_SUBJECT_ATTENDANCE, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    SubjectAttendance(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        subjectName = it.getString(it.getColumnIndexOrThrow("subjectName")),
                        subjectCode = it.getString(it.getColumnIndexOrThrow("subjectCode")),
                        attendedClasses = it.getInt(it.getColumnIndexOrThrow("attendedClasses")),
                        totalClasses = it.getInt(it.getColumnIndexOrThrow("totalClasses")),
                        percentage = it.getInt(it.getColumnIndexOrThrow("percentage")),
                        isAlert = it.getInt(it.getColumnIndexOrThrow("isAlert")) == 1
                    )
                )
            }
        }
        return list
    }

    fun getTimetable(): List<StudentTimetableEntry> {
        val list = mutableListOf<StudentTimetableEntry>()
        val cursor = db.query(DbConstants.TABLE_TIMETABLE, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    StudentTimetableEntry(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        dayOfWeek = it.getString(it.getColumnIndexOrThrow("dayOfWeek")),
                        time = it.getString(it.getColumnIndexOrThrow("time")),
                        duration = it.getString(it.getColumnIndexOrThrow("duration")),
                        subjectName = it.getString(it.getColumnIndexOrThrow("subjectName")),
                        facultyName = it.getString(it.getColumnIndexOrThrow("facultyName")),
                        room = it.getString(it.getColumnIndexOrThrow("room")),
                        isOngoing = it.getInt(it.getColumnIndexOrThrow("isOngoing")) == 1,
                        isBreak = it.getInt(it.getColumnIndexOrThrow("isBreak")) == 1
                    )
                )
            }
        }
        return list
    }

    fun getAssignments(): List<StudentAssignment> {
        val list = mutableListOf<StudentAssignment>()
        val cursor = db.query(DbConstants.TABLE_ASSIGNMENTS, null, null, null, null, null, null)
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

    fun submitAssignment(assignmentId: String, fileName: String) {
        val cv = ContentValues().apply {
            put("status", "SUBMITTED")
            put("submissionDate", "Just now")
            put("submittedFileName", fileName)
        }
        db.update(DbConstants.TABLE_ASSIGNMENTS, cv, "id = ?", arrayOf(assignmentId))
    }

    fun getStudyMaterials(): List<StudentStudyMaterial> {
        val list = mutableListOf<StudentStudyMaterial>()
        val cursor = db.query(DbConstants.TABLE_STUDY_MATERIALS, null, null, null, null, null, null)
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

    fun getSemesterResults(): List<SemesterResult> {
        val results = mutableListOf<SemesterResult>()
        val cursor = db.query(DbConstants.TABLE_SEMESTER_RESULTS, null, null, null, null, null, "semesterNumber DESC")
        cursor.use {
            while (it.moveToNext()) {
                val semNum = it.getInt(it.getColumnIndexOrThrow("semesterNumber"))
                val semName = it.getString(it.getColumnIndexOrThrow("semesterName"))
                val sgpa = it.getDouble(it.getColumnIndexOrThrow("sgpa"))

                val grades = getGradeItems(semNum)
                results.add(SemesterResult(semName, semNum, sgpa, grades))
            }
        }
        return results
    }

    private fun getGradeItems(semNum: Int): List<GradeItem> {
        val list = mutableListOf<GradeItem>()
        val cursor = db.query(DbConstants.TABLE_GRADESHEET_ITEMS, null, "semesterNumber = ?", arrayOf(semNum.toString()), null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    GradeItem(
                        subjectName = it.getString(it.getColumnIndexOrThrow("subjectName")),
                        subjectCode = it.getString(it.getColumnIndexOrThrow("subjectCode")),
                        internalMarks = it.getInt(it.getColumnIndexOrThrow("internalMarks")),
                        externalMarks = it.getInt(it.getColumnIndexOrThrow("externalMarks")),
                        totalMarks = it.getInt(it.getColumnIndexOrThrow("totalMarks")),
                        grade = it.getString(it.getColumnIndexOrThrow("grade"))
                    )
                )
            }
        }
        return list
    }

    fun getFeeDetails(): FeeDetails {
        val cursor = db.query(DbConstants.TABLE_FEE_DETAILS, null, null, null, null, null, null)
        return cursor.use {
            if (it.moveToFirst()) {
                FeeDetails(
                    pendingAmount = it.getLong(it.getColumnIndexOrThrow("pendingAmount")),
                    totalInvoiced = it.getLong(it.getColumnIndexOrThrow("totalInvoiced")),
                    paidTillDate = it.getLong(it.getColumnIndexOrThrow("paidTillDate")),
                    currentSemesterFee = it.getLong(it.getColumnIndexOrThrow("currentSemesterFee")),
                    semesterLabel = it.getString(it.getColumnIndexOrThrow("semesterLabel"))
                )
            } else {
                FeeDetails(45000L, 125000L, 80000L, 45000L, "Semester 5 Tuition Fee")
            }
        }
    }

    fun payFee(amount: Long, txn: FeeTransaction) {
        val current = getFeeDetails()
        val newPending = (current.pendingAmount - amount).coerceAtLeast(0L)
        val newPaid = current.paidTillDate + amount

        val cv = ContentValues().apply {
            put("pendingAmount", newPending)
            put("paidTillDate", newPaid)
        }
        db.update(DbConstants.TABLE_FEE_DETAILS, cv, "id = ?", arrayOf("fee_std_1"))

        val tcv = ContentValues().apply {
            put("id", txn.id)
            put("title", txn.title)
            put("transactionId", txn.transactionId)
            put("date", txn.date)
            put("amount", txn.amount)
            put("status", txn.status)
        }
        db.insert(DbConstants.TABLE_FEE_TRANSACTIONS, null, tcv)
    }

    fun getFeeTransactions(): List<FeeTransaction> {
        val list = mutableListOf<FeeTransaction>()
        val cursor = db.query(DbConstants.TABLE_FEE_TRANSACTIONS, null, null, null, null, null, "rowid DESC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    FeeTransaction(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        transactionId = it.getString(it.getColumnIndexOrThrow("transactionId")),
                        date = it.getString(it.getColumnIndexOrThrow("date")),
                        amount = it.getLong(it.getColumnIndexOrThrow("amount")),
                        status = it.getString(it.getColumnIndexOrThrow("status"))
                    )
                )
            }
        }
        return list
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

    fun getLeaveApplications(): List<StudentLeaveApplication> {
        val list = mutableListOf<StudentLeaveApplication>()
        val cursor = db.query(DbConstants.TABLE_LEAVE_APPLICATIONS, null, null, null, null, null, "rowid DESC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    StudentLeaveApplication(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        studentName = it.getString(it.getColumnIndexOrThrow("studentName")),
                        rollNo = it.getString(it.getColumnIndexOrThrow("rollNo")),
                        fromDate = it.getString(it.getColumnIndexOrThrow("fromDate")),
                        toDate = it.getString(it.getColumnIndexOrThrow("toDate")),
                        durationDays = it.getInt(it.getColumnIndexOrThrow("durationDays")),
                        reason = it.getString(it.getColumnIndexOrThrow("reason")),
                        status = it.getString(it.getColumnIndexOrThrow("status")),
                        appliedDate = it.getString(it.getColumnIndexOrThrow("appliedDate"))
                    )
                )
            }
        }
        return list
    }

    fun insertLeaveApplication(leave: StudentLeaveApplication) {
        val cv = ContentValues().apply {
            put("id", leave.id)
            put("studentName", leave.studentName)
            put("rollNo", leave.rollNo)
            put("fromDate", leave.fromDate)
            put("toDate", leave.toDate)
            put("durationDays", leave.durationDays)
            put("reason", leave.reason)
            put("status", leave.status)
            put("appliedDate", leave.appliedDate)
        }
        db.insert(DbConstants.TABLE_LEAVE_APPLICATIONS, null, cv)
    }

    fun saveAssignments(list: List<StudentAssignment>) {
        if (list.isEmpty()) return
        for (item in list) {
            val cv = ContentValues().apply {
                put("id", item.id)
                put("subjectName", item.subjectName)
                put("title", item.title)
                put("instructions", item.instructions)
                put("dueDate", item.dueDate)
                put("status", item.status.name)
                put("submissionDate", item.submissionDate)
                put("maxMarks", item.maxMarks)
                put("obtainedMarks", item.obtainedMarks)
                put("submittedFileName", item.submittedFileName)
                put("professorName", item.professorName)
                put("fileName", item.fileName)
                put("fileUri", item.fileUri)
            }
            val count = db.update(DbConstants.TABLE_ASSIGNMENTS, cv, "id = ?", arrayOf(item.id))
            if (count == 0) {
                db.insert(DbConstants.TABLE_ASSIGNMENTS, null, cv)
            }
        }
    }

    fun saveStudyMaterials(list: List<StudentStudyMaterial>) {
        if (list.isEmpty()) return
        for (item in list) {
            val cv = ContentValues().apply {
                put("id", item.id)
                put("title", item.title)
                put("subjectName", item.subjectName)
                put("sharedBy", item.sharedBy)
                put("sharedDate", item.sharedDate)
                put("fileSize", item.fileSize)
                put("fileType", item.fileType)
                put("downloadUrl", item.downloadUrl)
                put("fileName", item.fileName)
                put("fileUri", item.fileUri)
            }
            val count = db.update(DbConstants.TABLE_STUDY_MATERIALS, cv, "id = ?", arrayOf(item.id))
            if (count == 0) {
                db.insert(DbConstants.TABLE_STUDY_MATERIALS, null, cv)
            }
        }
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
}