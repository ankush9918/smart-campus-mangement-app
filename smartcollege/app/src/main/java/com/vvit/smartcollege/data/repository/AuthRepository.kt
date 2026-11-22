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
            if (!authResult.accessToken.isNullOrBlank()) {
                SupabaseApiClient.setAuthToken(authResult.accessToken)
            }
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
                UserRole.STUDENT -> "VVIT/CS/2021/045"
                UserRole.TEACHER -> "VVIT/CS/T-104"
                UserRole.ADMIN -> "EMP-ADM-001"
            }

            var user = User(
                id = authResult.userId ?: UUID.randomUUID().toString(),
                name = authResult.name?.ifBlank { null } ?: (if (finalRole == UserRole.STUDENT) "Student User" else "Faculty Member"),
                email = trimmedEmail,
                role = finalRole,
                identifier = identifier
            )

            // Immediately sync profile and attendance from Supabase upon login
            try {
                if (finalRole == UserRole.STUDENT) {
                    val remoteProfile = SupabaseApiClient.fetchStudentProfile(trimmedEmail, authResult.userId)
                    if (remoteProfile != null) {
                        user = user.copy(
                            name = remoteProfile.name,
                            identifier = remoteProfile.rollNo,
                            avatarUrl = remoteProfile.photoUrl
                        )
                        MockDatabase.studentProfile.value = remoteProfile
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
                    }
                }
            } catch (e: Exception) {
                // Keep local fallback if network error
            }

            sessionManager?.saveSession(user, authResult.accessToken)
            if (!authResult.accessToken.isNullOrBlank()) {
                SupabaseApiClient.setAuthToken(authResult.accessToken)
            }
            _currentUser.value = user
            MockDatabase.currentUser.value = user
            return@withContext Result.success(user)
        }

        // Return generic error message to prevent user enumeration
        val genericErrorMsg = "Invalid email or password. Please verify your credentials."
        val errorMsg = if (authResult.errorMessage?.contains("Invalid login credentials", ignoreCase = true) == true) {
            genericErrorMsg
        } else {
            authResult.errorMessage ?: genericErrorMsg
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
                Result.failure(Exception("Invalid email or password. Please verify your credentials."))
            }
        }

        // Fallback if DAO not available (e.g. unit tests without Android SQLite)
        val defaultUser = when (role) {
            UserRole.STUDENT -> User(id = "std_1", name = "Aditya Sharma", email = if (trimmedEmail.isNotBlank()) trimmedEmail else "student.vvit@college.edu", role = UserRole.STUDENT, identifier = "VVIT/CS/2021/045")
            UserRole.TEACHER -> User(id = "tch_1", name = "Dr. Rajesh Kumar", email = if (trimmedEmail.isNotBlank()) trimmedEmail else "teacher.vvit@college.edu", role = UserRole.TEACHER, identifier = "VVIT/CS/T-104")
            UserRole.ADMIN -> User(id = "adm_1", name = "Dr. Prakash Singh", email = if (trimmedEmail.isNotBlank()) trimmedEmail else "admin.vvit@college.edu", role = UserRole.ADMIN, identifier = "EMP-ADM-001")
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

            val success = authDao.registerUser(newUser, department, phone)
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
        sessionManager?.clearSession()
        SupabaseApiClient.setAuthToken(null)
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
        return Result.success(
            User(id = "std_1", name = "Aditya Sharma", email = trimmedEmail, role = UserRole.STUDENT, identifier = "VVIT/CS/2021/045")
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

    suspend fun requestResetOtpAsync(email: String, phone: String = ""): Result<String> = withContext(Dispatchers.IO) {
        val res = SupabaseApiClient.requestPasswordResetOtp(email, phone)
        if (res != null && res.optBoolean("success", false)) {
            val devOtp = res.optString("dev_otp", "")
            Result.success(devOtp)
        } else {
            val err = res?.optString("error", "Unable to request OTP. Please try again.") ?: "Network error."
            Result.failure(Exception(err))
        }
    }

    suspend fun verifyResetOtpAsync(email: String, otp: String): Result<String> = withContext(Dispatchers.IO) {
        val res = SupabaseApiClient.verifyPasswordResetOtp(email, otp)
        if (res != null && res.optBoolean("success", false)) {
            val token = res.optString("reset_token", "")
            Result.success(token)
        } else {
            val err = res?.optString("error", "Invalid or expired OTP.") ?: "Network error."
            Result.failure(Exception(err))
        }
    }

    suspend fun completeResetPasswordAsync(email: String, resetToken: String, newPassword: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val res = SupabaseApiClient.completePasswordReset(email, resetToken, newPassword)
        if (res != null && res.optBoolean("success", false)) {
            authDao?.updatePasswordByEmail(email, newPassword)
            Result.success(true)
        } else {
            val err = res?.optString("error", "Failed to update password.") ?: "Network error."
            Result.failure(Exception(err))
        }
    }
}
