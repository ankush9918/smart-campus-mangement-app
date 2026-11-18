package com.vvit.smartcollege

import com.vvit.smartcollege.data.model.AssignmentStatus
import com.vvit.smartcollege.data.model.StudentAttendanceStatus
import com.vvit.smartcollege.data.model.UserRole
import com.vvit.smartcollege.data.repository.AdminRepository
import com.vvit.smartcollege.data.repository.AuthRepository
import com.vvit.smartcollege.data.repository.StudentRepository
import com.vvit.smartcollege.data.repository.TeacherRepository
import com.vvit.smartcollege.ui.auth.AuthViewModel
import org.junit.Assert.*
import org.junit.Test

class SmartCollegeUnitTest {

    @Test
    fun testAuthRoleSwitchingAndPermanentLogin() {
        val authRepo = AuthRepository()
        val studentResult = authRepo.login("student.vvit@college.edu", "password123", UserRole.STUDENT)
        assertTrue(studentResult.isSuccess)
        assertEquals(UserRole.STUDENT, authRepo.currentUser.value?.role)
        assertTrue(authRepo.isUserLoggedIn())

        val teacherResult = authRepo.login("teacher.vvit@college.edu", "password123", UserRole.TEACHER)
        assertTrue(teacherResult.isSuccess)
        assertEquals(UserRole.TEACHER, authRepo.currentUser.value?.role)

        val adminResult = authRepo.login("admin.vvit@college.edu", "password123", UserRole.ADMIN)
        assertTrue(adminResult.isSuccess)
        assertEquals(UserRole.ADMIN, authRepo.currentUser.value?.role)

        authRepo.logout()
        assertNull(authRepo.currentUser.value)
    }

    @Test
    fun testUserRegistration() {
        val authRepo = AuthRepository()
        val regResult = authRepo.register(
            name = "Priya Nair",
            email = "priya.nair@vvit.edu",
            password = "securePassword123",
            role = UserRole.STUDENT,
            identifier = "VVIT/CS/2026/099",
            department = "Computer Science & Eng.",
            phone = "+91 98888 77777"
        )
        assertTrue(regResult.isSuccess)
        val user = regResult.getOrNull()
        assertNotNull(user)
        assertEquals("Priya Nair", user?.name)
        assertEquals(UserRole.STUDENT, user?.role)
        assertEquals("VVIT/CS/2026/099", user?.identifier)
        assertEquals(user, authRepo.currentUser.value)
    }

    @Test
    fun testForgotPasswordVerificationAndReset() {
        val authRepo = AuthRepository()
        // Verify with email and mobile
        val verifyResult = authRepo.verifyResetCredentials("student.vvit@college.edu", "9876543210")
        assertTrue(verifyResult.isSuccess)
        assertNotNull(verifyResult.getOrNull())

        // Reset password
        val resetResult = authRepo.resetPassword("student.vvit@college.edu", "newSecurePassword456")
        assertTrue(resetResult.isSuccess)

        // Login with new password
        val loginResult = authRepo.login("student.vvit@college.edu", "newSecurePassword456", UserRole.STUDENT)
        assertTrue(loginResult.isSuccess)
    }

    @Test
    fun testOtpVerificationSecurityWithoutBypass() {
        val authRepo = AuthRepository()
        val viewModel = AuthViewModel(authRepo)

        viewModel.openForgotPassword()
        viewModel.onResetEmailChange("student.vvit@college.edu")
        viewModel.onResetPhoneChange("9876543210")
        viewModel.submitResetVerification()

        assertEquals(2, viewModel.forgotPasswordStep.value)
        val legitOtp = viewModel.generatedOtp.value
        assertEquals(6, legitOtp.length)

        // Attempting with wrong OTP (e.g. deprecated bypass "482910" if different from legitOtp)
        val wrongOtp = if (legitOtp == "482910") "123456" else "482910"
        viewModel.onResetOtpChange(wrongOtp)
        viewModel.verifyResetOtp()
        assertEquals(2, viewModel.forgotPasswordStep.value)
        assertNotNull(viewModel.resetErrorMessage.value)

        // Attempting with the correct generated OTP
        viewModel.onResetOtpChange(legitOtp)
        viewModel.verifyResetOtp()
        assertEquals(3, viewModel.forgotPasswordStep.value)
        assertNull(viewModel.resetErrorMessage.value)
    }

    @Test
    fun testStudentFeePayment() {
        val studentRepo = StudentRepository()
        val initialPending = studentRepo.feeDetails.value.pendingAmount
        val initialPaid = studentRepo.feeDetails.value.paidTillDate

        val payAmount = 10000L
        studentRepo.payOutstandingFee(payAmount)

        assertEquals(initialPending - payAmount, studentRepo.feeDetails.value.pendingAmount)
        assertEquals(initialPaid + payAmount, studentRepo.feeDetails.value.paidTillDate)
    }

