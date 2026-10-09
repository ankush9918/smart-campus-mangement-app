package com.vvit.smartcollege.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

object DatabaseInitializer {

    fun seedDatabase(db: SQLiteDatabase) {
        // 1. Users
        insertUser(db, "std_1", "Aditya Sharma", "student.vvit@college.edu", "STUDENT", "VVIT/CS/2021/045")
        insertUser(db, "tch_1", "Dr. Rajesh Kumar", "teacher.vvit@college.edu", "TEACHER", "VVIT/CS/T-104")
        insertUser(db, "adm_1", "Dr. Rajesh Varma", "admin.vvit@college.edu", "ADMIN", "EMP-ADM-001")

        // 2. Student Profile
        val sp = ContentValues().apply {
            put("id", "std_1")
            put("name", "Aditya Sharma")
            put("rollNo", "VVIT/CS/2021/045")
            put("email", "aditya.sharma@vvit.edu")
            put("phone", "+91 98765 43210")
            put("dateOfBirth", "14th August 2003")
            put("bloodGroup", "O+ Positive")
            put("department", "Computer Science & Eng.")
            put("currentSemester", "Semester 5 (Year 3)")
            put("section", "Section B")
            put("admissionYear", "2021")
            put("guardianName", "Ramesh Sharma")
            put("guardianRelationship", "Father")
            put("guardianContact", "+91 98111 22233")
            put("cgpa", 8.41)
            put("attendancePercentage", 87)
        }
        db.insert(DbConstants.TABLE_STUDENT_PROFILES, null, sp)

        // 3. Teacher Profile
        val tp = ContentValues().apply {
            put("id", "tch_1")
            put("name", "Dr. Rajesh Kumar")
            put("employeeId", "VVIT/CS/T-104")
            put("designation", "Associate Professor")
            put("department", "Information Technology")
            put("email", "rajesh.kumar@vvit.edu")
            put("phone", "+91 98123 45678")
            put("weeklyWorkloadHours", 16)
            put("activeSubjectsCount", 3)
            put("totalStudents", 248)
            put("classesToday", 3)
            put("pendingTasks", 12)
        }
        db.insert(DbConstants.TABLE_TEACHER_PROFILES, null, tp)

        // 4. Admin Profile
        val ap = ContentValues().apply {
            put("id", "adm_1")
            put("name", "Dr. Rajesh Varma")
            put("title", "System Administrator")
            put("email", "admin.varma@vvit.edu")
            put("phone", "+91 98999 11223")
        }
        db.insert(DbConstants.TABLE_ADMIN_PROFILES, null, ap)

        // 5. Subject Attendance (Official IT 3rd Sem Theory Subjects - No Labs)
        insertAttendance(db, "att_1", "Data Structure", "BCS-301", 36, 42, 86, 0)
        insertAttendance(db, "att_2", "Computer Organization & Architecture", "BCS-302", 35, 40, 88, 0)
        insertAttendance(db, "att_3", "Discrete Structure & Theory of Logic", "BCS-303", 31, 38, 82, 0)
        insertAttendance(db, "att_4", "Digital Electronics", "BOE-310", 31, 36, 86, 0)
        insertAttendance(db, "att_5", "Technical Communication", "BAS-301", 27, 32, 84, 0)
        insertAttendance(db, "att_6", "Python Programming", "BCC-302", 31, 35, 89, 0)


        // 6. Timetable (IT - 3rd Sem)
        // Monday
        insertTimetable(db, "tt_m1", "Mon", "10:00 AM", "50 mins", "Python Programming (BCC-302)", "Dharamraj Yadav (DRY)", "LH-1", 0, 0)
        insertTimetable(db, "tt_m2", "Mon", "10:50 AM", "50 mins", "Data Structure (BCS-301)", "Kalpana Suman (KS)", "LH-1", 0, 0)
        insertTimetable(db, "tt_m3", "Mon", "11:40 AM", "50 mins", "Digital Electronics (BOE-310)", "Anant Sikarwar (AS)", "LH-1", 0, 0)
        insertTimetable(db, "tt_m4", "Mon", "12:30 PM", "50 mins", "Python Programming (BCC-302)", "Dharamraj Yadav (DRY)", "LH-1", 0, 0)
        insertTimetable(db, "tt_ml", "Mon", "01:20 PM", "1 hour", "Lunch Break", "Campus Cafeteria", "Cafeteria", 0, 1)
        insertTimetable(db, "tt_m5", "Mon", "02:20 PM", "50 mins", "Computer Organization & Architecture (BCS-302)", "Prakash Pandey (PP)", "LH-1", 0, 0)
        insertTimetable(db, "tt_m6", "Mon", "03:10 PM", "50 mins", "Digital Electronics (BOE-310)", "Anant Sikarwar (AS)", "LH-1", 0, 0)
        insertTimetable(db, "tt_m7", "Mon", "04:00 PM", "50 mins", "Discrete Structure & Theory of Logic (BCS-303) [T]", "Rishav Kumar Singh (RKS)", "LH-1", 0, 0)
        // Tuesday
        insertTimetable(db, "tt_t1", "Tue", "10:00 AM", "50 mins", "Technical Communication (BAS-301)", "Dr. Pinki Negi Bora (PNB)", "LH-1", 0, 0)
        insertTimetable(db, "tt_t2", "Tue", "10:50 AM", "50 mins", "Discrete Structure & Theory of Logic (BCS-303)", "Rishav Kumar Singh (RKS)", "LH-1", 0, 0)
        insertTimetable(db, "tt_t3", "Tue", "11:40 AM", "50 mins", "Computer Organization & Architecture (BCS-302)", "Prakash Pandey (PP)", "LH-1", 0, 0)
        insertTimetable(db, "tt_t4", "Tue", "12:30 PM", "50 mins", "Data Structure (BCS-301)", "Kalpana Suman (KS)", "LH-1", 0, 0)
        insertTimetable(db, "tt_tl", "Tue", "01:20 PM", "1 hour", "Lunch Break", "Campus Cafeteria", "Cafeteria", 0, 1)
        insertTimetable(db, "tt_t5", "Tue", "02:20 PM", "50 mins", "Digital Electronics (BOE-310)", "Anant Sikarwar (AS)", "LH-1", 0, 0)
        insertTimetable(db, "tt_t6", "Tue", "03:10 PM", "1 hr 40 mins", "COA Lab G1 (BCS-352)", "Kalpana Suman (KS) / Dr. Prakash Kumar Singh (PKS)", "COA Lab", 0, 0)
        // Wednesday
        insertTimetable(db, "tt_w1", "Wed", "10:00 AM", "50 mins", "Technical Communication (BAS-301)", "Dr. Pinki Negi Bora (PNB)", "LH-1", 0, 0)
        insertTimetable(db, "tt_w2", "Wed", "10:50 AM", "1 hr 40 mins", "COA Lab G2 (BCS-352)", "Akash Garg (AG) / Dharamraj Yadav (DRY)", "COA Lab", 0, 0)
        insertTimetable(db, "tt_w4", "Wed", "12:30 PM", "50 mins", "Data Structure (BCS-301)", "Kalpana Suman (KS)", "LH-1", 0, 0)
        insertTimetable(db, "tt_wl", "Wed", "01:20 PM", "1 hour", "Lunch Break", "Campus Cafeteria", "Cafeteria", 0, 1)
        insertTimetable(db, "tt_w5", "Wed", "02:20 PM", "1 hr 40 mins", "Web Designing Workshop G2 (BCS-353)", "Akash Garg (AG) / Dr. Prakash Kumar Singh (PKS)", "Web Tech Lab", 0, 0)
        insertTimetable(db, "tt_w7", "Wed", "04:00 PM", "50 mins", "Computer Organization & Architecture (BCS-302)", "Prakash Pandey (PP)", "LH-1", 0, 0)
        insertTimetable(db, "tt_w8", "Wed", "04:50 PM", "50 mins", "Sports & Yoga", "Sports Department", "Sports Ground", 0, 0)
        // Thursday
        insertTimetable(db, "tt_th1", "Thu", "10:00 AM", "50 mins", "Technical Communication (BAS-301)", "Dr. Pinki Negi Bora (PNB)", "LH-1", 0, 0)
        insertTimetable(db, "tt_th2", "Thu", "10:50 AM", "50 mins", "Discrete Structure & Theory of Logic (BCS-303)", "Rishav Kumar Singh (RKS)", "LH-1", 0, 0)
        insertTimetable(db, "tt_th3", "Thu", "11:40 AM", "1 hr 40 mins", "Web Designing Workshop G1 (BCS-353)", "Kalpana Suman (KS) / Anant Sikarwar (AS)", "Web Tech Lab", 0, 0)
        insertTimetable(db, "tt_thl", "Thu", "01:20 PM", "1 hour", "Lunch Break", "Campus Cafeteria", "Cafeteria", 0, 1)
        insertTimetable(db, "tt_th5", "Thu", "02:20 PM", "1 hr 40 mins", "Data Structure Lab G1 (BCS-351)", "Kalpana Suman (KS) / Dr. Prakash Kumar Singh (PKS)", "DS Lab", 0, 0)
        insertTimetable(db, "tt_th7", "Thu", "04:00 PM", "50 mins", "Computer Organization & Architecture (BCS-302)", "Prakash Pandey (PP)", "LH-1", 0, 0)
        insertTimetable(db, "tt_th8", "Thu", "04:50 PM", "50 mins", "Sports & Yoga", "Sports Department", "Sports Ground", 0, 0)
        // Friday
        insertTimetable(db, "tt_f1", "Fri", "10:00 AM", "50 mins", "Data Structure (BCS-301)", "Kalpana Suman (KS)", "LH-1", 0, 0)
        insertTimetable(db, "tt_f2", "Fri", "10:50 AM", "1 hr 40 mins", "Internship Assessment / Mini Project (BCC-351)", "Dr. Prakash Kumar Singh (PKS)", "Project Lab", 0, 0)
        insertTimetable(db, "tt_f4", "Fri", "12:30 PM", "50 mins", "Digital Electronics (BOE-310) [Tutorial]", "Anant Sikarwar (AS)", "LH-1", 0, 0)
        insertTimetable(db, "tt_fl", "Fri", "01:20 PM", "1 hour", "Lunch Break", "Campus Cafeteria", "Cafeteria", 0, 1)
        insertTimetable(db, "tt_f5", "Fri", "02:20 PM", "1 hr 40 mins", "Data Structure Lab G2 (BCS-351)", "Kalpana Suman (KS) / Anant Sikarwar (AS)", "DS Lab", 0, 0)
        insertTimetable(db, "tt_f7", "Fri", "04:00 PM", "50 mins", "Library / Self-Study", "Librarian", "Central Library", 0, 0)

        // 7. Assignments (No dummy assignments - start clean)
        db.delete(DbConstants.TABLE_ASSIGNMENTS, "id IN ('asg_1', 'asg_2', 'asg_3')", null)

        // 8. Study Materials (No dummy materials - start clean)
        db.delete(DbConstants.TABLE_STUDY_MATERIALS, "id IN ('mat_1', 'mat_2', 'mat_3')", null)

        // 9. Results & Gradesheets
        insertSemesterResult(db, 4, "Semester 4", 8.65)
        insertGradeItem(db, "grd_41", 4, "Discrete Mathematics", "CS-201", 28, 54, 82, "A+")
        insertGradeItem(db, "grd_42", 4, "Microprocessors", "CS-202", 26, 62, 88, "O")
        insertGradeItem(db, "grd_43", 4, "Operating Systems", "CS-203", 22, 48, 70, "B+")
        insertGradeItem(db, "grd_44", 4, "Data Structures Lab", "CS-204", 45, 47, 92, "O")
        insertGradeItem(db, "grd_45", 4, "Formal Languages", "CS-205", 25, 50, 75, "A")

        insertSemesterResult(db, 3, "Semester 3", 8.35)
        insertGradeItem(db, "grd_31", 3, "Object Oriented Prog", "CS-105", 27, 52, 79, "A")
        insertGradeItem(db, "grd_32", 3, "Digital Electronics", "EC-102", 24, 50, 74, "B+")
        insertGradeItem(db, "grd_33", 3, "Mathematics III", "MA-201", 29, 60, 89, "O")

        insertSemesterResult(db, 2, "Semester 2", 8.20)
        insertGradeItem(db, "grd_21", 2, "Basic Electronics", "EC-101", 23, 49, 72, "B+")
        insertGradeItem(db, "grd_22", 2, "Engineering Chemistry", "CH-101", 26, 56, 82, "A+")

        insertSemesterResult(db, 1, "Semester 1", 8.45)
        insertGradeItem(db, "grd_11", 1, "Engineering Physics", "PH-101", 28, 58, 86, "A+")
        insertGradeItem(db, "grd_12", 1, "Problem Solving in C", "CS-101", 29, 61, 90, "O")

        // 10. Fees
        val fd = ContentValues().apply {
            put("id", "fee_std_1")
            put("pendingAmount", 45000L)
            put("totalInvoiced", 125000L)
            put("paidTillDate", 80000L)
            put("currentSemesterFee", 45000L)
            put("semesterLabel", "Semester 5 Tuition Fee")
        }
        db.insert(DbConstants.TABLE_FEE_DETAILS, null, fd)

        insertFeeTransaction(db, "txn_1", "Semester 4 Tuition Fee", "TXN-VVIT98273", "14 Jul 2024", 45000L, "Success")
        insertFeeTransaction(db, "txn_2", "Annual Exam & Lab Fee", "TXN-VVIT98112", "02 Feb 2024", 15000L, "Success")
        insertFeeTransaction(db, "txn_3", "Semester 3 Tuition Fee", "TXN-VVIT87123", "10 Jul 2023", 40000L, "Success")

        // 11. Notices - no dummy data; genuine notices populated dynamically from Supabase or Teacher broadcast
        try {
            db.delete(DbConstants.TABLE_NOTICES, "id IN ('not_1', 'not_2', 'not_3')", null)
        } catch (_: Exception) {}

        // 12. Leaves
        insertLeave(db, "lv_1", "Aditya Sharma", "VVIT/CS/2021/045", "Oct 16, 2026", "Oct 18, 2026", 3, "Attending Sister's Wedding. Invitation attached.", "Pending", "12 Oct 2026")
        insertLeave(db, "lv_2", "Chaitanya Jha", "VVIT/CS/2021/022", "Oct 15, 2026", "Oct 15, 2026", 1, "Medical Appointment. Prescriptions uploaded.", "Pending", "13 Oct 2026")

        // 13. Teacher Schedule
        insertTeacherSchedule(db, "tsch_1", "09:00 AM", "Data Structures & Algorithms", "CS-Lab 1", "Semester 3 (Sec-A)", "General Lecture", "Mon")
        insertTeacherSchedule(db, "tsch_2", "11:15 AM", "Object Oriented Programming", "Room 402", "Semester 3 (Sec-A)", "General Lecture", "Mon")
        insertTeacherSchedule(db, "tsch_3", "02:00 PM", "Discrete Mathematics", "Room 105", "Semester 3 (Sec-A)", "General Lecture", "Mon")
        insertTeacherSchedule(db, "tsch_4", "03:15 PM", "Project Review Seminar", "CS Seminar Hall", "Semester 7 (Sec-B)", "General Lecture", "Mon")

        // 14. Attendance Roster (Actual registered students from Supabase)
        insertRoster(db, "std_1791009612955_3hhbwb", "25084001312", "ankush", "IT-Sec A", "UNMARKED", "ankush251312@recmainpuri.in", "+91 98765 43210", "Information Technology", "Semester 3 (Year 2)", 78, 7.3, "Guardian / Parent", "+91 98765 43210", "https://easrebumarwkloibtemf.supabase.co/storage/v1/object/public/student-photos/std_1791009612955_3hhbwb.jpg")
        insertRoster(db, "std_1791018184263_gq8k83", "2508400130011", "ankur", "IT-Sec A", "UNMARKED", "ankur251311@recmainpuri.in", "+91 98765 43211", "Information Technology", "Semester 3 (Year 2)", 70, 6.9, "Guardian / Parent", "+91 98765 43211", "https://easrebumarwkloibtemf.supabase.co/storage/v1/object/public/student-photos/std_1791018184263_gq8k83.jpg")
        insertRoster(db, "std_1791010841660_9fx4k9", "2508400130004", "ajay", "IT-Sec A", "UNMARKED", "ajay251304@recmainpuri.in", "+91 98765 43212", "Information Technology", "Semester 3 (Year 2)", 73, 8.0, "Guardian / Parent", "+91 98765 43212", "https://easrebumarwkloibtemf.supabase.co/storage/v1/object/public/student-photos/std_1791010841660_9fx4k9.jpg")
        insertRoster(db, "std_1791280766460_uwebco", "2508400130015", "anuj", "IT-Sec A", "UNMARKED", "anuj251215@recmainpuri.in", "+91 98765 43213", "Information Technology", "Semester 3 (Year 2)", 80, 8.0, "Guardian / Parent", "+91 98765 43213", "https://easrebumarwkloibtemf.supabase.co/storage/v1/object/public/student-photos/std_1791280766460_uwebco.jpg")

        // 15. At-risk students (Real registered students with attendance < 75%)
        insertAtRisk(db, "risk_std_1791018184263_gq8k83", "ankur", "2508400130011", "Low Attendance (70%) & Academic Alert", 70, 0)
        insertAtRisk(db, "risk_std_1791010841660_9fx4k9", "ajay", "2508400130004", "Low Attendance (73%) & Academic Alert", 73, 0)

        // 16. Student Directory
        insertStudentDirectory(db, "sdir_1", "Aditya Sharma", "VVIT/CS/2021/045", "Computer Science", "Semester 5", "aditya.sharma@vvit.edu", "Ramesh Sharma (Father)", "+91 98111 22233", 8.4, 87, 1)
        insertStudentDirectory(db, "sdir_2", "Meera Nair", "VVIT/EC/2021/012", "Electronics & Comm", "Semester 5", "meera.nair@vvit.edu", "K. Nair (Mother)", "+91 98222 33445", 7.6, 79, 1)
        insertStudentDirectory(db, "sdir_3", "Rohan Das", "VVIT/ME/2020/098", "Mechanical Eng", "Semester 7", "rohan.das@vvit.edu", "Sunil Das (Father)", "+91 98333 44556", 6.8, 54, 0)
        insertStudentDirectory(db, "sdir_4", "Pooja Hegde", "VVIT/CS/2021/089", "Computer Science", "Semester 5", "pooja.h@vvit.edu", "R. Hegde (Father)", "+91 98444 55667", 9.1, 94, 1)
        insertStudentDirectory(db, "sdir_5", "Kiran Rao", "VVIT/EE/2022/034", "Electrical Eng", "Semester 3", "kiran.r@vvit.edu", "M. Rao (Father)", "+91 98555 66778", 8.0, 83, 1)

        // 17. Faculty Directory
        insertFacultyDirectory(db, "fdir_1", "Dr. S. Rao", "EMP-CS-2015-08", "Professor", "Computer Science", "Active", "Mathematics-IV (CS-302), Advanced Algorithms (CS-401)", 16)
        insertFacultyDirectory(db, "fdir_2", "Prof. K. Mehta", "EMP-CS-2018-14", "Asst. Professor", "Computer Science", "Active", "Computer Networks (CS-301), Networks Lab", 14)
        insertFacultyDirectory(db, "fdir_3", "Dr. A. Verma", "EMP-CS-2012-03", "Head of Dept", "Computer Science", "On Leave", "Database Management System", 12)
        insertFacultyDirectory(db, "fdir_4", "Dr. P. Sharma", "EMP-EC-2014-02", "Head of Dept", "Electronics & Comm", "Active", "Analog Electronics, VLSI Design", 16)
        insertFacultyDirectory(db, "fdir_5", "Prof. S. Khurana", "EMP-ME-2016-19", "Head of Dept", "Mechanical Eng", "Active", "Thermodynamics, Fluid Mechanics", 15)

        // 18. Departments
        insertDepartment(db, "dept_1", "Computer Science & Engineering", "Dr. A. Verma", 24, 480, 1)
        insertDepartment(db, "dept_2", "Electronics & Communication", "Dr. P. Sharma", 18, 360, 1)
        insertDepartment(db, "dept_3", "Mechanical Engineering", "Prof. S. Khurana", 15, 300, 1)
        insertDepartment(db, "dept_4", "Civil Engineering", "Dr. R. Bannerjee", 12, 240, 1)
        insertDepartment(db, "dept_5", "Electrical Engineering", "Dr. N. Iyer", 14, 280, 1)

        // 19. Scheduled Exams
        insertExam(db, "ex_1", "Data Structures & Algorithms", "CS-301", "Oct 18, 2026", "10:00 AM (3 Hrs)", "Block C - Hall 1")
        insertExam(db, "ex_2", "Database Management System", "CS-304", "Oct 20, 2026", "02:00 PM (3 Hrs)", "Block A - Room 204")
        insertExam(db, "ex_3", "Computer Networks", "CS-302", "Oct 22, 2026", "10:00 AM (3 Hrs)", "Block C - Hall 2")
        insertExam(db, "ex_4", "Operating Systems", "CS-303", "Oct 24, 2026", "02:00 PM (3 Hrs)", "Block B - Room 102")

        // 20. Library Books
        insertBook(db, "bk_1", "Introduction to Algorithms", "Cormen, Leiserson, Rivest", "978-0262033848", 4, 10)
        insertBook(db, "bk_2", "Computer Networking: A Top-Down Approach", "James Kurose, Keith Ross", "978-0133594140", 1, 8)
        insertBook(db, "bk_3", "Database System Concepts", "Silberschatz, Korth, Sudarshan", "978-0078022159", 6, 12)
        insertBook(db, "bk_4", "Operating System Concepts", "Abraham Silberschatz, Peter Galvin", "978-1118063330", 0, 7)

        // 21. Issued Books
        insertIssuedBook(db, "ibk_1", "Amit Patel", "CS-102", "Operating System Concepts", "Oct 10, 2026", 1)
        insertIssuedBook(db, "ibk_2", "Rohan Verma", "EE-205", "Electromagnetics Volume 2", "Oct 22, 2026", 0)
        insertIssuedBook(db, "ibk_3", "Sneha Roy", "CS-088", "Introduction to Algorithms", "Oct 25, 2026", 0)

        // 22. User Roles
        insertRole(db, "urg_1", "Super Admin", 2, "Full System Access, User Management, Finance Audit, Settings")
        insertRole(db, "urg_2", "HOD", 12, "Department Management, Faculty Allocations, Leave Approvals, Reports")
        insertRole(db, "urg_3", "Faculty", 115, "Attendance Manager, Marks Entry & Grading, Library Issue Book, Manage System Settings")
        insertRole(db, "urg_4", "Student", 2320, "View Attendance, Submit Assignments, Fee Payment, Library Catalog")

        // 23. Department Fees
        insertDeptFee(db, "CS & IT", 85)
        insertDeptFee(db, "Electrical", 60)
        insertDeptFee(db, "Mechanical", 45)
        insertDeptFee(db, "Civil Eng", 30)
    }

