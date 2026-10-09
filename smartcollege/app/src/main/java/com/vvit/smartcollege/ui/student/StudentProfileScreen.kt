package com.vvit.smartcollege.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vvit.smartcollege.ui.components.StudentAvatar
import com.vvit.smartcollege.ui.components.VVITConfirmDialog
import com.vvit.smartcollege.ui.components.VVITHeader
import com.vvit.smartcollege.ui.components.VVITOutlinedButton
import com.vvit.smartcollege.ui.theme.*

@Composable
fun StudentProfileScreen(
    viewModel: StudentViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.profile.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        VVITHeader(
            title = "My Profile",
            subtitle = "Student Identity & Records",
            onLogoutClick = { showLogoutDialog = true }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        textStyle = Typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = profile.name,
                            style = Typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Roll No: ${profile.rollNo}",
                            style = Typography.bodySmall.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            // Personal Details Card
            ProfileInfoSection(
                title = "Personal Details",
                items = listOf(
                    "Date of Birth" to profile.dateOfBirth,
                    "Phone Number" to profile.phone,
                    "Email" to profile.email,
                    "Blood Group" to profile.bloodGroup
                )
            )

            // Academic Details Card
            ProfileInfoSection(
                title = "Academic Details",
                items = listOf(
                    "Department" to "Information Technology",
                    "Current Semester" to profile.currentSemester,
                    "Admission Year" to profile.admissionYear
                )
            )

            // Guardian Details Card
            ProfileInfoSection(
                title = "Guardian Details",
                items = listOf(
                    "Guardian Name" to profile.guardianName,
                    "Relationship" to profile.guardianRelationship,
                    "Contact" to profile.guardianContact
                )
            )

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
            message = "Are you sure you want to log out of your student session?",
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

@Composable
fun ProfileInfoSection(
    title: String,
    items: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = Typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavy,
                    fontSize = 14.sp
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            items.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    rowItems.forEach { (label, value) ->
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = label,
                                style = Typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = value,
                                style = Typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getInitials(name: String): String {
    val clean = name.trim()
    if (clean.isBlank()) return "ST"
    val parts = clean.split("\\s+".toRegex()).filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
        clean.length >= 2 -> clean.substring(0, 2).uppercase()
        clean.isNotEmpty() -> clean.first().uppercaseChar().toString()
        else -> "ST"
    }
}
