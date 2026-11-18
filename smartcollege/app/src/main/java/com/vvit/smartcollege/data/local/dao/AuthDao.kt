package com.vvit.smartcollege.data.local.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.vvit.smartcollege.data.local.DbConstants
import com.vvit.smartcollege.data.model.User
import com.vvit.smartcollege.data.model.UserRole

class AuthDao(private val db: SQLiteDatabase) {

    /**
     * Passwords and password hashes are NEVER stored or verified in local SQLite.
     * All authentication is strictly delegated to Supabase Auth.
     */
    fun authenticate(emailOrIdentifier: String, password: String, role: UserRole): User? {
        return null
    }

    fun isEmailRegistered(email: String): Boolean {
        val cursor = db.query(
            DbConstants.TABLE_USERS,
            arrayOf("id"),
            "LOWER(email) = LOWER(?)",
            arrayOf(email.trim()),
            null, null, null
        )
        return cursor.use { it.count > 0 }
    }

    fun isIdentifierRegistered(identifier: String): Boolean {
        val cursor = db.query(
            DbConstants.TABLE_USERS,
            arrayOf("id"),
            "UPPER(identifier) = UPPER(?)",
            arrayOf(identifier.trim()),
            null, null, null
        )
        return cursor.use { it.count > 0 }
    }

