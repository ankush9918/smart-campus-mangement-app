package com.vvit.smartcollege.data.local.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.vvit.smartcollege.data.local.DbConstants
import com.vvit.smartcollege.data.model.*

class AdminDao(private val db: SQLiteDatabase) {

    fun getKPIs(): AdminKPIs {
        return AdminKPIs(
            totalStudents = 2450,
            totalFaculty = 186,
            totalDepartments = 12,
            totalCourses = 48,
            pendingFacultyApprovals = 5,
            newAdmissionsToday = 18,
            feeCollectionPercent = 82,
            passRatePercent = 89,
            avgAttendancePercent = 84,
            yoyGrowthPercent = 4.2
        )
    }

    fun getStudentDirectory(): List<StudentDirectoryEntry> {
        val list = mutableListOf<StudentDirectoryEntry>()
        val cursor = db.query(DbConstants.TABLE_STUDENT_DIRECTORY, null, null, null, null, null, "rowid DESC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    StudentDirectoryEntry(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        name = it.getString(it.getColumnIndexOrThrow("name")),
                        rollNo = it.getString(it.getColumnIndexOrThrow("rollNo")),
                        department = it.getString(it.getColumnIndexOrThrow("department")),
                        semester = it.getString(it.getColumnIndexOrThrow("semester")),
                        email = it.getString(it.getColumnIndexOrThrow("email")),
                        guardianName = it.getString(it.getColumnIndexOrThrow("guardianName")),
                        guardianPhone = it.getString(it.getColumnIndexOrThrow("guardianPhone")),
                        cgpa = it.getDouble(it.getColumnIndexOrThrow("cgpa")),
                        attendancePercent = it.getInt(it.getColumnIndexOrThrow("attendancePercent")),
                        isActive = it.getInt(it.getColumnIndexOrThrow("isActive")) == 1
                    )
                )
            }
        }
        return list
    }

    fun insertStudentDirectory(entry: StudentDirectoryEntry) {
        val cv = ContentValues().apply {
            put("id", entry.id)
            put("name", entry.name)
            put("rollNo", entry.rollNo)
            put("department", entry.department)
            put("semester", entry.semester)
            put("email", entry.email)
            put("guardianName", entry.guardianName)
            put("guardianPhone", entry.guardianPhone)
            put("cgpa", entry.cgpa)
            put("attendancePercent", entry.attendancePercent)
            put("isActive", if (entry.isActive) 1 else 0)
        }
        db.insert(DbConstants.TABLE_STUDENT_DIRECTORY, null, cv)
    }

    fun getFacultyDirectory(): List<FacultyDirectoryEntry> {
        val list = mutableListOf<FacultyDirectoryEntry>()
        val cursor = db.query(DbConstants.TABLE_FACULTY_DIRECTORY, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                val subjectsStr = it.getString(it.getColumnIndexOrThrow("assignedSubjects"))
                val subjects = subjectsStr.split(",").map { s -> s.trim() }.filter { s -> s.isNotEmpty() }
                list.add(
                    FacultyDirectoryEntry(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        name = it.getString(it.getColumnIndexOrThrow("name")),
                        employeeId = it.getString(it.getColumnIndexOrThrow("employeeId")),
                        designation = it.getString(it.getColumnIndexOrThrow("designation")),
                        department = it.getString(it.getColumnIndexOrThrow("department")),
                        status = it.getString(it.getColumnIndexOrThrow("status")),
                        assignedSubjects = subjects,
                        weeklyWorkloadHours = it.getInt(it.getColumnIndexOrThrow("weeklyWorkloadHours"))
                    )
                )
            }
        }
        return list
    }

    fun insertFacultyDirectory(entry: FacultyDirectoryEntry) {
        val cv = ContentValues().apply {
            put("id", entry.id)
            put("name", entry.name)
            put("employeeId", entry.employeeId)
            put("designation", entry.designation)
            put("department", entry.department)
            put("status", entry.status)
            put("assignedSubjects", entry.assignedSubjects.joinToString(", "))
            put("weeklyWorkloadHours", entry.weeklyWorkloadHours)
        }
        db.insert(DbConstants.TABLE_FACULTY_DIRECTORY, null, cv)
    }

    fun getDepartments(): List<DepartmentEntry> {
        val list = mutableListOf<DepartmentEntry>()
        val cursor = db.query(DbConstants.TABLE_DEPARTMENTS, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    DepartmentEntry(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        name = it.getString(it.getColumnIndexOrThrow("name")),
                        hodName = it.getString(it.getColumnIndexOrThrow("hodName")),
                        facultyCount = it.getInt(it.getColumnIndexOrThrow("facultyCount")),
                        studentCount = it.getInt(it.getColumnIndexOrThrow("studentCount")),
                        isActive = it.getInt(it.getColumnIndexOrThrow("isActive")) == 1
                    )
                )
            }
        }
        return list
    }

    fun getScheduledExams(): List<ScheduledExam> {
        val list = mutableListOf<ScheduledExam>()
        val cursor = db.query(DbConstants.TABLE_SCHEDULED_EXAMS, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    ScheduledExam(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        subjectName = it.getString(it.getColumnIndexOrThrow("subjectName")),
                        subjectCode = it.getString(it.getColumnIndexOrThrow("subjectCode")),
                        examDate = it.getString(it.getColumnIndexOrThrow("examDate")),
                        timeDuration = it.getString(it.getColumnIndexOrThrow("timeDuration")),
                        venue = it.getString(it.getColumnIndexOrThrow("venue"))
                    )
                )
            }
        }
        return list
    }

    fun insertScheduledExam(exam: ScheduledExam) {
        val cv = ContentValues().apply {
            put("id", exam.id)
            put("subjectName", exam.subjectName)
            put("subjectCode", exam.subjectCode)
            put("examDate", exam.examDate)
            put("timeDuration", exam.timeDuration)
            put("venue", exam.venue)
        }
        db.insert(DbConstants.TABLE_SCHEDULED_EXAMS, null, cv)
    }

    fun getLibraryBooks(): List<LibraryBook> {
        val list = mutableListOf<LibraryBook>()
        val cursor = db.query(DbConstants.TABLE_LIBRARY_BOOKS, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    LibraryBook(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        author = it.getString(it.getColumnIndexOrThrow("author")),
                        isbn = it.getString(it.getColumnIndexOrThrow("isbn")),
                        availableCopies = it.getInt(it.getColumnIndexOrThrow("availableCopies")),
                        totalCopies = it.getInt(it.getColumnIndexOrThrow("totalCopies"))
                    )
                )
            }
        }
        return list
    }

    fun getIssuedBooks(): List<IssuedBookRecord> {
        val list = mutableListOf<IssuedBookRecord>()
        val cursor = db.query(DbConstants.TABLE_ISSUED_BOOKS, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    IssuedBookRecord(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        studentName = it.getString(it.getColumnIndexOrThrow("studentName")),
                        rollNo = it.getString(it.getColumnIndexOrThrow("rollNo")),
                        bookTitle = it.getString(it.getColumnIndexOrThrow("bookTitle")),
                        dueDate = it.getString(it.getColumnIndexOrThrow("dueDate")),
                        isOverdue = it.getInt(it.getColumnIndexOrThrow("isOverdue")) == 1
                    )
                )
            }
        }
        return list
    }

    fun getUserRoles(): List<UserRoleGroup> {
        val list = mutableListOf<UserRoleGroup>()
        val cursor = db.query(DbConstants.TABLE_USER_ROLES, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                val permsStr = it.getString(it.getColumnIndexOrThrow("permissions"))
                val perms = permsStr.split(",").map { p -> p.trim() }.filter { p -> p.isNotEmpty() }
                list.add(
                    UserRoleGroup(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        roleName = it.getString(it.getColumnIndexOrThrow("roleName")),
                        memberCount = it.getInt(it.getColumnIndexOrThrow("memberCount")),
                        permissions = perms
                    )
                )
            }
        }
        return list
    }

    fun getDepartmentFees(): List<DepartmentFeeCollection> {
        val list = mutableListOf<DepartmentFeeCollection>()
        val cursor = db.query(DbConstants.TABLE_DEPARTMENT_FEES, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    DepartmentFeeCollection(
                        departmentName = it.getString(it.getColumnIndexOrThrow("departmentName")),
                        collectedPercent = it.getInt(it.getColumnIndexOrThrow("collectedPercent"))
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
                val rawDesc = it.getString(it.getColumnIndexOrThrow("description"))
                var cleanDesc = rawDesc
                if (rawDesc.trim().startsWith("{")) {
                    try {
                        val meta = org.json.JSONObject(rawDesc)
                        if (meta.has("subject")) {
                            val sub = meta.optString("subject", "").trim()
                            if (sub.isNotBlank()) cleanDesc = sub
                        } else if (meta.has("instructions")) {
                            val inst = meta.optString("instructions", "").trim()
                            if (inst.isNotBlank()) cleanDesc = inst
                        }
                    } catch (_: Exception) {}
                }

                list.add(
                    NoticeItem(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        category = it.getString(it.getColumnIndexOrThrow("category")),
                        timeAgo = it.getString(it.getColumnIndexOrThrow("timeAgo")),
                        description = cleanDesc,
                        isUrgent = it.getInt(it.getColumnIndexOrThrow("isUrgent")) == 1,
                        date = it.getString(it.getColumnIndexOrThrow("date"))
                    )
                )
            }
        }
        return list
    }
}