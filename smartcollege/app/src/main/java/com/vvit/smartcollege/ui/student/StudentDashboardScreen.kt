package com.vvit.smartcollege.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.vvit.smartcollege.data.model.StudentTimetableEntry
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vvit.smartcollege.ui.components.*
import com.vvit.smartcollege.ui.theme.*

import androidx.compose.material.icons.automirrored.filled.ExitToApp

@Composable
fun StudentDashboardScreen(
    viewModel: StudentViewModel,
    onNavigateToSection: (String) -> Unit,
    onLogout: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val profile by viewModel.profile.collectAsState()
    val notices by viewModel.notices.collectAsState()
    val timetable by viewModel.timetable.collectAsState()
    val assignments by viewModel.assignments.collectAsState()
    val pendingAssignmentsCount = remember(assignments) {
        assignments.count { it.status == com.vvit.smartcollege.data.model.AssignmentStatus.PENDING }
    }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

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

    // Filter today's classes (excluding lunch break for cleaner class schedule)
    val todayClasses = remember(timetable, currentDay) {
        val classesForDay = timetable.filter { 
            (it.dayOfWeek.equals(currentDay, ignoreCase = true) || it.dayOfWeek.startsWith(currentDay, ignoreCase = true))
            && !it.isBreak
        }
        if (classesForDay.isNotEmpty()) {
            classesForDay
        } else {
            // If today has no classes (e.g. Saturday or Sunday), show Monday's schedule so student sees their upcoming classes
            timetable.filter { 
                (it.dayOfWeek.equals("Mon", ignoreCase = true) || it.dayOfWeek.startsWith("Mon", ignoreCase = true))
                && !it.isBreak
            }
        }
    }

    val isWeekendOrEmpty = remember(timetable, currentDay) {
        timetable.none { 
            (it.dayOfWeek.equals(currentDay, ignoreCase = true) || it.dayOfWeek.startsWith(currentDay, ignoreCase = true))
            && !it.isBreak
        }
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

        // Quick Stats row
        item {
            val semNumber = remember(profile.currentSemester) {
                Regex("""\d+""").find(profile.currentSemester)?.value ?: "3"
            }
            val semSubtitle = remember(profile.currentSemester) {
                val yr = Regex("""Year\s*\d+""", RegexOption.IGNORE_CASE).find(profile.currentSemester)?.value
                yr ?: "Active Term"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Attendance",
                    value = "${profile.attendancePercentage}%",
                    subtitle = "Satisfactory",
                    highlightColor = SuccessGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToSection("attendance") }
                )
                StatCard(
                    title = "Semester",
                    value = "Sem $semNumber",
                    subtitle = semSubtitle,
                    highlightColor = InfoBlue,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToSection("timetable") }
                )
                StatCard(
                    title = "Pending Tasks",
                    value = "3",
                    subtitle = "Due this week",
                    highlightColor = AccentOrange,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToSection("academics") }
                )
            }
        }

        // Quick Actions Grid (Clean Academic Actions)
        item {
            SectionHeader(title = "Quick Actions")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    QuickActionGridItem(
                        title = "Attendance",
                        icon = Icons.Default.CheckCircle,
                        iconBgColor = if (ThemeState.isDarkMode.value) Color(0xFF2563EB) else PrimaryNavyDark,
                        onClick = { onNavigateToSection("attendance") },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionGridItem(
                        title = "Timetable",
                        icon = Icons.Default.DateRange,
                        iconBgColor = InfoBlue,
                        onClick = { onNavigateToSection("timetable") },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionGridItem(
                        title = "Assignments",
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        iconBgColor = AccentOrange,
                        badgeCount = if (pendingAssignmentsCount > 0) pendingAssignmentsCount else null,
                        onClick = { onNavigateToSection("academics") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    QuickActionGridItem(
                        title = "Materials",
                        icon = Icons.Default.FolderOpen,
                        iconBgColor = Color(0xFF6B46C1),
                        onClick = { onNavigateToSection("academics") },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionGridItem(
                        title = "Notices",
                        icon = Icons.Default.Notifications,
                        iconBgColor = Color(0xFF0D9488),
                        onClick = { onNavigateToSection("notices_fees") },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionGridItem(
                        title = "Faculty",
                        icon = Icons.Default.Groups,
                        iconBgColor = Color(0xFF4F46E5),
                        onClick = { onNavigateToSection("faculty") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Today's Classes (Dynamic from Timetable)
        item {
            SectionHeader(
                title = if (isWeekendOrEmpty) "Upcoming Classes (Monday)" else "Today's Classes",
                actionText = "Full Schedule",
                onActionClick = { onNavigateToSection("timetable") }
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                if (todayClasses.isEmpty()) {
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
                        todayClasses.take(4).forEachIndexed { index, slot ->
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
                                        text = slot.subjectName,
                                        style = Typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = TextPrimary
                                        )
                                    )
                                    val subtext = when {
                                        slot.room.isBlank() || slot.room.equals("LH-1", ignoreCase = true) || slot.room.equals("LH1", ignoreCase = true) -> slot.facultyName
                                        slot.facultyName.isNotBlank() -> "${slot.room} • ${slot.facultyName}"
                                        else -> slot.room
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = subtext,
                                        style = Typography.bodySmall.copy(
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusBadge(
                                    text = slot.time.split("-").firstOrNull()?.trim() ?: slot.time,
                                    style = if (index == 0) BadgeStyle.PRIMARY else BadgeStyle.NEUTRAL
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Notices (only shown when genuine notices exist)
        if (notices.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Recent Notices",
                    actionText = "View All",
                    onActionClick = { onNavigateToSection("notices_fees") }
                )

                val notice = notices.first()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AccentOrangeLight),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentOrange.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(text = notice.category, style = BadgeStyle.PRIMARY)
                            Text(
                                text = notice.timeAgo,
                                style = Typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = notice.title,
                            style = Typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy,
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = notice.displayDescription,
                            style = Typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp),
                            maxLines = 2
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
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

private fun getInitials(name: String): String {
    val clean = name.trim()
    if (clean.isBlank()) return "ST"
    val parts = clean.split("\\s+".toRegex()).filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
        clean.length >= 2 -> clean.substring(0, 2).uppercase()
        clean.isNotEmpty() -> clean.first().uppercaseChar().toString()
        else -> "ST"
    }
}
