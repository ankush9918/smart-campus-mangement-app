package com.vvit.smartcollege.ui.student

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.vvit.smartcollege.data.model.FacultyInfo
import com.vvit.smartcollege.ui.components.EmptyStateView
import com.vvit.smartcollege.ui.components.StudentAvatar
import com.vvit.smartcollege.ui.components.VVITHeader
import com.vvit.smartcollege.ui.theme.*

@Composable
fun StudentFacultyScreen(
    viewModel: StudentViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val facultyList by viewModel.facultyList.collectAsState()
    val isFacultyLoading by viewModel.isFacultyLoading.collectAsState()
    var selectedFaculty by remember { mutableStateOf<FacultyInfo?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    BackHandler(onBack = onBack)

    LaunchedEffect(Unit) {
        viewModel.refreshFaculty()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshFaculty()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        VVITHeader(
            title = "Faculty & Subjects",
            subtitle = "Tap any faculty to view details",
            onBackClick = onBack
        )

        if (isFacultyLoading && facultyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryNavyLight)
            }
        } else if (facultyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    title = "No Faculty Listed",
                    message = "No faculty records found in Supabase."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 20.dp)
            ) {
                items(facultyList, key = { it.id }) { faculty ->
                    FacultySubjectCard(
                        faculty = faculty,
                        onClick = { selectedFaculty = faculty }
                    )
                }
            }
        }
    }

    if (selectedFaculty != null) {
        FacultyDetailDialog(
            faculty = selectedFaculty!!,
            onDismiss = { selectedFaculty = null }
        )
    }
}

@Composable
private fun FacultySubjectCard(
    faculty: FacultyInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudentAvatar(
                    photoUrl = faculty.photoUrl,
                    name = faculty.name,
                    size = 46.dp,
                    textStyle = Typography.titleMedium,
                    backgroundColor = PrimaryNavyLight
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = faculty.name,
                            style = Typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                        )
                        if (faculty.shortCode.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(InfoBlueLight)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = faculty.shortCode,
                                    style = Typography.labelSmall.copy(
                                        color = InfoBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                    val subtitleLine = listOf(faculty.designation, faculty.department)
                        .filter { it.isNotBlank() }
                        .joinToString(" • ")
                    if (subtitleLine.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitleLine,
                            style = Typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View Faculty Details",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (faculty.subjects.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (faculty.subjects.size == 1) "SUBJECT TAUGHT" else "SUBJECTS TAUGHT",
                    style = Typography.labelSmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    faculty.subjects.forEach { subject ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = SurfaceVariantLight,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = AccentOrange,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = subject,
                                style = Typography.bodySmall.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FacultyDetailDialog(
    faculty: FacultyInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                StudentAvatar(
                    photoUrl = faculty.photoUrl,
                    name = faculty.name,
                    size = 50.dp,
                    textStyle = Typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    backgroundColor = PrimaryNavyLight
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = faculty.name,
                        style = Typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                    )
                    if (faculty.designation.isNotBlank()) {
                        Text(
                            text = faculty.designation,
                            style = Typography.bodySmall.copy(
                                color = AccentOrange,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        )
                    }
                    val idLine = buildString {
                        if (faculty.shortCode.isNotBlank()) append("Code: ${faculty.shortCode}")
                        if (faculty.employeeId.isNotBlank()) {
                            if (isNotEmpty()) append(" • ")
                            append("ID: ${faculty.employeeId}")
                        }
                    }
                    if (idLine.isNotBlank()) {
                        Text(
                            text = idLine,
                            style = Typography.labelSmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (faculty.subjects.isNotEmpty()) {
                    Text(
                        text = "Assigned Subjects",
                        style = Typography.labelMedium.copy(
                            color = PrimaryNavy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        faculty.subjects.forEach { subject ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = SurfaceVariantLight,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(AccentOrange)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = subject,
                                    style = Typography.bodySmall.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = DividerColor)
                }

                FacultyDetailRow(
                    icon = Icons.Default.Phone,
                    label = "Mobile Number",
                    value = faculty.phone.ifBlank { "Not Available" },
                    onClick = if (faculty.phone.isNotBlank()) {
                        {
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:${faculty.phone}"))
                                )
                            }
                        }
                    } else null
                )

                FacultyDetailRow(
                    icon = Icons.Default.Email,
                    label = "Email ID",
                    value = faculty.email.ifBlank { "Not Available" },
                    onClick = if (faculty.email.isNotBlank()) {
                        {
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${faculty.email}"))
                                )
                            }
                        }
                    } else null
                )

                if (faculty.employeeId.isNotBlank()) {
                    FacultyDetailRow(
                        icon = Icons.Default.Badge,
                        label = "Faculty ID",
                        value = faculty.employeeId
                    )
                }

                if (faculty.department.isNotBlank()) {
                    FacultyDetailRow(
                        icon = Icons.Default.School,
                        label = "Department",
                        value = faculty.department
                    )
                }

                if (faculty.qualification.isNotBlank()) {
                    FacultyDetailRow(
                        icon = Icons.Default.WorkspacePremium,
                        label = "Qualification",
                        value = faculty.qualification
                    )
                }

                if (faculty.experienceYears > 0) {
                    FacultyDetailRow(
                        icon = Icons.Default.Timeline,
                        label = "Experience",
                        value = "${faculty.experienceYears} Years"
                    )
                }

                if (faculty.cabinRoom.isNotBlank()) {
                    FacultyDetailRow(
                        icon = Icons.Default.LocationOn,
                        label = "Cabin / Office",
                        value = faculty.cabinRoom
                    )
                }

                if (faculty.teachingDays.isNotEmpty()) {
                    FacultyDetailRow(
                        icon = Icons.Default.DateRange,
                        label = "Schedule Days",
                        value = faculty.teachingDays.joinToString(", ")
                    )
                }

                if (faculty.rooms.isNotEmpty()) {
                    FacultyDetailRow(
                        icon = Icons.Default.MeetingRoom,
                        label = "Classrooms / Labs",
                        value = faculty.rooms.joinToString(", ")
                    )
                }

                if (faculty.linkedin.isNotBlank()) {
                    FacultyDetailRow(
                        icon = Icons.Default.Link,
                        label = "LinkedIn",
                        value = faculty.linkedin,
                        onClick = {
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(faculty.linkedin))
                                )
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavyLight),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Close", color = Color.White)
            }
        },
        containerColor = CardWhite,
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
private fun FacultyDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryNavy,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = Typography.labelSmall.copy(
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            )
            Text(
                text = value,
                style = Typography.bodySmall.copy(
                    color = if (onClick != null) InfoBlue else TextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            )
        }
    }
}
