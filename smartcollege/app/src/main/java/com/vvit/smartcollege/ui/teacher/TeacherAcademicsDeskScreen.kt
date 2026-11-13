package com.vvit.smartcollege.ui.teacher

import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vvit.smartcollege.ui.components.*
import com.vvit.smartcollege.ui.theme.*
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun TeacherAcademicsDeskScreen(
    viewModel: TeacherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val studyMaterials by viewModel.studyMaterials.collectAsState()
    val assignedSubjects by viewModel.assignedSubjects.collectAsState()

    var showUploadModal by remember { mutableStateOf(false) }
    var uploadTitle by remember { mutableStateOf("") }
    var uploadSubject by remember(assignedSubjects) {
        mutableStateOf(assignedSubjects.firstOrNull() ?: "Data Structure (BCS-301)")
    }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFileUri by remember { mutableStateOf<String?>(null) }
    var selectedFileBytes by remember { mutableStateOf<ByteArray?>(null) }
    var selectedFileMimeType by remember { mutableStateOf("application/pdf") }
    var selectedFileSize by remember { mutableStateOf("2.4 MB") }
    var selectedFileType by remember { mutableStateOf("PDF") }
    var showSubjectDropdown by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri.toString()
            var name = "Document"
            var sizeStr = "2.4 MB"
            val mime = context.contentResolver.getType(uri) ?: "application/pdf"
            selectedFileMimeType = mime
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        name = cursor.getString(nameIndex)
                    }
                    if (sizeIndex != -1 && cursor.moveToFirst()) {
                        val bytes = cursor.getLong(sizeIndex)
                        if (bytes > 0) {
                            val mb = bytes / (1024.0 * 1024.0)
                            sizeStr = if (mb >= 1.0) String.format(Locale.getDefault(), "%.1f MB", mb) else String.format(Locale.getDefault(), "%d KB", bytes / 1024)
                        }
                    }
                }
            } catch (_: Exception) {}
            selectedFileName = name
            selectedFileSize = sizeStr
            val ext = if (name.contains(".")) name.substringAfterLast(".").uppercase() else "PDF"
            selectedFileType = ext
            if (uploadTitle.isBlank()) {
                uploadTitle = name.substringBeforeLast(".")
            }
            try {
                selectedFileBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            } catch (e: Exception) {
                Log.e("TeacherAcademicsDesk", "Failed to read file bytes: ${e.message}")
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        VVITHeader(
            title = "Academics Desk",
            subtitle = "Syllabus, Notes & Study Materials"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    // Upload Dropzone Card matching design
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showUploadModal = true
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, PrimaryNavy.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Upload",
                                tint = PrimaryNavy,
                                modifier = Modifier.size(38.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Upload New Material",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryNavy)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap to select notes, slides, or documents",
                                style = Typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Supported: PDF, DOC, DOCX, PPT, PPTX, TXT",
                                style = Typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                            )
                        }
                    }
                }

                item {
                    SectionHeader(title = "Previously Shared Files")
                }

                if (studyMaterials.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = TextSecondary.copy(alpha = 0.4f),
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "No study materials shared yet",
                                    style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                )
                                Text(
                                    text = "Upload notes, syllabus or slides above to share them with your students.",
                                    style = Typography.bodySmall.copy(color = TextSecondary, textAlign = TextAlign.Center, fontSize = 11.sp)
                                )
                            }
                        }
                    }
                } else {
                    items(studyMaterials) { material ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val uriStr = material.fileUri ?: material.downloadUrl
                                    if (!uriStr.isNullOrBlank()) {
                                        if (uriStr.contains(".supabase.co/storage/") || uriStr.startsWith("content://")) {
                                            try {
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uriStr)).apply {
                                                    flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                context.startActivity(intent)
                                            } catch (_: Exception) {}
                                        } else {
                                            coroutineScope.launch {
                                                val signed = com.vvit.smartcollege.data.remote.SupabaseApiClient.resolveDownloadUrl(uriStr, material.r2Key)
                                                if (!signed.isNullOrBlank()) {
                                                    try {
                                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(signed)).apply {
                                                            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                                        }
                                                        context.startActivity(intent)
                                                    } catch (_: Exception) {}
                                                }
                                            }
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = AccentOrange,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = material.title,
                                            style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        val fileDisplay = if (!material.fileName.isNullOrBlank() && material.fileName != material.title) " • ${material.fileName}" else ""
                                        Text(
                                            text = "Shared ${material.sharedDate} • ${material.subjectName}$fileDisplay",
                                            style = Typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                        )
                                    }
                                }
                                StatusBadge(text = material.fileType, style = BadgeStyle.NEUTRAL)
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

    if (showUploadModal) {
        AlertDialog(
            onDismissRequest = { showUploadModal = false },
            title = {
                Text(
                    text = "Upload Study Material",
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    VVITTextField(
                        value = uploadTitle,
                        onValueChange = { uploadTitle = it },
                        label = "Study Material Title",
                        placeholder = "e.g. Unit 1 – Data Structures Notes"
                    )

                    // Subject Selector
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showSubjectDropdown = true },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                            colors = CardDefaults.outlinedCardColors(containerColor = SurfaceLight)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "Subject", style = Typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp))
                                    Text(
                                        text = uploadSubject,
                                        style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = PrimaryNavy),
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
                            onDismissRequest = { showSubjectDropdown = false },
                            modifier = Modifier.background(CardWhite)
                        ) {
                            val availableSubjects = if (assignedSubjects.isNotEmpty()) assignedSubjects else listOf(
                                "Computer Organization & Architecture (BCS-302)",
                                "Data Structure (BCS-301)",
                                "Python Programming (BCC-302)",
                                "Digital Electronics (BOE-310)"
                            )
                            availableSubjects.forEach { subj ->
                                DropdownMenuItem(
                                    text = { Text(subj, style = Typography.bodyMedium) },
                                    onClick = {
                                        uploadSubject = subj
                                        showSubjectDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // File Selection
                    OutlinedButton(
                        onClick = {
                            filePickerLauncher.launch(
                                arrayOf(
                                    "application/pdf",
                                    "application/msword",
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                    "application/vnd.ms-powerpoint",
                                    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                                    "text/plain",
                                    "*/*"
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryNavy)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedFileName != null) "Change File" else "Select File from Device",
                            style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    if (selectedFileName != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SurfaceVariantLight,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = AccentOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = selectedFileName!!,
                                        style = Typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = PrimaryNavy),
                                        maxLines = 1
                                    )
                                }
                                Text(
                                    text = selectedFileSize,
                                    style = Typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (uploadTitle.isNotBlank()) {
                            val resolvedBytes = selectedFileBytes ?: selectedFileUri?.let { uriStr ->
                                try {
                                    context.contentResolver.openInputStream(Uri.parse(uriStr))?.use { it.readBytes() }
                                } catch (_: Exception) {
                                    null
                                }
                            }
                            viewModel.uploadMaterial(
                                title = uploadTitle.trim(),
                                subjectName = uploadSubject,
                                fileName = selectedFileName ?: uploadTitle.trim(),
                                fileUri = selectedFileUri,
                                fileBytes = resolvedBytes,
                                mimeType = selectedFileMimeType,
                                fileSize = selectedFileSize,
                                fileType = selectedFileType
                            )
                            showUploadModal = false
                            uploadTitle = ""
                            selectedFileName = null
                            selectedFileUri = null
                            selectedFileBytes = null
                            selectedFileMimeType = "application/pdf"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryNavy,
                        contentColor = CardWhite
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Upload", color = CardWhite)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showUploadModal = false
                        selectedFileBytes = null
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel")
                }
            },
            containerColor = CardWhite,
            shape = RoundedCornerShape(14.dp)
        )
    }
}
