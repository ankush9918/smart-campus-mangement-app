package com.vvit.smartcollege.ui.student

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.vvit.smartcollege.data.model.AssignmentStatus
import com.vvit.smartcollege.data.model.StudentAssignment
import com.vvit.smartcollege.data.model.StudentStudyMaterial
import com.vvit.smartcollege.ui.components.*
import com.vvit.smartcollege.ui.theme.*

@Composable
fun StudentAcademicsScreen(
    viewModel: StudentViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val assignments by viewModel.assignments.collectAsState()
    val studyMaterials by viewModel.studyMaterials.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var viewingAssignmentFile by remember { mutableStateOf<StudentAssignment?>(null) }
    var viewingStudyMaterial by remember { mutableStateOf<StudentStudyMaterial?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        VVITHeader(
            title = "Academics",
            subtitle = "Syllabus, Assignments & Study Materials"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            VVITSegmentedTabRow(
                tabs = listOf("Assignments (${assignments.size})", "Study Materials"),
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTab == 0) {
                if (assignments.isEmpty()) {
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
                                modifier = Modifier.size(54.dp)
                            )
                            Text(
                                text = "No assignments available",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                            )
                            Text(
                                text = "Your professors haven't posted any assignments yet.",
                                style = Typography.bodySmall.copy(color = TextSecondary, textAlign = TextAlign.Center)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(assignments) { item ->
                            AssignmentItemCard(
                                item = item,
                                onViewFile = {
                                    viewingAssignmentFile = item
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            } else {
                if (studyMaterials.isEmpty()) {
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
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = TextSecondary.copy(alpha = 0.4f),
                                modifier = Modifier.size(54.dp)
                            )
                            Text(
                                text = "No study materials available",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                            )
                            Text(
                                text = "Your professors haven't shared any study materials yet.",
                                style = Typography.bodySmall.copy(color = TextSecondary, textAlign = TextAlign.Center)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(studyMaterials) { material ->
                            StudyMaterialCard(
                                material = material,
                                onDownload = {
                                    viewingStudyMaterial = material
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }

    if (viewingAssignmentFile != null) {
        val asg = viewingAssignmentFile!!
        AlertDialog(
            onDismissRequest = { viewingAssignmentFile = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = PrimaryNavy,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Assignment Document",
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = asg.fileName ?: "Document",
                        style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryNavy)
                    )
                    Text(
                        text = "Subject: ${asg.subjectName}",
                        style = Typography.bodySmall.copy(color = TextSecondary)
                    )
                    if (asg.professorName.isNotBlank()) {
                        Text(
                            text = "Faculty: ${asg.professorName}",
                            style = Typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                    Text(
                        text = "Deadline: ${asg.dueDate}",
                        style = Typography.bodySmall.copy(color = AccentOrange, fontWeight = FontWeight.SemiBold)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        openDocumentUrl(context, coroutineScope, asg.fileUri, asg.r2Key)
                        viewingAssignmentFile = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Open / View", color = CardWhite)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewingAssignmentFile = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close")
                }
            },
            containerColor = CardWhite,
            shape = RoundedCornerShape(14.dp)
        )
    }

    if (viewingStudyMaterial != null) {
        val mat = viewingStudyMaterial!!
        AlertDialog(
            onDismissRequest = { viewingStudyMaterial = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = PrimaryNavy,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Study Material",
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = mat.title,
                        style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryNavy)
                    )
                    Text(
                        text = "Subject: ${mat.subjectName}",
                        style = Typography.bodySmall.copy(color = TextSecondary)
                    )
                    if (mat.sharedBy.isNotBlank()) {
                        Text(
                            text = "Faculty: ${mat.sharedBy}",
                            style = Typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                    if (!mat.fileName.isNullOrBlank()) {
                        Text(
                            text = "File: ${mat.fileName} (${mat.fileSize})",
                            style = Typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                    Text(
                        text = "Shared Date: ${mat.sharedDate}",
                        style = Typography.labelSmall.copy(color = TextMuted)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        openDocumentUrl(context, coroutineScope, mat.fileUri ?: mat.downloadUrl, mat.r2Key)
                        viewingStudyMaterial = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Open / View", color = CardWhite)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewingStudyMaterial = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close")
                }
            },
            containerColor = CardWhite,
            shape = RoundedCornerShape(14.dp)
        )
    }

}

@Composable
fun AssignmentItemCard(
    item: StudentAssignment,
    onViewFile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val (statusText, badgeStyle) = when (item.status) {
        AssignmentStatus.PENDING -> "Pending" to BadgeStyle.WARNING
        AssignmentStatus.SUBMITTED -> "Submitted" to BadgeStyle.SUCCESS
        AssignmentStatus.OVERDUE -> "Overdue" to BadgeStyle.ERROR
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
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
                Text(
                    text = item.subjectName,
                    style = Typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy,
                        fontSize = 11.sp
                    )
                )
                StatusBadge(text = statusText, style = badgeStyle)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.title,
                style = Typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            )

            if (item.cleanInstructions.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.cleanInstructions,
                    style = Typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                )
            }

            if (item.professorName.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Faculty: ${item.professorName}",
                    style = Typography.labelSmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            // Attached Assignment File
            if (!item.fileName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceVariantLight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = null,
                                tint = PrimaryNavy,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.fileName,
                                style = Typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryNavy,
                                    fontSize = 12.sp
                                ),
                                maxLines = 1
                            )
                        }
                        TextButton(
                            onClick = onViewFile,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "View / Download",
                                fontSize = 11.sp,
                                color = AccentOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                    text = "Deadline: ${item.dueDate}",
                    style = Typography.labelSmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                if (item.status == AssignmentStatus.SUBMITTED) {
                    Text(
                        text = "Submitted: ${item.submissionDate ?: "Done"}",
                        style = Typography.labelSmall.copy(
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun StudyMaterialCard(
    material: StudentStudyMaterial,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onDownload() },
        shape = RoundedCornerShape(12.dp),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = material.title,
                    style = Typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy,
                        fontSize = 14.sp
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                val facultyText = if (material.sharedBy.isNotBlank()) " • Prof. ${material.sharedBy}" else ""
                Text(
                    text = "${material.subjectName}$facultyText",
                    style = Typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                val fileLabel = material.fileName?.takeIf { it.isNotBlank() } ?: material.fileType
                Text(
                    text = "File: $fileLabel • ${material.fileSize} • Shared ${material.sharedDate}",
                    style = Typography.labelSmall.copy(
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                )
            }

            IconButton(onClick = onDownload) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download",
                    tint = PrimaryNavy
                )
            }
        }
    }
}

private fun openDocumentUrl(
    context: android.content.Context,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    rawUrl: String?,
    r2Key: String? = null
) {
    if (rawUrl.isNullOrBlank() && r2Key.isNullOrBlank()) {
        Toast.makeText(context, "Document URL not available.", Toast.LENGTH_SHORT).show()
        return
    }

    val uriStr = rawUrl ?: ""
    if (uriStr.startsWith("content://")) {
        Toast.makeText(
            context,
            "This file was uploaded from an earlier local build. Please ask professor to re-upload to cloud storage.",
            Toast.LENGTH_LONG
        ).show()
        return
    }

    val isLegacySupabase = uriStr.contains(".supabase.co/storage/")
    val isR2 = !r2Key.isNullOrBlank() || uriStr.startsWith("r2://") || uriStr.startsWith("assignments/") || uriStr.startsWith("study-materials/")

    // Fast-path: Legacy Supabase Storage URLs or external web links open directly (backward compatibility)
    if (isLegacySupabase || (!isR2 && (uriStr.startsWith("http://") || uriStr.startsWith("https://")))) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
            } catch (e2: Exception) {
                Toast.makeText(context, "Could not open document: ${e2.message}", Toast.LENGTH_SHORT).show()
            }
        }
        return
    }

    // Cloudflare R2 path: Resolve temporary presigned download URL from Edge Function
    coroutineScope.launch {
        Toast.makeText(context, "Fetching secure link from Cloudflare R2...", Toast.LENGTH_SHORT).show()
        val signedUrl = com.vvit.smartcollege.data.remote.SupabaseApiClient.resolveDownloadUrl(uriStr, r2Key)
        if (!signedUrl.isNullOrBlank()) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(signedUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(signedUrl)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(browserIntent)
                } catch (e2: Exception) {
                    Toast.makeText(context, "Could not open document: ${e2.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Failed to load document: Link expired or unauthorized.", Toast.LENGTH_SHORT).show()
        }
    }
}
