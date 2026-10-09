package com.vvit.smartcollege.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vvit.smartcollege.data.model.StudentTimetableEntry
import com.vvit.smartcollege.ui.components.*
import com.vvit.smartcollege.ui.theme.*

@Composable
fun StudentTimetableScreen(
    viewModel: StudentViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDay by viewModel.selectedTimetableDay.collectAsState()
    val timetable by viewModel.timetable.collectAsState()

    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val filteredEntries = timetable.filter { 
        it.dayOfWeek.equals(selectedDay, ignoreCase = true) || 
        it.dayOfWeek.startsWith(selectedDay, ignoreCase = true) 
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        VVITHeader(
            title = "Weekly Timetable",
            subtitle = "IT - 3rd Semester • Odd 2026-27"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            VVITSegmentedTabRow(
                tabs = days,
                selectedIndex = days.indexOf(selectedDay).coerceAtLeast(0),
                onTabSelected = { index -> viewModel.selectTimetableDay(days[index]) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredEntries.isEmpty()) {
                EmptyStateView(
                    title = "No Classes Scheduled",
                    message = "There are no scheduled lectures for $selectedDay."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredEntries) { entry ->
                        TimetablePeriodCard(entry = entry)
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TimetablePeriodCard(
    entry: StudentTimetableEntry,
    modifier: Modifier = Modifier
) {
    if (entry.isBreak) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = AccentOrangeLight),
            border = androidx.compose.foundation.BorderStroke(1.dp, AccentOrange.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${entry.time} • ${entry.subjectName}",
                    style = Typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = AccentOrangeDark
                    )
                )
                Text(
                    text = entry.duration,
                    style = Typography.labelSmall.copy(color = AccentOrangeDark)
                )
            }
        }
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isOngoing) PrimaryNavyLight.copy(alpha = 0.08f) else CardWhite
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (entry.isOngoing) PrimaryNavy else CardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(72.dp)
            ) {
                Text(
                    text = entry.time,
                    style = Typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy,
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = entry.duration,
                    style = Typography.labelSmall.copy(
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                )
                if (entry.isOngoing) {
                    Spacer(modifier = Modifier.height(4.dp))
                    StatusBadge(text = "NOW", style = BadgeStyle.PRIMARY)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(44.dp)
                    .background(CardBorder)
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.subjectName,
                    style = Typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${entry.facultyName} • ${entry.room}",
                    style = Typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
