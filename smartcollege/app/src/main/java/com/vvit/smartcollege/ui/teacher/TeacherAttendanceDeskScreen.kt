package com.vvit.smartcollege.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.vvit.smartcollege.data.model.StudentAttendanceStatus
import com.vvit.smartcollege.data.model.StudentRosterItem
import com.vvit.smartcollege.ui.components.*
import com.vvit.smartcollege.ui.theme.*

@Composable
fun TeacherAttendanceDeskScreen(
    viewModel: TeacherViewModel,
    modifier: Modifier = Modifier
) {
    val selectedClass by viewModel.selectedClass.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val assignedSubjects by viewModel.assignedSubjects.collectAsState()
    val roster by viewModel.attendanceRoster.collectAsState()
    val selectedStudentDetail by viewModel.selectedStudentDetail.collectAsState()

    var showClassDropdown by remember { mutableStateOf(false) }
    var showSubjectDropdown by remember { mutableStateOf(false) }
    val classOptions = listOf("IT - Sem 3", "IT - Sem 5", "CSE - Sem 3", "CSE - Sem 5")

    // Refresh roster on display so newly registered students appear immediately
    LaunchedEffect(Unit) {
        viewModel.refreshRoster()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        VVITHeader(
            title = "Attendance Desk",
            subtitle = "Mark Daily Student Presence"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Class, Date & Subject selectors matching design
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showClassDropdown = true }
                        ) {
                            Text(text = "SELECT CLASS", style = Typography.labelSmall.copy(color = TextSecondary, fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = selectedClass,
                                style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = PrimaryNavy)
                            )

                            DropdownMenu(
                                expanded = showClassDropdown,
                                onDismissRequest = { showClassDropdown = false }
                            ) {
                                classOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt) },
                                        onClick = {
                                            viewModel.selectClass(opt)
                                            showClassDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(CardBorder)
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                        ) {
                            Text(text = "DATE", style = Typography.labelSmall.copy(color = TextSecondary, fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = selectedDate,
                                style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = PrimaryNavy)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(CardBorder)
                    )

                    // Assigned Subject Selector
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showSubjectDropdown = true }
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ASSIGNED SUBJECT",
                                    style = Typography.labelSmall.copy(color = TextSecondary, fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = selectedSubject.ifEmpty { "Select Assigned Subject" },
                                    style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = PrimaryNavy),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Subject",
                                tint = PrimaryNavy
                            )
                        }

                        DropdownMenu(
                            expanded = showSubjectDropdown,
                            onDismissRequest = { showSubjectDropdown = false }
                        ) {
                            if (assignedSubjects.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No subjects assigned", style = Typography.bodyMedium) },
                                    onClick = { showSubjectDropdown = false },
                                    enabled = false
                                )
                            } else {
                                assignedSubjects.forEach { sub ->
                                    DropdownMenuItem(
                                        text = { Text(sub, style = Typography.bodyMedium) },
                                        onClick = {
                                            viewModel.selectSubject(sub)
                                            showSubjectDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Student List Header with "Mark All Present" & "Clear" actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val markedCount = roster.count { it.status != StudentAttendanceStatus.UNMARKED }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Student List (${roster.size})",
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    if (markedCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "($markedCount marked)",
                            style = Typography.bodySmall.copy(color = AccentOrange, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (markedCount > 0) {
                        TextButton(
                            onClick = { viewModel.clearAttendance() },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Clear",
                                style = Typography.labelMedium.copy(
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    TextButton(
                        onClick = { viewModel.markAllPresent() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Mark All Present",
                            style = Typography.labelMedium.copy(
                                color = AccentOrange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Roster items with P / A / L chips & tap-to-view student details
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(roster) { student ->
                    StudentRosterRow(
                        student = student,
                        onStudentClick = { viewModel.selectStudentForDetail(student) },
                        onStatusChange = { newStatus ->
                            viewModel.updateStudentAttendance(student.id, newStatus)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Submit Attendance Button
            VVITPrimaryButton(
                text = "Submit Attendance",
                onClick = { viewModel.submitAttendance() }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
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
                                Text(text = "Today Status", style = Typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp))
                                Text(
                                    text = student.status.name,
                                    style = Typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = when (student.status) {
                                            StudentAttendanceStatus.PRESENT -> SuccessGreen
                                            StudentAttendanceStatus.ABSENT -> ErrorRed
                                            StudentAttendanceStatus.LEAVE -> WarningYellow
                                            StudentAttendanceStatus.UNMARKED -> TextSecondary
                                        },
                                        fontSize = 11.sp
                                    )
                                )
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
}

@Composable
fun StudentDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryNavyLight,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, style = Typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp))
            Text(text = value, style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 12.sp))
        }
    }
}

@Composable
fun StudentRosterRow(
    student: StudentRosterItem,
    onStudentClick: () -> Unit,
    onStatusChange: (StudentAttendanceStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Student identity item (clickable to view student details)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onStudentClick() }
                    .padding(vertical = 2.dp)
            ) {
                StudentAvatar(
                    photoUrl = student.photoUrl,
                    name = student.studentName,
                    size = 34.dp,
                    textStyle = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                    backgroundColor = PrimaryNavyLight
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = student.studentName,
                            style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "View Details",
                            tint = TextSecondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = "Roll: ${student.rollNo}",
                        style = Typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                    )
                }
            }

            // P / A / L Status Toggle Chips matching design
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AttendanceStatusChip(
                    label = "P",
                    isSelected = student.status == StudentAttendanceStatus.PRESENT,
                    selectedColor = SuccessGreen,
                    onClick = { onStatusChange(StudentAttendanceStatus.PRESENT) }
                )
                AttendanceStatusChip(
                    label = "A",
                    isSelected = student.status == StudentAttendanceStatus.ABSENT,
                    selectedColor = ErrorRed,
                    onClick = { onStatusChange(StudentAttendanceStatus.ABSENT) }
                )
                AttendanceStatusChip(
                    label = "L",
                    isSelected = student.status == StudentAttendanceStatus.LEAVE,
                    selectedColor = WarningYellow,
                    onClick = { onStatusChange(StudentAttendanceStatus.LEAVE) }
                )
            }
        }
    }
}

@Composable
fun AttendanceStatusChip(
    label: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (isSelected) selectedColor else SurfaceVariantLight)
            .border(
                width = 1.dp,
                color = if (isSelected) selectedColor else CardBorder,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = Typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else TextSecondary,
                fontSize = 12.sp
            )
        )
    }
}
