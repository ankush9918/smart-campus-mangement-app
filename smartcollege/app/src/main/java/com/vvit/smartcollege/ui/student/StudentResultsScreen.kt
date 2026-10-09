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
import com.vvit.smartcollege.data.model.GradeItem
import com.vvit.smartcollege.data.model.SemesterResult
import com.vvit.smartcollege.ui.components.*
import com.vvit.smartcollege.ui.theme.*

@Composable
fun StudentResultsScreen(
    viewModel: StudentViewModel,
    modifier: Modifier = Modifier
) {
    val semesterResults by viewModel.semesterResults.collectAsState()
    val selectedSem by viewModel.selectedSemester.collectAsState()

    var activeTab by remember { mutableStateOf(1) }
    val currentResult = semesterResults.find { it.semesterNumber == selectedSem } ?: semesterResults.first()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        VVITHeader(
            title = "Exams & Performance",
            subtitle = "Evaluation results & timelines"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            VVITSegmentedTabRow(
                tabs = listOf("Exam Schedule", "Results"),
                selectedIndex = activeTab,
                onTabSelected = { activeTab = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (activeTab == 0) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        SectionHeader(title = "Upcoming Semester End Exams")
                    }
                    items(listOf(
                        Triple("Data Structures & Algorithms", "Oct 18, 2026 • 10:00 AM", "Block C - Hall 1"),
                        Triple("Database Management System", "Oct 20, 2026 • 02:00 PM", "Block A - Room 204"),
                        Triple("Computer Networks", "Oct 22, 2026 • 10:00 AM", "Block C - Hall 2")
                    )) { (name, date, venue) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = name,
                                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "$date • $venue",
                                    style = Typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
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
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "SEMESTER SGPA",
                                        style = Typography.bodySmall.copy(
                                            color = TextSecondary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = String.format("%.2f", currentResult.sgpa),
                                        style = Typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryNavy,
                                            fontSize = 24.sp
                                        )
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Select Semester",
                                        style = Typography.labelSmall.copy(color = TextSecondary)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        (1..4).forEach { sem ->
                                            VVITActionChip(
                                                text = "Sem $sem",
                                                selected = selectedSem == sem,
                                                onClick = { viewModel.selectSemester(sem) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        SectionHeader(title = "${currentResult.semesterName} Gradesheet")

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SurfaceVariantLight)
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Subject",
                                        modifier = Modifier.weight(2f),
                                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextSecondary)
                                    )
                                    Text(
                                        text = "Int",
                                        modifier = Modifier.weight(0.7f),
                                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextSecondary)
                                    )
                                    Text(
                                        text = "Ext",
                                        modifier = Modifier.weight(0.7f),
                                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextSecondary)
                                    )
                                    Text(
                                        text = "Tot",
                                        modifier = Modifier.weight(0.7f),
                                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextSecondary)
                                    )
                                    Text(
                                        text = "Grd",
                                        modifier = Modifier.weight(0.7f),
                                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextSecondary)
                                    )
                                }

                                currentResult.grades.forEach { grade ->
                                    HorizontalDivider(color = DividerColor)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = grade.subjectName,
                                            modifier = Modifier.weight(2f),
                                            style = Typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = TextPrimary)
                                        )
                                        Text(
                                            text = "${grade.internalMarks}",
                                            modifier = Modifier.weight(0.7f),
                                            style = Typography.bodySmall.copy(color = TextSecondary)
                                        )
                                        Text(
                                            text = "${grade.externalMarks}",
                                            modifier = Modifier.weight(0.7f),
                                            style = Typography.bodySmall.copy(color = TextSecondary)
                                        )
                                        Text(
                                            text = "${grade.totalMarks}",
                                            modifier = Modifier.weight(0.7f),
                                            style = Typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = PrimaryNavy)
                                        )
                                        Text(
                                            text = grade.grade,
                                            modifier = Modifier.weight(0.7f),
                                            style = Typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (grade.grade in listOf("O", "A+", "A")) SuccessGreen else PrimaryNavy
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        SectionHeader(title = "CGPA Progression")
                        SimpleBarChart(
                            items = semesterResults.map { "Sem ${it.semesterNumber}" to it.sgpa.toFloat() },
                            maxVal = 10f,
                            barColor = PrimaryNavy
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