    private fun insertUser(db: SQLiteDatabase, id: String, name: String, email: String, role: String, identifier: String, password: String = "password123") {
        val cv = ContentValues().apply {
            put("id", id); put("name", name); put("email", email); put("password", password); put("role", role); put("identifier", identifier)
        }
        db.insertWithOnConflict(DbConstants.TABLE_USERS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    private fun insertAttendance(db: SQLiteDatabase, id: String, name: String, code: String, attended: Int, total: Int, pct: Int, isAlert: Int) {
        val cv = ContentValues().apply {
            put("id", id); put("subjectName", name); put("subjectCode", code); put("attendedClasses", attended); put("totalClasses", total); put("percentage", pct); put("isAlert", isAlert)
        }
        db.insert(DbConstants.TABLE_SUBJECT_ATTENDANCE, null, cv)
    }

    private fun insertTimetable(db: SQLiteDatabase, id: String, day: String, time: String, dur: String, sub: String, fac: String, room: String, ongoing: Int, isBreak: Int) {
        val cv = ContentValues().apply {
            put("id", id); put("dayOfWeek", day); put("time", time); put("duration", dur); put("subjectName", sub); put("facultyName", fac); put("room", room); put("isOngoing", ongoing); put("isBreak", isBreak)
        }
        db.insert(DbConstants.TABLE_TIMETABLE, null, cv)
    }

    private fun insertAssignment(db: SQLiteDatabase, id: String, sub: String, title: String, inst: String, due: String, status: String, subDate: String?, maxMarks: Int, marks: Int?, file: String?) {
        val cv = ContentValues().apply {
            put("id", id); put("subjectName", sub); put("title", title); put("instructions", inst); put("dueDate", due); put("status", status); put("submissionDate", subDate); put("maxMarks", maxMarks); put("obtainedMarks", marks); put("submittedFileName", file)
        }
        db.insert(DbConstants.TABLE_ASSIGNMENTS, null, cv)
    }

    private fun insertStudyMaterial(db: SQLiteDatabase, id: String, title: String, sub: String, sharedBy: String, date: String, size: String, type: String, url: String) {
        val cv = ContentValues().apply {
            put("id", id); put("title", title); put("subjectName", sub); put("sharedBy", sharedBy); put("sharedDate", date); put("fileSize", size); put("fileType", type); put("downloadUrl", url)
        }
        db.insert(DbConstants.TABLE_STUDY_MATERIALS, null, cv)
    }

    private fun insertSemesterResult(db: SQLiteDatabase, semNum: Int, name: String, sgpa: Double) {
        val cv = ContentValues().apply {
            put("semesterNumber", semNum); put("semesterName", name); put("sgpa", sgpa)
        }
        db.insert(DbConstants.TABLE_SEMESTER_RESULTS, null, cv)
    }

    private fun insertGradeItem(db: SQLiteDatabase, id: String, semNum: Int, name: String, code: String, intMarks: Int, extMarks: Int, totMarks: Int, grade: String) {
        val cv = ContentValues().apply {
            put("id", id); put("semesterNumber", semNum); put("subjectName", name); put("subjectCode", code); put("internalMarks", intMarks); put("externalMarks", extMarks); put("totalMarks", totMarks); put("grade", grade)
        }
        db.insert(DbConstants.TABLE_GRADESHEET_ITEMS, null, cv)
    }

    private fun insertFeeTransaction(db: SQLiteDatabase, id: String, title: String, txnId: String, date: String, amount: Long, status: String) {
        val cv = ContentValues().apply {
            put("id", id); put("title", title); put("transactionId", txnId); put("date", date); put("amount", amount); put("status", status)
        }
        db.insert(DbConstants.TABLE_FEE_TRANSACTIONS, null, cv)
    }

    private fun insertNotice(db: SQLiteDatabase, id: String, title: String, cat: String, timeAgo: String, desc: String, isUrgent: Int, date: String) {
        val cv = ContentValues().apply {
            put("id", id); put("title", title); put("category", cat); put("timeAgo", timeAgo); put("description", desc); put("isUrgent", isUrgent); put("date", date)
        }
        db.insert(DbConstants.TABLE_NOTICES, null, cv)
    }

    private fun insertLeave(db: SQLiteDatabase, id: String, name: String, roll: String, from: String, to: String, days: Int, reason: String, status: String, applied: String) {
        val cv = ContentValues().apply {
            put("id", id); put("studentName", name); put("rollNo", roll); put("fromDate", from); put("toDate", to); put("durationDays", days); put("reason", reason); put("status", status); put("appliedDate", applied)
        }
        db.insert(DbConstants.TABLE_LEAVE_APPLICATIONS, null, cv)
    }

    private fun insertTeacherSchedule(db: SQLiteDatabase, id: String, time: String, sub: String, room: String, sec: String, type: String, day: String) {
        val cv = ContentValues().apply {
            put("id", id); put("time", time); put("subjectName", sub); put("room", room); put("section", sec); put("lectureType", type); put("dayOfWeek", day)
        }
        db.insert(DbConstants.TABLE_TEACHER_SCHEDULE, null, cv)
    }

    private fun insertRoster(
        db: SQLiteDatabase,
        id: String,
        roll: String,
        name: String,
        sec: String,
        status: String,
        email: String = "",
        phone: String = "",
        dept: String = "Information Technology",
        sem: String = "Semester 3 (Year 2)",
        att: Int = 85,
        cgpa: Double = 8.0,
        gName: String = "Guardian / Parent",
        gContact: String = "+91 98765 43210",
        photoUrl: String? = null
    ) {
        val cv = ContentValues().apply {
            put("id", id); put("rollNo", roll); put("studentName", name); put("section", sec); put("status", status)
            put("email", email); put("phone", phone); put("department", dept); put("currentSemester", sem)
            put("attendancePercentage", att); put("cgpa", cgpa); put("guardianName", gName); put("guardianContact", gContact)
            put("photoUrl", photoUrl)
        }
        db.insertWithOnConflict(DbConstants.TABLE_ATTENDANCE_ROSTER, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    private fun insertAtRisk(db: SQLiteDatabase, id: String, name: String, roll: String, reason: String, pct: Int, isReminded: Int) {
        val cv = ContentValues().apply {
            put("id", id); put("studentName", name); put("rollNo", roll); put("reason", reason); put("attendancePercent", pct); put("isReminded", isReminded)
        }
        db.insert(DbConstants.TABLE_AT_RISK_STUDENTS, null, cv)
    }

    private fun insertStudentDirectory(db: SQLiteDatabase, id: String, name: String, roll: String, dept: String, sem: String, email: String, guardian: String, phone: String, cgpa: Double, att: Int, isActive: Int) {
        val cv = ContentValues().apply {
            put("id", id); put("name", name); put("rollNo", roll); put("department", dept); put("semester", sem); put("email", email); put("guardianName", guardian); put("guardianPhone", phone); put("cgpa", cgpa); put("attendancePercent", att); put("isActive", isActive)
        }
        db.insert(DbConstants.TABLE_STUDENT_DIRECTORY, null, cv)
    }

    private fun insertFacultyDirectory(db: SQLiteDatabase, id: String, name: String, empId: String, desig: String, dept: String, status: String, subjects: String, hours: Int) {
        val cv = ContentValues().apply {
            put("id", id); put("name", name); put("employeeId", empId); put("designation", desig); put("department", dept); put("status", status); put("assignedSubjects", subjects); put("weeklyWorkloadHours", hours)
        }
        db.insert(DbConstants.TABLE_FACULTY_DIRECTORY, null, cv)
    }

    private fun insertDepartment(db: SQLiteDatabase, id: String, name: String, hod: String, fac: Int, std: Int, isActive: Int) {
        val cv = ContentValues().apply {
            put("id", id); put("name", name); put("hodName", hod); put("facultyCount", fac); put("studentCount", std); put("isActive", isActive)
        }
        db.insert(DbConstants.TABLE_DEPARTMENTS, null, cv)
    }

    private fun insertExam(db: SQLiteDatabase, id: String, name: String, code: String, date: String, duration: String, venue: String) {
        val cv = ContentValues().apply {
            put("id", id); put("subjectName", name); put("subjectCode", code); put("examDate", date); put("timeDuration", duration); put("venue", venue)
        }
        db.insert(DbConstants.TABLE_SCHEDULED_EXAMS, null, cv)
    }

    private fun insertBook(db: SQLiteDatabase, id: String, title: String, author: String, isbn: String, avail: Int, total: Int) {
        val cv = ContentValues().apply {
            put("id", id); put("title", title); put("author", author); put("isbn", isbn); put("availableCopies", avail); put("totalCopies", total)
        }
        db.insert(DbConstants.TABLE_LIBRARY_BOOKS, null, cv)
    }

    private fun insertIssuedBook(db: SQLiteDatabase, id: String, name: String, roll: String, book: String, due: String, isOverdue: Int) {
        val cv = ContentValues().apply {
            put("id", id); put("studentName", name); put("rollNo", roll); put("bookTitle", book); put("dueDate", due); put("isOverdue", isOverdue)
        }
        db.insert(DbConstants.TABLE_ISSUED_BOOKS, null, cv)
    }

    private fun insertRole(db: SQLiteDatabase, id: String, name: String, members: Int, perms: String) {
        val cv = ContentValues().apply {
            put("id", id); put("roleName", name); put("memberCount", members); put("permissions", perms)
        }
        db.insert(DbConstants.TABLE_USER_ROLES, null, cv)
    }

    private fun insertDeptFee(db: SQLiteDatabase, dept: String, pct: Int) {
        val cv = ContentValues().apply {
            put("departmentName", dept); put("collectedPercent", pct)
        }
        db.insert(DbConstants.TABLE_DEPARTMENT_FEES, null, cv)
    }
}