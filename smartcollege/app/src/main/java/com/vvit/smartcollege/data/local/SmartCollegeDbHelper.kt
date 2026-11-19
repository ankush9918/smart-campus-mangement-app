package com.vvit.smartcollege.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SmartCollegeDbHelper(context: Context) : SQLiteOpenHelper(
    context,
    DbConstants.DATABASE_NAME,
    null,
    DbConstants.DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase) {
        // 1. Users table
        db.execSQL("""
            CREATE TABLE ${DbConstants.TABLE_USERS} (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL DEFAULT 'password123',
                role TEXT NOT NULL,
                identifier TEXT NOT NULL,
                avatarUrl TEXT
            )
        """.trimIndent())

        // 2. Student Profiles
        db.execSQL("""
            CREATE TABLE ${DbConstants.TABLE_STUDENT_PROFILES} (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                rollNo TEXT NOT NULL,
                email TEXT NOT NULL,
                phone TEXT NOT NULL,
                dateOfBirth TEXT NOT NULL,
                bloodGroup TEXT NOT NULL,
                department TEXT NOT NULL,
                currentSemester TEXT NOT NULL,
                section TEXT NOT NULL,
                admissionYear TEXT NOT NULL,
                guardianName TEXT NOT NULL,
                guardianRelationship TEXT NOT NULL,
                guardianContact TEXT NOT NULL,
                cgpa REAL NOT NULL,
                attendancePercentage INTEGER NOT NULL,
                photoUrl TEXT
            )
        """.trimIndent())

        // 3. Teacher Profiles
        db.execSQL("""
            CREATE TABLE ${DbConstants.TABLE_TEACHER_PROFILES} (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                employeeId TEXT NOT NULL,
                designation TEXT NOT NULL,
                department TEXT NOT NULL,
                email TEXT NOT NULL,
                phone TEXT NOT NULL,
                weeklyWorkloadHours INTEGER NOT NULL,
                activeSubjectsCount INTEGER NOT NULL,
                totalStudents INTEGER NOT NULL,
                classesToday INTEGER NOT NULL,
                pendingTasks INTEGER NOT NULL,
                photoUrl TEXT
            )
        """.trimIndent())

        // 4. Admin Profiles
        db.execSQL("""
            CREATE TABLE ${DbConstants.TABLE_ADMIN_PROFILES} (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                title TEXT NOT NULL,
                email TEXT NOT NULL,
                phone TEXT NOT NULL
            )
        """.trimIndent())

        // 5. Subject Attendance
        db.execSQL("""
            CREATE TABLE ${DbConstants.TABLE_SUBJECT_ATTENDANCE} (
                id TEXT PRIMARY KEY,
                subjectName TEXT NOT NULL,
                subjectCode TEXT NOT NULL,
                attendedClasses INTEGER NOT NULL,
                totalClasses INTEGER NOT NULL,
                percentage INTEGER NOT NULL,
                isAlert INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        // 6. Timetable
        db.execSQL("""
            CREATE TABLE ${DbConstants.TABLE_TIMETABLE} (
                id TEXT PRIMARY KEY,
                dayOfWeek TEXT NOT NULL,
                time TEXT NOT NULL,
                duration TEXT NOT NULL,
                subjectName TEXT NOT NULL,
                facultyName TEXT NOT NULL,
                room TEXT NOT NULL,
                isOngoing INTEGER NOT NULL DEFAULT 0,
                isBreak INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        // 7. Assignments
        db.execSQL("""
            CREATE TABLE ${DbConstants.TABLE_ASSIGNMENTS} (
                id TEXT PRIMARY KEY,
                subjectName TEXT NOT NULL,
                title TEXT NOT NULL,
                instructions TEXT NOT NULL,
                dueDate TEXT NOT NULL,
                status TEXT NOT NULL,
                submissionDate TEXT,
                maxMarks INTEGER NOT NULL DEFAULT 100,
                obtainedMarks INTEGER,
                submittedFileName TEXT,
                professorName TEXT,
                fileName TEXT,
                fileUri TEXT
            )
        """.trimIndent())

        // 8. Study Materials
        db.execSQL("""
            CREATE TABLE ${DbConstants.TABLE_STUDY_MATERIALS} (
                id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                subjectName TEXT NOT NULL,
                sharedBy TEXT NOT NULL,
                sharedDate TEXT NOT NULL,
                fileSize TEXT NOT NULL,
                fileType TEXT NOT NULL,
                downloadUrl TEXT NOT NULL,
                fileName TEXT,
                fileUri TEXT
            )
        """.trimIndent())

        // 9. Semester Results
        db.execSQL("""
            CREATE TABLE ${DbConstants.TABLE_SEMESTER_RESULTS} (
                semesterNumber INTEGER PRIMARY KEY,
                semesterName TEXT NOT NULL,
                sgpa REAL NOT NULL
            )
        """.trimIndent())

        // 10. Gradesheet items
        db.execSQL("""
            CREATE TABLE ${DbConstants.TABLE_GRADESHEET_ITEMS} (
                id TEXT PRIMARY KEY,
                semesterNumber INTEGER NOT NULL,
                subjectName TEXT NOT NULL,
                subjectCode TEXT NOT NULL,
                internalMarks INTEGER NOT NULL,
                externalMarks INTEGER NOT NULL,
                totalMarks INTEGER NOT NULL,
                grade TEXT NOT NULL
            )
        """.trimIndent())

        // 11. Fee Details
        db.execSQL("""
            CREATE TABLE ${DbConstants.TABLE_FEE_DETAILS} (
                id TEXT PRIMARY KEY,
                pendingAmount INTEGER NOT NULL,
                totalInvoiced INTEGER NOT NULL,
                paidTillDate INTEGER NOT NULL,
                currentSemesterFee INTEGER NOT NULL,
                semesterLabel TEXT NOT NULL
            )
        """.trimIndent())

        // 12. Fee Transactions
        db.execSQL("""
            CREATE TABLE ${DbConstants.TABLE_FEE_TRANSACTIONS} (
                id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                transactionId TEXT NOT NULL,
                date TEXT NOT NULL,
                amount INTEGER NOT NULL,
                status TEXT NOT NULL DEFAULT 'Success'
            )
        """.trimIndent())

        // 13. Notices
        db.execSQL("""
            CREATE TABLE ${DbConstants.TABLE_NOTICES} (
                id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                category TEXT NOT NULL,
                timeAgo TEXT NOT NULL,
                description TEXT NOT NULL,
                isUrgent INTEGER NOT NULL DEFAULT 0,
                date TEXT NOT NULL,
                publishedBy TEXT DEFAULT ''
            )
        """.trimIndent())

        // 14. Leave Applications
        db.execSQL("""
            CREATE TABLE ${DbConstants.TABLE_LEAVE_APPLICATIONS} (
                id TEXT PRIMARY KEY,
                studentName TEXT NOT NULL,
                rollNo TEXT NOT NULL,
                fromDate TEXT NOT NULL,
                toDate TEXT NOT NULL,
                durationDays INTEGER NOT NULL,
                reason TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'Pending',
                appliedDate TEXT NOT NULL
            )
        """.trimIndent())

        // 15. Teacher Schedule
        db.execSQL("""
            CREATE TABLE ${DbConstants.TABLE_TEACHER_SCHEDULE} (
                id TEXT PRIMARY KEY,
                time TEXT NOT NULL,
                subjectName TEXT NOT NULL,
                room TEXT NOT NULL,
                section TEXT NOT NULL,
                lectureType TEXT NOT NULL DEFAULT 'General Lecture',
                dayOfWeek TEXT NOT NULL DEFAULT 'Mon'
            )
        """.trimIndent())

        // 16. Attendance Roster
        db.execSQL("""
            CREATE TABLE ${DbConstants.TABLE_ATTENDANCE_ROSTER} (
                id TEXT PRIMARY KEY,
                rollNo TEXT NOT NULL,
                studentName TEXT NOT NULL,
                section TEXT NOT NULL,
                status TEXT NOT NULL,
                email TEXT,
                phone TEXT,
                department TEXT,
                currentSemester TEXT,
                attendancePercentage INTEGER DEFAULT 85,
                cgpa REAL DEFAULT 8.0,
                guardianName TEXT,
                guardianContact TEXT,
                photoUrl TEXT
            )
        """.trimIndent())

        // 17. At Risk Students
        db.execSQL("""
            CREATE TABLE ${DbConstants.TABLE_AT_RISK_STUDENTS} (
                id TEXT PRIMARY KEY,
                studentName TEXT NOT NULL,
                rollNo TEXT NOT NULL,
                reason TEXT NOT NULL,
                attendancePercent INTEGER NOT NULL,
                isReminded INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        // 18. Student Directory
        db.execSQL("""
            CREATE TABLE ${DbConstants.TABLE_STUDENT_DIRECTORY} (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                rollNo TEXT NOT NULL,
                department TEXT NOT NULL,
                semester TEXT NOT NULL,
                email TEXT NOT NULL,
                guardianName TEXT NOT NULL,
                guardianPhone TEXT NOT NULL,
                cgpa REAL NOT NULL,
                attendancePercent INTEGER NOT NULL,
                isActive INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent())

        // 19. Faculty Directory
        db.execSQL("""
            CREATE TABLE ${DbConstants.TABLE_FACULTY_DIRECTORY} (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                employeeId TEXT NOT NULL,
                designation TEXT NOT NULL,
                department TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'Active',
                assignedSubjects TEXT NOT NULL,
                weeklyWorkloadHours INTEGER NOT NULL
            )
        """.trimIndent())

        // 20. Departments
        db.execSQL("""
            CREATE TABLE ${DbConstants.TABLE_DEPARTMENTS} (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                hodName TEXT NOT NULL,
                facultyCount INTEGER NOT NULL,
                studentCount INTEGER NOT NULL,
                isActive INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent())

        // 21. Scheduled Exams
        db.execSQL("""
            CREATE TABLE ${DbConstants.TABLE_SCHEDULED_EXAMS} (
                id TEXT PRIMARY KEY,
                subjectName TEXT NOT NULL,
                subjectCode TEXT NOT NULL,
                examDate TEXT NOT NULL,
                timeDuration TEXT NOT NULL,
                venue TEXT NOT NULL
            )
        """.trimIndent())

        // 22. Library Books
        db.execSQL("""
            CREATE TABLE ${DbConstants.TABLE_LIBRARY_BOOKS} (
                id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                author TEXT NOT NULL,
                isbn TEXT NOT NULL,
                availableCopies INTEGER NOT NULL,
                totalCopies INTEGER NOT NULL
            )
        """.trimIndent())

        // 23. Issued Books
        db.execSQL("""
            CREATE TABLE ${DbConstants.TABLE_ISSUED_BOOKS} (
                id TEXT PRIMARY KEY,
                studentName TEXT NOT NULL,
                rollNo TEXT NOT NULL,
                bookTitle TEXT NOT NULL,
                dueDate TEXT NOT NULL,
                isOverdue INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        // 24. User Roles
        db.execSQL("""
            CREATE TABLE ${DbConstants.TABLE_USER_ROLES} (
                id TEXT PRIMARY KEY,
                roleName TEXT NOT NULL,
                memberCount INTEGER NOT NULL,
                permissions TEXT NOT NULL
            )
        """.trimIndent())

        // 25. Department Fees
        db.execSQL("""
            CREATE TABLE ${DbConstants.TABLE_DEPARTMENT_FEES} (
                departmentName TEXT PRIMARY KEY,
                collectedPercent INTEGER NOT NULL
            )
        """.trimIndent())

        // Seed initial records into SQLite
        DatabaseInitializer.seedDatabase(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE ${DbConstants.TABLE_USERS} ADD COLUMN password TEXT NOT NULL DEFAULT 'password123'")
            } catch (e: Exception) {
                // Column might already exist
            }
        }
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        try {
            db.execSQL("ALTER TABLE ${DbConstants.TABLE_USERS} ADD COLUMN password TEXT NOT NULL DEFAULT 'password123'")
        } catch (e: Exception) {
            // Ignored if column exists
        }
        try {
            db.execSQL("ALTER TABLE ${DbConstants.TABLE_STUDENT_PROFILES} ADD COLUMN photoUrl TEXT")
        } catch (e: Exception) {
            // Ignored if column exists
        }
        try {
            db.execSQL("ALTER TABLE ${DbConstants.TABLE_TEACHER_PROFILES} ADD COLUMN photoUrl TEXT")
        } catch (e: Exception) {
            // Ignored if column exists
        }
        val rosterColumns = listOf(
            "photoUrl TEXT",
            "email TEXT",
            "phone TEXT",
            "department TEXT",
            "currentSemester TEXT",
            "attendancePercentage INTEGER DEFAULT 85",
            "cgpa REAL DEFAULT 8.0",
            "guardianName TEXT",
            "guardianContact TEXT"
        )
        for (col in rosterColumns) {
            try {
                db.execSQL("ALTER TABLE ${DbConstants.TABLE_ATTENDANCE_ROSTER} ADD COLUMN $col")
            } catch (e: Exception) {
                // Column might already exist
            }
        }
        val asgColumns = listOf("professorName TEXT", "fileName TEXT", "fileUri TEXT")
        for (col in asgColumns) {
            try {
                db.execSQL("ALTER TABLE ${DbConstants.TABLE_ASSIGNMENTS} ADD COLUMN $col")
            } catch (e: Exception) {
                // Column might already exist
            }
        }
        val matColumns = listOf("fileName TEXT", "fileUri TEXT")
        for (col in matColumns) {
            try {
                db.execSQL("ALTER TABLE ${DbConstants.TABLE_STUDY_MATERIALS} ADD COLUMN $col")
            } catch (e: Exception) {
                // Column might already exist
            }
        }
        try {
            db.delete(DbConstants.TABLE_ASSIGNMENTS, "id IN ('asg_1', 'asg_2', 'asg_3')", null)
            db.delete(DbConstants.TABLE_STUDY_MATERIALS, "id IN ('mat_1', 'mat_2', 'mat_3')", null)
            db.delete(DbConstants.TABLE_NOTICES, "id IN ('not_1', 'not_2', 'not_3')", null)
        } catch (_: Exception) {}

        try {
            db.execSQL("ALTER TABLE ${DbConstants.TABLE_NOTICES} ADD COLUMN publishedBy TEXT DEFAULT ''")
        } catch (_: Exception) {}

        try {
            val cvNotice = android.content.ContentValues().apply {
                put("id", "not_teacher_cancel_1")
                put("title", "class cancel")
                put("category", "FACULTY COUNCIL")
                put("timeAgo", "Just now")
                put("description", "i am unable to take class so everyone goto library and study")
                put("isUrgent", 0)
                put("date", "Today")
                put("publishedBy", "Rishav Kumar Singh")
            }
            db.insertWithOnConflict(DbConstants.TABLE_NOTICES, null, cvNotice, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
        } catch (_: Exception) {}

        try {
            // Reset attendance data to clean 0-0 state
            val cvZero = android.content.ContentValues().apply {
                put("attendedClasses", 0)
                put("totalClasses", 0)
                put("percentage", 0)
                put("isAlert", 0)
            }
            db.update(DbConstants.TABLE_SUBJECT_ATTENDANCE, cvZero, null, null)

            val cvProfileZero = android.content.ContentValues().apply {
                put("attendancePercentage", 0)
            }
            db.update(DbConstants.TABLE_STUDENT_PROFILES, cvProfileZero, null, null)

            val cvRosterZero = android.content.ContentValues().apply {
                put("attendancePercentage", 0)
            }
            db.update(DbConstants.TABLE_ATTENDANCE_ROSTER, cvRosterZero, null, null)
            db.delete(DbConstants.TABLE_AT_RISK_STUDENTS, null, null)
        } catch (_: Exception) {}
    }
}