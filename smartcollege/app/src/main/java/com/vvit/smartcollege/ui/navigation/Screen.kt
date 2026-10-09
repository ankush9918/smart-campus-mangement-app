package com.vvit.smartcollege.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object StudentRoot : Screen("student_root")
    object TeacherRoot : Screen("teacher_root")
    object AdminRoot : Screen("admin_root")

    // Student Sub-screens
    object StudentDashboard : Screen("student_dashboard")
    object StudentAttendance : Screen("student_attendance")
    object StudentTimetable : Screen("student_timetable")
    object StudentAcademics : Screen("student_academics")
    object StudentResults : Screen("student_results")
    object StudentNoticesFees : Screen("student_notices_fees")
    object StudentProfile : Screen("student_profile")

    // Teacher Sub-screens
    object TeacherDashboard : Screen("teacher_dashboard")
    object TeacherSchedule : Screen("teacher_schedule")
    object TeacherAttendanceDesk : Screen("teacher_attendance_desk")
    object TeacherAssignments : Screen("teacher_assignments")
    object TeacherAcademicsDesk : Screen("teacher_academics_desk")
    object TeacherPerformance : Screen("teacher_performance")
    object TeacherNoticesLeave : Screen("teacher_notices_leave")

    // Admin Sub-screens
    object AdminDashboard : Screen("admin_dashboard")
    object AdminStudentDirectory : Screen("admin_student_directory")
    object AdminFacultyDirectory : Screen("admin_faculty_directory")
    object AdminAcademicManagement : Screen("admin_academic_management")
    object AdminMonitoringReports : Screen("admin_monitoring_reports")
    object AdminExamsResults : Screen("admin_exams_results")
    object AdminNoticesFinance : Screen("admin_notices_finance")
    object AdminLibrary : Screen("admin_library")
    object AdminUserRoles : Screen("admin_user_roles")
    object AdminReportsInsights : Screen("admin_reports_insights")
    object AdminSettings : Screen("admin_settings")
}
