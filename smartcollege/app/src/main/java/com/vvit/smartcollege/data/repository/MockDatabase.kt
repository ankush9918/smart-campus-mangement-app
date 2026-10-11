package com.vvit.smartcollege.data.repository

import com.vvit.smartcollege.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object MockDatabase {

    val currentUser = MutableStateFlow<User?>(null)

    val studentProfile = MutableStateFlow(
        StudentProfile(
            id = "std_1",
            name = "Aditya Sharma",
            rollNo = "VVIT/CS/2021/045",
            email = "aditya.sharma@vvit.edu",
            phone = "+91 98765 43210",
            dateOfBirth = "14th August 2003",
            bloodGroup = "O+ Positive",
            department = "Information Technology",
            currentSemester = "Semester 5 (Year 3)",
            section = "Section B",
            admissionYear = "2021",
            guardianName = "Ramesh Sharma",
            guardianRelationship = "Father",
            guardianContact = "+91 98111 22233",
            cgpa = 8.41,
            attendancePercentage = 87
        )
    )

    val teacherProfile = MutableStateFlow(
        TeacherProfile(
            id = "tch_1",
            name = "Dr. Rajesh Kumar",
            employeeId = "VVIT/CS/T-104",
            designation = "Associate Professor",
            department = "Information Technology",
            email = "rajesh.kumar@vvit.edu",
            phone = "+91 98123 45678",
            weeklyWorkloadHours = 16,
            activeSubjectsCount = 3,
            totalStudents = 248,
            classesToday = 3,
            pendingTasks = 12,
            photoUrl = null
        )
    )

    val adminProfile = MutableStateFlow(
        AdminProfile(
            id = "adm_1",
            name = "Dr. Rajesh Varma",
            title = "System Administrator",
            email = "admin.varma@vvit.edu",
            phone = "+91 98999 11223"
        )
    )

    val studentAttendance = MutableStateFlow(
        listOf(
            SubjectAttendance("att_1", "Data Structure", "BCS-301", 36, 42, 86),
            SubjectAttendance("att_2", "Computer Organization & Architecture", "BCS-302", 35, 40, 88),
            SubjectAttendance("att_3", "Discrete Structure & Theory of Logic", "BCS-303", 31, 38, 82),
            SubjectAttendance("att_4", "Digital Electronics", "BOE-310", 31, 36, 86),
            SubjectAttendance("att_5", "Technical Communication", "BAS-301", 27, 32, 84),
            SubjectAttendance("att_6", "Python Programming", "BCC-302", 31, 35, 89)
        )
    )


    val studentTimetable = MutableStateFlow(
        listOf(
            // Monday
            StudentTimetableEntry("tt_m1", "Mon", "10:00 AM - 10:50 AM", "50 Mins", "Python Programming (BCC-302)", "Dharamraj Yadav (DRY)", "LH-1"),
            StudentTimetableEntry("tt_m2", "Mon", "10:50 AM - 11:40 AM", "50 Mins", "Data Structure (BCS-301)", "Kalpana Suman (KS)", "LH-1"),
            StudentTimetableEntry("tt_m3", "Mon", "11:40 AM - 12:30 PM", "50 Mins", "Digital Electronics (BOE-310)", "Anant Sikarwar (AS)", "LH-1"),
            StudentTimetableEntry("tt_m4", "Mon", "12:30 PM - 01:20 PM", "50 Mins", "Python Programming (BCC-302)", "Dharamraj Yadav (DRY)", "LH-1"),
            StudentTimetableEntry("tt_ml", "Mon", "01:20 PM - 02:20 PM", "1 Hour", "Lunch Break", "Campus Cafeteria", "Cafeteria", isBreak = true),
            StudentTimetableEntry("tt_m5", "Mon", "02:20 PM - 03:10 PM", "50 Mins", "Computer Organization & Architecture (BCS-302)", "Prakash Pandey (PP)", "LH-1"),
            StudentTimetableEntry("tt_m6", "Mon", "03:10 PM - 04:00 PM", "50 Mins", "Digital Electronics (BOE-310)", "Anant Sikarwar (AS)", "LH-1"),
            StudentTimetableEntry("tt_m7", "Mon", "04:00 PM - 04:50 PM", "50 Mins", "Discrete Structure & Theory of Logic (BCS-303) [T]", "Rishav Kumar Singh (RKS)", "LH-1"),

            // Tuesday
            StudentTimetableEntry("tt_t1", "Tue", "10:00 AM - 10:50 AM", "50 Mins", "Technical Communication (BAS-301)", "Dr. Pinki Negi Bora (PNB)", "LH-1"),
            StudentTimetableEntry("tt_t2", "Tue", "10:50 AM - 11:40 AM", "50 Mins", "Discrete Structure & Theory of Logic (BCS-303)", "Rishav Kumar Singh (RKS)", "LH-1"),
            StudentTimetableEntry("tt_t3", "Tue", "11:40 AM - 12:30 PM", "50 Mins", "Computer Organization & Architecture (BCS-302)", "Prakash Pandey (PP)", "LH-1"),
            StudentTimetableEntry("tt_t4", "Tue", "12:30 PM - 01:20 PM", "50 Mins", "Data Structure (BCS-301)", "Kalpana Suman (KS)", "LH-1"),
            StudentTimetableEntry("tt_tl", "Tue", "01:20 PM - 02:20 PM", "1 Hour", "Lunch Break", "Campus Cafeteria", "Cafeteria", isBreak = true),
            StudentTimetableEntry("tt_t5", "Tue", "02:20 PM - 03:10 PM", "50 Mins", "Digital Electronics (BOE-310)", "Anant Sikarwar (AS)", "LH-1"),
            StudentTimetableEntry("tt_t6", "Tue", "03:10 PM - 04:50 PM", "1 Hr 40 Mins", "COA Lab G1 (BCS-352)", "Kalpana Suman (KS) / Dr. Prakash Kumar Singh (PKS)", "COA Lab"),

            // Wednesday
            StudentTimetableEntry("tt_w1", "Wed", "10:00 AM - 10:50 AM", "50 Mins", "Technical Communication (BAS-301)", "Dr. Pinki Negi Bora (PNB)", "LH-1"),
            StudentTimetableEntry("tt_w2", "Wed", "10:50 AM - 12:30 PM", "1 Hr 40 Mins", "COA Lab G2 (BCS-352)", "Akash Garg (AG) / Dharamraj Yadav (DRY)", "COA Lab"),
            StudentTimetableEntry("tt_w4", "Wed", "12:30 PM - 01:20 PM", "50 Mins", "Data Structure (BCS-301)", "Kalpana Suman (KS)", "LH-1"),
            StudentTimetableEntry("tt_wl", "Wed", "01:20 PM - 02:20 PM", "1 Hour", "Lunch Break", "Campus Cafeteria", "Cafeteria", isBreak = true),
            StudentTimetableEntry("tt_w5", "Wed", "02:20 PM - 04:00 PM", "1 Hr 40 Mins", "Web Designing Workshop G2 (BCS-353)", "Akash Garg (AG) / Dr. Prakash Kumar Singh (PKS)", "Web Tech Lab"),
            StudentTimetableEntry("tt_w7", "Wed", "04:00 PM - 04:50 PM", "50 Mins", "Computer Organization & Architecture (BCS-302)", "Prakash Pandey (PP)", "LH-1"),
            StudentTimetableEntry("tt_w8", "Wed", "04:50 PM - 05:40 PM", "50 Mins", "Sports & Yoga", "Sports Department", "Sports Ground"),

            // Thursday
            StudentTimetableEntry("tt_th1", "Thu", "10:00 AM - 10:50 AM", "50 Mins", "Technical Communication (BAS-301)", "Dr. Pinki Negi Bora (PNB)", "LH-1"),
            StudentTimetableEntry("tt_th2", "Thu", "10:50 AM - 11:40 AM", "50 Mins", "Discrete Structure & Theory of Logic (BCS-303)", "Rishav Kumar Singh (RKS)", "LH-1"),
            StudentTimetableEntry("tt_th3", "Thu", "11:40 AM - 01:20 PM", "1 Hr 40 Mins", "Web Designing Workshop G1 (BCS-353)", "Kalpana Suman (KS) / Anant Sikarwar (AS)", "Web Tech Lab"),
            StudentTimetableEntry("tt_thl", "Thu", "01:20 PM - 02:20 PM", "1 Hour", "Lunch Break", "Campus Cafeteria", "Cafeteria", isBreak = true),
            StudentTimetableEntry("tt_th5", "Thu", "02:20 PM - 04:00 PM", "1 Hr 40 Mins", "Data Structure Lab G1 (BCS-351)", "Kalpana Suman (KS) / Dr. Prakash Kumar Singh (PKS)", "DS Lab"),
            StudentTimetableEntry("tt_th7", "Thu", "04:00 PM - 04:50 PM", "50 Mins", "Computer Organization & Architecture (BCS-302)", "Prakash Pandey (PP)", "LH-1"),
            StudentTimetableEntry("tt_th8", "Thu", "04:50 PM - 05:40 PM", "50 Mins", "Sports & Yoga", "Sports Department", "Sports Ground"),

            // Friday
            StudentTimetableEntry("tt_f1", "Fri", "10:00 AM - 10:50 AM", "50 Mins", "Data Structure (BCS-301)", "Kalpana Suman (KS)", "LH-1"),
            StudentTimetableEntry("tt_f2", "Fri", "10:50 AM - 12:30 PM", "1 Hr 40 Mins", "Internship Assessment / Mini Project (BCC-351)", "Dr. Prakash Kumar Singh (PKS)", "Project Lab"),
            StudentTimetableEntry("tt_f4", "Fri", "12:30 PM - 01:20 PM", "50 Mins", "Digital Electronics (BOE-310) [Tutorial]", "Anant Sikarwar (AS)", "LH-1"),
            StudentTimetableEntry("tt_fl", "Fri", "01:20 PM - 02:20 PM", "1 Hour", "Lunch Break", "Campus Cafeteria", "Cafeteria", isBreak = true),
            StudentTimetableEntry("tt_f5", "Fri", "02:20 PM - 04:00 PM", "1 Hr 40 Mins", "Data Structure Lab G2 (BCS-351)", "Kalpana Suman (KS) / Anant Sikarwar (AS)", "DS Lab"),
            StudentTimetableEntry("tt_f7", "Fri", "04:00 PM - 04:50 PM", "50 Mins", "Library / Self-Study", "Librarian", "Central Library")
        )
    )

    val studentAssignments = MutableStateFlow<List<StudentAssignment>>(emptyList())

    val studyMaterials = MutableStateFlow<List<StudentStudyMaterial>>(emptyList())

    val semesterResults = MutableStateFlow(
        listOf(
            SemesterResult(
                semesterName = "Semester 4",
                semesterNumber = 4,
                sgpa = 8.65,
                grades = listOf(
                    GradeItem("Discrete Mathematics", "CS-201", 28, 54, 82, "A+"),
                    GradeItem("Microprocessors", "CS-202", 26, 62, 88, "O"),
                    GradeItem("Operating Systems", "CS-203", 22, 48, 70, "B+"),
                    GradeItem("Data Structures Lab", "CS-204", 45, 47, 92, "O"),
                    GradeItem("Formal Languages", "CS-205", 25, 50, 75, "A")
                )
            ),
            SemesterResult(
                semesterName = "Semester 3",
                semesterNumber = 3,
                sgpa = 8.35,
                grades = listOf(
                    GradeItem("Object Oriented Prog", "CS-105", 27, 52, 79, "A"),
                    GradeItem("Digital Electronics", "EC-102", 24, 50, 74, "B+"),
                    GradeItem("Mathematics III", "MA-201", 29, 60, 89, "O")
                )
            ),
            SemesterResult(
                semesterName = "Semester 2",
                semesterNumber = 2,
                sgpa = 8.20,
                grades = listOf(
                    GradeItem("Basic Electronics", "EC-101", 23, 49, 72, "B+"),
                    GradeItem("Engineering Chemistry", "CH-101", 26, 56, 82, "A+")
                )
            ),
            SemesterResult(
                semesterName = "Semester 1",
                semesterNumber = 1,
                sgpa = 8.45,
                grades = listOf(
                    GradeItem("Engineering Physics", "PH-101", 28, 58, 86, "A+"),
                    GradeItem("Problem Solving in C", "CS-101", 29, 61, 90, "O")
                )
            )
        )
    )

    val studentFees = MutableStateFlow(
        FeeDetails(
            pendingAmount = 45000L,
            totalInvoiced = 125000L,
            paidTillDate = 80000L,
            currentSemesterFee = 45000L,
            semesterLabel = "Semester 5 Tuition Fee"
        )
    )

    val feeTransactions = MutableStateFlow(
        listOf(
            FeeTransaction("txn_1", "Semester 4 Tuition Fee", "TXN-VVIT98273", "14 Jul 2024", 45000L),
            FeeTransaction("txn_2", "Annual Exam & Lab Fee", "TXN-VVIT98112", "02 Feb 2024", 15000L),
            FeeTransaction("txn_3", "Semester 3 Tuition Fee", "TXN-VVIT87123", "10 Jul 2023", 40000L)
        )
    )

    val notices = MutableStateFlow<List<NoticeItem>>(emptyList())

    val leaveApplications = MutableStateFlow(
        listOf(
            StudentLeaveApplication(
                id = "lv_1",
                studentName = "Aditya Sharma",
                rollNo = "VVIT/CS/2021/045",
                fromDate = "Oct 16, 2026",
                toDate = "Oct 18, 2026",
                durationDays = 3,
                reason = "Attending Sister's Wedding. Invitation attached.",
                status = "Pending",
                appliedDate = "12 Oct 2026"
            ),
            StudentLeaveApplication(
                id = "lv_2",
                studentName = "Chaitanya Jha",
                rollNo = "VVIT/CS/2021/022",
                fromDate = "Oct 15, 2026",
                toDate = "Oct 15, 2026",
                durationDays = 1,
                reason = "Medical Appointment. Prescriptions uploaded.",
                status = "Pending",
                appliedDate = "13 Oct 2026"
            )
        )
    )

    val teacherSchedule = MutableStateFlow(
        listOf(
            // Monday
            TeacherScheduleItem("tsch_1", "10:50 AM - 11:40 AM", "Data Structure (BCS-301)", "LH-1", "IT-3rd Sem", "General Lecture", "Mon"),
            TeacherScheduleItem("tsch_2", "02:20 PM - 03:10 PM", "Computer Organization & Architecture (BCS-302)", "LH-1", "IT-3rd Sem", "General Lecture", "Mon"),
            TeacherScheduleItem("tsch_3", "04:00 PM - 04:50 PM", "Discrete Structure & Theory of Logic (BCS-303)", "LH-1", "IT-3rd Sem", "Tutorial Session", "Mon"),

            // Tuesday
            TeacherScheduleItem("tsch_4", "10:00 AM - 10:50 AM", "Technical Communication (BAS-301)", "LH-1", "IT-3rd Sem", "General Lecture", "Tue"),
            TeacherScheduleItem("tsch_5", "11:40 AM - 12:30 PM", "Computer Organization & Architecture (BCS-302)", "LH-1", "IT-3rd Sem", "General Lecture", "Tue"),
            TeacherScheduleItem("tsch_6", "12:30 PM - 01:20 PM", "Data Structure (BCS-301)", "LH-1", "IT-3rd Sem", "General Lecture", "Tue"),
            TeacherScheduleItem("tsch_7", "02:20 PM - 03:10 PM", "Digital Electronics (BOE-310)", "LH-1", "IT-3rd Sem", "General Lecture", "Tue"),

            // Wednesday
            TeacherScheduleItem("tsch_8", "10:00 AM - 10:50 AM", "Technical Communication (BAS-301)", "LH-1", "IT-3rd Sem", "General Lecture", "Wed"),
            TeacherScheduleItem("tsch_9", "12:30 PM - 01:20 PM", "Data Structure (BCS-301)", "LH-1", "IT-3rd Sem", "General Lecture", "Wed"),
            TeacherScheduleItem("tsch_10", "04:00 PM - 04:50 PM", "Computer Organization & Architecture (BCS-302)", "LH-1", "IT-3rd Sem", "General Lecture", "Wed"),

            // Thursday
            TeacherScheduleItem("tsch_11", "10:00 AM - 10:50 AM", "Technical Communication (BAS-301)", "LH-1", "IT-3rd Sem", "General Lecture", "Thu"),
            TeacherScheduleItem("tsch_12", "10:50 AM - 11:40 AM", "Discrete Structure & Theory of Logic (BCS-303)", "LH-1", "IT-3rd Sem", "General Lecture", "Thu"),
            TeacherScheduleItem("tsch_13", "04:00 PM - 04:50 PM", "Computer Organization & Architecture (BCS-302)", "LH-1", "IT-3rd Sem", "General Lecture", "Thu"),

            // Friday
            TeacherScheduleItem("tsch_14", "10:00 AM - 10:50 AM", "Data Structure (BCS-301)", "LH-1", "IT-3rd Sem", "General Lecture", "Fri"),
            TeacherScheduleItem("tsch_15", "12:30 PM - 01:20 PM", "Digital Electronics (BOE-310)", "LH-1", "IT-3rd Sem", "Tutorial Session", "Fri"),
            TeacherScheduleItem("tsch_16", "03:00 PM - 04:30 PM", "Python Programming & Project Review", "LH-1", "IT-3rd Sem", "Lab / Review", "Fri"),

            // Saturday
            TeacherScheduleItem("tsch_17", "10:00 AM - 11:30 AM", "Python Programming & Coding Workshop", "LH-1", "IT-3rd Sem", "Workshop", "Sat"),
            TeacherScheduleItem("tsch_18", "12:00 PM - 01:00 PM", "Weekly Assessment & Mentorship", "LH-1", "IT-3rd Sem", "Assessment", "Sat")
        )
    )

    val teacherAttendanceRoster = MutableStateFlow(
        listOf(
            StudentRosterItem(
                id = "std_1791009612955_3hhbwb",
                rollNo = "25084001312",
                studentName = "ankush",
                section = "IT-Sec A",
                status = StudentAttendanceStatus.UNMARKED,
                email = "ankush251312@recmainpuri.in",
                phone = "+91 98765 43210",
                department = "Information Technology",
                currentSemester = "Semester 3 (Year 2)",
                attendancePercentage = 78,
                cgpa = 7.3,
                guardianName = "Guardian / Parent",
                guardianContact = "+91 98765 43210",
                photoUrl = "https://easrebumarwkloibtemf.supabase.co/storage/v1/object/public/student-photos/std_1791009612955_3hhbwb.jpg"
            ),
            StudentRosterItem(
                id = "std_1791018184263_gq8k83",
                rollNo = "2508400130011",
                studentName = "ankur",
                section = "IT-Sec A",
                status = StudentAttendanceStatus.UNMARKED,
                email = "ankur251311@recmainpuri.in",
                phone = "+91 98765 43211",
                department = "Information Technology",
                currentSemester = "Semester 3 (Year 2)",
                attendancePercentage = 70,
                cgpa = 6.9,
                guardianName = "Guardian / Parent",
                guardianContact = "+91 98765 43211",
                photoUrl = "https://easrebumarwkloibtemf.supabase.co/storage/v1/object/public/student-photos/std_1791018184263_gq8k83.jpg"
            ),
            StudentRosterItem(
                id = "std_1791010841660_9fx4k9",
                rollNo = "2508400130004",
                studentName = "ajay",
                section = "IT-Sec A",
                status = StudentAttendanceStatus.UNMARKED,
                email = "ajay251304@recmainpuri.in",
                phone = "+91 98765 43212",
                department = "Information Technology",
                currentSemester = "Semester 3 (Year 2)",
                attendancePercentage = 73,
                cgpa = 8.0,
                guardianName = "Guardian / Parent",
                guardianContact = "+91 98765 43212",
                photoUrl = "https://easrebumarwkloibtemf.supabase.co/storage/v1/object/public/student-photos/std_1791010841660_9fx4k9.jpg"
            ),
            StudentRosterItem(
                id = "std_1791280766460_uwebco",
                rollNo = "2508400130015",
                studentName = "anuj",
                section = "IT-Sec A",
                status = StudentAttendanceStatus.UNMARKED,
                email = "anuj251215@recmainpuri.in",
                phone = "+91 98765 43213",
                department = "Information Technology",
                currentSemester = "Semester 3 (Year 2)",
                attendancePercentage = 80,
                cgpa = 8.0,
                guardianName = "Guardian / Parent",
                guardianContact = "+91 98765 43213",
                photoUrl = "https://easrebumarwkloibtemf.supabase.co/storage/v1/object/public/student-photos/std_1791280766460_uwebco.jpg"
            )
        )
    )

    val atRiskStudents = MutableStateFlow(
        listOf(
            AtRiskStudent("risk_std_1791018184263_gq8k83", "ankur", "2508400130011", "Low Attendance (70%) & Performance Alert", 70),
            AtRiskStudent("risk_std_1791010841660_9fx4k9", "ajay", "2508400130004", "Low Attendance (73%) & Performance Alert", 73)
        )
    )

    val adminKPIs = MutableStateFlow(
        AdminKPIs(
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
    )

    val studentDirectory = MutableStateFlow(
        listOf(
            StudentDirectoryEntry("sdir_1", "Aditya Sharma", "VVIT/CS/2021/045", "Computer Science", "Semester 5", "aditya.sharma@vvit.edu", "Ramesh Sharma (Father)", "+91 98111 22233", 8.4, 87, true),
            StudentDirectoryEntry("sdir_2", "Meera Nair", "VVIT/EC/2021/012", "Electronics & Comm", "Semester 5", "meera.nair@vvit.edu", "K. Nair (Mother)", "+91 98222 33445", 7.6, 79, true),
            StudentDirectoryEntry("sdir_3", "Rohan Das", "VVIT/ME/2020/098", "Mechanical Eng", "Semester 7", "rohan.das@vvit.edu", "Sunil Das (Father)", "+91 98333 44556", 6.8, 54, false),
            StudentDirectoryEntry("sdir_4", "Pooja Hegde", "VVIT/CS/2021/089", "Computer Science", "Semester 5", "pooja.h@vvit.edu", "R. Hegde (Father)", "+91 98444 55667", 9.1, 94, true),
            StudentDirectoryEntry("sdir_5", "Kiran Rao", "VVIT/EE/2022/034", "Electrical Eng", "Semester 3", "kiran.r@vvit.edu", "M. Rao (Father)", "+91 98555 66778", 8.0, 83, true)
        )
    )

    val facultyDirectory = MutableStateFlow(
        listOf(
            FacultyDirectoryEntry("fdir_1", "Dr. S. Rao", "EMP-CS-2015-08", "Professor", "Computer Science", "Active", listOf("Mathematics-IV (CS-302)", "Advanced Algorithms (CS-401)"), 16),
            FacultyDirectoryEntry("fdir_2", "Prof. K. Mehta", "EMP-CS-2018-14", "Asst. Professor", "Computer Science", "Active", listOf("Computer Networks (CS-301)", "Networks Lab"), 14),
            FacultyDirectoryEntry("fdir_3", "Dr. A. Verma", "EMP-CS-2012-03", "Head of Dept", "Computer Science", "On Leave", listOf("Database Management System"), 12),
            FacultyDirectoryEntry("fdir_4", "Dr. P. Sharma", "EMP-EC-2014-02", "Head of Dept", "Electronics & Comm", "Active", listOf("Analog Electronics", "VLSI Design"), 16),
            FacultyDirectoryEntry("fdir_5", "Prof. S. Khurana", "EMP-ME-2016-19", "Head of Dept", "Mechanical Eng", "Active", listOf("Thermodynamics", "Fluid Mechanics"), 15)
        )
    )

    val departments = MutableStateFlow(
        listOf(
            DepartmentEntry("dept_1", "Computer Science & Engineering", "Dr. A. Verma", 24, 480, true),
            DepartmentEntry("dept_2", "Electronics & Communication", "Dr. P. Sharma", 18, 360, true),
            DepartmentEntry("dept_3", "Mechanical Engineering", "Prof. S. Khurana", 15, 300, true),
            DepartmentEntry("dept_4", "Civil Engineering", "Dr. R. Bannerjee", 12, 240, true),
            DepartmentEntry("dept_5", "Electrical Engineering", "Dr. N. Iyer", 14, 280, true)
        )
    )

    val scheduledExams = MutableStateFlow(
        listOf(
            ScheduledExam("ex_1", "Data Structures & Algorithms", "CS-301", "Oct 18, 2026", "10:00 AM (3 Hrs)", "Block C - Hall 1"),
            ScheduledExam("ex_2", "Database Management System", "CS-304", "Oct 20, 2026", "02:00 PM (3 Hrs)", "Block A - Room 204"),
            ScheduledExam("ex_3", "Computer Networks", "CS-302", "Oct 22, 2026", "10:00 AM (3 Hrs)", "Block C - Hall 2"),
            ScheduledExam("ex_4", "Operating Systems", "CS-303", "Oct 24, 2026", "02:00 PM (3 Hrs)", "Block B - Room 102")
        )
    )

    val libraryBooks = MutableStateFlow(
        listOf(
            LibraryBook("bk_1", "Introduction to Algorithms", "Cormen, Leiserson, Rivest", "978-0262033848", 4, 10),
            LibraryBook("bk_2", "Computer Networking: A Top-Down Approach", "James Kurose, Keith Ross", "978-0133594140", 1, 8),
            LibraryBook("bk_3", "Database System Concepts", "Silberschatz, Korth, Sudarshan", "978-0078022159", 6, 12),
            LibraryBook("bk_4", "Operating System Concepts", "Abraham Silberschatz, Peter Galvin", "978-1118063330", 0, 7)
        )
    )

    val issuedBooks = MutableStateFlow(
        listOf(
            IssuedBookRecord("ibk_1", "Amit Patel", "CS-102", "Operating System Concepts", "Oct 10, 2026", isOverdue = true),
            IssuedBookRecord("ibk_2", "Rohan Verma", "EE-205", "Electromagnetics Volume 2", "Oct 22, 2026", isOverdue = false),
            IssuedBookRecord("ibk_3", "Sneha Roy", "CS-088", "Introduction to Algorithms", "Oct 25, 2026", isOverdue = false)
        )
    )

    val userRoles = MutableStateFlow(
        listOf(
            UserRoleGroup("urg_1", "Super Admin", 2, listOf("Full System Access", "User Management", "Finance Audit", "Settings")),
            UserRoleGroup("urg_2", "HOD", 12, listOf("Department Management", "Faculty Allocations", "Leave Approvals", "Reports")),
            UserRoleGroup("urg_3", "Faculty", 115, listOf("Attendance Manager", "Marks Entry & Grading", "Library Issue Book", "Manage System Settings")),
            UserRoleGroup("urg_4", "Student", 2320, listOf("View Attendance", "Submit Assignments", "Fee Payment", "Library Catalog"))
        )
    )

    val departmentFees = MutableStateFlow(
        listOf(
            DepartmentFeeCollection("CS & IT", 85),
            DepartmentFeeCollection("Electrical", 60),
            DepartmentFeeCollection("Mechanical", 45),
            DepartmentFeeCollection("Civil Eng", 30)
        )
    )
}
