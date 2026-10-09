package com.vvit.smartcollege.ui.teacher

import android.app.DatePickerDialog
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vvit.smartcollege.ui.components.*
import com.vvit.smartcollege.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun TeacherAssignmentsHubScreen(
    viewModel: TeacherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(0) } // 0: Create, 1: Uploaded Tasks

    val assignedSubjects by viewModel.assignedSubjects.collectAsState()
    val activeSubject by viewModel.selectedSubject.collectAsState()
    val teacherAssignments by viewModel.assignments.collectAsState()

    var selectedSubject by remember(activeSubject) { mutableStateOf(activeSubject) }
    var showSubjectDropdown by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var maxMarks by remember { mutableStateOf("100") }
    var dueDate by remember { mutableStateOf("") }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFileUri by remember { mutableStateOf<String?>(null) }
    var selectedFileBytes by remember { mutableStateOf<ByteArray?>(null) }
    var selectedFileMimeType by remember { mutableStateOf("application/octet-stream") }

    // System Document Picker for selecting actual assignment file
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri.toString()
            var name = "Assignment_Document"
            val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
            selectedFileMimeType = mime
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        name = cursor.getString(nameIndex)
                    }
                }
            } catch (_: Exception) {}
            selectedFileName = name
            try {
                selectedFileBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            } catch (e: Exception) {
                Log.e("TeacherAssignmentsHub", "Failed to read file bytes: ${e.message}")
            }
        }
    }

    // Native DatePickerDialog for submission deadline
    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                dueDate = sdf.format(cal.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        VVITHeader(
            title = "Assignments Hub",
            subtitle = "Create & Manage Assignments"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            VVITSegmentedTabRow(
                tabs = listOf("Create Assignment", "Uploaded Tasks (${teacherAssignments.size})"),
                selectedIndex = activeTab,
                onTabSelected = { activeTab = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (activeTab == 0) {
                // Create Assignment Form
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Subject Selector
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showSubjectDropdown = true },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, CardBorder),
                                    colors = CardDefaults.outlinedCardColors(containerColor = SurfaceVariantLight)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "SUBJECT",
                                                style = Typography.labelSmall.copy(color = TextSecondary, fontWeight = FontWeight.Bold)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = selectedSubject.ifEmpty { "Select Subject" },
                                                style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = PrimaryNavy),
                                                maxLines = 1
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Select Subject",
                                            tint = PrimaryNavy
                                        )
                                    }
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
                                                    selectedSubject = sub
                                                    showSubjectDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            VVITTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = "Assignment Title",
                                placeholder = "e.g. Unit 3 Programming & Theory Assignment"
                            )

                            VVITTextField(
                                value = instructions,
                                onValueChange = { instructions = it },
                                label = "Task Description / Instructions",
                                placeholder = "Enter detailed instructions, submission guidelines, questions...",
                                singleLine = false,
                                maxLines = 4
                            )

                            // Document / File Picker Section
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "ATTACHMENT / REFERENCE FILE",
                                    style = Typography.labelSmall.copy(color = TextSecondary, fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (selectedFileName != null) {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        color = SurfaceVariantLight,
                                        border = BorderStroke(1.dp, PrimaryNavy.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AttachFile,
                                                    contentDescription = null,
                                                    tint = PrimaryNavy,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = selectedFileName ?: "",
                                                    style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = PrimaryNavy),
                                                    maxLines = 1
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    selectedFileName = null
                                                    selectedFileUri = null
                                                    selectedFileBytes = null
                                                    selectedFileMimeType = "application/octet-stream"
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove file",
                                                    tint = ErrorRed,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = {
                                            filePickerLauncher.launch(
                                                arrayOf(
                                                    "application/pdf",
                                                    "application/msword",
                                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                                    "application/vnd.ms-powerpoint",
                                                    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                                                    "text/*",
                                                    "*/*"
                                                )
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, CardBorder),
                                        colors = ButtonDefaults.outlinedButtonColors(containerColor = CardWhite)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.UploadFile,
                                            contentDescription = null,
                                            tint = PrimaryNavy,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Select File (PDF, DOC, PPTX)",
                                            color = PrimaryNavy,
                                            style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                VVITTextField(
                                    value = maxMarks,
                                    onValueChange = { maxMarks = it },
                                    label = "Max Marks",
                                    placeholder = "100",
                                    modifier = Modifier.weight(1f)
                                )

                                // Interactive Deadline Selector with Calendar Picker
                                Box(modifier = Modifier.weight(1f)) {
                                    Column {
                                        Text(
                                            text = "LAST DATE / DEADLINE",
                                            style = Typography.labelSmall.copy(color = TextSecondary, fontWeight = FontWeight.Bold)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedCard(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { datePickerDialog.show() },
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, CardBorder),
                                            colors = CardDefaults.outlinedCardColors(containerColor = SurfaceVariantLight)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = dueDate.ifEmpty { "Select Date" },
                                                    style = Typography.bodyMedium.copy(
                                                        fontWeight = if (dueDate.isNotEmpty()) FontWeight.SemiBold else FontWeight.Normal,
                                                        color = if (dueDate.isNotEmpty()) PrimaryNavy else TextSecondary
                                                    )
                                                )
                                                Icon(
                                                    imageVector = Icons.Default.CalendarToday,
                                                    contentDescription = "Pick Deadline",
                                                    tint = PrimaryNavy,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    VVITPrimaryButton(
                        text = "Upload / Publish Assignment",
                        onClick = {
                            if (title.isBlank()) {
                                return@VVITPrimaryButton
                            }
                            val resolvedBytes = selectedFileBytes ?: selectedFileUri?.let { uriStr ->
                                try {
                                    context.contentResolver.openInputStream(Uri.parse(uriStr))?.use { it.readBytes() }
                                } catch (_: Exception) {
                                    null
                                }
                            }
                            viewModel.createAssignment(
                                title = title,
                                instructions = instructions,
                                maxMarks = maxMarks.toIntOrNull() ?: 100,
                                dueDate = dueDate.ifBlank { "Oct 30, 2026" },
                                subjectName = selectedSubject,
                                fileName = selectedFileName,
                                fileUri = selectedFileUri,
                                fileBytes = resolvedBytes,
                                mimeType = selectedFileMimeType
                            )
                            title = ""
                            instructions = ""
                            dueDate = ""
                            selectedFileName = null
                            selectedFileUri = null
                            selectedFileBytes = null
                            selectedFileMimeType = "application/octet-stream"
                            activeTab = 1
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                // Uploaded Tasks View (Real assignments uploaded by professor)
                if (teacherAssignments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Assignment,
                                contentDescription = null,
                                tint = TextSecondary.copy(alpha = 0.4f),
                                modifier = Modifier.size(52.dp)
                            )
                            Text(
                                text = "No assignments published yet",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                            )
                            Text(
                                text = "Assignments you upload will appear here and in the Student Dashboard.",
                                style = Typography.bodySmall.copy(color = TextSecondary, textAlign = TextAlign.Center)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(teacherAssignments) { asg ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = CardWhite),
                                border = BorderStroke(1.dp, CardBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = asg.subjectName,
                                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = PrimaryNavy, fontSize = 11.sp)
                                        )
                                        StatusBadge(text = "Published", style = BadgeStyle.SUCCESS)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = asg.title,
                                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    )

                                    if (asg.cleanInstructions.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = asg.cleanInstructions,
                                            style = Typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
                                        )
                                    }

                                    if (!asg.fileName.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = SurfaceVariantLight,
                                            border = BorderStroke(1.dp, CardBorder),
                                            modifier = if (!asg.fileUri.isNullOrBlank()) {
                                                Modifier.clickable {
                                                    try {
                                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(asg.fileUri)).apply {
                                                            flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                                        }
                                                        context.startActivity(intent)
                                                    } catch (_: Exception) {}
                                                }
                                            } else Modifier
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AttachFile,
                                                    contentDescription = null,
                                                    tint = PrimaryNavy,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = asg.fileName,
                                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, color = PrimaryNavy)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Deadline: ${asg.dueDate}",
                                            style = Typography.labelSmall.copy(color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        )
                                        Text(
                                            text = "Max Marks: ${asg.maxMarks}",
                                            style = Typography.labelSmall.copy(color = TextSecondary, fontSize = 11.sp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
