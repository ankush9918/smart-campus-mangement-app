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
import com.vvit.smartcollege.ui.theme.*

@Composable
fun TeacherProfileScreen(
    viewModel: TeacherViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.profile.collectAsState()
    val schedule by viewModel.schedule.collectAsState()
    var selectedDay by remember { mutableStateOf("Mon") }
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val filteredSchedule = remember(selectedDay, schedule) {
        schedule.filter { it.dayOfWeek.equals(selectedDay, ignoreCase = true) }
    }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        VVITHeader(
            title = "Profile & Schedule",
            subtitle = "Employee Info & Teaching Load",
            onLogoutClick = { showLogoutDialog = true }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                            text = "ID: ${profile.employeeId}",
                            style = Typography.bodySmall.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${profile.designation} • ${profile.department}",
                            style = Typography.labelSmall.copy(
                                color = AccentOrange,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    ThemeToggleButton()
                }
            }

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

            // Weekly Teaching Load Card matching design
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Weekly Teaching Load",
                        style = Typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy,
                            fontSize = 14.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Total Lectures Scheduled", style = Typography.bodySmall.copy(color = TextSecondary))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${profile.weeklyWorkloadHours} Hours / Week",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryNavy)
                            )
                        }

                        Column {
                            Text(text = "Active Subject Domains", style = Typography.bodySmall.copy(color = TextSecondary))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${profile.activeSubjectsCount} Major Subjects",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SuccessGreen)
                            )
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
