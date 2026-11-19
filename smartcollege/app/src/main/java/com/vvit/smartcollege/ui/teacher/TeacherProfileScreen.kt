package com.vvit.smartcollege.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vvit.smartcollege.ui.components.*
import com.vvit.smartcollege.ui.student.ProfileInfoSection
import com.vvit.smartcollege.ui.theme.*

@Composable
fun TeacherProfileScreen(
    viewModel: TeacherViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.profile.collectAsState()
    val assignedSubjects by viewModel.assignedSubjects.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        VVITHeader(
            title = "My Profile",
            subtitle = "Faculty Identity & Records",
            onLogoutClick = { showLogoutDialog = true }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Identity Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StudentAvatar(
                        photoUrl = profile.photoUrl,
                        name = profile.name,
                        size = 54.dp,
                        textStyle = Typography.headlineSmall,
                        backgroundColor = PrimaryNavy
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.name,
                            style = Typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Faculty ID: ${profile.employeeId}",
                            style = Typography.bodySmall.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = profile.designation,
                            style = Typography.labelSmall.copy(
                                color = AccentOrange,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    ThemeToggleButton()
                }
            }

            // Personal & Contact Details Card
            ProfileInfoSection(
                title = "Personal Details",
                items = listOf(
                    "Email ID" to profile.email,
                    "Phone Number" to profile.phone,
                    "Faculty ID" to profile.employeeId,
                    "Designation" to profile.designation
                )
            )

            // Academic & Department Details Card
            ProfileInfoSection(
                title = "Department & Academic Role",
                items = listOf(
                    "Department" to profile.department,
                    "Assigned Subjects" to "${profile.activeSubjectsCount} Subjects",
                    "Total Students" to "${profile.totalStudents} Students"
                )
            )

            // Assigned Subjects Card
            if (assignedSubjects.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Assigned Teaching Subjects",
                            style = Typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy,
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        assignedSubjects.forEachIndexed { index, subject ->
                            if (index > 0) Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = SurfaceVariantLight,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(PrimaryNavy, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = subject,
                                    style = Typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            VVITOutlinedButton(
                text = "Sign Out",
                onClick = { showLogoutDialog = true },
                borderColor = ErrorRed,
                textColor = ErrorRed
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showLogoutDialog) {
        VVITConfirmDialog(
            title = "Sign Out",
            message = "Are you sure you want to log out of your faculty account?",
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

@Composable
fun TeacherScheduleScreen(
    viewModel: TeacherViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.profile.collectAsState()
    val schedule by viewModel.schedule.collectAsState()
    val currentDayFromCalendar = remember {
        val cal = java.util.Calendar.getInstance()
        when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> "Mon"
            java.util.Calendar.TUESDAY -> "Tue"
            java.util.Calendar.WEDNESDAY -> "Wed"
            java.util.Calendar.THURSDAY -> "Thu"
            java.util.Calendar.FRIDAY -> "Fri"
            java.util.Calendar.SATURDAY -> "Sat"
            else -> "Mon"
        }
    }
    var selectedDay by remember { mutableStateOf(currentDayFromCalendar) }
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val filteredSchedule = remember(selectedDay, schedule) {
        schedule.filter { it.dayOfWeek.equals(selectedDay, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        VVITHeader(
            title = "Class Schedule",
            subtitle = "Daily Teaching Routine & Load"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Day Selector for Schedule
            VVITSegmentedTabRow(
                tabs = days,
                selectedIndex = days.indexOf(selectedDay).coerceAtLeast(0),
                onTabSelected = { selectedDay = days[it] }
            )

            // Teaching Periods for the selected day
            if (filteredSchedule.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Classes Scheduled",
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "There are no teaching periods scheduled for $selectedDay.",
                            style = Typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        filteredSchedule.forEachIndexed { index, item ->
                            if (index > 0) HorizontalDivider(color = DividerColor)
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.time,
                                        style = Typography.labelSmall.copy(
                                            color = AccentOrange,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    StatusBadge(text = item.section, style = BadgeStyle.PRIMARY)
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = item.subjectName,
                                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                )
                                val subtext = when {
                                    item.room.isBlank() || item.room.equals("LH-1", ignoreCase = true) || item.room.equals("LH1", ignoreCase = true) -> "${item.section} • ${item.lectureType}"
                                    else -> "${item.section} • ${item.room} • ${item.lectureType}"
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = subtext,
                                    style = Typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
