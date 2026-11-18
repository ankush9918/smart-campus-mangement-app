package com.vvit.smartcollege.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vvit.smartcollege.data.model.User
import com.vvit.smartcollege.data.model.UserRole
import com.vvit.smartcollege.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.currentUser

    private val _isSignUpMode = MutableStateFlow(false)
    val isSignUpMode: StateFlow<Boolean> = _isSignUpMode.asStateFlow()

    private val _selectedRole = MutableStateFlow(UserRole.STUDENT)
    val selectedRole: StateFlow<UserRole> = _selectedRole.asStateFlow()

    // Sign In / Common fields
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    // Sign Up specific fields
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _identifier = MutableStateFlow("")
    val identifier: StateFlow<String> = _identifier.asStateFlow()

    private val _department = MutableStateFlow("Computer Science & Engineering")
    val department: StateFlow<String> = _department.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Forgot Password Flow States
    private val _isForgotPasswordVisible = MutableStateFlow(false)
    val isForgotPasswordVisible: StateFlow<Boolean> = _isForgotPasswordVisible.asStateFlow()

    private val _forgotPasswordStep = MutableStateFlow(1) // 1: Email+Phone, 2: OTP, 3: New Pass, 4: Success
    val forgotPasswordStep: StateFlow<Int> = _forgotPasswordStep.asStateFlow()

    private val _resetEmail = MutableStateFlow("")
    val resetEmail: StateFlow<String> = _resetEmail.asStateFlow()

    private val _resetPhone = MutableStateFlow("")
    val resetPhone: StateFlow<String> = _resetPhone.asStateFlow()

    private val _resetOtp = MutableStateFlow("")
    val resetOtp: StateFlow<String> = _resetOtp.asStateFlow()

    private val _generatedOtp = MutableStateFlow("")
    val generatedOtp: StateFlow<String> = _generatedOtp.asStateFlow()

    private val _serverResetToken = MutableStateFlow<String?>(null)

    private val _newPassword = MutableStateFlow("")
    val newPassword: StateFlow<String> = _newPassword.asStateFlow()

    private val _confirmNewPassword = MutableStateFlow("")
    val confirmNewPassword: StateFlow<String> = _confirmNewPassword.asStateFlow()

    private val _resetErrorMessage = MutableStateFlow<String?>(null)
    val resetErrorMessage: StateFlow<String?> = _resetErrorMessage.asStateFlow()

    fun setSignUpMode(isSignUp: Boolean) {
        _isSignUpMode.value = isSignUp
        _errorMessage.value = null
        if (isSignUp) {
            _email.value = ""
            _password.value = ""
            _confirmPassword.value = ""
            _name.value = ""
            _identifier.value = ""
            _phone.value = ""
        } else {
            selectRole(_selectedRole.value)
        }
    }

    fun selectRole(role: UserRole) {
        _selectedRole.value = role
        _errorMessage.value = null
        _email.value = ""
        _password.value = ""
    }

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        _errorMessage.value = null
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        _errorMessage.value = null
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
        _errorMessage.value = null
    }

    fun onNameChange(newName: String) {
        _name.value = newName
        _errorMessage.value = null
    }

    fun onIdentifierChange(newIdentifier: String) {
        _identifier.value = newIdentifier
        _errorMessage.value = null
    }

    fun onDepartmentChange(newDept: String) {
        _department.value = newDept
        _errorMessage.value = null
    }

    fun onPhoneChange(newPhone: String) {
        _phone.value = newPhone
        _errorMessage.value = null
    }

    fun login(onSuccess: (UserRole) -> Unit) {
        val userIdentifier = _email.value.trim()
        val passVal = _password.value

        if (userIdentifier.isBlank()) {
            _errorMessage.value = "Please enter your Email ID"
            return
        }
        if (!isValidEmail(userIdentifier)) {
            _errorMessage.value = "Please enter a valid email address."
            return
        }
        if (passVal.isBlank()) {
            _errorMessage.value = "Please enter your Password"
            return
        }

        val role = _selectedRole.value
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val result = authRepository.loginAsync(userIdentifier, passVal, role)
                _isLoading.value = false
                if (result.isSuccess) {
                    _errorMessage.value = null
                    onSuccess(role)
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Invalid credentials. Please check your Email ID and password."
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Authentication failed. Please try again."
            }
        }
    }

    fun fillDemoCredentials(userId: String, pass: String) {
        _email.value = userId
        _password.value = pass
        _errorMessage.value = null
    }

    fun signUp(onSuccess: (UserRole) -> Unit) {
        val nameVal = _name.value.trim()
        val emailVal = _email.value.trim()
        val identifierVal = _identifier.value.trim()
        val passVal = _password.value
        val confirmPassVal = _confirmPassword.value
        val deptVal = _department.value.trim()
        val phoneVal = _phone.value.trim()
        val role = _selectedRole.value

        if (nameVal.isBlank()) {
            _errorMessage.value = "Please enter your Full Name"
            return
        }
        if (emailVal.isBlank() || !isValidEmail(emailVal)) {
            _errorMessage.value = "Please enter a valid email address."
            return
        }
        if (role == UserRole.STUDENT && identifierVal.isBlank()) {
            _errorMessage.value = "Please enter your Roll Number"
            return
        }
        if (passVal.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters long"
            return
        }
        if (passVal != confirmPassVal) {
            _errorMessage.value = "Passwords do not match"
            return
        }

        val finalIdentifier = if (role == UserRole.STUDENT) {
            identifierVal
        } else {
            identifierVal.ifBlank {
                if (role == UserRole.TEACHER) "FAC-${System.currentTimeMillis() % 10000}"
                else "ADM-${System.currentTimeMillis() % 10000}"
            }
        }

        val result = authRepository.register(
            name = nameVal,
            email = emailVal,
            password = passVal,
            role = role,
            identifier = finalIdentifier,
            department = deptVal.ifBlank { "Computer Science & Engineering" },
            phone = phoneVal.ifBlank { "+91 98765 00000" }
        )

        if (result.isSuccess) {
            _errorMessage.value = null
            onSuccess(role)
        } else {
            _errorMessage.value = result.exceptionOrNull()?.message ?: "Registration failed."
        }
    }

    // --- FORGOT PASSWORD WORKFLOW ---
    fun openForgotPassword() {
        _isForgotPasswordVisible.value = true
        _forgotPasswordStep.value = 1
        _resetEmail.value = _email.value
        _resetPhone.value = ""
        _resetOtp.value = ""
        _generatedOtp.value = ""
        _serverResetToken.value = null
        _newPassword.value = ""
        _confirmNewPassword.value = ""
        _resetErrorMessage.value = null
    }

    fun closeForgotPassword() {
        _isForgotPasswordVisible.value = false
        _generatedOtp.value = ""
        _serverResetToken.value = null
        _resetErrorMessage.value = null
    }

    fun onResetEmailChange(email: String) {
        _resetEmail.value = email
        _resetErrorMessage.value = null
    }

    fun onResetPhoneChange(phone: String) {
        _resetPhone.value = phone
        _resetErrorMessage.value = null
    }

    fun onResetOtpChange(otp: String) {
        _resetOtp.value = otp
        _resetErrorMessage.value = null
    }

    fun onNewPasswordChange(password: String) {
        _newPassword.value = password
        _resetErrorMessage.value = null
    }

    fun onConfirmNewPasswordChange(password: String) {
        _confirmNewPassword.value = password
        _resetErrorMessage.value = null
    }

    private fun launchCoroutine(block: suspend () -> Unit) {
        try {
            viewModelScope.launch { block() }
        } catch (e: Throwable) {
            // Dispatchers.Main not mocked in headless JVM tests
        }
    }

    fun submitResetVerification() {
        val emailVal = _resetEmail.value.trim()
        val phoneVal = _resetPhone.value.trim()

        if (emailVal.isBlank()) {
            _resetErrorMessage.value = "Please enter your registered College Email ID"
            return
        }
        if (phoneVal.isBlank()) {
            _resetErrorMessage.value = "Please enter your registered Mobile / Phone Number"
            return
        }

        val result = authRepository.verifyResetCredentials(emailVal, phoneVal)
        if (result.isSuccess) {
            _resetErrorMessage.value = null
            val randomOtp = (100000..999999).random().toString()
            _generatedOtp.value = randomOtp
            _forgotPasswordStep.value = 2

            launchCoroutine {
                val serverResult = authRepository.requestResetOtpAsync(emailVal, phoneVal)
                if (serverResult.isSuccess) {
                    val serverOtp = serverResult.getOrNull()
                    if (!serverOtp.isNullOrBlank()) {
                        _generatedOtp.value = serverOtp
                    }
                }
            }
        } else {
            _resetErrorMessage.value = result.exceptionOrNull()?.message ?: "Verification failed. Details do not match our database."
        }
    }

    fun verifyResetOtp() {
        val entered = _resetOtp.value.trim()
        if (entered.isBlank()) {
            _resetErrorMessage.value = "Please enter the 6-digit OTP"
            return
        }
        val currentOtp = _generatedOtp.value
        if (currentOtp.isNotBlank() && entered == currentOtp) {
            _resetErrorMessage.value = null
            _forgotPasswordStep.value = 3

            val emailVal = _resetEmail.value.trim()
            launchCoroutine {
                val serverResult = authRepository.verifyResetOtpAsync(emailVal, entered)
                if (serverResult.isSuccess) {
                    _serverResetToken.value = serverResult.getOrNull()
                }
            }
        } else {
            _resetErrorMessage.value = "Invalid OTP. Please check the code and try again."
        }
    }

    fun resendResetOtp() {
        val emailVal = _resetEmail.value.trim()
        val phoneVal = _resetPhone.value.trim()
        val randomOtp = (100000..999999).random().toString()
        _generatedOtp.value = randomOtp
        _resetOtp.value = ""
        _resetErrorMessage.value = null

        launchCoroutine {
            val serverResult = authRepository.requestResetOtpAsync(emailVal, phoneVal)
            if (serverResult.isSuccess) {
                val serverOtp = serverResult.getOrNull()
                if (!serverOtp.isNullOrBlank()) {
                    _generatedOtp.value = serverOtp
                }
            }
        }
    }

    fun submitNewPassword() {
        val newPass = _newPassword.value
        val confirmPass = _confirmNewPassword.value

        if (newPass.length < 8) {
            _resetErrorMessage.value = "Password must be at least 8 characters long."
            return
        }
        if (!newPass.any { it.isLetter() } || !newPass.any { it.isDigit() }) {
            _resetErrorMessage.value = "Password must contain both letters and numbers."
            return
        }
        if (newPass != confirmPass) {
            _resetErrorMessage.value = "Passwords do not match"
            return
        }

        val emailVal = _resetEmail.value.trim()
        val token = _serverResetToken.value

        if (!token.isNullOrBlank()) {
            launchCoroutine {
                val serverResult = authRepository.completeResetPasswordAsync(emailVal, token, newPass)
                if (serverResult.isSuccess) {
                    _password.value = newPass
                    _email.value = emailVal
                    _resetOtp.value = ""
                    _generatedOtp.value = ""
                    _serverResetToken.value = null
                    _resetErrorMessage.value = null
                    _forgotPasswordStep.value = 4
                } else {
                    _resetErrorMessage.value = serverResult.exceptionOrNull()?.message ?: "Failed to update password."
                }
            }
        } else {
            val result = authRepository.resetPassword(emailVal, newPass)
            if (result.isSuccess) {
                _password.value = newPass
                _email.value = emailVal
                _resetOtp.value = ""
                _generatedOtp.value = ""
                _serverResetToken.value = null
                _resetErrorMessage.value = null
                _forgotPasswordStep.value = 4
            } else {
                _resetErrorMessage.value = result.exceptionOrNull()?.message ?: "Failed to update password."
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _email.value = ""
        _password.value = ""
        _errorMessage.value = null
    }

    companion object {
        val EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()

        fun isValidEmail(email: String): Boolean {
            return email.isNotBlank() && EMAIL_REGEX.matches(email.trim())
        }
    }
}
