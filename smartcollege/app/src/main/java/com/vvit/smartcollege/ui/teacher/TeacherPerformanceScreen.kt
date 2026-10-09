package com.vvit.smartcollege.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.vvit.smartcollege.ui.components.*
import com.vvit.smartcollege.ui.theme.*

@Composable
fun TeacherPerformanceScreen(
    viewModel: TeacherViewModel,
    modifier: Modifier = Modifier
) {
    val atRiskStudents by viewModel.atRiskStudents.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        VVITHeader(
            title = "Performance Analytics",
            subtitle = "Grade Distributions & Risk Assessment"
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Class Performance Summary (Mid-Sem 2026) matching design
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Class Performance Summary",
                                style = Typography.bodySmall.copy(color = TextSecondary, fontWeight = FontWeight.Bold)
                            )
                            StatusBadge(text = "Mid-Sem 2026", style = BadgeStyle.PRIMARY)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Class Average", style = Typography.labelSmall.copy(color = TextSecondary))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "78.4 %",
                                    style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = PrimaryNavy)
                                )
                            }

                            Column {
                                Text(text = "Pass Percentage", style = Typography.labelSmall.copy(color = TextSecondary))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "94.2 %",
                                    style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = SuccessGreen)
                                )
                            }

                            Column {
                                Text(text = "At Risk Students", style = Typography.labelSmall.copy(color = TextSecondary))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${atRiskStudents.size}",
                                    style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = ErrorRed)
                                )
                            }
                        }
                    }
                }
            }

            // Sessional Grade Spread bar chart matching design
            item {
                SectionHeader(title = "Sessional Grade Spread")
                SimpleBarChart(
                    items = listOf(
                        "A+" to 8.5f,
                        "A" to 7.2f,
                        "B+" to 9.0f,
                        "B" to 6.0f,
                        "C" to 3.5f,
                        "F" to 1.8f
                    ),
                    maxVal = 10f,
                    barColor = PrimaryNavy
                )
            }

            // Action Required: At-Risk Alerts matching design
            item {
                SectionHeader(title = "Action Required: At-Risk Alerts")
            }

            items(atRiskStudents) { student ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${student.studentName} (${student.rollNo})",
                                style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = student.reason,
                                style = Typography.bodySmall.copy(color = ErrorRed, fontSize = 11.sp)
                            )
                        }

                        Button(
                            onClick = { viewModel.remindStudent(student.id) },
                            enabled = !student.isReminded,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (student.isReminded) CardBorder else AccentOrange
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (student.isReminded) "Sent" else "Remind",
                                fontSize = 11.sp,
                                color = if (student.isReminded) TextSecondary else Color.White
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}