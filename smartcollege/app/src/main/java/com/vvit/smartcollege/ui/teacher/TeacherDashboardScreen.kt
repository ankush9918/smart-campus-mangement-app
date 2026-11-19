package com.vvit.smartcollege.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vvit.smartcollege.data.model.StudentRosterItem
import com.vvit.smartcollege.ui.components.*
import com.vvit.smartcollege.ui.theme.*

import androidx.compose.material.icons.automirrored.filled.ExitToApp

@Composable
fun TeacherDashboardScreen(
    viewModel: TeacherViewModel,
    onNavigateToSection: (String) -> Unit,
    onLogout: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val profile by viewModel.profile.collectAsState()
    val schedule by viewModel.schedule.collectAsState()
    val notices by viewModel.notices.collectAsState()
    val roster by viewModel.attendanceRoster.collectAsState()
    val teacherAssignments by viewModel.assignments.collectAsState()
    val isStudentListDialogVisible by viewModel.isStudentListDialogVisible.collectAsState()
    val studentSearchQuery by viewModel.studentSearchQuery.collectAsState()
    val selectedStudentDetail by viewModel.selectedStudentDetail.collectAsState()

    // Calculate actual total students (including all active & registered students)
    val displayStudentCount = if (roster.isNotEmpty()) roster.size else profile.totalStudents
    val atRiskCount = remember(roster) { roster.count { it.attendancePercentage < 75 } }

    // Determine current day of week (e.g. Mon, Tue, Wed, Thu, Fri, Sat)
    val currentDay = remember {
        val cal = java.util.Calendar.getInstance()
        when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> "Mon"
            java.util.Calendar.TUESDAY -> "Tue"
            java.util.Calendar.WEDNESDAY -> "Wed"
            java.util.Calendar.THURSDAY -> "Thu"
            java.util.Calendar.FRIDAY -> "Fri"
            java.util.Calendar.SATURDAY -> "Sat"
            else -> "Sun"
        }
    }

    val currentDayFullName = remember {
        val cal = java.util.Calendar.getInstance()
        when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> "Monday"
            java.util.Calendar.TUESDAY -> "Tuesday"
            java.util.Calendar.WEDNESDAY -> "Wednesday"
            java.util.Calendar.THURSDAY -> "Thursday"
            java.util.Calendar.FRIDAY -> "Friday"
            java.util.Calendar.SATURDAY -> "Saturday"
            else -> "Sunday"
        }
    }

    // Filter today's classes matching official schedule
    val todayClasses = remember(schedule, currentDay) {
        schedule.filter { 
            it.dayOfWeek.equals(currentDay, ignoreCase = true) || it.dayOfWeek.startsWith(currentDay, ignoreCase = true)
        }
    }

    // Find next upcoming day with classes if today has none
    val nextTeachingDay = remember(schedule, currentDay) {
        val daysOrder = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val currentIndex = daysOrder.indexOf(currentDay)
        if (currentIndex >= 0) {
            val ordered = daysOrder.drop(currentIndex + 1) + daysOrder.take(currentIndex)
            ordered.firstOrNull { d -> schedule.any { it.dayOfWeek.equals(d, ignoreCase = true) } }
        } else {
            "Mon"
        }
    }

    val displayClasses = remember(todayClasses, schedule, nextTeachingDay) {
        if (todayClasses.isNotEmpty()) {
            todayClasses
        } else if (nextTeachingDay != null) {
            schedule.filter { it.dayOfWeek.equals(nextTeachingDay, ignoreCase = true) }
        } else {
            emptyList()
        }
    }

    val isShowingUpcoming = todayClasses.isEmpty() && displayClasses.isNotEmpty()

    LaunchedEffect(Unit) {
        viewModel.refreshRoster()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLight)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Welcome Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Welcome,",
                        style = Typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    )
                    Text(
                        text = profile.name,
                        style = Typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy,
                            fontSize = 20.sp
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onLogout != null) {
                        IconButton(
                            onClick = { showLogoutDialog = true },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Log Out",
                                tint = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Box(
                        modifier = Modifier.clickable { onNavigateToSection("profile") }
                    ) {
                        StudentAvatar(
                            photoUrl = profile.photoUrl,
                            name = profile.name,
                            size = 40.dp,
                            textStyle = Typography.labelLarge,
                            backgroundColor = PrimaryNavyLight
                        )
                    }
                }
            }
        }

        // Stats Row (Total Students, Classes Today, Pending Tasks) - Clickable to open students list!
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Total Students",
                    value = "$displayStudentCount",
                    highlightColor = PrimaryNavy,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.openStudentListDialog() }
                )
                StatCard(
                    title = "Classes Today",
                    value = "${todayClasses.size}",
                    highlightColor = InfoBlue,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToSection("schedule") }
                )
                StatCard(
                    title = "Pending Tasks",
                    value = "${teacherAssignments.size}",
                    highlightColor = AccentOrange,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToSection("assignments") }
                )
            }
        }

        // Today's Schedule matching design
        item {
            val scheduleHeaderTitle = if (!isShowingUpcoming) {
                "Today's Schedule ($currentDayFullName)"
            } else {
                val nextFull = when (nextTeachingDay) {
                    "Mon" -> "Monday"
                    "Tue" -> "Tuesday"
                    "Wed" -> "Wednesday"
                    "Thu" -> "Thursday"
                    "Fri" -> "Friday"
                    "Sat" -> "Saturday"
                    else -> "Upcoming"
                }
                "Upcoming Schedule ($nextFull)"
            }

            SectionHeader(
                title = scheduleHeaderTitle,
                actionText = "View All",
                onActionClick = { onNavigateToSection("schedule") }
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                if (displayClasses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No classes scheduled for today.",
                            style = Typography.bodyMedium.copy(color = TextSecondary)
                        )
                    }
                } else {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        displayClasses.take(4).forEachIndexed { index, item ->
                            if (index > 0) {
                                HorizontalDivider(color = DividerColor)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.subjectName,
                                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    )
                                    val subtext = when {
                                        item.room.isBlank() || item.room.equals("LH-1", ignoreCase = true) || item.room.equals("LH1", ignoreCase = true) -> item.section
                                        item.section.isNotBlank() -> "${item.room} • ${item.section}"
                                        else -> item.room
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = subtext,
                                        style = Typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                    )
                                }
                                StatusBadge(
                                    text = item.time,
                                    style = if (index == 0) BadgeStyle.PRIMARY else BadgeStyle.NEUTRAL
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Actions Grid (Mark Attendance, Upload Material, Create Assignment, Enter Marks, Notices Desk, Student Analytics)
        item {
            SectionHeader(title = "Quick Actions")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    QuickActionGridItem(
                        title = "Mark Attendance",
                        icon = Icons.Default.CheckCircle,
                        iconBgColor = if (ThemeState.isDarkMode.value) Color(0xFF2563EB) else PrimaryNavyDark,
                        onClick = { onNavigateToSection("attendance") },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionGridItem(
                        title = "Upload Material",
                        icon = Icons.Default.UploadFile,
                        iconBgColor = Color(0xFF6B46C1),
                        onClick = { onNavigateToSection("academics") },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionGridItem(
                        title = "Create Assignment",
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        iconBgColor = AccentOrange,
                        onClick = { onNavigateToSection("assignments") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    QuickActionGridItem(
                        title = "Schedule",
                        icon = Icons.Default.DateRange,
                        iconBgColor = SuccessGreen,
                        onClick = { onNavigateToSection("schedule") },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionGridItem(
                        title = "Notices Desk",
                        icon = Icons.Default.Campaign,
                        iconBgColor = Color(0xFF0D9488),
                        onClick = { onNavigateToSection("notices_leave") },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionGridItem(
                        title = "Student Analytics",
                        icon = Icons.Default.Analytics,
                        iconBgColor = ErrorRed,
                        badgeCount = if (atRiskCount > 0) atRiskCount else null,
                        onClick = { viewModel.openStudentListDialog() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // --- ENROLLED STUDENTS DIRECTORY MODAL ---
    if (isStudentListDialogVisible) {
        val filteredStudents = roster.filter { student ->
            studentSearchQuery.isBlank() ||
                student.studentName.contains(studentSearchQuery, ignoreCase = true) ||
                student.rollNo.contains(studentSearchQuery, ignoreCase = true) ||
                student.department.contains(studentSearchQuery, ignoreCase = true)
        }

        AlertDialog(
            onDismissRequest = { viewModel.closeStudentListDialog() },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PrimaryNavy.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = PrimaryNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Enrolled Students",
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryNavy, fontSize = 16.sp)
                        )
                        Text(
                            text = "${filteredStudents.size} Students in your classes",
                            style = Typography.bodySmall.copy(color = AccentOrange, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                ) {
                    // Search bar
                    VVITTextField(
                        value = studentSearchQuery,
                        onValueChange = { viewModel.onStudentSearchChange(it) },
                        placeholder = "Search by student name or roll no...",
                        leadingIcon = Icons.Default.Search,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (filteredStudents.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No students found matching '$studentSearchQuery'",
                                style = Typography.bodySmall.copy(color = TextSecondary)
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(filteredStudents) { student ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.selectStudentForDetail(student) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            StudentAvatar(
                                                photoUrl = student.photoUrl,
                                                name = student.studentName,
                                                size = 36.dp,
                                                textStyle = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                                                backgroundColor = PrimaryNavyLight
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = student.studentName,
                                                    style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                )
                                                Text(
                                                    text = "Roll: ${student.rollNo} • ${student.section}",
                                                    style = Typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                                )
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            StatusBadge(
                                                text = "${student.attendancePercentage}% Att",
                                                style = if (student.attendancePercentage >= 75) BadgeStyle.SUCCESS else BadgeStyle.ERROR
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "CGPA: ${student.cgpa}",
                                                style = Typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.closeStudentListDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close", color = CardWhite)
                }
            },
            containerColor = CardWhite,
            shape = RoundedCornerShape(14.dp)
        )
    }

    // --- STUDENT DETAILS POPUP MODAL ---
    if (selectedStudentDetail != null) {
        val student = selectedStudentDetail!!
        AlertDialog(
            onDismissRequest = { viewModel.closeStudentDetail() },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StudentAvatar(
                        photoUrl = student.photoUrl,
                        name = student.studentName,
                        size = 46.dp,
                        textStyle = Typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp),
                        backgroundColor = PrimaryNavy
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = student.studentName,
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryNavy, fontSize = 16.sp)
                        )
                        Text(
                            text = "Roll No: ${student.rollNo}",
                            style = Typography.bodySmall.copy(color = AccentOrange, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    // Quick stats badges row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = SurfaceVariantLight,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Attendance", style = Typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp))
                                Text(text = "${student.attendancePercentage}%", style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = if (student.attendancePercentage >= 75) SuccessGreen else ErrorRed))
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = SurfaceVariantLight,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "CGPA", style = Typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp))
                                Text(text = "${student.cgpa}", style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryNavy))
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = SurfaceVariantLight,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Section", style = Typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp))
                                Text(text = student.section, style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = AccentOrange, fontSize = 12.sp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Detailed info list
                    StudentDetailRow(icon = Icons.Default.School, label = "Department", value = student.department)
                    StudentDetailRow(icon = Icons.Default.Class, label = "Semester & Section", value = "${student.currentSemester} (${student.section})")
                    StudentDetailRow(icon = Icons.Default.Email, label = "College Email", value = student.email.ifBlank { "student.vvit@college.edu" })
                    StudentDetailRow(icon = Icons.Default.Phone, label = "Student Contact", value = student.phone.ifBlank { "+91 98765 43210" })
                    StudentDetailRow(icon = Icons.Default.FamilyRestroom, label = "Guardian Name", value = student.guardianName)
                    StudentDetailRow(icon = Icons.Default.ContactPhone, label = "Guardian Contact", value = student.guardianContact)
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.closeStudentDetail() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close", color = CardWhite)
                }
            },
            containerColor = CardWhite,
            shape = RoundedCornerShape(14.dp)
        )
    }

    if (showLogoutDialog && onLogout != null) {
        VVITConfirmDialog(
            title = "Log Out",
            message = "Are you sure you want to log out of Recm?",
            confirmText = "Log Out",
            confirmButtonColor = ErrorRed,
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}
