package com.vvit.smartcollege.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vvit.smartcollege.data.model.SubjectAttendance
import com.vvit.smartcollege.ui.components.*
import com.vvit.smartcollege.ui.theme.*

@Composable
fun StudentAttendanceScreen(
    viewModel: StudentViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.profile.collectAsState()
    val attendanceList by viewModel.attendanceList.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    // Strictly display Theory subjects only (no Labs, Workshops, or Projects)
    val theoryAttendanceList = remember(attendanceList) {
        attendanceList.filter { item ->
            val name = item.subjectName
            !name.contains("Lab", ignoreCase = true) &&
            !name.contains("Workshop", ignoreCase = true) &&
            !name.contains("Practical", ignoreCase = true) &&
            !name.contains("Project", ignoreCase = true) &&
            !name.contains("Sports", ignoreCase = true) &&
            !name.contains("Library", ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        VVITHeader(
            title = "Attendance",
            subtitle = "Track Class Progress"
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Overall Status Gauge Card matching design
                AttendanceGaugeCard(
                    percentage = profile.attendancePercentage,
                    statusText = "Satisfactory Margin",
                    descriptionText = "Your performance is safely above the required university norm of 75%.",
                    periodText = "October 2026"
                )
            }

            item {
                SectionHeader(title = "Subject-wise Breakdown")
            }

            items(theoryAttendanceList) { item ->
                SubjectAttendanceCard(item = item)
            }


            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SubjectAttendanceCard(
    item: SubjectAttendance,
    modifier: Modifier = Modifier
) {
    val isAlert = item.percentage < 75

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAlert) ErrorRedLight.copy(alpha = 0.5f) else CardWhite
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isAlert) ErrorRed.copy(alpha = 0.4f) else CardBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.subjectName,
                        style = Typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Classes: ${item.attendedClasses}/${item.totalClasses} attended",
                        style = Typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }

                Text(
                    text = "${item.percentage}%",
                    style = Typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isAlert) ErrorRed else SuccessGreen,
                        fontSize = 16.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { item.percentage.toFloat() / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = if (isAlert) ErrorRed else SuccessGreen,
                trackColor = CardBorder
            )

            if (isAlert) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(text = "Low Attendance alert!", style = BadgeStyle.ERROR)
                }
            }
        }
    }
}