    @Test
    fun testAssignmentSubmission() {
        val teacherRepo = TeacherRepository()
        teacherRepo.createAssignment(
            title = "Test Unit Assignment",
            subjectName = "Computer Networks",
            instructions = "Submit in PDF format",
            dueDate = "2026-10-15",
            maxMarks = 100
        )
        val studentRepo = StudentRepository()
        val assignment = studentRepo.assignments.value.first { it.title == "Test Unit Assignment" }
        studentRepo.submitAssignment(assignment.id, "Test_Submission.pdf")

        val updated = studentRepo.assignments.value.first { it.id == assignment.id }
        assertEquals(AssignmentStatus.SUBMITTED, updated.status)
        assertEquals("Test_Submission.pdf", updated.submittedFileName)
    }

    @Test
    fun testTeacherAttendanceOperationsAndReset() {
        val teacherRepo = TeacherRepository()

        // Test marking all present
        teacherRepo.markAllPresent()
        val allPresent = teacherRepo.attendanceRoster.value.all { it.status == StudentAttendanceStatus.PRESENT }
        assertTrue(allPresent)

        // Test individual update
        val firstStudent = teacherRepo.attendanceRoster.value.first()
        teacherRepo.updateStudentAttendance(firstStudent.id, StudentAttendanceStatus.ABSENT)
        val updatedStudent = teacherRepo.attendanceRoster.value.first { it.id == firstStudent.id }
        assertEquals(StudentAttendanceStatus.ABSENT, updatedStudent.status)

        // Test reset/clear after submission
        teacherRepo.resetAttendance()
        val allUnmarked = teacherRepo.attendanceRoster.value.all { it.status == StudentAttendanceStatus.UNMARKED }
        assertTrue(allUnmarked)
    }

    @Test
    fun testAdminStudentEnrollment() {
        val adminRepo = AdminRepository()
        val initialCount = adminRepo.studentDirectory.value.size

        adminRepo.addStudent(
            name = "Test Student",
            rollNo = "VVIT/TEST/2026/001",
            department = "Computer Science",
            semester = "Semester 1",
            email = "test@vvit.edu",
            guardian = "Parent",
            phone = "+91 99999 88888"
        )

        assertEquals(initialCount + 1, adminRepo.studentDirectory.value.size)
        val newStudent = adminRepo.studentDirectory.value.first()
        assertEquals("Test Student", newStudent.name)
    }

    @Test
    fun testEmailValidationLogic() {
        // Invalid emails from requirements
        assertFalse(AuthViewModel.isValidEmail("ankush"))
        assertFalse(AuthViewModel.isValidEmail("ankush@gmail"))
        assertFalse(AuthViewModel.isValidEmail("test123"))

        // Other invalid cases
        assertFalse(AuthViewModel.isValidEmail(""))
        assertFalse(AuthViewModel.isValidEmail("   "))
        assertFalse(AuthViewModel.isValidEmail("ankush@"))
        assertFalse(AuthViewModel.isValidEmail("@gmail.com"))
        assertFalse(AuthViewModel.isValidEmail("ankush@.com"))
        assertFalse(AuthViewModel.isValidEmail("ankush@gmail."))
        assertFalse(AuthViewModel.isValidEmail("ankush@gmail.c"))

        // Valid emails
        assertTrue(AuthViewModel.isValidEmail("ankush@gmail.com"))
        assertTrue(AuthViewModel.isValidEmail("student.vvit@college.edu"))
        assertTrue(AuthViewModel.isValidEmail("teacher.vvit@college.edu"))
        assertTrue(AuthViewModel.isValidEmail("admin.vvit@college.edu"))
        assertTrue(AuthViewModel.isValidEmail("user@sub.domain.co.in"))
    }

    @Test
    fun testLoginPreventsSubmissionForInvalidEmail() {
        val viewModel = AuthViewModel()
        var loginSucceeded = false

        // Test invalid email
        viewModel.onEmailChange("ankush@gmail")
        viewModel.onPasswordChange("password123")
        viewModel.login { loginSucceeded = true }

        assertFalse(loginSucceeded)
        assertEquals("Please enter a valid email address.", viewModel.errorMessage.value)

        // Test another invalid email
        viewModel.onEmailChange("test123")
        viewModel.login { loginSucceeded = true }

        assertFalse(loginSucceeded)
        assertEquals("Please enter a valid email address.", viewModel.errorMessage.value)
    }
}