    fun getUserByEmail(email: String): User? {
        val cursor = db.query(
            DbConstants.TABLE_USERS,
            null,
            "LOWER(email) = LOWER(?)",
            arrayOf(email.trim()),
            null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) {
                User(
                    id = it.getString(it.getColumnIndexOrThrow("id")),
                    name = it.getString(it.getColumnIndexOrThrow("name")),
                    email = it.getString(it.getColumnIndexOrThrow("email")),
                    role = UserRole.valueOf(it.getString(it.getColumnIndexOrThrow("role"))),
                    avatarUrl = it.getString(it.getColumnIndexOrThrow("avatarUrl")),
                    identifier = it.getString(it.getColumnIndexOrThrow("identifier"))
                )
            } else null
        }
    }

    fun getUserByRole(role: UserRole): User? {
        val cursor = db.query(
            DbConstants.TABLE_USERS,
            null,
            "role = ?",
            arrayOf(role.name),
            null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) {
                User(
                    id = it.getString(it.getColumnIndexOrThrow("id")),
                    name = it.getString(it.getColumnIndexOrThrow("name")),
                    email = it.getString(it.getColumnIndexOrThrow("email")),
                    role = UserRole.valueOf(it.getString(it.getColumnIndexOrThrow("role"))),
                    avatarUrl = it.getString(it.getColumnIndexOrThrow("avatarUrl")),
                    identifier = it.getString(it.getColumnIndexOrThrow("identifier"))
                )
            } else null
        }
    }

    fun insertOrUpdateUser(user: User) {
        val cv = ContentValues().apply {
            put("id", user.id)
            put("name", user.name)
            put("email", user.email)
            put("role", user.role.name)
            put("identifier", user.identifier)
            put("avatarUrl", user.avatarUrl)
        }
        db.insertWithOnConflict(DbConstants.TABLE_USERS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun registerUser(
        user: User,
        department: String = "Computer Science",
        phone: String = "+91 98765 00000"
    ): Boolean {
        db.beginTransaction()
        try {
            // 1. Insert into users table (Cached profile data only)
            val ucv = ContentValues().apply {
                put("id", user.id)
                put("name", user.name)
                put("email", user.email)
                put("role", user.role.name)
                put("identifier", user.identifier)
                put("avatarUrl", user.avatarUrl)
            }
            db.insertWithOnConflict(DbConstants.TABLE_USERS, null, ucv, SQLiteDatabase.CONFLICT_REPLACE)

            // 2. Insert into role-specific profile & directory tables
            when (user.role) {
                UserRole.STUDENT -> {
                    val sp = ContentValues().apply {
                        put("id", user.id)
                        put("name", user.name)
                        put("rollNo", user.identifier)
                        put("email", user.email)
                        put("phone", phone)
                        put("dateOfBirth", "01st Jan 2004")
                        put("bloodGroup", "B+ Positive")
                        put("department", department)
                        put("currentSemester", "Semester 1 (Year 1)")
                        put("section", "Section A")
                        put("admissionYear", "2026")
                        put("guardianName", "Guardian Name")
                        put("guardianRelationship", "Parent")
                        put("guardianContact", phone)
                        put("cgpa", 8.0)
                        put("attendancePercentage", 85)
                    }
                    db.insertWithOnConflict(DbConstants.TABLE_STUDENT_PROFILES, null, sp, SQLiteDatabase.CONFLICT_REPLACE)

                    val sd = ContentValues().apply {
                        put("id", "sdir_" + user.id)
                        put("name", user.name)
                        put("rollNo", user.identifier)
                        put("department", department)
                        put("semester", "Semester 1")
                        put("email", user.email)
                        put("guardianName", "Parent")
                        put("guardianPhone", phone)
                        put("cgpa", 8.0)
                        put("attendancePercent", 85)
                        put("isActive", 1)
                    }
                    db.insertWithOnConflict(DbConstants.TABLE_STUDENT_DIRECTORY, null, sd, SQLiteDatabase.CONFLICT_REPLACE)

                    val rst = ContentValues().apply {
                        put("id", "rst_" + user.id)
                        put("rollNo", user.identifier)
                        put("studentName", user.name)
                        put("section", "CS-Sec B")
                        put("status", "UNMARKED")
                    }
                    db.insertWithOnConflict(DbConstants.TABLE_ATTENDANCE_ROSTER, null, rst, SQLiteDatabase.CONFLICT_REPLACE)
                }
                UserRole.TEACHER -> {
                    val tp = ContentValues().apply {
                        put("id", user.id)
                        put("name", user.name)
                        put("employeeId", user.identifier)
                        put("designation", "Assistant Professor")
                        put("department", department)
                        put("email", user.email)
                        put("phone", phone)
                        put("weeklyWorkloadHours", 14)
                        put("activeSubjectsCount", 2)
                        put("totalStudents", 120)
                        put("classesToday", 3)
                        put("pendingTasks", 4)
                    }
                    db.insertWithOnConflict(DbConstants.TABLE_TEACHER_PROFILES, null, tp, SQLiteDatabase.CONFLICT_REPLACE)

                    val fd = ContentValues().apply {
                        put("id", "fdir_" + user.id)
                        put("name", user.name)
                        put("employeeId", user.identifier)
                        put("designation", "Assistant Professor")
                        put("department", department)
                        put("status", "Active")
                        put("assignedSubjects", department)
                        put("weeklyWorkloadHours", 14)
                    }
                    db.insertWithOnConflict(DbConstants.TABLE_FACULTY_DIRECTORY, null, fd, SQLiteDatabase.CONFLICT_REPLACE)
                }
                UserRole.ADMIN -> {
                    val ap = ContentValues().apply {
                        put("id", user.id)
                        put("name", user.name)
                        put("title", "Administrator")
                        put("email", user.email)
                        put("phone", phone)
                    }
                    db.insertWithOnConflict(DbConstants.TABLE_ADMIN_PROFILES, null, ap, SQLiteDatabase.CONFLICT_REPLACE)
                }
            }

            db.setTransactionSuccessful()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            db.endTransaction()
        }
    }

    fun verifyUserByEmailAndPhone(email: String, rawPhone: String): User? {
        val cleanEmail = email.trim().lowercase()
        val cleanDigits = rawPhone.replace(Regex("[^0-9]"), "")

        // 1. Check user by email
        val user = getUserByEmail(cleanEmail) ?: return null

        // 2. Check phone in respective profile table
        var foundPhone: String? = null
        when (user.role) {
            UserRole.STUDENT -> {
                val cursor = db.query(
                    DbConstants.TABLE_STUDENT_PROFILES,
                    arrayOf("phone"),
                    "id = ? OR LOWER(email) = LOWER(?)",
                    arrayOf(user.id, cleanEmail),
                    null, null, null
                )
                cursor.use { if (it.moveToFirst()) foundPhone = it.getString(0) }
            }
            UserRole.TEACHER -> {
                val cursor = db.query(
                    DbConstants.TABLE_TEACHER_PROFILES,
                    arrayOf("phone"),
                    "id = ? OR LOWER(email) = LOWER(?)",
                    arrayOf(user.id, cleanEmail),
                    null, null, null
                )
                cursor.use { if (it.moveToFirst()) foundPhone = it.getString(0) }
            }
            UserRole.ADMIN -> {
                val cursor = db.query(
                    DbConstants.TABLE_ADMIN_PROFILES,
                    arrayOf("phone"),
                    "id = ? OR LOWER(email) = LOWER(?)",
                    arrayOf(user.id, cleanEmail),
                    null, null, null
                )
                cursor.use { if (it.moveToFirst()) foundPhone = it.getString(0) }
            }
        }

        if (foundPhone != null) {
            val storedDigits = foundPhone!!.replace(Regex("[^0-9]"), "")
            if (storedDigits.endsWith(cleanDigits) || cleanDigits.endsWith(storedDigits) || storedDigits == cleanDigits) {
                return user
            }
        }

        return null
    }

    fun updatePasswordByEmail(email: String, newPassword: String): Boolean {
        // Passwords are exclusively managed by Supabase Auth; no local SQLite mutation
        return true
    }
}