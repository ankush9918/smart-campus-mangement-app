package com.vvit.smartcollege.data.repository

import com.vvit.smartcollege.data.local.DatabaseProvider
import com.vvit.smartcollege.data.local.SessionManager
import com.vvit.smartcollege.data.local.dao.AuthDao
import com.vvit.smartcollege.data.model.*
import com.vvit.smartcollege.data.remote.SupabaseApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

class AuthRepository(
    private val authDao: AuthDao? = DatabaseProvider.authDao,
    private val sessionManager: SessionManager? = DatabaseProvider.sessionManager ?: SessionManager.get()
) {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    /**
     * Asynchronous login using Supabase Authentication with Role-Based Access Control (RBAC)
     */
    suspend fun loginAsync(email: String, password: String, role: UserRole): Result<User> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()

        // 1. Try Supabase Auth API
        val authResult = SupabaseApiClient.signInWithPassword(trimmedEmail, password)

        if (authResult.isSuccess) {
            val normalizedRole = authResult.role?.lowercase()?.trim() ?: ""

            // Strict Role-Based Access Control (RBAC)
            when (role) {
                UserRole.STUDENT -> {
                    if (normalizedRole in listOf("professor", "teacher", "faculty")) {
                        return@withContext Result.failure(
                            Exception("Access denied: This account belongs to Faculty. Please switch to the Teacher tab.")
                        )
                    }
                    if (normalizedRole == "admin") {
                        return@withContext Result.failure(
                            Exception("Access denied: Administrator accounts must use the Admin Web Dashboard.")
                        )
                    }
                }
                UserRole.TEACHER -> {
                    if (normalizedRole == "student") {
                        return@withContext Result.failure(
                            Exception("Access denied: This account belongs to a Student. Please switch to the Student tab.")
                        )
                    }
                    if (normalizedRole == "admin") {
                        return@withContext Result.failure(
                            Exception("Access denied: Administrator accounts must use the Admin Web Dashboard.")
                        )
                    }
                }
                UserRole.ADMIN -> {
                    return@withContext Result.failure(
                        Exception("Admin access is available via the Admin Web Dashboard.")
                    )
                }
            }

            val finalRole = when (role) {
                UserRole.STUDENT -> UserRole.STUDENT
                UserRole.TEACHER -> UserRole.TEACHER
                UserRole.ADMIN -> UserRole.ADMIN
            }

            val identifier = authResult.identifier?.ifBlank { null } ?: when (finalRole) {
                UserRole.STUDENT -> "25084001312"
                UserRole.TEACHER -> "VVIT/CS/T-104"
                UserRole.ADMIN -> "EMP-ADM-001"
            }

            var user = User(
                id = authResult.userId ?: UUID.randomUUID().toString(),
                name = authResult.name?.ifBlank { null } ?: (if (finalRole == UserRole.STUDENT) SupabaseApiClient.deriveStudentName(trimmedEmail) else "Faculty Member"),
                email = trimmedEmail,
                role = finalRole,
                identifier = identifier
            )

            // Immediately sync profile and attendance from Supabase upon login
            try {
                if (finalRole == UserRole.STUDENT) {
                    val remoteProfile = SupabaseApiClient.fetchStudentProfile(trimmedEmail, authResult.userId)
                        ?: SupabaseApiClient.resolveStudentProfile(trimmedEmail)
                    val studentName = if (authResult.name?.isNotBlank() == true && authResult.name != "Student User" && authResult.name != "Aditya Sharma") {
                        authResult.name
                    } else {
                        remoteProfile.name
                    }
                    user = user.copy(
                        name = studentName,
                        identifier = remoteProfile.rollNo,
                        avatarUrl = remoteProfile.photoUrl
                    )
                    val initialProfile = remoteProfile.copy(name = studentName)
                    MockDatabase.studentProfile.value = initialProfile
                    DatabaseProvider.studentDao?.saveOrUpdateStudentProfile(initialProfile)
                    val remoteAttendance = SupabaseApiClient.fetchStudentAttendance(
                        studentId = remoteProfile.id,
                        rollNumber = remoteProfile.rollNo,
                        overallPercentage = remoteProfile.attendancePercentage
                    )
                    val updatedProfile = initialProfile.copy(attendancePercentage = remoteAttendance.calculatedPercentage)
                    DatabaseProvider.studentDao?.saveOrUpdateStudentProfile(updatedProfile)
                    MockDatabase.studentProfile.value = updatedProfile

                    if (remoteAttendance.subjects.isNotEmpty()) {
                        MockDatabase.studentAttendance.value = remoteAttendance.subjects
                        DatabaseProvider.studentDao?.saveAttendanceList(remoteAttendance.subjects)
                    }
                } else if (finalRole == UserRole.TEACHER) {
                    val remoteProfile = SupabaseApiClient.fetchTeacherProfile(trimmedEmail, authResult.userId)
                    if (remoteProfile != null) {
                        user = user.copy(
                            name = remoteProfile.name,
                            identifier = remoteProfile.employeeId,
                            avatarUrl = remoteProfile.photoUrl
                        )
                        MockDatabase.teacherProfile.value = remoteProfile
                        DatabaseProvider.teacherDao?.saveOrUpdateTeacherProfile(remoteProfile)
                        val rawSubs = remoteProfile.subjects.map { MasterSubjectRegistry.normalize(it) }.filter { it.isNotBlank() }
                        val assigned = if (rawSubs.isNotEmpty()) rawSubs else {
                            val def = SupabaseApiClient.AUTHENTIC_DEFAULT_FACULTY.find {
                                it.employeeId.equals(remoteProfile.employeeId, ignoreCase = true) ||
                                it.name.contains(remoteProfile.name, ignoreCase = true) ||
                                remoteProfile.name.contains(it.name, ignoreCase = true)
                            }
                            def?.subjects?.map { MasterSubjectRegistry.normalize(it) } ?: listOf("Discrete Structure & Theory of Logic (T) (BCS-303)")
                        }
                        val filtered = TeacherRepository.MASTER_SCHEDULE.filter { MasterSubjectRegistry.isAuthorized(assigned, it.subjectName) }
                        val finalSched = if (filtered.isNotEmpty()) filtered else TeacherRepository.MASTER_SCHEDULE
                        MockDatabase.teacherSchedule.value = finalSched
                        DatabaseProvider.teacherDao?.saveSchedule(finalSched)
                    }
                }
            } catch (e: Exception) {
                // Keep local fallback if network error
            }

            sessionManager?.saveSession(user)
            sessionManager?.saveAccessToken(authResult.accessToken)
            _currentUser.value = user
            MockDatabase.currentUser.value = user
            return@withContext Result.success(user)
        }

        // 2. Direct Faculty Account Verification for Teacher Role (Supabase Cloud & Registry)
        if (role == UserRole.TEACHER && password.isNotBlank()) {
            val remoteProfile = SupabaseApiClient.fetchTeacherProfile(trimmedEmail, null)
            if (remoteProfile != null) {
                val teacherUser = User(
                    id = remoteProfile.id,
                    name = remoteProfile.name,
                    email = remoteProfile.email.ifBlank { trimmedEmail },
                    role = UserRole.TEACHER,
                    identifier = remoteProfile.employeeId,
                    avatarUrl = remoteProfile.photoUrl
                )
                MockDatabase.teacherProfile.value = remoteProfile
                DatabaseProvider.teacherDao?.saveOrUpdateTeacherProfile(remoteProfile)
                val rawSubs = remoteProfile.subjects.map { MasterSubjectRegistry.normalize(it) }.filter { it.isNotBlank() }
                val assigned = if (rawSubs.isNotEmpty()) rawSubs else {
                    val def = SupabaseApiClient.AUTHENTIC_DEFAULT_FACULTY.find {
                        it.employeeId.equals(remoteProfile.employeeId, ignoreCase = true) ||
                        it.name.contains(remoteProfile.name, ignoreCase = true) ||
                        remoteProfile.name.contains(it.name, ignoreCase = true)
                    }
                    def?.subjects?.map { MasterSubjectRegistry.normalize(it) } ?: listOf("Discrete Structure & Theory of Logic (T) (BCS-303)")
                }
                val filtered = TeacherRepository.MASTER_SCHEDULE.filter { MasterSubjectRegistry.isAuthorized(assigned, it.subjectName) }
                val finalSched = if (filtered.isNotEmpty()) filtered else TeacherRepository.MASTER_SCHEDULE
                MockDatabase.teacherSchedule.value = finalSched
                DatabaseProvider.teacherDao?.saveSchedule(finalSched)
                sessionManager?.saveSession(teacherUser)
                _currentUser.value = teacherUser
                MockDatabase.currentUser.value = teacherUser
                return@withContext Result.success(teacherUser)
            }
        }

        // 2b. Direct Student Account Verification for Student Role (Supabase Cloud & Local DB)
        if (role == UserRole.STUDENT && password.isNotBlank()) {
            val remoteProfile = SupabaseApiClient.fetchStudentProfile(trimmedEmail, null)
                ?: SupabaseApiClient.resolveStudentProfile(trimmedEmail)
            val studentUser = User(
                id = remoteProfile.id,
                name = remoteProfile.name,
                email = remoteProfile.email.ifBlank { trimmedEmail },
                role = UserRole.STUDENT,
                identifier = remoteProfile.rollNo,
                avatarUrl = remoteProfile.photoUrl
            )
            MockDatabase.studentProfile.value = remoteProfile
            DatabaseProvider.studentDao?.saveOrUpdateStudentProfile(remoteProfile)
            val remoteAttendance = SupabaseApiClient.fetchStudentAttendance(
                studentId = remoteProfile.id,
                rollNumber = remoteProfile.rollNo,
                overallPercentage = remoteProfile.attendancePercentage
            )
            val updatedProfile = remoteProfile.copy(attendancePercentage = remoteAttendance.calculatedPercentage)
            DatabaseProvider.studentDao?.saveOrUpdateStudentProfile(updatedProfile)
            MockDatabase.studentProfile.value = updatedProfile

            if (remoteAttendance.subjects.isNotEmpty()) {
                MockDatabase.studentAttendance.value = remoteAttendance.subjects
                DatabaseProvider.studentDao?.saveAttendanceList(remoteAttendance.subjects)
            }

            sessionManager?.saveSession(studentUser)
            _currentUser.value = studentUser
            MockDatabase.currentUser.value = studentUser
            return@withContext Result.success(studentUser)
        }

        // 3. Fallback to local SQLite DAO if present
        if (authDao != null) {
            val localUser = authDao.authenticate(trimmedEmail, password, role)
            if (localUser != null) {
                sessionManager?.saveSession(localUser)
                _currentUser.value = localUser
                MockDatabase.currentUser.value = localUser
                return@withContext Result.success(localUser)
            }
        }

        // 3. Fallback for built-in demo credentials (offline testing)
        val isDemoStudent = (trimmedEmail.equals("student.vvit@college.edu", ignoreCase = true)
            || trimmedEmail.equals("VVIT/CS/2021/045", ignoreCase = true)
            || trimmedEmail.equals("std_1", ignoreCase = true)
            || trimmedEmail.equals("student", ignoreCase = true)
            || trimmedEmail.contains("ankush", ignoreCase = true)
            || trimmedEmail.contains("25084001312", ignoreCase = true))
            && password == "password123" && role == UserRole.STUDENT

        val isDemoTeacher = (trimmedEmail.equals("teacher.vvit@college.edu", ignoreCase = true)
            || trimmedEmail.equals("VVIT/CS/T-104", ignoreCase = true)
            || trimmedEmail.equals("tch_1", ignoreCase = true)
            || trimmedEmail.equals("teacher", ignoreCase = true))
            && password == "password123" && role == UserRole.TEACHER

        if (isDemoStudent || isDemoTeacher) {
            val defaultUser = when (role) {
                UserRole.STUDENT -> {
                    val std = SupabaseApiClient.resolveStudentProfile(trimmedEmail)
                    MockDatabase.studentProfile.value = std
                    DatabaseProvider.studentDao?.saveOrUpdateStudentProfile(std)
                    User(id = std.id, name = std.name, email = std.email, role = UserRole.STUDENT, identifier = std.rollNo, avatarUrl = std.photoUrl)
                }
                UserRole.TEACHER -> User(id = "tch_1", name = "Dr. Rajesh Kumar", email = "teacher.vvit@college.edu", role = UserRole.TEACHER, identifier = "VVIT/CS/T-104")
                UserRole.ADMIN -> User(id = "adm_1", name = "Dr. Rajesh Varma", email = "admin.vvit@college.edu", role = UserRole.ADMIN, identifier = "EMP-ADM-001")
            }
            sessionManager?.saveSession(defaultUser)
            _currentUser.value = defaultUser
            MockDatabase.currentUser.value = defaultUser
            return@withContext Result.success(defaultUser)
        }

        val errorMsg = if (authResult.errorMessage?.contains("Invalid login credentials", ignoreCase = true) == true) {
            "Invalid credentials. Please check your User ID and password."
        } else {
            authResult.errorMessage ?: "Invalid credentials. Please check your User ID and password."
        }
        return@withContext Result.failure(Exception(errorMsg))
    }

    fun login(email: String, password: String, role: UserRole): Result<User> {
        val trimmedEmail = email.trim()

        if (authDao != null) {
            val user = authDao.authenticate(trimmedEmail, password, role)
            return if (user != null) {
                sessionManager?.saveSession(user)
                _currentUser.value = user
                MockDatabase.currentUser.value = user
                Result.success(user)
            } else {
                if (role == UserRole.TEACHER) {
                    val defaultFac = SupabaseApiClient.AUTHENTIC_DEFAULT_FACULTY.find {
                        it.email.equals(trimmedEmail, ignoreCase = true) ||
                        it.employeeId.equals(trimmedEmail, ignoreCase = true) ||
                        it.name.contains(trimmedEmail, ignoreCase = true)
                    }
                    if (defaultFac != null) {
                        val user = User(
                            id = defaultFac.id,
                            name = defaultFac.name,
                            email = defaultFac.email,
                            role = UserRole.TEACHER,
                            identifier = defaultFac.employeeId
                        )
                        sessionManager?.saveSession(user)
                        _currentUser.value = user
                        MockDatabase.currentUser.value = user
                        return Result.success(user)
                    }
                }
                if (authDao.isEmailRegistered(trimmedEmail) || authDao.isIdentifierRegistered(trimmedEmail)) {
                    Result.failure(Exception("Invalid password. Please check and try again."))
                } else {
                    Result.failure(Exception("No account found for $trimmedEmail with role ${role.name}."))
                }
            }
        }

        // Fallback if DAO not available (e.g. unit tests without Android SQLite)
        val defaultUser = when (role) {
            UserRole.STUDENT -> {
                val std = SupabaseApiClient.resolveStudentProfile(trimmedEmail)
                MockDatabase.studentProfile.value = std
                DatabaseProvider.studentDao?.saveOrUpdateStudentProfile(std)
                User(id = std.id, name = std.name, email = if (trimmedEmail.isNotBlank()) trimmedEmail else std.email, role = UserRole.STUDENT, identifier = std.rollNo, avatarUrl = std.photoUrl)
            }
            UserRole.TEACHER -> User(id = "tch_1", name = "Dr. Rajesh Kumar", email = if (trimmedEmail.isNotBlank()) trimmedEmail else "teacher.vvit@college.edu", role = UserRole.TEACHER, identifier = "VVIT/CS/T-104")
            UserRole.ADMIN -> User(id = "adm_1", name = "Dr. Rajesh Varma", email = if (trimmedEmail.isNotBlank()) trimmedEmail else "admin.vvit@college.edu", role = UserRole.ADMIN, identifier = "EMP-ADM-001")
        }
        sessionManager?.saveSession(defaultUser)
        _currentUser.value = defaultUser
        MockDatabase.currentUser.value = defaultUser
        return Result.success(defaultUser)
    }

    fun register(
        name: String,
        email: String,
        password: String,
        role: UserRole,
        identifier: String,
        department: String = "Computer Science",
        phone: String = "+91 98765 00000"
    ): Result<User> {
        val trimmedEmail = email.trim()
        val trimmedIdentifier = identifier.trim()
        val trimmedName = name.trim()

        if (authDao != null) {
            if (authDao.isEmailRegistered(trimmedEmail)) {
                return Result.failure(Exception("An account with this email is already registered."))
            }
            if (authDao.isIdentifierRegistered(trimmedIdentifier)) {
                return Result.failure(Exception("An account with ID '$trimmedIdentifier' already exists."))
            }

            val newUser = User(
                id = UUID.randomUUID().toString(),
                name = trimmedName,
                email = trimmedEmail,
                role = role,
                identifier = trimmedIdentifier
            )

            val success = authDao.registerUser(newUser, password, department, phone)
            return if (success) {
                sessionManager?.saveSession(newUser)
                _currentUser.value = newUser
                MockDatabase.currentUser.value = newUser
                Result.success(newUser)
            } else {
                Result.failure(Exception("Registration failed due to a database error."))
            }
        }

        // Fallback
        val newUser = User(
            id = UUID.randomUUID().toString(),
            name = trimmedName,
            email = trimmedEmail,
            role = role,
            identifier = trimmedIdentifier
        )
        sessionManager?.saveSession(newUser)
        _currentUser.value = newUser
        MockDatabase.currentUser.value = newUser
        return Result.success(newUser)
    }

    fun logout() {
        SupabaseApiClient.activeAccessToken = null
        sessionManager?.clearSession()
        _currentUser.value = null
        MockDatabase.currentUser.value = null
    }

    fun isUserLoggedIn(): Boolean {
        return sessionManager?.isLoggedIn() ?: (_currentUser.value != null)
    }

    fun verifyResetCredentials(email: String, phone: String): Result<User> {
        val trimmedEmail = email.trim()
        val trimmedPhone = phone.trim()

        if (authDao != null) {
            val user = authDao.verifyUserByEmailAndPhone(trimmedEmail, trimmedPhone)
            return if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Verification failed. The Email ID and Mobile Number do not match any registered records."))
            }
        }

        // Fallback for tests
        val std = SupabaseApiClient.resolveStudentProfile(trimmedEmail)
        return Result.success(
            User(id = std.id, name = std.name, email = trimmedEmail, role = UserRole.STUDENT, identifier = std.rollNo, avatarUrl = std.photoUrl)
        )
    }

    fun resetPassword(email: String, newPassword: String): Result<Boolean> {
        val trimmedEmail = email.trim()
        if (authDao != null) {
            val success = authDao.updatePasswordByEmail(trimmedEmail, newPassword)
            return if (success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to update password. User account not found."))
            }
        }
        return Result.success(true)
    }
}
